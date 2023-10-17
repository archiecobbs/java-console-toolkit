
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple.command;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.dellroad.jct.core.simple.CommandProvider;
import org.dellroad.jct.core.simple.SimpleCommand;

public class SimpleCommandProvider implements CommandProvider {

    @Override
    public Stream<? extends Map.Entry<String, ? extends SimpleCommand>> generateSimpleCommands() {
        final HashMap<String, SimpleCommand> commandMap = new HashMap<>();
        commandMap.put("date", new DateCommand());
        commandMap.put("echo", new EchoCommand());
        commandMap.put("exit", new ExitCommand());
        commandMap.put("help", new HelpCommand());
        commandMap.put("quit", new ExitCommand());
        commandMap.put("sleep", new SleepCommand());
        return commandMap.entrySet().stream();
    }
}
