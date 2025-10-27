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

package dev.karmakrafts.jbpl.assembler.scope;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;
import java.util.function.Function;

public record Scope( // @formatter:off
    @Nullable Scope parent,
    @NotNull ScopeOwner owner
) { // @formatter:on
    /**
     * Perform a depth-first search in the current scope going outwards,
     * traversing the parent hierarchy of all scopes until the top level scope is reached.
     *
     * @param selector The selector function to map the current scope to the requested values.
     *                 This function may return null to indicate that the requested value hasn't been found.
     * @param <T>      The type of value being searched for.
     * @return The requested value if it is found, otherwise null.
     */
    public <T> @Nullable T find(final @NotNull Function<Scope, ? extends T> selector) {
        final var stack = new Stack<Scope>();
        stack.push(this);
        while (!stack.isEmpty()) {
            final var scope = stack.pop();
            final var parentScope = scope.parent;
            final var result = selector.apply(scope);
            if (result == null) {
                if (parentScope != null) {
                    stack.push(parentScope);
                }
                continue;
            }
            return result;
        }
        return null;
    }

    @Override
    public @NotNull String toString() {
        return owner.toString();
    }
}
