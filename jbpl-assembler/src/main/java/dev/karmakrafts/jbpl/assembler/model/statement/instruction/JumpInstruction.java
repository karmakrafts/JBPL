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
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public final class JumpInstruction extends AbstractExprContainer implements Instruction {
    public static final int TARGET_INDEX = 0;

    public Opcode opcode;

    public JumpInstruction(final @NotNull Opcode opcode,
                           final @NotNull Expr target) {
        this.opcode = opcode;
        addExpression(target);
    }

    public void setTarget(final @NotNull Expr target) {
        getExpressions().set(TARGET_INDEX, target);
    }

    public @NotNull Expr getTarget() {
        return getExpressions().get(TARGET_INDEX);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        return opcode;
    }

    // TODO for Marlon:
    //  A jump instruction may be one of the following:
    //  GOTO and any IF-like instruction
    //  It's target expression can either evaluate to a LiteralExpr directly
    //  which contains an i32 value (for the stack index) or a string (the name of a label)
    //  It can also be a reference, so it needs to be evaluated
    //  -----
    //  ElementVisitor also needs to be adapted to include JumpInstructions when traversing the tree
    @Override
    public void evaluate(@NotNull AssemblerContext context) {

    }
}
