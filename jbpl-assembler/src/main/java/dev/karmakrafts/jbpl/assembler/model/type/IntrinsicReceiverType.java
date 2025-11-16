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

public enum IntrinsicReceiverType implements Type {
    // @formatter:off
    FUN,
    FIELD;
    // @formatter:on

    @Override
    public @NotNull TypeCategory getCategory(final @NotNull EvaluationContext context) throws EvaluationException {
        return TypeCategory.INTRINSIC_RECEIVER;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) throws EvaluationException {
        throw new UnsupportedOperationException("Intrinsic receiver type has no default value");
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(@NotNull EvaluationContext context) throws EvaluationException {
        throw new UnsupportedOperationException("Intrinsic receiver type cannot be materialized");
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public @NotNull Type resolve(final @NotNull EvaluationContext context) throws EvaluationException {
        return this;
    }
}
