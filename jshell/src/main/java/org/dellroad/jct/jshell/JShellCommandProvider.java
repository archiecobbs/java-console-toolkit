
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.dellroad.jct.core.simple.CommandProvider;
import org.dellroad.jct.core.simple.SimpleCommand;
import org.dellroad.jct.core.util.ConsoleUtil;

public class JShellCommandProvider implements CommandProvider {

// SimpleCommandProvider

    @Override
    public Stream<? extends Map.Entry<String, ? extends SimpleCommand>> generateSimpleCommands() {

        // Require JDK 9+
        if (ConsoleUtil.getJavaVersion() < 9)
            return Stream.empty();

        // Return our one command
        return Collections.singletonMap("jshell", new JShellCommandCreator().create())
          .entrySet()
          .stream();
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
