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

import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.util.XBiConsumer;
import dev.karmakrafts.jbpl.assembler.util.XFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;

public final class IntrinsicsHandler {
    private final EvaluationContext context;

    IntrinsicsHandler(final @NotNull EvaluationContext context) {
        this.context = context;
    }

    public @NotNull IntrinsicDefine addIntrinsicDefine(final @NotNull String name,
                                                       final @NotNull XFunction<EvaluationContext, Expr, EvaluationException> getter,
                                                       final @Nullable XBiConsumer<EvaluationContext, Expr, EvaluationException> setter) {
        final var define = new IntrinsicDefine(name, getter, setter);
        context.peekFrame().intrinsicDefines.put(name, define);
        return define;
    }

    public @NotNull IntrinsicDefine addIntrinsicDefine(final @NotNull String name,
                                                       final @NotNull XFunction<EvaluationContext, Expr, EvaluationException> getter) {
        return addIntrinsicDefine(name, getter, null);
    }

    public @NotNull IntrinsicDefine addIntrinsicDefine(final @NotNull String name, final @NotNull Expr value) {
        return addIntrinsicDefine(name, ctx -> value, null);
    }

    public @Nullable IntrinsicDefine getIntrinsicDefine(final @NotNull String name) {
        return context.peekFrame().intrinsicDefines.get(name);
    }

    public void addIntrinsicMacro(final @NotNull IntrinsicMacroSignature signature,
                                  final @NotNull XBiConsumer<EvaluationContext, IntrinsicMacroArguments, EvaluationException> callback) {
        context.peekFrame().intrinsicMacros.put(signature, new IntrinsicMacro(signature, callback));
    }

    public void initGlobal() {
        addIntrinsicMacro(new IntrinsicMacroSignature("info", BuiltinType.VOID, Map.of("message", BuiltinType.STRING)),
            (ctx, args) -> {
                final var message = args.arguments().get(0).evaluateAs(context, String.class);
                ctx.infoConsumer.accept(message);
            });
        addIntrinsicMacro(new IntrinsicMacroSignature("error", BuiltinType.VOID, Map.of("message", BuiltinType.STRING)),
            (ctx, args) -> {
                final var message = args.arguments().get(0).evaluateAs(context, String.class);
                ctx.errorConsumer.accept(message);
            });
    }

    public void initForField(final @NotNull FieldNode node) {
        // Field access modifiers
        addIntrinsicDefine("access",
            ctx -> ConstExpr.of(node.access),
            (ctx, value) -> node.access = value.evaluateAs(ctx, Integer.class));
    }

    public void initForFunction(final @NotNull MethodNode node) {
        // Function access modifiers
        addIntrinsicDefine("access",
            ctx -> ConstExpr.of(node.access),
            (ctx, value) -> node.access = value.evaluateAs(ctx, Integer.class));
        // Function instructions
        addIntrinsicDefine("instructions", ctx -> {
            final var instructions = InstructionCodec.decode(ctx.instructionBuffer, ctx).toArray(Instruction[]::new);
            return ConstExpr.of(instructions);
        }, (ctx, value) -> {
            final var instructions = InstructionCodec.encode(List.of(value.evaluateAs(ctx, Instruction[].class)), ctx);
            ctx.flushInstructionBuffer();
            ctx.emitAll(instructions); // Replace instruction buffer contents
        });
        // Function exceptions
        addIntrinsicDefine("exceptions", ctx -> {
            final var exceptions = node.exceptions.toArray(String[]::new);
            return ConstExpr.of(exceptions);
        }, (ctx, value) -> {
            final var exceptions = value.evaluateAs(ctx, String[].class);
            node.exceptions = List.of(exceptions);
        });
    }
}
