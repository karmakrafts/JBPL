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
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.InsnNode;

public final class OplessInstruction extends AbstractElement implements Instruction {
    public Opcode opcode;

    public OplessInstruction(final @NotNull Opcode opcode) {
        this.opcode = opcode;
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull EvaluationContext context) {
        return opcode;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {
        context.emit(new InsnNode(opcode.encodedValue));
    }

    @Override
    public @NotNull OplessInstruction copy() {
        return copyParentAndSourceTo(new OplessInstruction(opcode));
    }

    @Override
    public @NotNull String toString() {
        return opcode.toString();
    }
}
