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
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class SelectorReferenceExpr extends AbstractElement implements Expr {
    public final String name;

    public SelectorReferenceExpr(final @NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return PreproType.SELECTOR;
    }

    private @NotNull SelectorDecl getSelector(final @NotNull EvaluationContext context) {
        final var scope = context.getScope();
        final var define = context.resolveByName(SelectorDecl.class, name);
        if (define == null) {
            throw new IllegalStateException(String.format("Could not find selector '%s' in scope %s", name, scope));
        }
        return define;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {
        context.pushValue(LiteralExpr.of(getSelector(context), getTokenRange()));
    }

    @Override
    public @NotNull SelectorReferenceExpr copy() {
        return copyParentAndSourceTo(new SelectorReferenceExpr(name));
    }

    @Override
    public @NotNull String toString() {
        return String.format("selector(%s)", name);
    }
}
