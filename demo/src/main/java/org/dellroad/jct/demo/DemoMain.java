
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.demo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.ExecSession;
import org.dellroad.jct.core.ShellRequest;
import org.dellroad.jct.core.ShellSession;
import org.dellroad.jct.core.simple.CommandBundle;
import org.dellroad.jct.core.simple.SimpleCommandSupport;
import org.dellroad.jct.core.simple.SimpleExec;
import org.dellroad.jct.core.simple.SimpleExecRequest;
import org.dellroad.jct.core.simple.SimpleShell;
import org.dellroad.jct.core.simple.SimpleShellRequest;
import org.dellroad.jct.core.simple.command.HelpCommand;
import org.dellroad.jct.core.util.ConsoleUtil;
import org.dellroad.jct.jshell.JShellShell;
import org.dellroad.jct.jshell.JShellShellSession;
import org.dellroad.jct.jshell.command.JShellCommand;
import org.dellroad.jct.ssh.simple.SimpleConsoleSshServer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Demonstration of various Java Console Toolkit functionality.
 */
public class DemoMain {

    private static final String AUTHORIZED_KEYS = "authorized_keys";

    private static DemoMain instance;

    private final List<CommandBundle> commandBundles = CommandBundle.scanAndGenerate().collect(Collectors.toList());

    public DemoMain() {
        // Replace standard "jshell" command (if present) with our custom version
        commandBundles.forEach(bundle -> bundle.computeIfPresent("jshell", (name, value) -> new DemoJShellCommand()));
    }

    public String getName() {
        return "jct-demo";
    }

    public static DemoMain getInstance() {
        return DemoMain.instance;
    }

    public int run(String[] args) {

        // Parse command line
        File sshHostKeyFile = this.getDefaultHostKeyFile();
        File sshAuthKeysFile = this.defaultAuthKeys().file();
        int sshListenPort = this.getDefaultListenPort();
        final ArrayDeque<String> params = new ArrayDeque<>(Arrays.asList(args));
        boolean ssh = false;
        boolean console = true;
    argLoop:
        while (!params.isEmpty() && params.peekFirst().startsWith("-")) {
            String option = params.removeFirst();
            switch (option) {
            case "--no-console":
                console = false;
                break;
            case "--ssh":
                ssh = true;
                break;
            case "--ssh-auth-keys-file":
                if (params.isEmpty()) {
                    this.usage(System.err);
                    return 1;
                }
                sshAuthKeysFile = new File(params.removeFirst());
                break;
            case "--ssh-host-key-file":
                if (params.isEmpty()) {
                    this.usage(System.err);
                    return 1;
                }
                sshHostKeyFile = new File(params.removeFirst());
                break;
            case "--ssh-listen-port":
                if (params.isEmpty()) {
                    this.usage(System.err);
                    return 1;
                }
                final String portString = params.removeFirst();
                try {
                    sshListenPort = Integer.parseInt(portString, 10);
                    if (sshListenPort < 1 || sshListenPort > 65535)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    System.err.println(String.format("%s: invalid SSH listen port \"%s\"", this.getName(), portString));
                    this.usage(System.err);
                    return 1;
                }
                break;
            case "--help":
                this.usage(System.err);
                return 0;
            case "--":
                break argLoop;
            default:
                System.err.println(String.format("%s: unknown option \"%s\"", this.getName(), option));
                this.usage(System.err);
                return 1;
            }
        }

        // Create and configure console components
        final SimpleExec exec = new SimpleExec();
        final SimpleShell shell = new SimpleShell();
        exec.getCommandBundles().addAll(this.commandBundles);
        shell.getCommandBundles().addAll(this.commandBundles);

        // Interactive shell or execute command directly?
        final ConsoleSession<?, ?> session;
        if (!params.isEmpty()) {

            // Sanity check
            if (ssh) {
                System.err.println(String.format(
                  "%s: SSH server is not compatible with direct command execution", "Error", params.getFirst()));
                return 1;
            }

            // Create exec session
            session = this.createExecSession(exec, params);
        } else {

            // Start SSH server
            if (ssh) {
                SimpleConsoleSshServer server = SimpleConsoleSshServer.builder()
                  .exec(exec)
                  .shell(shell)
                  .hostKey(sshHostKeyFile.toPath())
                  .authorizedKeys(sshAuthKeysFile.toPath())
                  .listenPort(sshListenPort)
                  .loopbackOnly(true)
                  .build();
                try {
                    server.start();
                } catch (IOException e) {
                    System.err.println(String.format("%s: error starting SSH server", this.getName()));
                    e.printStackTrace(System.err);
                    return 1;
                }
                System.err.println(String.format("%s: started SSH server on port %d", this.getName(), sshListenPort));
            }

            // Start console, or else just sleep
            session = console ?
              this.createShellSession(shell) :
              this.createExecSession(exec, Arrays.asList("sleep", "99999999"));
        }

        // Check for session creation failure
        if (session == null)
            return 1;

        // Execute session, if any, otherwise just hang
        try {
            return session.execute();
        } catch (InterruptedException e) {
            System.err.println(String.format("%s: interrupted", this.getName()));
            return 1;
        }
    }

