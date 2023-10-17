
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.dellroad.jct.core.simple.CommandProvider;
import org.dellroad.jct.core.simple.SimpleCommand;
import org.dellroad.jct.core.util.ConsoleUtil;

public class JShellCommandProvider implements CommandProvider {

    /**
     * Determine whether the current version of the JDK supports JShell.
     *
     * @return true if JShell supported, otherwise false
     */
    public static boolean isJShellSupported() {
        return ConsoleUtil.getJavaVersion() >= 9;
    }

// SimpleCommandProvider

    @Override
    public Stream<? extends Map.Entry<String, ? extends SimpleCommand>> generateSimpleCommands() {

        // Require JDK 9+
        if (!JShellCommandProvider.isJShellSupported())
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
            return new JShellCommand();
        }
    }
}
