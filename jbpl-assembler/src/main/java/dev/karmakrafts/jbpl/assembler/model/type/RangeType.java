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
import dev.karmakrafts.jbpl.assembler.model.expr.RangeExpr;
import org.jetbrains.annotations.NotNull;

public record RangeType(Type type) implements Type {
    @Override
    public @NotNull TypeCategory getCategory(final @NotNull EvaluationContext context) {
        return TypeCategory.RANGE;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) throws EvaluationException {
        return new RangeExpr(type.createDefaultValue(context), type.createDefaultValue(context), false);
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) throws EvaluationException {
        throw new UnsupportedOperationException("Range types cannot be materialized");
    }

    @Override
    public boolean isResolved() {
        return type.isResolved();
    }

    @Override
    public @NotNull Type resolve(final @NotNull EvaluationContext context) throws EvaluationException {
        if (isResolved()) {
            return this;
        }
        return new RangeType(type.resolve(context));
    }
}
