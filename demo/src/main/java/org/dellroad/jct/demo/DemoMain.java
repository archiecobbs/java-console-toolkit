
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.demo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import org.dellroad.jct.core.JctShellSession;
import org.dellroad.jct.core.JctUtils;
import org.dellroad.jct.core.simple.SimpleConsole;
import org.dellroad.jct.core.simple.SimpleShellRequest;
import org.dellroad.jct.ssh.simple.SimpleConsoleSshServer;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Demonstration of various Java Console Toolkit components.
 */
public class DemoMain {

    public String getName() {
        return "jct-demo";
    }

    public int run(String[] args) {

        // Parse command line
        File sshHostKeyFile = null;
        File sshAuthKeysFile = getDefaultAuthKeysFile();
        int sshListenPort = this.getDefaultListenPort();
        final ArrayDeque<String> params = new ArrayDeque<>(Arrays.asList(args));
        boolean ssh = false;
        boolean syscon = false;
    argLoop:
        while (!params.isEmpty() && params.peekFirst().startsWith("-")) {
            String option = params.removeFirst();
            switch (option) {
            case "--ssh-auth-keys-file":
                if (params.isEmpty()) {
                    this.usage(System.err);
                    return 1;
                }
                sshAuthKeysFile = new File(params.removeFirst());
                ssh = true;
                break;
            case "--ssh-host-key-file":
                if (params.isEmpty()) {
                    this.usage(System.err);
                    return 1;
                }
                sshHostKeyFile = new File(params.removeFirst());
                ssh = true;
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
                ssh = true;
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
        switch (params.size()) {
        case 0:
            break;
        default:
            this.usage(System.err);
            return 1;
        }

        // Create console
        final SimpleConsole demoConsole = new SimpleConsole();

        // Start SSH server
        if (ssh) {
            if (sshHostKeyFile == null) {
                System.err.println(String.format("%s: \"%s\" is required for SSH server", this.getName(), "--ssh-host-key-file"));
                return 1;
            }
            SimpleConsoleSshServer server = SimpleConsoleSshServer.builder()
              .console(demoConsole)
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
        final AtomicReference<JctShellSession> shellSessionRef = new AtomicReference<>();
        final Terminal terminal;
        try {
            terminal = TerminalBuilder.builder()
              .name("JCT")
              .system(true)
              .nativeSignals(true)
              .signalHandler(JctUtils.interrruptHandler(shellSessionRef::get, Terminal.SignalHandler.SIG_DFL))
              .build();
        } catch (IOException e) {
            System.err.println(String.format("%s: error creating system terminal: %s", this.getName(), e));
            return 1;
        }

        // Create system console
        final JctShellSession shellSession;
        try {
            shellSession = demoConsole.newShellSession(new SimpleShellRequest(terminal, System.getenv()));
        } catch (IOException e) {
            System.err.println(String.format("%s: error creating shell session: %s", this.getName(), e));
            return 1;
        }
        shellSessionRef.set(shellSession);

        // Execute system console
        try {
            return shellSession.execute() ? 0 : 1;
        } catch (InterruptedException e) {
            System.err.println(String.format("%s: interrupted", this.getName()));
            return 1;
        }
    }

    public void usage(PrintStream out) {
        out.println(String.format("Usage:"));
        out.println(String.format("    %s [options]", this.getName()));
        out.println(String.format("Options:"));
        out.println(String.format(
          "    --ssh-auth-keys-file path    Specify SSH authorized users file (default %s)", this.getDefaultAuthKeysFile()));
        out.println(String.format(
          "    --ssh-auth-keys-file path    Specify SSH authorized users file (default %s)", this.getDefaultAuthKeysFile()));
        out.println(String.format(
          "    --ssh-host-key-file path     Specify SSH host key file (required for SSH server)"));
        out.println(String.format(
          "    --ssh-listen-port port       Specify SSH server TCP port (default %d)", this.getDefaultListenPort()));
        out.println(String.format(
          "    --help                       Display this usage message"));
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

    public int getDefaultListenPort() {
        return 9191;
    }

    public static void main(String[] args) {
        int exitValue;
        try {
            exitValue = new DemoMain().run(args);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            exitValue = 1;
        }
        System.exit(exitValue);
    }
}
