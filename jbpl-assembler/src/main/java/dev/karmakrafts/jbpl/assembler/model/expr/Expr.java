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
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public interface Expr extends Statement {
    @Override
    @NotNull Expr copy();

    @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException;

    default @NotNull LiteralExpr evaluateAsConst(final @NotNull EvaluationContext context) throws EvaluationException {
        // After evaluation
        evaluate(context);
        var result = context.popValue();
        if (result instanceof LiteralExpr literalExpr) {
            return literalExpr;
        }
        return result.evaluateAsConst(context);
    }

    default <T> @NotNull T evaluateAs(final @NotNull EvaluationContext context,
                                      final @NotNull Class<T> type) throws EvaluationException {
        return type.cast(evaluateAsConst(context).value);
    }

    default @NotNull Object evaluateAsConstAndMaterialize(final @NotNull EvaluationContext context) throws EvaluationException {
        final var type = getType(context);
        if (type == PreproType.TYPE) {
            // Const types are materialized after unwrapping
            return evaluateAs(context, Type.class).materialize(context);
        }
        return evaluateAs(context, Object.class);
    }

    default boolean isUnit() {
        return this instanceof LiteralExpr literalExpr && literalExpr.type == BuiltinType.VOID;
    }
}
