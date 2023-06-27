
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.io.PrintStream;
import java.util.List;
import java.util.NavigableMap;

import org.dellroad.jct.core.JctSession;

/**
 * A simple "help" command that can be used with a {@link SimpleConsole}.
 */
public class HelpCommand extends AbstractSimpleCommand {

    public HelpCommand() {
        super(
          "[command]",
          "Displays information about available commands.",
          "When used with no arguments, lists all of the available console commands along with short descriptions."
            + "\nWhen used with a specific command, displays detailed information about that command.");
    }

    @Override
    public boolean execute(JctSession session, String name, List<String> args) throws InterruptedException {

        // Get console
        final SimpleConsole console = this.getSimpleConsole(session, name);
        if (console == null)
            return false;

        // Check command line
        final PrintStream out = session.getOutputStream();
        switch (args.size()) {
        case 0:

            // List all commands
            final NavigableMap<String, SimpleCommand> commands = console.getCommands();
            final int maxNameLen = commands.keySet().stream().mapToInt(String::length).max().orElse(0);
            final String format = String.format("  %%-%ds  %%s", maxNameLen);
            commands.forEach((commandName, command) ->
              out.println(String.format(format, commandName, command.getHelpSummary(commandName))));
            break;
        case 1:

            // Find command
            final String commandName = args.get(0);
            final SimpleCommand command = console.getCommands().get(commandName);
            if (command == null) {
                out.println(String.format("%s: command not found", commandName));
                return false;
            }

            // Show detailed help
            out.println(String.format("%s - %s", commandName, command.getHelpSummary(commandName)));
            out.print(String.format("Usage: %s", commandName));
            final String usage = command.getUsage(commandName);
            if (usage != null) {
                out.print(' ');
                out.print(usage);
            }
            out.println();
            out.println("Description:");
              out.println(command.getHelpDetail(commandName));
            break;
        default:
            this.printUsage(session, name);
            return false;
        }
        return true;
    }
}
