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

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.statement.ReturnStatement;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class TypeCommonizer {
    private TypeCommonizer() {
    }

    // TODO: check if any element exists after the last return statement
    public static @NotNull Optional<? extends Type> getCommonType(final @NotNull ElementContainer container,
                                                                  final @NotNull AssemblerContext context) throws EvaluationException {
        final var elements = container.getElements();
        // @formatter:off
        final var returnedTypes = elements.stream()
            .filter(ReturnStatement.class::isInstance)
            .map(ExceptionUtils.propagateUnchecked(statement -> ((ReturnStatement)statement).getValue().getType(context)))
            .toList();
        // @formatter:on
        if (returnedTypes.isEmpty()) {
            // @formatter:off
            final var expressions = elements.stream()
                .filter(Expr.class::isInstance)
                .map(ExceptionUtils.propagateUnchecked(statement -> ((Expr)statement).getType(context)))
                .collect(Collectors.toCollection(ArrayList::new));
            // @formatter:on
            Collections.reverse(expressions);
            return expressions.stream().findFirst();
        }
        return getCommonType(returnedTypes);
    }

    public static @NotNull Optional<? extends Type> getCommonType(final @NotNull Collection<? extends Type> types) {
        final var categories = types.stream().map(Type::getCategory).collect(Collectors.toSet());
        if (categories.size() != 1) {
            return Optional.empty(); // If we found more than one type category, no commonization can occur
        }
        final var category = categories.stream().findFirst().orElseThrow();
        final var uniqueTypes = new HashSet<>(types);
        return switch(category) { // @formatter:off
            case INTEGER, FLOAT -> uniqueTypes.stream()
                .filter(BuiltinType.class::isInstance)
                .map(BuiltinType.class::cast)
                .max(BuiltinType::compareTo); // Ints and floats are ordered by size in ascending order in the enum
            default -> uniqueTypes.stream().findFirst();
        }; // @formatter:on
    }

    public static @NotNull Optional<? extends Type> getCommonType(final @NotNull Type... types) {
        return getCommonType(List.of(types));
    }
}
