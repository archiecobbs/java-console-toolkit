
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.ssh.simple;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.dellroad.jct.core.Exec;
import org.dellroad.jct.core.Shell;
import org.dellroad.jct.ssh.JctExecFactory;
import org.dellroad.jct.ssh.JctShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple SSH server exposing a {@link Shell} and/or and {@link Exec}.
 *
 * <p>
 * The only supported authentication type is public key authentication.
 *
 * <p>
 * Instances must be {@link #start start()}'ed before use and should be {@link #stop stop()}'ed when no longer needed.
 */
public class SimpleConsoleSshServer implements Closeable {

    private static final String LOOPBACK_HOST_ADDRESS = "127.0.0.1";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final Exec exec;
    protected final Shell shell;
    protected final int listenPort;
    protected final boolean loopbackOnly;
    protected final PublickeyAuthenticator authenticator;
    protected final KeyPairProvider hostKeyProvider;

    protected SshServer sshd;

    /**
     * Constructor.
     *
     * @param builder configuration
     */
    protected SimpleConsoleSshServer(Builder builder) {
        this.exec = builder.exec;
        this.shell = builder.shell;
        this.listenPort = builder.listenPort;
        this.loopbackOnly = builder.loopbackOnly;
        this.authenticator = builder.authenticator;
        this.hostKeyProvider = builder.hostKeyProvider;
        if (this.exec == null && this.shell == null)
            throw new IllegalArgumentException("no exec or shell configured");
        if (this.authenticator == null)
            throw new IllegalArgumentException("no authenticator configured");
        if (this.hostKeyProvider == null)
            throw new IllegalArgumentException("no host key provider configured");
    }

// Public methods

    /**
     * Create an instance builder.
     *
     * @return new instance builder
     */
    public static Builder builder() {
        return new Builder();
    }

// SelectorSupport

    /**
     * Start this instance.
     *
     * <p>
     * Does nothing if already started.
     *
     * @throws IOException if an I/O error occurs setting up the listen socket
     */
    public synchronized void start() throws IOException {

        // Already running?
        if (this.sshd != null)
            return;

        // Configure and start server
        boolean success = false;
        try {

            // Create server
            this.sshd = SshServer.setUpDefaultServer();

            // Configure listen port
            this.sshd.setPort(this.listenPort);
            if (this.loopbackOnly)
                this.sshd.setHost(LOOPBACK_HOST_ADDRESS);

            // Configure security stuff
            this.sshd.setPublickeyAuthenticator(this.authenticator);
            this.sshd.setKeyPairProvider(this.hostKeyProvider);

            // Connect to console
            if (this.shell != null)
                this.sshd.setShellFactory(new JctShellFactory(this.shell));
            if (this.exec != null)
                this.sshd.setCommandFactory(new JctExecFactory(this.exec));

            // Start server
            this.sshd.start();

            // Done
            success = true;
        } finally {
            if (!success)
                this.stop();
        }
    }

    /**
     * Stop this instance.
     */
    public synchronized void stop() {
        if (this.sshd == null)
            return;
        try {
            this.sshd.stop();
        } catch (IOException e) {
            // ignore
        } finally {
            this.sshd = null;
        }
    }

// Closeable

    /**
     * Close this instance.
     *
     * <p>
     * Delegates to {@link #stop}.
     */
    @Override
    public void close() {
        this.stop();
    }

// Builder

    /**
     * Builder for new {@link SimpleConsoleSshServer} instances.
     *
     * <p>
     * The following properties are required: console, authenticator, and host key.
     */
    public static final class Builder {

        private Exec exec;
        private Shell shell;
        private int listenPort = SshConstants.DEFAULT_PORT;
        private boolean loopbackOnly = true;
        private PublickeyAuthenticator authenticator;
        private KeyPairProvider hostKeyProvider;

        private Builder() {
        }

        /**
         * Configure the {@link Exec} that successful incoming execute connections should connect to.
         *
         * <p>
         * This property or {@code #shell shell()} is required.
         *
         * @param exec target exec
         * @return this instance
         */
        public Builder exec(Exec exec) {
            this.exec = exec;
            return this;
        }

        /**
         * Configure the {@link Shell} that successful incoming shell connections should connect to.
         *
         * <p>
         * This property or {@code #shell shell()} is required.
         *
         * @param shell target shell
         * @return this instance
         */
        public Builder shell(Shell shell) {
            this.shell = shell;
            return this;
        }

        /**
         * Configure whether to listen for new connections only on the loopback interface, or on all interfaces.
         *
         * <p>
         * Default is to listen only on the loopback interface.
         *
         * @param loopbackOnly true to listen for new connections only on the loopback interface
         * @return this instance
         */
        public Builder loopbackOnly(boolean loopbackOnly) {
            this.loopbackOnly = loopbackOnly;
            return this;
        }

        /**
         * Configure the TCP port on which to listen for new connections.
         *
         * <p>
         * Default is {@link SshConstants#DEFAULT_PORT}.
         *
         * @param port TCP port to listen on
         * @return this instance
         * @throws IllegalArgumentException if {@code port} is invalid
         */
        public Builder listenPort(int port) {
            if (port < 1 || port > 65535)
                throw new IllegalArgumentException("invalid port");
            this.listenPort = port;
            return this;
        }

    // PublickeyAuthenticator

        /**
         * Configure the public key authenticator that authenticates new incoming connections.
         *
         * @param authenticator public key authenticator
         * @return this instance
         */
        public Builder authenticator(PublickeyAuthenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        /**
         * Read authorized users' public keys from a {@link String}.
         *
         * @param authorizedKeys the content of an openssh {@code authorized_keys} file
         * @return this instance
         * @throws IOException if {@code authorizedKeys} contains invalid content
         * @throws GeneralSecurityException if public key data could not be parsed
         * @throws IllegalArgumentException if {@code authorizedKeys} is null
         */
        public Builder authorizedKeys(String authorizedKeys) throws IOException, GeneralSecurityException {
            if (authorizedKeys == null)
                throw new IllegalArgumentException("null authorizedKeys");
            this.authenticator = PublickeyAuthenticator.fromAuthorizedEntries("id",
              null, AuthorizedKeyEntry.readAuthorizedKeys(new StringReader(authorizedKeys), true), null);
            return this;
        }

        /**
         * Read authorized users' public keys from an {@link InputStream}.
         *
         * <p>
         * The given stream will not be closed by this method.
         *
         * @param authorizedKeys input from an openssh {@code authorized_keys} file
         * @return this instance
         * @throws IOException if {@code authorizedKeys} contains invalid content
         * @throws GeneralSecurityException if public key data could not be parsed
         * @throws IllegalArgumentException if {@code authorizedKeys} is null
         */
        public Builder authorizedKeys(InputStream authorizedKeys) throws IOException, GeneralSecurityException {
            if (authorizedKeys == null)
                throw new IllegalArgumentException("null authorizedKeys");
            this.authenticator = PublickeyAuthenticator.fromAuthorizedEntries("id",
              null, AuthorizedKeyEntry.readAuthorizedKeys(authorizedKeys, false), null);
            return this;
        }

        /**
         * Configurea authorized users' public keys to be read from a file at connection time.
         *
         * @param authorizedKeys openssh {@code authorized_keys} file
         * @return this instance
         * @throws IllegalArgumentException if {@code authorizedKeys} is null
         */
        public Builder authorizedKeys(Path authorizedKeys) {
            if (authorizedKeys == null)
                throw new IllegalArgumentException("null authorizedKeys");
            this.authenticator = new AuthorizedKeysAuthenticator(authorizedKeys);
            return this;
        }

    // KeyPairProvider

        /**
         * Configure the host key provider.
         *
         * @param hostKeyProvider public key provider for host keys
         * @return this instance
         */
        public Builder hostKeyProvider(KeyPairProvider hostKeyProvider) {
            this.hostKeyProvider = hostKeyProvider;
            return this;
        }

        /**
         * Configure the host key to be read from a file at connection time.
         *
         * @param hostKey openssh host key file
         * @return this instance
         * @throws IllegalArgumentException if {@code hostKey} is null
         */
        public Builder hostKey(Path hostKey) {
            if (hostKey == null)
                throw new IllegalArgumentException("null hostKey");
            this.hostKeyProvider = new FileKeyPairProvider(hostKey);
            return this;
        }

        /**
         * Configure the host key to be read from a classpath resource at connection time.
         *
         * @param loader class loader
         * @param resource classpath resource
         * @return this instance
         * @throws IllegalArgumentException if either parameter is null
         */
        public Builder hostKey(ClassLoader loader, String resource) {
            if (loader == null)
                throw new IllegalArgumentException("null loader");
            if (resource == null)
                throw new IllegalArgumentException("null resource");
            this.hostKeyProvider = new ClassLoadableResourceKeyPairProvider(loader, resource);
            return this;
        }

    // Build

        /**
         * Build a new {@link SimpleConsoleSshServer} based on this instance.
         *
         * @return new server configured by this instance
         * @throws IllegalArgumentException if this builder is incompletely configured
         */
        public SimpleConsoleSshServer build() {
            return new SimpleConsoleSshServer(this);
        }
    }
}
