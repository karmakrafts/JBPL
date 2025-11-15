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
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public sealed interface Type
    permits ArrayType, BuiltinType, ClassType, IntersectionType, PreproClassType, PreproType, RangeType, UnresolvedType {
    /**
     * Attempt to parse a type from the given string value.
     * This will assume the input values are in one of the following formats:
     * <ul>
     *     <li>A builtin type by its keyword like <b>i8</b>, <b>f32</b>, <b>string</b> etc.</li>
     *     <li>A preprocessor type by its name (or keyword) like <b>type</b>, <b>opcode</b> etc.</li>
     *     <li>A JVM class type in the form of <b>&lt;fully/qualified/Name&gt;</b></li>
     *     <li>An intersection type in the form of <b>(OneType | AnotherType)</b></li>
     *     <li>An array type in the form of <b>[type]</b>, <b>[[type]]</b> etc.</li>
     * </ul>
     */
    static @NotNull Optional<Type> tryParse(final @Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }
        // @formatter:off
        return BuiltinType.findByName(value)
            .map(Type.class::cast)
            .or(() -> PreproType.findByName(value))
            .or(() -> ArrayType.tryParse(value))
            .or(() -> ClassType.tryParse(value))
            .or(() -> IntersectionType.tryParse(value));
        // @formatter:on
    }

    @NotNull TypeCategory getCategory(final @NotNull EvaluationContext context) throws EvaluationException;

    @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) throws EvaluationException;

    @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) throws EvaluationException;

    boolean isResolved();

    @NotNull Type resolve(final @NotNull EvaluationContext context) throws EvaluationException;

    default @NotNull Type resolveIfNeeded(final @NotNull EvaluationContext context) throws EvaluationException {
        if (isResolved()) {
            return this;
        }
        return resolve(context);
    }

    default boolean isAssignableFrom(final @NotNull Type other,
                                     final @NotNull EvaluationContext context) throws EvaluationException {
        return equals(other);
    }

    default boolean canCastTo(final @NotNull Type other,
                              final @NotNull EvaluationContext context) throws EvaluationException {
        return equals(other);
    }

    default @NotNull Expr cast(final @NotNull Expr value,
                               final @NotNull EvaluationContext context) throws EvaluationException {
        return value;
    }

    default @NotNull TypeConversion conversionTypeFrom(final @NotNull Type other,
                                                       final @NotNull EvaluationContext context) throws EvaluationException {
        if (other == this) {
            return TypeConversion.DIRECT;
        }
        else if (isAssignableFrom(other, context)) {
            return TypeConversion.COERCE;
        }
        return TypeConversion.NONE;
    }

    default @NotNull ArrayType array() {
        return new ArrayType(this);
    }

    default @NotNull RangeType range() {
        return new RangeType(this);
    }
}
