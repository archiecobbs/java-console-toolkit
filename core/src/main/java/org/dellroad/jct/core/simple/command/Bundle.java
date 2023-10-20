
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple.command;

import org.dellroad.jct.core.simple.TreeMapBundle;

@SuppressWarnings("serial")
public class Bundle extends TreeMapBundle {

    public Bundle() {
        super("Java Console Toolkit built-in simple commands");
        this.put("date", new DateCommand());
        this.put("echo", new EchoCommand());
        this.put("exit", new ExitCommand());
        this.put("help", new HelpCommand());
        this.put("quit", new ExitCommand());
        this.put("sleep", new SleepCommand());
    }
}
