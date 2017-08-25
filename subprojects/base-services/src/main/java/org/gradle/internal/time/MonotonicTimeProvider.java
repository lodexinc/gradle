/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal.time;

import java.util.concurrent.atomic.AtomicLong;

public class MonotonicTimeProvider implements TimeProvider {

    private final TimeProvider timeProvider;

    private final AtomicLong max = new AtomicLong();

    public MonotonicTimeProvider() {
        this(new ReliableTimeProvider());
    }

    public MonotonicTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public long getCurrentTime() {
        long currentTime = timeProvider.getCurrentTime();
        long currentMax;
        do {
            currentMax = max.get();
            currentTime = Math.max(currentTime, currentMax);
        } while (!max.compareAndSet(currentMax, currentTime));

        return currentTime;
    }

}
