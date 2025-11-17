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
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.type.ReceiverType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.scope.Scope;
import org.jetbrains.annotations.NotNull;

public final class ScopeReceiverExpr extends AbstractElement implements Expr {
    public Scope scope;

    public ScopeReceiverExpr(final @NotNull Scope scope) {
        this.scope = scope;
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return ReceiverType.SCOPE;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
    }

    @Override
    public @NotNull ScopeReceiverExpr copy() {
        return copyParentAndSourceTo(new ScopeReceiverExpr(scope));
    }
}
