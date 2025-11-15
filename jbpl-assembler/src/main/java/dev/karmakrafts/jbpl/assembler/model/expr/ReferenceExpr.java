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
import dev.karmakrafts.jbpl.assembler.model.statement.DefineStatement;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ReferenceExpr extends AbstractReceiverExpr implements Expr, ExprContainer, Reference {
    public String name;

    public ReferenceExpr(final @NotNull String name) {
        super();
        this.name = name;
    }

    public @Nullable Expr findArgument(final @NotNull EvaluationContext context) {
        return context.peekFrame().namedLocalValues.get(name);
    }

    public @NotNull DefineStatement getDefine(final @NotNull EvaluationContext context) throws EvaluationException {
        var define = context.resolveByName(DefineStatement.class, name);
        if (define == null) { // Second attempt is for resolving private declarations
            context.pushFrame(getContainingFile());
            define = context.resolveByName(DefineStatement.class, name);
            context.popFrame();
        }
        if (define == null) {
            final var scope = context.getScope();
            final var message = String.format("Could not find define '%s' in scope %s", name, scope);
            throw new EvaluationException(message, SourceDiagnostic.from(this, message), context.createStackTrace());
        }
        return define;
    }

    @Override
    public @NotNull ConstExpr loadFromReference(final @NotNull EvaluationContext context) throws EvaluationException {
        final var argument = findArgument(context);
        if (argument != null) {
            return argument.evaluateAsConst(context);
        }
        getDefine(context).evaluate(context);
        return (ConstExpr) context.popValue();
    }

    @Override
    public void storeToReference(final @NotNull ConstExpr value,
                                 final @NotNull EvaluationContext context) throws EvaluationException {
        final var argument = findArgument(context);
        if (argument != null) {
            context.peekFrame().namedLocalValues.put(name, ConstExpr.of(value, getTokenRange()));
            return;
        }
        getDefine(context).setValue(value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        final var argument = findArgument(context);
        if (argument != null) {
            return argument.getType(context).resolveIfNeeded(context);
        }
        return getDefine(context).getType().evaluateAs(context, Type.class).resolveIfNeeded(context);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.pushValue(loadFromReference(context));
    }

    @Override
    public @NotNull ReferenceExpr copy() {
        final var reference = copyParentAndSourceTo(new ReferenceExpr(name));
        reference.setReceiver(getReceiver().copy());
        return reference;
    }

    @Override
    public @NotNull String toString() {
        return name;
    }
}
