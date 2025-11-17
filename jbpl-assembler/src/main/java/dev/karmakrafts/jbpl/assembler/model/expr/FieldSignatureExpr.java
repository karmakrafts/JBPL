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
import dev.karmakrafts.jbpl.assembler.model.type.PreproType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;

import java.util.Optional;

public final class FieldSignatureExpr extends AbstractExprContainer implements SignatureExpr {
    public static final int OWNER_INDEX = 0;
    public static final int NAME_INDEX = 1;
    public static final int TYPE_INDEX = 2;

    public FieldSignatureExpr(final @NotNull Expr owner, final @NotNull Expr name, final @NotNull Expr type) {
        addExpression(owner);
        addExpression(name);
        addExpression(type);
    }

    public static @NotNull Optional<FieldSignatureExpr> dematerialize(final @NotNull String owner,
                                                                      final @NotNull String name,
                                                                      final @NotNull String desc) { // @formatter:off
        return Type.dematerialize(org.objectweb.asm.Type.getObjectType(owner))
            .flatMap(type -> Type.dematerialize(org.objectweb.asm.Type.getType(desc))
                .map(value -> new FieldSignatureExpr(ConstExpr.of(type),
                    ConstExpr.of(name),
                    ConstExpr.of(value))));
    } // @formatter:on

    public static @NotNull Optional<FieldSignatureExpr> dematerialize(final @NotNull Handle handle) {
        return dematerialize(handle.getOwner(), handle.getName(), handle.getDesc());
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) {
        return PreproType.FIELD_SIGNATURE;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var owner = getFieldOwner().evaluateAsConst(context);
        final var name = getFieldName().evaluateAsConst(context);
        final var type = getFieldType().evaluateAsConst(context);
        final var signature = new FieldSignatureExpr(owner, name, type);
        signature.setParent(getParent());
        signature.setTokenRange(getTokenRange());
        context.pushValue(ConstExpr.of(signature, getTokenRange()));
    }

    @Override
    public @NotNull String evaluateAsConstDescriptor(final @NotNull EvaluationContext context) throws EvaluationException {
        return getFieldType().evaluateAs(context, Type.class).materialize(context).getDescriptor();
    }

    public @NotNull Expr getFieldOwner() {
        return getExpressions().get(OWNER_INDEX);
    }

    public @NotNull Expr getFieldName() {
        return getExpressions().get(NAME_INDEX);
    }

    public @NotNull Expr getFieldType() {
        return getExpressions().get(TYPE_INDEX);
    }

    @Override
    public @NotNull FieldSignatureExpr copy() {
        return copyParentAndSourceTo(new FieldSignatureExpr(getFieldOwner().copy(),
            getFieldName().copy(),
            getFieldType().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s.%s: %s", getFieldOwner(), getFieldName(), getFieldType());
    }
}
