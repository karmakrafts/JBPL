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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public final class LoadConstantInstruction extends AbstractExprContainer implements Instruction {
    public static final int VALUE_INDEX = 0;
    public Opcode opcode;

    public LoadConstantInstruction(final @NotNull Opcode opcode, final @NotNull Expr value) {
        this.opcode = opcode;
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull EvaluationContext context) {
        return opcode;
    }

    private @NotNull AbstractInsnNode createConstantInt(final int value) {
        if (value == -1) {
            return new InsnNode(Opcodes.ICONST_M1);
        }
        else if (value >= 0 && value <= 5) {
            return new InsnNode(Opcodes.ICONST_0 + value);
        }
        return new LdcInsnNode(value);
    }

    private @NotNull AbstractInsnNode createConstantLong(final long value) {
        if (value >= 0 && value <= 1) {
            return new InsnNode(Opcodes.LCONST_0 + (int) value);
        }
        return new LdcInsnNode(value);
    }

    private @NotNull AbstractInsnNode createConstantFloat(final float value) {
        if (value == 0F) {
            return new InsnNode(Opcodes.FCONST_0);
        }
        else if (value == 1F) {
            return new InsnNode(Opcodes.FCONST_1);
        }
        else if (value == 2) {
            return new InsnNode(Opcodes.FCONST_2);
        }
        return new LdcInsnNode(value);
    }

    private @NotNull AbstractInsnNode createConstantDouble(final double value) {
        if (value == 0.0) {
            return new InsnNode(Opcodes.DCONST_0);
        }
        else if (value == 1.0) {
            return new InsnNode(Opcodes.DCONST_1);
        }
        return new LdcInsnNode(value);
    }

    @Override
    public @NotNull AbstractInsnNode emit(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue().evaluateAs(context, Number.class);
        if (value instanceof Integer intValue) {
            return createConstantInt(intValue);
        }
        else if (value instanceof Long longValue) {
            return createConstantLong(longValue);
        }
        else if (value instanceof Float floatValue) {
            return createConstantFloat(floatValue);
        }
        else if (value instanceof Double doubleValue) {
            return createConstantDouble(doubleValue);
        }
        return switch (opcode) {
            case BIPUSH -> new IntInsnNode(Opcodes.BIPUSH, value.byteValue());
            case SIPUSH -> new IntInsnNode(Opcodes.SIPUSH, value.shortValue());
            default -> new LdcInsnNode(value);
        };
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        Instruction.super.evaluate(context);
    }

    @Override
    public @NotNull LoadConstantInstruction copy() {
        return copyParentAndSourceTo(new LoadConstantInstruction(opcode, getValue().copy()));
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s %s", opcode, getValue());
    }
}
