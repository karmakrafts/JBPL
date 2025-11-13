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

package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record IntersectionType(@NotNull List<Type> alternatives) implements Type {
    public static @NotNull IntersectionType unfold(final List<Type> types) {
        final var queue = new ArrayDeque<>(types);
        final var unfoldedTypes = new ArrayList<Type>();
        while (!queue.isEmpty()) {
            final var type = queue.remove();
            if (type instanceof IntersectionType intersectionType) {
                queue.addAll(intersectionType.alternatives);
            }
            unfoldedTypes.add(type);
        }
        return new IntersectionType(unfoldedTypes);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static @NotNull Optional<IntersectionType> tryParse(final @Nullable String value) {
        if (value == null || !value.startsWith("(") || !value.endsWith(")")) {
            return Optional.empty();
        }
        // @formatter:off
        final var alternatives = Arrays.stream(value.substring(1, value.length() - 1).replace(" ", "").split("\\|"))
            .map(Type::tryParse)
            .toList();
        // @formatter:on
        if (alternatives.stream().anyMatch(Optional::isEmpty)) {
            return Optional.empty();
        }
        return Optional.of(new IntersectionType(alternatives.stream().map(Optional::get).toList()));
    }

    public @NotNull IntersectionType unfold() {
        return unfold(alternatives);
    }

    @Override
    public @NotNull TypeCategory getCategory() {
        return TypeCategory.INTERSECTION;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) {
        throw new UnsupportedOperationException("Intersection types do not have a default value");
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) {
        throw new UnsupportedOperationException("Intersection types cannot be materialized");
    }

    @Override
    public @NotNull String toString() { // @formatter:off
        return String.format("(%s)", alternatives.stream()
            .map(Type::toString)
            .collect(Collectors.joining("|")));
    } // @formatter:on
}
