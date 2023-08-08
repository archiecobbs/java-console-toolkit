
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple command line parser:
 *
 * <ul>
 *  <li>Command name and parameters are separated by whitespace
 *  <li>Java-style doubly-quoted string literals are supported, and may span multiple lines
 *  <li>Backslash escapes are supported for (in particular) double quote and backslash characters
 *  <li>Backslash escapes are supported for end of line continuations to the next line
 * </ul>
 */
public class SimpleCommandLineParser implements CommandLineParser {

    @Override
    public List<String> parseCommandLine(String line) throws SyntaxException {
        if (line == null)
            throw new IllegalArgumentException("null line");
        final ArrayList<String> argList = new ArrayList<>();
        final int length = line.length();
        boolean inquote = false;
        int posn = 0;
        StringBuilder nextArg = null;           // null means we are searching for the next word
        while (posn < length) {

            // Get next character
            char ch = line.charAt(posn++);

            // Handle unquoted state
            if (!inquote) {

                // Start of quoted word?
                if (ch == '"') {
                    if (nextArg == null)
                        nextArg = new StringBuilder();
                    inquote = true;
                    continue;
                }

                // Backslash escape outside of quotes?
                if (ch == '\\') {
                    if (posn >= line.length())
                        return null;
                    ch = line.charAt(posn++);
                }

                // Whitespace outside of quotes?
                if (Character.isWhitespace(ch)) {

                    // Terminate any unquoted word
                    if (nextArg != null) {
                        argList.add(nextArg.toString());
                        nextArg = null;
                    }
                    continue;
                }

                // Non-whitespace in unquoted state - start new word if needed
                if (nextArg == null)
                    nextArg = new StringBuilder();

                // Normal character - append to current word
                nextArg.append(ch);
            } else {

                // Quote character in quoted state?
                if (ch == '"') {
                    inquote = false;
                    continue;
                }

                // Non-quote character in quoted state
                if ((posn = this.scanQuotedChar(line, posn - 1, nextArg)) == -1)
                    return null;
            }
        }

        // If line ended inside quote, continue
        if (inquote)
            return null;

        // If word in progress, terminate and add to list
        if (nextArg != null)
            argList.add(nextArg.toString());

        // Done
        return argList;
    }

    private int scanQuotedChar(String line, int posn, StringBuilder arg) throws SyntaxException {
        if (posn >= line.length())
            return -1;
        final char ch = line.charAt(posn++);
        switch (ch) {
        case '"':
            throw new AssertionError();
        case '\\':
            return this.scanQuotedBackslashEscape(line, posn, arg);
        default:
            arg.append(ch);
            return posn;
        }
    }

    private int scanQuotedBackslashEscape(String line, int posn, StringBuilder arg) throws SyntaxException {
        final int origPosn = posn - 1;
        final int length = line.length();
        if (posn >= length)
            return -1;                      // line continuation
        boolean simpleEscape = true;
        char ch = line.charAt(posn++);
        switch (ch) {
        case 'b':
            ch = '\b';
            break;
        case 't':
            ch = '\t';
            break;
        case 'n':
            ch = '\n';
            break;
        case 'f':
            ch = '\f';
            break;
        case 'r':
            ch = '\r';
            break;
        case '\'':
        case '\"':
        case '\\':
            break;
        default:
            simpleEscape = false;
            break;
        }
        if (simpleEscape) {
            arg.append(ch);
            return posn;
        }

        // Decode octal escape
        int charValue = Character.digit(ch, 8);
        if (charValue != -1) {
            for (int i = 0; i < 2 && (charValue & ~0x3f) == 0; i++) {
                if (posn >= length || line.charAt(posn) == '"')
                    throw this.makeError(origPosn, "truncated octal escape");
                ch = line.charAt(posn++);
                final int oct = Character.digit(ch, 8);
                if (oct == -1)
                    break;
                charValue = (charValue << 3) | oct;
            }
            arg.append((char)charValue);
            return posn;
        }

        // Decode Unicode escape
        if (ch == 'u') {
            charValue = 0;
            for (int i = 0; i < 4; i++) {
                if (posn >= length || line.charAt(posn) == '"')
                    throw this.makeError(origPosn, "truncated Unicode escape");
                ch = line.charAt(posn++);
                if (i == 0 && ch == 'u') {      // allow arbitrarily many 'u' characters per JLS
                    i--;
                    continue;
                }
                final int hex = Character.digit(ch, 16);
                if (hex == -1)
                    throw this.makeError(origPosn, "invalid Unicode escape");
                charValue = (charValue << 4) | hex;
            }
            arg.append((char)charValue);
            return posn;
        }

        // Bogus
        throw this.makeError(origPosn, "invalid backslash escape");
    }

    private SyntaxException makeError(int offset, String message) {
        return new SyntaxException(offset, message);
    }
}
