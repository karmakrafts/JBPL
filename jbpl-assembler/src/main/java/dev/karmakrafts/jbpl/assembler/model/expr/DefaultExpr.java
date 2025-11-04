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
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class DefaultExpr extends AbstractExprContainer implements Expr {
    public static final int TYPE_INDEX = 0;

    public DefaultExpr(final @NotNull Expr type) {
        addExpression(type);
    }

    public @NotNull Expr getType() {
        return getExpressions().get(TYPE_INDEX);
    }

    public void setType(final @NotNull Expr type) {
        getExpressions().set(TYPE_INDEX, type);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return getType().evaluateAsConst(context, Type.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.pushValue(getType(context).createDefaultValue(context).evaluateAsConst(context));
    }

    @Override
    public @NotNull DefaultExpr copy() {
        return copyParentAndSourceTo(new DefaultExpr(getType().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("default(%s)", getType());
    }
}
