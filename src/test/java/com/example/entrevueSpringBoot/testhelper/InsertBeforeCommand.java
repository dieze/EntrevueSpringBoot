package com.example.entrevueSpringBoot.testhelper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Test helper for inserting a text before another text on a target.
 * <p>
 * This is the implementation of a chained staged command. Usage:
 * <p>
 * insert("the id").before("some field").on("the payload");
 * <p>
 * insert("the id").before("some field")
 * .andInsert("the other id").before("some other field")
 * .on("the payload");
 */
public class InsertBeforeCommand {

    /**
     * Public entry-point of the command.
     */
    public static Inserting insert(String toInsert) {
        return new Inserting(new ArrayList<>(), toInsert);
    }

    // variant of lombok's @Value but with private constructor
    // and without the getters : this is a one-shot, syntaxic sugar, command
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Inserting {
        List<InsertingBefore> previousCommands;
        String toInsert;

        public InsertingBefore before(String before) {
            return new InsertingBefore(previousCommands, toInsert, before);
        }
    }

    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class InsertingBefore {
        @Getter // TODO: new usage... needs better API for modifying previousCommands
        List<InsertingBefore> previousCommands;
        String toInsert;
        String before;

        public Inserting andInsert(String toInsert) {
            previousCommands.add(this);
            return new Inserting(previousCommands, toInsert); // passing the same List previousCommands instance ; no memory leak
        }

        public String on(String targetText) {
            return on(targetText, true); // fail-safe by default
        }

        public String on(String targetText, boolean failSafe) {
            String inserting = targetText;

            for (InsertingBefore previousCommand : previousCommands) {
                inserting = previousCommand.doOn(inserting, failSafe);
            }

            return doOn(inserting, failSafe);
        }

        private String doOn(String targetText, boolean failSafe) {
            if (failSafe && !targetText.contains(before)) throw new RuntimeException(); // fail-safe
            return targetText.replace(before, toInsert + before); // end of command
        }
    }
}
