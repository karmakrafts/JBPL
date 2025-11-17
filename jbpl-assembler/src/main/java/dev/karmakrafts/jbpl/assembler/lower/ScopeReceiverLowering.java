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

package dev.karmakrafts.jbpl.assembler.lower;

import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.scope.ScopeAwareElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Lowering pass to insert the correct scope receivers for things like references.
 */
public final class ScopeReceiverLowering extends ScopeAwareElementVisitor {
    public static final ScopeReceiverLowering INSTANCE = new ScopeReceiverLowering();

    private ScopeReceiverLowering() {
    }

    private @NotNull Expr visitReceiverExpr(final @NotNull Expr expr) {
        if (!(expr instanceof AbstractReceiverExpr receiverExpr) || !receiverExpr.getReceiver().isUnit()) {
            return expr;
        }
        receiverExpr.setReceiver(new ScopeReceiverExpr(getScope()));
        return receiverExpr;
    }

    @Override
    public @NotNull Expr visitReferenceExpr(final @NotNull ReferenceExpr referenceExpr) {
        return visitReceiverExpr(super.visitReferenceExpr(referenceExpr));
    }

    @Override
    public @NotNull Expr visitMacroCallExpr(final @NotNull MacroCallExpr macroCallExpr) {
        return visitReceiverExpr(super.visitMacroCallExpr(macroCallExpr));
    }
}
