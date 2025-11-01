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
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.VarInsnNode;

public final class StackInstruction extends AbstractExprContainer implements Instruction {
    public static final int SLOT_INDEX = 0;
    public Opcode opcode;

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
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var encodedOpcode = getOpcode(context).encodedValue;
        final var slotIdObject = getSlot().evaluateAsConst(context, Object.class);
        if (slotIdObject instanceof Integer slotId) {
            context.emit(new VarInsnNode(encodedOpcode, slotId));
        }
        // If we fall through the above case, we need to resolve the local using pre-defined locals in the sack frame
        // TODO: context.emit(new VarInsnNode(encodedOpcode, lineIndex));
    }

    @Override
    public @NotNull StackInstruction copy() {
        return copyParentAndSourceTo(new StackInstruction(opcode, getSlot().copy()));
    }
}
