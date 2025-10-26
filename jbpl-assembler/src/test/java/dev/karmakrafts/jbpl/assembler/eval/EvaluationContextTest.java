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

package dev.karmakrafts.jbpl.assembler.eval;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.decl.MacroDecl;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;

import java.util.List;

@TestInstance(Lifecycle.PER_METHOD)
public final class EvaluationContextTest {
    private @NotNull EvaluationContext createContext() {
        final var file = new AssemblyFile("test/test.jbpl");
        return new EvaluationContext(file, name -> new ClassNode(), System.out::println, System.err::println);
    }

    @Test
    public void pushAndPopSingleValue() {
        final var context = createContext();
        context.pushFrame(context.file);
        context.pushValue(LiteralExpr.of("Testing"));
        final var value = context.popValue();
        Assertions.assertEquals(LiteralExpr.of("Testing"), value);
    }

    @Test
    public void pushAndPopMultipleValues() {
        final var context = createContext();
        context.pushFrame(context.file);
        context.pushValues(List.of(LiteralExpr.of(1), LiteralExpr.of(2)));
        final var values = context.popValues(2);
        Assertions.assertEquals(List.of(LiteralExpr.of(1), LiteralExpr.of(2)), values);
    }

    @Test
    public void mergeInstructionBufferOnFrameExit() {
        final var dummyMacro = new MacroDecl(LiteralExpr.of("test"), LiteralExpr.of(BuiltinType.VOID));
        final var context = createContext();
        context.pushFrame(context.file);
        context.emit(new InsnNode(Opcodes.DUP));

        context.pushFrame(dummyMacro);
        context.emit(new InsnNode(Opcodes.POP));
        context.emit(new InsnNode(Opcodes.RETURN));
        context.popFrame(); // Instruction should be merged with parent frame buffer

        context.emit(new InsnNode(Opcodes.NOP));

        Assertions.assertEquals(Opcodes.DUP, context.getInstructionBuffer().get(0).getOpcode());
        Assertions.assertEquals(Opcodes.POP, context.getInstructionBuffer().get(1).getOpcode());
        Assertions.assertEquals(Opcodes.RETURN, context.getInstructionBuffer().get(2).getOpcode());
        Assertions.assertEquals(Opcodes.NOP, context.getInstructionBuffer().get(3).getOpcode());
    }

    @Test
    public void mergeValueStackOnFrameExit() {
        final var dummyMacro = new MacroDecl(LiteralExpr.of("test"), LiteralExpr.of(BuiltinType.VOID));
        final var context = createContext();
        context.pushFrame(context.file);
        context.pushValue(LiteralExpr.of(0));
        context.pushFrame(dummyMacro);
        context.pushValue(LiteralExpr.of(1));
        context.pushValue(LiteralExpr.of(2));
        context.popFrame();
        context.pushValue(LiteralExpr.of(3));

        Assertions.assertEquals(LiteralExpr.of(3), context.popValue());
        Assertions.assertEquals(LiteralExpr.of(2), context.popValue());
        Assertions.assertEquals(LiteralExpr.of(1), context.popValue());
        Assertions.assertEquals(LiteralExpr.of(0), context.popValue());
    }
}
