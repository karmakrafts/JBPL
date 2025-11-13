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
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public final class StringLerpExpr extends AbstractExprContainer implements Expr {
    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return BuiltinType.STRING;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var buffer = new StringBuilder();
        final var expressions = getExpressions();
        for (final var expr : expressions) {
            buffer.append(expr.evaluateAs(context, Object.class));
        }
        context.pushValue(ConstExpr.of(buffer.toString(), getTokenRange()));
    }

    @Override
    public @NotNull StringLerpExpr copy() {
        final var result = copyParentAndSourceTo(new StringLerpExpr());
        result.addExpressions(getExpressions().stream().map(Expr::copy).toList());
        return result;
    }

    @Override
    public @NotNull String toString() {
        final var builder = new StringBuilder();
        builder.append('"');
        // @formatter:off
        builder.append(getExpressions().stream()
            .map(expr -> {
                if(expr instanceof ConstExpr literalExpr) {
                    return literalExpr.getConstValue().toString();
                }
                return expr.toString();
            })
            .collect(Collectors.joining("")));
        // @formatter:on
        builder.append('"');
        return builder.toString();
    }
}
