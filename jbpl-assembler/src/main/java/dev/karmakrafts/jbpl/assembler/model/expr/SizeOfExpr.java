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

package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.ArrayType;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public final class SizeOfExpr extends AbstractExprContainer implements Expr {
    public static final int VALUE_INDEX = 0;

    public SizeOfExpr(final @NotNull Expr value) {
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getValue().setParent(null);
        value.setParent(this);
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return BuiltinType.I32;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var valueType = getValue().getType(context);
        final var value = getValue().evaluateAs(context, Object.class);
        if (valueType == PreproType.TYPE && value instanceof BuiltinType builtinType) {
            switch (builtinType) {
                case VOID -> {
                    context.pushValue(ConstExpr.of(0, getTokenRange()));
                    return;
                }
                case I8 -> {
                    context.pushValue(ConstExpr.of(Byte.BYTES, getTokenRange()));
                    return;
                }
                case I16 -> {
                    context.pushValue(ConstExpr.of(Short.BYTES, getTokenRange()));
                    return;
                }
                case I32 -> {
                    context.pushValue(ConstExpr.of(Integer.BYTES, getTokenRange()));
                    return;
                }
                case I64 -> {
                    context.pushValue(ConstExpr.of(Long.BYTES, getTokenRange()));
                    return;
                }
                case F32 -> {
                    context.pushValue(ConstExpr.of(Float.BYTES, getTokenRange()));
                    return;
                }
                case F64 -> {
                    context.pushValue(ConstExpr.of(Double.BYTES, getTokenRange()));
                    return;
                }
                case CHAR -> {
                    context.pushValue(ConstExpr.of(Character.BYTES, getTokenRange()));
                    return;
                }
            }
        }
        else if (valueType == BuiltinType.STRING) {
            context.pushValue(ConstExpr.of(((String) value).length(), getTokenRange()));
            return;
        }
        else if (valueType instanceof ArrayType) {
            context.pushValue(ConstExpr.of(Array.getLength(value), getTokenRange()));
            return;
        }
        throw new EvaluationException(String.format("Incompatible type %s for sizeof-expression", valueType),
            SourceDiagnostic.from(this, getValue()),
            context.createStackTrace());
    }

    @Override
    public @NotNull SizeOfExpr copy() {
        return copyParentAndSourceTo(new SizeOfExpr(getValue().copy()));
    }
}
