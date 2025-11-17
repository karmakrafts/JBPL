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

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public final class ScopeResolver {
    private final Scope scope;

    public ScopeResolver(final @NotNull Scope scope) {
        this.scope = scope;
    }

    public static <E extends Element> @NotNull List<E> resolveAllLocally(final @NotNull Scope scope,
                                                                         final @NotNull Class<E> type,
                                                                         final @NotNull Predicate<E> filter) {
        if (!(scope.owner() instanceof ElementContainer elementContainer)) {
            return List.of();
        }
        return elementContainer.findElements(type, filter);
    }

    public static <E extends Element> @NotNull List<E> resolveAll(final @NotNull Scope scope,
                                                                  final @NotNull Class<E> type,
                                                                  final @NotNull Predicate<E> filter) {
        return scope.findAll(currentScope -> {
            final var owner = currentScope.owner();
            if (!(owner instanceof ElementContainer ownerElement)) {
                return null;
            }
            return ownerElement.findElement(type, filter).orElse(null);
        });
    }

    public static <E extends Element> @Nullable E resolveLocally(final @NotNull Scope scope,
                                                                 final @NotNull Class<E> type,
                                                                 final @NotNull Predicate<E> filter) {
        if (!(scope.owner() instanceof ElementContainer elementContainer)) {
            return null;
        }
        return elementContainer.findElement(type, filter).orElse(null);
    }

    public static <E extends Element> @Nullable E resolve(final @NotNull Scope scope,
                                                          final @NotNull Class<E> type,
                                                          final @NotNull Predicate<E> filter) {
        return scope.find(currentScope -> {
            final var owner = currentScope.owner();
            if (!(owner instanceof ElementContainer ownerElement)) {
                return null;
            }
            return ownerElement.findElement(type, filter).orElse(null);
        });
    }

    public <E extends Element> @Nullable E resolveLocally(final @NotNull Class<E> type,
                                                          final @NotNull Predicate<E> filter) {
        return resolveLocally(scope, type, filter);
    }

    public <E extends Element> @Nullable E resolve(final @NotNull Class<E> type, final @NotNull Predicate<E> filter) {
        return resolve(scope, type, filter);
    }

    public <E extends Element> @NotNull List<E> resolveAllLocally(final @NotNull Class<E> type,
                                                                  final @NotNull Predicate<E> filter) {
        return resolveAllLocally(scope, type, filter);
    }

    public <E extends Element> @NotNull List<E> resolveAll(final @NotNull Class<E> type,
                                                           final @NotNull Predicate<E> filter) {
        return resolveAll(scope, type, filter);
    }
}
