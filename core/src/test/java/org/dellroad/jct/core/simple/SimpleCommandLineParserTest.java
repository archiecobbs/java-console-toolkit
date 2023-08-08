
/*
 * Copyright (C) 2023 Archie L. Cobbs. All rights reserved.
 */

package org.dellroad.jct.core.simple;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleCommandLineParserTest {

    @Test(dataProvider = "data")
    public void testCommandLineParse(List<String> inputLines, List<String> expected) {

        // Create parser
        final SimpleCommandLineParser parser = new SimpleCommandLineParser();

        // Parse command line
        StringBuilder buf = new StringBuilder();
        List<String> actual = null;
        for (int i = 0; i < inputLines.size(); i++) {
            final boolean last = i == inputLines.size() - 1;
            final String line = inputLines.get(i);
            if (i > 0)
                buf.append('\n');
            buf.append(line);
            try {
                actual = parser.parseCommandLine(buf.toString());
            } catch (CommandLineParser.SyntaxException e) {
                assert expected == null :
                  "Expected " + expected + " but parse failed with " + e;
                return;
            }
            assert actual != null || !last :
              "Expected completion after " + (i + 1) + " lines but got continuation";
            assert actual == null || last :
              "Expected continuation after " + (i + 1) + " lines but got completion";
        }
        assert expected != null :
            "Expected:\n  Syntax Error"
            + "\nActual:\n  " + actual.stream().collect(Collectors.joining("\n  "));
        assert actual.equals(expected) :
            "Expected:\n  " + expected.stream().collect(Collectors.joining("\n  "))
            + "\nActual:\n  " + actual.stream().collect(Collectors.joining("\n  "));
    }

    @DataProvider(name = "data")
    public Object[][] genTruncationCases() {
        return new Object[][] {

            // Simple test
            {
                Arrays.asList(
                    "this is a test"
                ),
                Arrays.asList(
                    "this",
                    "is",
                    "a",
                    "test"
                )
            },

            // Quoting
            {
                Arrays.asList(
                    "this is \"a test\""
                ),
                Arrays.asList(
                    "this",
                    "is",
                    "a test"
                )
            },

            // Multi-line outside quote
            {
                Arrays.asList(
                    "this is \\",
                    "a test"
                ),
                Arrays.asList(
                    "this",
                    "is",
                    "a",
                    "test"
                )
            },

            // Multi-line within quote
            {
                Arrays.asList(
                    "this \"is",
                    "a test\""
                ),
                Arrays.asList(
                    "this",
                    "is\na test"
                )
            },

            // Misc backslash escapes
            {
                Arrays.asList(
                    "test 123 \"\\b\\t\\n\\f\\r \\'foo\\' \\\"bar\\\" back\\\\slash \\141\\142\\143\\041 \\u0064\\u0065\\u0066\""
                ),
                Arrays.asList(
                    "test",
                    "123",
                    "\b\t\n\f\r 'foo' \"bar\" back\\slash abc! def"
                )
            },

            // Syntax errors
            {
                Arrays.asList(
                    "\"\\04\""
                ),
                null
            },
            {
                Arrays.asList(
                    "\"\\u004\""
                ),
                null
            },
            {
                Arrays.asList(
                    "\"\\u00g3\""
                ),
                null
            },
            {
                Arrays.asList(
                    "\"\\w\""
                ),
                null
            },
        };
    }
}
