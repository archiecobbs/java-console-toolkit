
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell.command;

import org.dellroad.jct.core.simple.TreeMapBundle;
import org.dellroad.jct.core.util.ConsoleUtil;

@SuppressWarnings("serial")
public class Bundle extends TreeMapBundle {

    public Bundle() {
        super("Java Console Toolkit JShell commands");

        // Require JDK 9+
        if (ConsoleUtil.getJavaVersion() >= 9)
            this.put("jshell", new JShellCommandCreator().create());
    }

    // Using a separate class avoids a class resolution error if JDK version < 9
    private static final class JShellCommandCreator {
        JShellCommand create() {
            return new JShellCommand();
        }
    }
}
