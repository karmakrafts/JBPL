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

public final class ReferenceExpr extends AbstractReceiverExpr implements Expr, ExprContainer, Reference {
    public static final int NAME_INDEX = RECEIVER_INDEX + 1;

    public ReferenceExpr(final @NotNull Expr name) {
        super();
        addExpression(name);
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        name.setParent(this);
        getExpressions().set(NAME_INDEX, name);
    }

    public @NotNull DefineStatement getDefine(final @NotNull String name,
                                              final @NotNull EvaluationContext context) throws EvaluationException {
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
        final var name = getName().evaluateAs(context, String.class);
        final var receiver = getReceiver();
        final var frame = context.peekFrame();
        if (receiver instanceof IntrinsicReceiverExpr) { // Load intrinsic define from the current stack frame if present
            final var value = frame.intrinsicDefines.get(name);
            if (value == null) {
                final var message = String.format("No intrinsic value named '%s' in %s", name, receiver);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
            return value.evaluateAsConst(context);
        }
        final var argument = frame.namedLocalValues.get(name);
        if (argument != null) {
            return argument.evaluateAsConst(context);
        }
        getDefine(name, context).evaluate(context);
        return (ConstExpr) context.popValue();
    }

    @Override
    public void storeToReference(final @NotNull ConstExpr value,
                                 final @NotNull EvaluationContext context) throws EvaluationException {
        final var name = getName().evaluateAs(context, String.class);
        final var receiver = getReceiver();
        final var frame = context.peekFrame();
        if (receiver instanceof IntrinsicReceiverExpr) {
            if (!frame.intrinsicDefines.containsKey(name)) {
                final var message = String.format("No intrinsic value named '%s' in %s", name, receiver);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
            frame.intrinsicDefines.put(name, value); // Update ref to value (replacing instruction lists etc.)
            return;
        }
        final var argument = frame.namedLocalValues.get(name);
        if (argument != null) {
            context.peekFrame().namedLocalValues.put(name, ConstExpr.of(value, getTokenRange()));
            return;
        }
        getDefine(name, context).setValue(value);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        final var name = getName().evaluateAs(context, String.class);
        final var frame = context.peekFrame();
        final var receiver = getReceiver();
        if (receiver instanceof IntrinsicReceiverExpr) {
            final var value = frame.intrinsicDefines.get(name);
            if (value == null) {
                final var message = String.format("No intrinsic value named '%s' in %s", name, receiver);
                throw new EvaluationException(message,
                    SourceDiagnostic.from(this, message),
                    context.createStackTrace());
            }
            return value.getType(context).resolveIfNeeded(context);
        }
        final var argument = frame.namedLocalValues.get(name);
        if (argument != null) {
            return argument.getType(context).resolveIfNeeded(context);
        }
        return getDefine(name, context).getType().evaluateAs(context, Type.class).resolveIfNeeded(context);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        context.pushValue(loadFromReference(context));
    }

    @Override
    public @NotNull ReferenceExpr copy() {
        final var reference = copyParentAndSourceTo(new ReferenceExpr(getName().copy()));
        reference.setReceiver(getReceiver().copy());
        return reference;
    }

    @Override
    public @NotNull String toString() {
        return getName().toString();
    }
}
