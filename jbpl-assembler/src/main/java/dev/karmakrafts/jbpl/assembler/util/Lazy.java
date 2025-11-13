/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.jbpl.assembler.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class Lazy<T> {
    private final Supplier<T> factory;
    private boolean isInitialized = false;
    private T value;

    public Lazy(final @NotNull Supplier<T> factory) {
        this.factory = factory;
    }

    public void reset() {
        value = null;
        isInitialized = false;
    }

    public void set(final T value) {
        this.value = value;
        isInitialized = true;
    }

    public T get() {
        if (!isInitialized) {
            value = factory.get();
            isInitialized = true;
        }
        return value;
    }
}
