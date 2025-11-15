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
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.ArrayExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ArrayType(Type elementType) implements Type {
    public static @NotNull Optional<ArrayType> tryParse(final @Nullable String value) {
        if (value == null || !value.startsWith("[")) {
            return Optional.empty();
        }
        final var length = value.length();
        var dimensions = 0;
        Type elementType = null;
        for (var i = 0; i < length; i++) {
            final var c = value.charAt(i);
            if (c == '[') {
                dimensions++;
                continue;
            }
            if (c == ']') {
                break; // We can stop parsing
            }
            elementType = Type.tryParse(value.substring(dimensions, length - dimensions)).orElse(null);
        }
        if (elementType == null) {
            return Optional.empty();
        }
        var arrayType = new ArrayType(elementType);
        for (var i = 0; i < dimensions - 1; i++) {
            arrayType = new ArrayType(elementType);
        }
        return Optional.of(arrayType);
    }

    @Override
    public boolean isResolved() {
        return elementType.isResolved();
    }

    @Override
    public @NotNull Type resolve(final @NotNull EvaluationContext context) throws EvaluationException {
        if (elementType.isResolved()) {
            return this;
        }
        return new ArrayType(elementType.resolve(context));
    }

    @Override
    public @NotNull TypeCategory getCategory(final @NotNull EvaluationContext context) {
        return TypeCategory.ARRAY;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) {
        return new ArrayExpr(ConstExpr.of(this));
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) throws EvaluationException {
        if (!elementType.getCategory(context).isMaterializable()) {
            throw new UnsupportedOperationException(String.format("Array of type %s cannot be materialized",
                elementType));
        }
        return org.objectweb.asm.Type.getType(String.format("[%s", elementType.materialize(context).getDescriptor()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("[%s]", elementType);
    }
}
