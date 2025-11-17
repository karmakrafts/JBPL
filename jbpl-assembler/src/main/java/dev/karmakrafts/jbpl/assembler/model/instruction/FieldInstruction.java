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

package dev.karmakrafts.jbpl.assembler.model.instruction;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

public final class FieldInstruction extends AbstractExprContainer implements Instruction {
    public static final int SIGNATURE_INDEX = 0;
    public final Opcode opcode;

    public FieldInstruction(final @NotNull Opcode opcode, final @NotNull Expr signature) {
        addExpression(signature);
        this.opcode = opcode;
    }

    public @NotNull Expr getSignature() {
        return getExpressions().get(SIGNATURE_INDEX);
    }

    public void setSignature(final @NotNull Expr signature) {
        getExpressions().set(SIGNATURE_INDEX, signature);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull EvaluationContext context) {
        return opcode;
    }

    @Override
    public @NotNull AbstractInsnNode emit(final @NotNull EvaluationContext context) throws EvaluationException {
        final var encodedOpcode = opcode.encodedValue;
        final var signature = getSignature().evaluateAs(context, FieldSignatureExpr.class);
        final var owner = signature.getFieldOwner().evaluateAs(context, ClassType.class);
        final var name = signature.getFieldName().evaluateAs(context, String.class);
        final var descriptor = signature.evaluateAsConstDescriptor(context);
        return new FieldInsnNode(encodedOpcode, owner.name(), name, descriptor);
    }

    @Override
    public void evaluate(@NotNull EvaluationContext context) throws EvaluationException {
        Instruction.super.evaluate(context);
    }

    @Override
    public @NotNull FieldInstruction copy() {
        return copyParentAndSourceTo(new FieldInstruction(opcode, getSignature().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s %s", opcode, getSignature());
    }
}
