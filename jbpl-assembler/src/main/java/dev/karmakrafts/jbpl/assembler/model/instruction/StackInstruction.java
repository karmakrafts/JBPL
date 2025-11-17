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
import dev.karmakrafts.jbpl.assembler.eval.InstructionCodec;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class StackInstruction extends AbstractExprContainer implements Instruction {
    public static final int SLOT_INDEX = 0;
    public Opcode opcode;

    static {
        InstructionCodec.registerDecoder(VarInsnNode.class, (ctx, node) -> {
            final var opcode = Opcode.findByEncodedValue(node.getOpcode()).orElseThrow();
            final var index = node.var;
            return new StackInstruction(opcode, ConstExpr.of(index));
        });
    }

    /**
     * @param opcode The wanted opcode
     * @param slot   Either a constant lineIndex or the name of a previously defined local.
     */
    public StackInstruction(final @NotNull Opcode opcode, final @NotNull Expr slot) {
        this.opcode = opcode;
        addExpression(slot);
    }

    public @NotNull Expr getSlot() {
        return getExpressions().get(SLOT_INDEX);
    }

    public void setSlot(final @NotNull Expr slot) {
        getExpressions().set(SLOT_INDEX, slot);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull EvaluationContext context) {
        return opcode;
    }

    @Override
    public @NotNull AbstractInsnNode emit(final @NotNull EvaluationContext context) throws EvaluationException {
        final var encodedOpcode = getOpcode(context).encodedValue;
        final var slotIdObject = getSlot().evaluateAs(context, Object.class);
        if (slotIdObject instanceof Integer slotId) {
            return new VarInsnNode(encodedOpcode, slotId);
        }
        // Otherwise slotIdObject is a String and we need to resolve the local by name, fail if we can't find it
        final var slotId = context.peekFrame().getOrAssignLocalIndex((String) slotIdObject, context);
        return new VarInsnNode(encodedOpcode, slotId);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        Instruction.super.evaluate(context);
    }

    @Override
    public @NotNull StackInstruction copy() {
        return copyParentAndSourceTo(new StackInstruction(opcode, getSlot().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s %s", opcode, getSlot());
    }
}
