
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
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.ShellSession;
import org.dellroad.jct.core.simple.SimpleCommand;
import org.dellroad.jct.core.simple.SimpleCommandSupport;
import org.dellroad.jct.core.simple.SimpleExec;
import org.dellroad.jct.core.simple.SimpleExecRequest;
import org.dellroad.jct.core.simple.SimpleShell;
import org.dellroad.jct.core.simple.SimpleShellRequest;
import org.dellroad.jct.core.simple.command.DateCommand;
import org.dellroad.jct.core.simple.command.EchoCommand;
import org.dellroad.jct.core.simple.command.ExitCommand;
import org.dellroad.jct.core.simple.command.HelpCommand;
import org.dellroad.jct.core.simple.command.SleepCommand;
import org.dellroad.jct.core.util.ConsoleUtil;
import org.dellroad.jct.jshell.JShellCommand;
import org.dellroad.jct.ssh.simple.SimpleConsoleSshServer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Demonstration of various Java Console Toolkit functionality.
 */
public class DemoMain {

    private static DemoMain instance;

    private final TreeMap<String, SimpleCommand> commandMap = new TreeMap<>();

    public DemoMain() {
        commandMap.put("date", new DateCommand());
        commandMap.put("echo", new EchoCommand());
        commandMap.put("exit", new ExitCommand());
        commandMap.put("help", new HelpCommand());
        if (ConsoleUtil.getJavaVersion() >= 9)
            commandMap.put("jshell", new JShellCommandCreator().create());
        commandMap.put("quit", new ExitCommand());
        commandMap.put("sleep", new SleepCommand());
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
        File sshAuthKeysFile = this.getDefaultAuthKeysFile();
        int sshListenPort = this.getDefaultListenPort();
        final ArrayDeque<String> params = new ArrayDeque<>(Arrays.asList(args));
        boolean ssh = false;
        boolean syscon = false;
    argLoop:
        while (!params.isEmpty() && params.peekFirst().startsWith("-")) {
            String option = params.removeFirst();
            switch (option) {
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

        // Interactive shell or execute command directly?
        final ConsoleSession<?, ?> session;
        if (!params.isEmpty()) {

            // Sanity check
            if (ssh) {
                System.err.println(String.format(
                  "%s: SSH server is not compatible with direct command execution", "Error", params.getFirst()));
                return 1;
            }

            // Create exec
            final SimpleExec exec = new SimpleExec();
            exec.setCommandRegistry(() -> this.commandMap);

            // Create request
            final SimpleExecRequest request = new SimpleExecRequest(System.in,
              System.out, System.err, System.getenv(), new ArrayList<>(params));

            // Find command
            final SimpleCommandSupport.FoundCommand foundCommand = exec.findCommand(System.err, request);
            if (foundCommand == null)
                return 1;

            // Create exec session
            try {
                session = exec.newExecSession(request, foundCommand);
            } catch (IOException e) {
                System.err.println(String.format("%s: error creating %s session: %s", this.getName(), "exec", e));
                return 1;
            }
        } else {

            // Create and configure console components
            final SimpleExec exec = new SimpleExec();
            final SimpleShell shell = new SimpleShell();
            exec.setCommandRegistry(() -> this.commandMap);
            shell.setCommandRegistry(() -> this.commandMap);

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
                return 1;
            }

            // Create shell session
            final ShellSession shellSession;
            try {
                shellSession = shell.newShellSession(
                  new SimpleShellRequest(terminal, Collections.emptyList(), System.getenv()));
            } catch (IOException e) {
                System.err.println(String.format("%s: error creating %s session: %s", this.getName(), "shell", e));
                return 1;
            }
            shellSessionRef.set(shellSession);
            session = shellSession;
        }

        // Execute session
        try {
            return session.execute();
        } catch (InterruptedException e) {
            System.err.println(String.format("%s: interrupted", this.getName()));
            return 1;
        }
    }

    public void usage(PrintStream out) {
        out.println(String.format("Usage:"));
        out.println(String.format("    %s [options] [command ...]", this.getName()));
        out.println(String.format("Options:"));
        out.println(String.format(
          "    --ssh                        Enable SSH server"));
        out.println(String.format(
          "    --ssh-auth-keys-file path    Specify SSH authorized users file (default %s)", this.getDefaultAuthKeysFile()));
        out.println(String.format(
          "    --ssh-host-key-file path     Specify SSH host key file (default %s)", this.getDefaultHostKeyFile()));
        out.println(String.format(
          "    --ssh-listen-port port       Specify SSH server TCP port (default %d)", this.getDefaultListenPort()));
        out.println(String.format(
          "    --help                       Display this usage message"));
        out.println(String.format("Commands:"));
        this.commandMap.forEach((name, command) ->
          out.println(String.format("    %-28s %s", name, command.getHelpSummary(name))));
    }

    public File getDefaultAuthKeysFile() {
        String file = "authorized_keys";
        final String homeDir = System.getProperty("user.home");
        if (homeDir != null) {
            final String fs = System.getProperty("file.separator");
            file = String.format("%s%s%s%s%s", homeDir, fs, ".ssh", fs, file);
        }
        return new File(file);
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

// JShellCommandCreator

    // This separate class avoids a class resolution error if JDK version < 9
    private static final class JShellCommandCreator {

        JShellCommand create() {
            final JShellCommand command = new JShellCommand() {

                // Add startup script to the jshell command line (if found)
                @Override
                protected List<String> buildJShellParams(List<String> commandLineParams) {
                    final List<String> jshellParams = new ArrayList<>();
                    final File startupFile = new File("startup.jsh");
                    if (startupFile.exists()) {
                        jshellParams.add("--startup");
                        jshellParams.add(startupFile.toString());
                    }
                    jshellParams.addAll(super.buildJShellParams(commandLineParams));
                    return jshellParams;
                }
            };

            // This fixes some class path and class loader issues
            command.enableLocalContextExecution();

            // Done
            return command;
        }
    }
}
