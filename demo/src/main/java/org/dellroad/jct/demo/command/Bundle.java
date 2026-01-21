
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.demo.command;

import org.dellroad.jct.core.simple.TreeMapBundle;

@SuppressWarnings("serial")
public class Bundle extends TreeMapBundle {

    public Bundle() {
        super("Java Console Toolkit demonstration commands");
        this.put("exec", new ExecCommand());
    }
}
