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

package dev.karmakrafts.jbpl.assembler.model.element;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public final class ElementAttributes implements Iterable<Entry<ElementAttributeKey<?>, Object>> {
    private final HashMap<ElementAttributeKey<?>, Object> values = new HashMap<>();

    public <T> void put(final @NotNull ElementAttributeKey<T> key, T value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(final @NotNull ElementAttributeKey<T> key) {
        return (T) values.get(key);
    }

    @Override
    public @NotNull Iterator<Entry<ElementAttributeKey<?>, Object>> iterator() {
        return values.entrySet().iterator();
    }
}
