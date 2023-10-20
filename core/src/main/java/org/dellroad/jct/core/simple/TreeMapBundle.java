
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.Map;
import java.util.TreeMap;

/**
 * {@link CommandBundle} implementation using {@link TreeMap}.
 */
@SuppressWarnings("serial")
public class TreeMapBundle extends TreeMap<String, SimpleCommand> implements CommandBundle {

    private final String description;

    /**
     * Default constructor.
     *
     * <p>
     * Creates an initially empty bundle.
     *
     * @param description bundle description
     * @throws IllegalArgumentException if {@code description} is null
     */
    public TreeMapBundle(String description) {
        if (description == null)
            throw new IllegalArgumentException("null description");
        this.description = description;
    }

    /**
     * Constructor.
     *
     * @param description bundle description
     * @param commands commands keyed by name
     * @throws IllegalArgumentException if anything is null
     */
    public TreeMapBundle(String description, Map<String, SimpleCommand> commands) {
        this(description);
        if (commands == null)
            throw new IllegalArgumentException("null commands");
        commands.forEach((key, value) -> {
            if (key == null)
                throw new IllegalArgumentException("null command name");
            if (value == null)
                throw new IllegalArgumentException("null command");
            this.put(key, value);
        });
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
