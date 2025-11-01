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
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public final class ArrayAccessExpr extends AbstractExprContainer implements Expr {
    public static final int REFERENCE_INDEX = 0;
    public static final int INDEX_INDEX = 1;

    public ArrayAccessExpr(final @NotNull Expr reference, final @NotNull Expr index) {
        addExpression(reference);
        addExpression(index);
    }

    public @NotNull Expr getReference() {
        return getExpressions().get(REFERENCE_INDEX);
    }

    public void setReference(final @NotNull Expr reference) {
        getExpressions().set(REFERENCE_INDEX, reference);
    }

    public @NotNull Expr getIndex() {
        return getExpressions().get(INDEX_INDEX);
    }

    public void setIndex(final @NotNull Expr index) {
        getExpressions().set(INDEX_INDEX, index);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        final var type = getReference().getType(context);
        if (!(type instanceof ArrayType arrayType)) {
            throw new EvaluationException("Array access requires array reference type", SourceDiagnostic.from(this));
        }
        return arrayType.elementType(); // We unwrap one layer of array so return element type
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var array = getReference().evaluateAsConst(context, Object.class);
        final var index = getIndex().evaluateAsConst(context, Integer.class);
        context.pushValue(LiteralExpr.of(Array.get(array, index)));
    }

    @Override
    public @NotNull ArrayAccessExpr copy() {
        return copyParentAndSourceTo(new ArrayAccessExpr(getReference().copy(), getIndex().copy()));
    }
}
