
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.jshell;

import org.dellroad.jct.core.Shell;
import org.dellroad.jct.core.ShellRequest;

/**
 * A {@link Shell} wrapper around {@link jdk.jshell.JShell}.
 */
public class JShellShell implements Shell {

    @Override
    public JShellShellSession newShellSession(ShellRequest request) {
        return new JShellShellSession(this, request);
    }
}
