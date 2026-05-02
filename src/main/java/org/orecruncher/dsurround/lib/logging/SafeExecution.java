/*
 * This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.orecruncher.dsurround.lib.logging;

import org.orecruncher.dsurround.lib.Library;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class for safe execution with automatic error logging.
 * Helps prevent crashes and provides detailed error information.
 */
public final class SafeExecution {

    private static final IModLog LOGGER = Library.LOGGER;

    /**
     * Execute a runnable with error handling
     * @param context Description of what's being executed (for logging)
     * @param action The action to execute
     * @return true if successful, false if an error occurred
     */
    public static boolean execute(String context, Runnable action) {
        try {
            action.run();
            return true;
        } catch (Exception e) {
            LOGGER.error(e, "Error during %s", context);
            return false;
        }
    }

    /**
     * Execute a supplier with error handling
     * @param context Description of what's being executed (for logging)
     * @param supplier The supplier to execute
     * @param defaultValue Value to return if an error occurs
     * @return The result of the supplier, or defaultValue if an error occurred
     */
    public static <T> T execute(String context, Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            LOGGER.error(e, "Error during %s", context);
            return defaultValue;
        }
    }

    /**
     * Execute a consumer with error handling
     * @param context Description of what's being executed (for logging)
     * @param consumer The consumer to execute
     * @param value The value to pass to the consumer
     * @return true if successful, false if an error occurred
     */
    public static <T> boolean execute(String context, Consumer<T> consumer, T value) {
        try {
            consumer.accept(value);
            return true;
        } catch (Exception e) {
            LOGGER.error(e, "Error during %s", context);
            return false;
        }
    }

    /**
     * Execute a runnable with error handling and custom error handler
     * @param context Description of what's being executed (for logging)
     * @param action The action to execute
     * @param errorHandler Custom error handler
     * @return true if successful, false if an error occurred
     */
    public static boolean executeWithHandler(String context, Runnable action, Consumer<Exception> errorHandler) {
        try {
            action.run();
            return true;
        } catch (Exception e) {
            LOGGER.error(e, "Error during %s", context);
            if (errorHandler != null) {
                try {
                    errorHandler.accept(e);
                } catch (Exception handlerError) {
                    LOGGER.error(handlerError, "Error in error handler for %s", context);
                }
            }
            return false;
        }
    }

    /**
     * Execute with debug logging before and after
     * @param context Description of what's being executed
     * @param action The action to execute
     * @return true if successful, false if an error occurred
     */
    public static boolean executeWithDebug(String context, Runnable action) {
        LOGGER.debug("Starting: %s", context);
        try {
            action.run();
            LOGGER.debug("Completed: %s", context);
            return true;
        } catch (Exception e) {
            LOGGER.error(e, "Error during %s", context);
            return false;
        }
    }
}
