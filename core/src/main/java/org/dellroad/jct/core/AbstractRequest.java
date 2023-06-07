
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core;

import java.util.Collections;
import java.util.Map;

class AbstractRequest {

    private final Map<String, String> env;

    AbstractRequest(Map<String, String> env) {
        this.env = env;
    }

    public Map<String, String> getEnvironment() {
        return this.env != null ? this.env : Collections.emptyMap();
    }
}
