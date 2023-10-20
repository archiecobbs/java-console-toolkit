
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple.command;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.dellroad.jct.core.ConsoleSession;
import org.dellroad.jct.core.simple.AbstractSimpleCommand;
import org.dellroad.jct.core.simple.CommandBundle;
import org.dellroad.jct.core.simple.SimpleCommand;
import org.dellroad.jct.core.simple.SimpleCommandSupport;

/**
 * A simple "help" command that can be used with a console component extending {@link SimpleCommandSupport}.
 */
public class HelpCommand extends AbstractSimpleCommand {

    private static final String INDENT = "    ";

    public HelpCommand() {
        super(
          "[command]",
          "Displays information about available commands.",
          "When used with no arguments, lists all of the available console commands along with short descriptions."
            + "\nWhen used with a specific command, displays detailed information about that command.");
    }

    /**
     * Pretty-print command names and descriptions.
     *
     * @param out where to print
     * @param bundles command bundles
     * @throws IllegalArgumentException if any parameter is null
     */
    public static void listCommands(PrintStream out, Collection<? extends CommandBundle> bundles) {
        if (out == null)
            throw new IllegalArgumentException("null out");
        if (bundles == null)
            throw new IllegalArgumentException("null bundles");
        bundles.stream().forEach(bundle -> {

            // Skip empty bundles
            if (bundle.isEmpty())
                return;

            // Show bundle description
            out.println(String.format("=== %s", bundle.getDescription()));
            out.println();

            // Calculate maximum command name length
            final int maxNameLen = bundle.keySet().stream()
              .mapToInt(String::length)
              .max()
              .orElse(0);

            // Show commands
            final String format = String.format("  %%-%ds  %%s", maxNameLen);
            bundle.entrySet().stream()
              .map(entry -> String.format(format, entry.getKey(), entry.getValue().getHelpSummary(entry.getKey())))
              .forEach(out::println);

            // Blank line
            out.println();
        });
    }

    @Override
    public int execute(ConsoleSession<?, ?> session, String name, List<String> args) throws InterruptedException {

        // Get console
        final SimpleCommandSupport owner;
        try {
            owner = (SimpleCommandSupport)session.getOwner();
        } catch (ClassCastException e) {
            session.getErrorStream().println(String.format(
              "Error: the \"%s\" command requires a %s", name, SimpleCommandSupport.class.getName()));
            return 1;
        }

        // Check command line
        final PrintStream out = session.getOutputStream();
        final List<CommandBundle> commandBundles = owner.getCommandBundles();
        switch (args.size()) {
        case 0:
            out.println();
            HelpCommand.listCommands(out, commandBundles);
            break;
        case 1:

            // Find command
            final String commandName = args.get(0);
            final SimpleCommand command = commandBundles.stream()
              .map(bundle -> bundle.get(commandName))
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
            if (command == null) {
                out.println(String.format("%s: command not found", commandName));
                return 1;
            }

            // Show detailed help
            out.println("Summary:");
            out.println(this.indent(command.getHelpSummary(commandName)));
            out.println("Usage:");
            out.print(this.indent(commandName));
            final String usage = command.getUsage(commandName);
            if (usage != null) {
                out.print(' ');
                out.print(usage);
            }
            out.println();
            out.println("Description:");
            out.println(this.indent(command.getHelpDetail(commandName)));
            break;
        default:
            this.printUsage(session, name);
            return 1;
        }

        // Done
        return 0;
    }

    private String indent(String s) {
        return s.trim().replaceAll("(?m)^", INDENT);
    }
}
