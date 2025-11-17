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

import dev.karmakrafts.jbpl.assembler.model.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.util.XBiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public final class InstructionCodec {
    // Decoders have to create their result instances, so we can't delegate to Instruction instance function
    private static final HashMap<Class<? extends AbstractInsnNode>, XBiFunction<EvaluationContext, AbstractInsnNode, Instruction, EvaluationException>> DECODERS = new HashMap<>();

    private InstructionCodec() {
    }

    @SuppressWarnings("unchecked")
    public static <I extends AbstractInsnNode> void registerDecoder(final @NotNull Class<I> type,
                                                                    final @NotNull XBiFunction<EvaluationContext, I, Instruction, EvaluationException> decoder) {
        DECODERS.put(type,
            (XBiFunction<EvaluationContext, AbstractInsnNode, Instruction, EvaluationException>) decoder);
    }

    @SuppressWarnings("unchecked")
    public static <I extends AbstractInsnNode> @Nullable XBiFunction<EvaluationContext, I, Instruction, EvaluationException> getDecoder(
        final @NotNull Class<I> type) {
        return (XBiFunction<EvaluationContext, I, Instruction, EvaluationException>) DECODERS.get(type);
    }

    @SuppressWarnings("unchecked")
    public static @NotNull Optional<Instruction> decode(final @NotNull AbstractInsnNode instruction,
                                                        final @NotNull EvaluationContext context) throws EvaluationException {
        final var decoder = getDecoder((Class<AbstractInsnNode>) instruction.getClass());
        if (decoder == null) {
            return Optional.empty();
        }
        return Optional.of(decoder.apply(context, instruction));
    }

    public static @NotNull List<Instruction> decode(final @NotNull InsnList list,
                                                    final @NotNull EvaluationContext context) throws EvaluationException {
        final var instructions = new ArrayList<Instruction>();
        for (final var node : list) {
            instructions.add(decode(node, context).orElseThrow());
        }
        return instructions;
    }

    public static @NotNull AbstractInsnNode encode(final @NotNull Instruction instruction,
                                                   final @NotNull EvaluationContext context) throws EvaluationException {
        return instruction.emit(context);
    }

    public static @NotNull InsnList encode(final @NotNull List<Instruction> instructions,
                                           final @NotNull EvaluationContext context) throws EvaluationException {
        final var list = new InsnList();
        for (final var instruction : instructions) {
            list.add(encode(instruction, context));
        }
        return list;
    }
}
