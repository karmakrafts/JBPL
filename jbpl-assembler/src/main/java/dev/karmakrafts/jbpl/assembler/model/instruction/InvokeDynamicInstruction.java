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
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class InvokeDynamicInstruction extends AbstractExprContainer implements Instruction {
    public static final int INSTANTIATED_SIGNATURE_INDEX = 0;
    public static final int SAM_SIGNATURE_INDEX = 1;
    public static final int BSM_INSTRUCTION_INDEX = 2;
    public static final int TARGET_INSTRUCTION_INDEX = 3;
    public static final int ARGS_INDEX = 4;

    private int argumentIndex = 0;

    public InvokeDynamicInstruction(final @NotNull Expr instantiatedSignature,
                                    final @NotNull Expr samSignature,
                                    final @NotNull Expr bsmInstruction,
                                    final @NotNull Expr targetInstruction) {
        addExpression(instantiatedSignature);
        addExpression(samSignature);
        addExpression(bsmInstruction);
        addExpression(targetInstruction);
    }

    private static @NotNull String computeFactoryDescriptor(final @NotNull ClassType type,
                                                            final @NotNull EvaluationContext context) {
        return org.objectweb.asm.Type.getMethodDescriptor(type.materialize(context));
    }

    private int getInvokeTag(final @NotNull Opcode opcode,
                             final @NotNull EvaluationContext context) throws EvaluationException {
        return switch (opcode) {
            case INVOKEVIRTUAL -> Opcodes.H_INVOKEVIRTUAL;
            case INVOKESTATIC -> Opcodes.H_INVOKESTATIC;
            case INVOKESPECIAL -> Opcodes.H_INVOKESPECIAL;
            case INVOKEINTERFACE -> Opcodes.H_INVOKEINTERFACE;
            default -> throw new EvaluationException(String.format("Unsupported invoke tag for opcode %s", opcode),
                SourceDiagnostic.from(this),
                context.createStackTrace());
        };
    }

    public void addArguments(final @NotNull Collection<? extends Expr> arguments) {
        getExpressions().addAll(ARGS_INDEX + argumentIndex, arguments);
        argumentIndex += arguments.size();
    }

    public void addArgument(final @NotNull Expr argument) {
        getExpressions().add(ARGS_INDEX + argumentIndex++, argument);
    }

    public void setArgument(final int index, final @NotNull Expr argument) {
        getExpressions().set(ARGS_INDEX + index, argument);
    }

    public @NotNull Expr getArgument(final int index) {
        return getExpressions().get(ARGS_INDEX + index);
    }

    public @NotNull List<Expr> getArguments() {
        return getExpressions().subList(ARGS_INDEX, ARGS_INDEX + argumentIndex);
    }

    public @NotNull Expr getInstantiatedSignature() {
        return getExpressions().get(INSTANTIATED_SIGNATURE_INDEX);
    }

    public void setInstantiatedSignature(final @NotNull Expr signature) {
        getExpressions().set(INSTANTIATED_SIGNATURE_INDEX, signature);
    }

    public @NotNull Expr getBSMInstruction() {
        return getExpressions().get(BSM_INSTRUCTION_INDEX);
    }

    public void setBSMInstruction(final @NotNull Expr bsm) {
        getExpressions().set(BSM_INSTRUCTION_INDEX, bsm);
    }

    public @NotNull Expr getSAMSignature() {
        return getExpressions().get(SAM_SIGNATURE_INDEX);
    }

    public void setSAMSignature(final @NotNull Expr target) {
        getExpressions().set(SAM_SIGNATURE_INDEX, target);
    }

    public @NotNull Expr getTargetInstruction() {
        return getExpressions().get(TARGET_INSTRUCTION_INDEX);
    }

    public void setTargetInstruction(final @NotNull Expr samTarget) {
        getExpressions().set(TARGET_INSTRUCTION_INDEX, samTarget);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull EvaluationContext context) {
        return Opcode.INVOKEDYNAMIC;
    }

    private @NotNull Handle evaluateInvokeHandle(final @NotNull Expr expr,
                                                 final @NotNull EvaluationContext context) throws EvaluationException {
        final var instruction = expr.evaluateAsConst(context, Instruction.class);
        if (!(instruction instanceof InvokeInstruction invokeInstruction)) {
            throw new EvaluationException(
                "Invoke handle requires INVOKESTATIC, INVOKEVIRTUAL, INVOKESPECIAL or INVOKEINTERFACE target",
                SourceDiagnostic.from(this),
                context.createStackTrace());
        }
        final var opcode = invokeInstruction.getOpcode(context);
        final var tag = getInvokeTag(opcode, context);
        final var signature = invokeInstruction.getSignature().evaluateAsConst(context, FunctionSignatureExpr.class);
        final var owner = signature.getFunctionOwner().evaluateAsConst(context, ClassType.class).materialize(context);
        final var name = signature.getFunctionName().evaluateAsConst(context, String.class);
        final var isInterface = opcode == Opcode.INVOKEINTERFACE;
        final var descriptor = signature.evaluateAsConstDescriptor(context);
        return new Handle(tag, owner.getInternalName(), name, descriptor, isInterface);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var instantiatedSignature = getInstantiatedSignature().evaluateAsConst(context,
            FunctionSignatureExpr.class);
        final var samSignature = getSAMSignature().evaluateAsConst(context, FunctionSignatureExpr.class);
        final var name = instantiatedSignature.getFunctionName().evaluateAsConst(context, String.class);
        final var owner = instantiatedSignature.getFunctionOwner().evaluateAsConst(context, ClassType.class);
        final var factoryDescriptor = computeFactoryDescriptor(owner, context);
        // @formatter:off
        final var samReturnType = samSignature.getFunctionReturnType()
            .evaluateAsConst(context, Type.class)
            .materialize(context);
        final var samParamTypes = samSignature.getFunctionParameters().stream()
            .map(ExceptionUtils.unsafeFunction(type -> type.evaluateAsConst(context, Type.class).materialize(context)))
            .toArray(org.objectweb.asm.Type[]::new);
        final var returnType = instantiatedSignature.getFunctionReturnType()
            .evaluateAsConst(context, Type.class)
            .materialize(context);
        final var paramTypes = instantiatedSignature.getFunctionParameters().stream()
            .map(ExceptionUtils.unsafeFunction(type -> type.evaluateAsConst(context, Type.class).materialize(context)))
            .toArray(org.objectweb.asm.Type[]::new);
        // @formatter:on
        final var bsmHandle = evaluateInvokeHandle(getBSMInstruction(), context);
        final var targetHandle = evaluateInvokeHandle(getTargetInstruction(), context);
        final var samType = org.objectweb.asm.Type.getMethodType(samReturnType, samParamTypes);
        final var instantiatedType = org.objectweb.asm.Type.getMethodType(returnType, paramTypes);
        // @formatter:off
        final var arguments = getArguments().stream()
            .map(ExceptionUtils.unsafeFunction(expr -> expr.evaluateAsConst(context, Object.class)))
            .toList();
        // @formatter:on
        // Compose BSM arguments
        final var joinedArguments = new ArrayList<>();
        joinedArguments.add(samType);
        joinedArguments.add(targetHandle);
        joinedArguments.add(instantiatedType);
        joinedArguments.addAll(arguments);
        context.emit(new InvokeDynamicInsnNode(name,
            factoryDescriptor,
            bsmHandle,
            joinedArguments.toArray(Object[]::new)));
    }

    @Override
    public @NotNull InvokeDynamicInstruction copy() {
        return copyParentAndSourceTo(new InvokeDynamicInstruction(getInstantiatedSignature().copy(),
            getSAMSignature().copy(),
            getBSMInstruction().copy(),
            getTargetInstruction().copy()));
    }

    @Override
    public String toString() {
        final var signature = getInstantiatedSignature();
        final var samSignature = getSAMSignature();
        final var arguments = getArguments();
        final var builder = new StringBuilder("invokedynamic ");
        builder.append(signature);
        if (signature != samSignature) { // If we have a distinct SAM signature, render it
            builder.append(" by ");
            builder.append(samSignature);
        }
        builder.append(" {\n");
        builder.append(getBSMInstruction()).append(",\n");
        builder.append(getTargetInstruction());
        if (!arguments.isEmpty()) {
            builder.append(",\n(");
            builder.append(arguments.stream().map(Expr::toString).collect(Collectors.joining(", ")));
            builder.append(")\n");
        }
        builder.append('}');
        return builder.toString();
    }
}
