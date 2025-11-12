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

package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class ErrorStatement extends AbstractExprContainer implements Statement {
    public static final int VALUE_INDEX = 0;

    public ErrorStatement(final @NotNull Expr value) {
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        value.setParent(this);
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.errorConsumer.accept(getValue().evaluateAsConst(context, Object.class).toString());
    }

    @Override
    public @NotNull ErrorStatement copy() {
        return copyParentAndSourceTo(new ErrorStatement(getValue().copy()));
    }
}
