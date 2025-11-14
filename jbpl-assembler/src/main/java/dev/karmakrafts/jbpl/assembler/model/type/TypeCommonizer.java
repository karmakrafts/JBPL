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
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.statement.ReturnStatement;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public final class TypeCommonizer {
    private TypeCommonizer() {
    }

    public static @NotNull Optional<? extends Type> getCommonType(final @NotNull Collection<? extends Element> elements,
                                                                  final @NotNull EvaluationContext context) {
        // @formatter:off
        final var returnedTypes = elements.stream()
            .filter(ReturnStatement.class::isInstance)
            .map(ExceptionUtils.unsafeFunction(statement -> ((ReturnStatement)statement).getValue().getType(context)))
            .toList();
        // @formatter:on
        if (returnedTypes.isEmpty()) {
            // @formatter:off
            final var expressions = elements.stream()
                .filter(Expr.class::isInstance)
                .map(ExceptionUtils.unsafeFunction(statement -> ((Expr)statement).getType(context)))
                .collect(Collectors.toCollection(ArrayList::new));
            // @formatter:on
            Collections.reverse(expressions);
            return expressions.stream().findFirst();
        }
        return getCommonType(returnedTypes);
    }

    public static @NotNull Optional<? extends Type> getCommonType(final @NotNull Collection<? extends Type> types) {
        final var categories = types.stream().map(Type::getCategory).collect(Collectors.toSet());
        if (categories.contains(TypeCategory.INTEGER) && categories.contains(TypeCategory.FLOAT)) {
            // @formatter:off
            final var maxIntSize = types.stream()
                .filter(type -> type.getCategory() == TypeCategory.INTEGER)
                .mapToInt(type -> ((BuiltinType)type).byteSize)
                .max()
                .orElse(-1);
            final var maxFloatSize = types.stream()
                .filter(type -> type.getCategory() == TypeCategory.FLOAT)
                .mapToInt(type -> ((BuiltinType)type).byteSize)
                .max()
                .orElse(-1);
            // @formatter:on
            // If either size cannot be resolved or if the max int size > max float size, promotion cannot happen
            if (maxIntSize == -1 || maxFloatSize == -1 || maxIntSize > maxFloatSize) {
                return Optional.empty();
            }
            return BuiltinType.floatBySize(maxFloatSize); // The largest float type we can fit all floats AND ints into
        }
        if (categories.size() > 1) {
            return Optional.empty();
        }
        final var category = categories.stream().findFirst().orElseThrow();
        final var uniqueTypes = new HashSet<>(types);
        return switch(category) { // @formatter:off
            case ARRAY -> getCommonType(uniqueTypes.stream()
                .map(ArrayType.class::cast)
                .map(ArrayType::elementType)
                .toList())
                .map(Type::array);
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
