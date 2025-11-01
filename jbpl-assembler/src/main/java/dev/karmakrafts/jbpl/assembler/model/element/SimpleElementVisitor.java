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

import java.util.function.Function;

public final class SimpleElementVisitor<R> implements ElementVisitor {
    private final Function<Element, R> function;
    private R result;

    public SimpleElementVisitor(final @NotNull Function<Element, R> function) {
        this.function = function;
    }

    public @Nullable R getResult() {
        return result;
    }

    @Override
    public @NotNull Element visitElement(final @NotNull Element element) {
        result = function.apply(element);
        if (result != null) {
            return element;
        }
        return ElementVisitor.super.visitElement(element);
    }
}
