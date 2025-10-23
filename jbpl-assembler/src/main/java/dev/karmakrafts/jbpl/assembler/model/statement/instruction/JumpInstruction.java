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

package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.JumpInsnNode;

public final class JumpInstruction extends AbstractExprContainer implements Instruction {
    public static final int TARGET_INDEX = 0;

    public Opcode opcode;

    public JumpInstruction(final @NotNull Opcode opcode, final @NotNull Expr target) {
        this.opcode = opcode;
        addExpression(target);
    }

    public @NotNull Expr getTarget() {
        return getExpressions().get(TARGET_INDEX);
    }

    public void setTarget(final @NotNull Expr target) {
        getExpressions().set(TARGET_INDEX, target);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        return opcode;
    }

    @Override
    public void evaluate(@NotNull AssemblerContext context) throws EvaluationException {
        final var encodedOpcode = opcode.encodedValue;
        final var target = getTarget();
        final var label = context.getOrCreateLabelNode(target.evaluateAsConst(context, String.class));
        context.emit(new JumpInsnNode(encodedOpcode, label));
    }
}
