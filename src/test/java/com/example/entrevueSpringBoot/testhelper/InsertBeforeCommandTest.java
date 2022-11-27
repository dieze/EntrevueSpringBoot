package com.example.entrevueSpringBoot.testhelper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map.Entry;

import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InsertBeforeCommandTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource
    public void insert(String testCaseName, List<Entry<String, String>> inserts, @Nullable Boolean failSafe, boolean shouldFail, String expectedResult) { // inserts: key=toInsert, value=before

        assertTrue(
                inserts.size() > 0,
                "Developer error! " +
                        "There must be at least one entry in inserts " +
                        "otherwise the test case is nonsense."
        );

        // construct the InsertingBefore command

        InsertBeforeCommand.InsertingBefore command = InsertBeforeCommand
                .insert(inserts.get(0).getKey()) // toInsert
                .before(inserts.get(0).getValue()); // before

        for (int i = 1; i < inserts.size(); i++) {
            command = command
                    .andInsert(inserts.get(i).getKey()) // toInsert
                    .before(inserts.get(i).getValue()); // before
        }

        // apply the InsertingBefore command on the target text

        String result = null;
        Exception thrown = null; // was not able not use JUnit5 assertThrows
        try {
            if (failSafe != null) result = command.on(TEXT, failSafe);
            else result = command.on(TEXT);
        } catch (Exception e) {
            thrown = e;
        }
        if (shouldFail) {
            assertTrue(thrown instanceof RuntimeException);
            return; // apply command threw, don't go further
        }

        assertEquals(expectedResult, result);
    }

    private static final String TEXT = "The quick brown fox jumps over the lazy dog";

    /**
     * Parameters for {@link #insert(String testCaseName, List inserts, Boolean failSafe, boolean shouldFail, String expectedResult)}.
     */
    static Object[][] insert() { // List<Entry<String, String>> inserts: key=toInsert, value=before

        //noinspection ArraysAsListWithZeroOrOneArgument ; use of asList with only one entry
        return new Object[][]{

                //
                // one insert & should not fail
                //
                new Object[]{
                        "one insert & default fail-safe",
                        asList(entry(" cute", " brown ")), null, false, "The quick cute brown fox jumps over the lazy dog",
                },
                new Object[]{
                        "one insert & explicit fail-safe",
                        asList(entry(" cute", " brown ")), true, false, "The quick cute brown fox jumps over the lazy dog",
                },
                new Object[]{
                        "one insert & non fail-safe",
                        asList(entry(" cute", " brown ")), false, false, "The quick cute brown fox jumps over the lazy dog",
                },

                //
                // one insert & should fail
                // brown -> bown typo makes it fail
                //
                new Object[]{
                        "one insert & default fail-safe & should fail",
                        asList(entry(" cute", " bown ")), null, true, "whatever",
                },
                new Object[]{
                        "one insert & explicit fail-safe & should fail",
                        asList(entry(" cute", " bown ")), true, true, "whatever",
                },
                new Object[]{
                        "one insert & non fail-safe => silent fail",
                        asList(entry(" cute", " bown ")), false, false, TEXT, // silent fail => TEXT is unchanged
                },

                //
                // two inserts & should not fail
                //
                new Object[]{
                        "two inserts & default fail-safe",
                        asList(entry(" cute", " brown "), entry(" black", " lazy ")), null, false, "The quick cute brown fox jumps over the black lazy dog",
                },
                new Object[]{
                        "two inserts & explicit fail-safe",
                        asList(entry(" cute", " brown "), entry(" black", " lazy ")), true, false, "The quick cute brown fox jumps over the black lazy dog",
                },
                new Object[]{
                        "two inserts & non fail-safe",
                        asList(entry(" cute", " brown "), entry(" black", " lazy ")), false, false, "The quick cute brown fox jumps over the black lazy dog",
                },

                //
                // two inserts & should fail
                // lazy -> lay typo makes it fail
                //
                // we make it fail on the second insert to test that the
                // first insert is actually made is fail-safe is true
                //
                new Object[]{
                        "two inserts & default fail-safe & should fail",
                        asList(entry(" cute", " brown "), entry(" black", " lay ")), null, true, "whatever",
                },
                new Object[]{
                        "two inserts & explicit fail-safe & should fail",
                        asList(entry(" cute", " brown "), entry(" black", " lay ")), true, true, "whatever",
                },
                new Object[]{
                        "two inserts & non fail-safe => silent fail", // but first insert " cute" succeeds
                        asList(entry(" cute", " brown "), entry(" black", " lay ")), false, false, "The quick cute brown fox jumps over the lazy dog",
                },
        };
    }
}