    private ExecSession createExecSession(SimpleExec exec, Collection<String> params) {

        // Create request
        final SimpleExecRequest request = new SimpleExecRequest(System.in,
          System.out, System.err, System.getenv(), new ArrayList<>(params));

        // Find command
        final SimpleCommandSupport.FoundCommand foundCommand = exec.findCommand(System.err, request);
        if (foundCommand == null)
            return null;

        // Create exec session
        try {
            return exec.newExecSession(request, foundCommand);
        } catch (IOException e) {
            System.err.println(String.format("%s: error creating %s session: %s", this.getName(), "exec", e));
            return null;
        }
    }

    private ShellSession createShellSession(SimpleShell shell) {

        // Create system terminal
        final AtomicReference<ShellSession> shellSessionRef = new AtomicReference<>();
        final Terminal terminal;
        try {
            terminal = TerminalBuilder.builder()
              .name("JCT")
              .system(true)
              .nativeSignals(true)
              .signalHandler(ConsoleUtil.interrruptHandler(shellSessionRef::get, Terminal.SignalHandler.SIG_DFL))
              .build();
        } catch (IOException e) {
            System.err.println(String.format("%s: error creating system terminal: %s", this.getName(), e));
            return null;
        }

        // Create shell session
        final ShellSession shellSession;
        try {
            shellSession = shell.newShellSession(
              new SimpleShellRequest(terminal, Collections.emptyList(), System.getenv()));
        } catch (IOException e) {
            System.err.println(String.format("%s: error creating %s session: %s", this.getName(), "shell", e));
            return null;
        }
        shellSessionRef.set(shellSession);
        return shellSession;
    }

    public void usage(PrintStream out) {
        out.println(String.format("Usage:"));
        out.println(String.format("    %s [options] [command ...]", this.getName()));
        out.println();
        out.println(String.format("Options:"));
        out.println(String.format(
          "    --no-console                 Don't start command line console"));
        out.println(String.format(
          "    --ssh                        Enable SSH server"));
        out.println(String.format(
          "    --ssh-auth-keys-file path    Specify SSH authorized users file (default %s)", this.defaultAuthKeys().display()));
        out.println(String.format(
          "    --ssh-host-key-file path     Specify SSH host key file (default %s)", this.getDefaultHostKeyFile()));
        out.println(String.format(
          "    --ssh-listen-port port       Specify SSH server TCP port (default %d)", this.getDefaultListenPort()));
        out.println(String.format(
          "    --help                       Display this usage message"));
        out.println();
        out.println(String.format("Commands:"));
        HelpCommand.listCommands(out, this.commandBundles);
    }

    public DefaultAuthKeys defaultAuthKeys() {
        return new DefaultAuthKeys();
    }

    public File getDefaultHostKeyFile() {
        return new File("hostkey");
    }

    public int getDefaultListenPort() {
        return 9191;
    }

    public static void main(String[] args) {

        // Create demo singleton
        final DemoMain demo = new DemoMain();
        DemoMain.instance = demo;

        // Run the demo
        int exitValue;
        try {
            exitValue = demo.run(args);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            exitValue = 1;
        }

        // Done
        System.exit(exitValue);
    }

    private record DefaultAuthKeys(File file, String display) {

        DefaultAuthKeys() {
            this(System.getProperty("user.home"), System.getProperty("file.separator"));
        }

        private DefaultAuthKeys(String homeDir, String fs) {
            this(new File(DefaultAuthKeys.fullPath(homeDir, fs)), DefaultAuthKeys.fullPath("${user.home}", fs));
        }

        private static String fullPath(String homeDir, String fs) {
            return homeDir != null ?
              String.format("%s%s%s%s%s", homeDir, fs, ".ssh", fs, AUTHORIZED_KEYS) :
              AUTHORIZED_KEYS;
        }
    }

// DemoJShellCommand

    private static class DemoJShellCommand extends JShellCommand {
        DemoJShellCommand() {
            super(new JShellShell() {
                @Override
                public JShellShellSession newShellSession(ShellRequest request) {
                    final JShellShellSession session = new JShellShellSession(this, request) {

                        // Add startup script to the jshell command line (if found)
                        @Override
                        protected List<String> modifyJShellParams(List<String> params) {
                            final File startupFile = new File("startup.jsh");
                            if (startupFile.exists()) {
                                params = new ArrayList<>(params);
                                params.add(0, "--startup");
                                params.add(1, startupFile.toString());
                            }
                            return super.modifyJShellParams(params);
                        }
                    };

                    // Configure local execution
                    session.setLocalContextClassLoader(Thread.currentThread().getContextClassLoader());
                    return session;
                }
            });
        }
    }
}
