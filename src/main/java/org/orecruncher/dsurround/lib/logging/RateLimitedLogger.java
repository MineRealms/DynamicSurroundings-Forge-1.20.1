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

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A logger wrapper that rate-limits log messages to prevent log spam.
 * Useful for messages that might be called frequently (e.g., every tick).
 */
public class RateLimitedLogger {

    private final IModLog logger;
    private final Object2LongOpenHashMap<String> lastLogTime = new Object2LongOpenHashMap<>();
    private final long minIntervalMs;

    /**
     * Create a rate-limited logger
     * @param logger The underlying logger
     * @param minIntervalMs Minimum time between identical messages in milliseconds
     */
    public RateLimitedLogger(IModLog logger, long minIntervalMs) {
        this.logger = logger;
        this.minIntervalMs = minIntervalMs;
        this.lastLogTime.defaultReturnValue(-1);
    }

    /**
     * Create a rate-limited logger with default 5 second interval
     */
    public RateLimitedLogger(IModLog logger) {
        this(logger, 5000);
    }

    private boolean shouldLog(String key) {
        long now = System.currentTimeMillis();
        long last = this.lastLogTime.getLong(key);

        if (last == -1 || (now - last) >= this.minIntervalMs) {
            this.lastLogTime.put(key, now);
            return true;
        }
        return false;
    }

    public void info(String msg, @Nullable Object... params) {
        if (shouldLog(msg)) {
            this.logger.info(msg, params);
        }
    }

    public void info(Supplier<String> message) {
        String msg = message.get();
        if (shouldLog(msg)) {
            this.logger.info(msg);
        }
    }

    public void warn(String msg, @Nullable Object... params) {
        if (shouldLog(msg)) {
            this.logger.warn(msg, params);
        }
    }

    public void warn(Supplier<String> message) {
        String msg = message.get();
        if (shouldLog(msg)) {
            this.logger.warn(msg);
        }
    }

    public void debug(String msg, @Nullable Object... params) {
        if (shouldLog(msg)) {
            this.logger.debug(msg, params);
        }
    }

    public void debug(Supplier<String> message) {
        String msg = message.get();
        if (shouldLog(msg)) {
            this.logger.debug(msg);
        }
    }

    public void error(Throwable e, String msg, @Nullable Object... params) {
        // Always log errors, but still rate limit
        if (shouldLog(msg + e.getClass().getName())) {
            this.logger.error(e, msg, params);
        }
    }

    /**
     * Clear the rate limit cache
     */
    public void clearCache() {
        this.lastLogTime.clear();
    }
}
