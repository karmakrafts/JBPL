package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
                                                            final @NotNull AssemblerContext context) {
        return org.objectweb.asm.Type.getMethodDescriptor(type.materialize(context));
    }

    private static int getInvokeTag(final @NotNull Opcode opcode) {
        return switch (opcode) {
            case INVOKEVIRTUAL -> Opcodes.H_INVOKEVIRTUAL;
            case INVOKESTATIC -> Opcodes.H_INVOKESTATIC;
            case INVOKESPECIAL -> Opcodes.H_INVOKESPECIAL;
            case INVOKEINTERFACE -> Opcodes.H_INVOKEINTERFACE;
            default -> throw new IllegalStateException(String.format("Unsupported invoke tag for opcode %s", opcode));
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
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        return Opcode.INVOKEDYNAMIC;
    }

    private @NotNull Handle evaluateInvokeHandle(final @NotNull Expr expr, final @NotNull AssemblerContext context) {
        final var instruction = expr.evaluateAsConst(context, Instruction.class);
        if (!(instruction instanceof InvokeInstruction invokeInstruction)) {
            throw new IllegalStateException(
                "Invoke handle requires INVOKESTATIC, INVOKEVIRTUAL, INVOKESPECIAL or INVOKEINTERFACE target");
        }
        final var opcode = invokeInstruction.getOpcode(context);
        final var tag = getInvokeTag(opcode);
        final var signature = invokeInstruction.getSignature().evaluateAs(context, FunctionSignatureExpr.class);
        final var owner = signature.evaluateFunctionOwner(context).materialize(context);
        final var name = signature.evaluateFunctionName(context);
        final var descriptor = signature.evaluateAsDescriptor(context);
        final var isInterface = opcode == Opcode.INVOKEINTERFACE;
        return new Handle(tag, owner.getInternalName(), name, descriptor, isInterface);
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        final var instantiatedSignature = getInstantiatedSignature().evaluateAs(context, FunctionSignatureExpr.class);
        final var samSignature = getSAMSignature().evaluateAs(context, FunctionSignatureExpr.class);
        final var name = instantiatedSignature.evaluateFunctionName(context);
        final var factoryDescriptor = computeFactoryDescriptor(instantiatedSignature.evaluateFunctionOwner(context),
            context);
        // @formatter:off
        final var samReturnType = samSignature.getFunctionReturnType()
            .evaluateAsConst(context, Type.class)
            .materialize(context);
        final var samParamTypes = samSignature.getFunctionParameters().stream()
            .map(type -> type.evaluateAsConst(context, Type.class).materialize(context))
            .toArray(org.objectweb.asm.Type[]::new);
        final var returnType = instantiatedSignature.getFunctionReturnType()
            .evaluateAsConst(context, Type.class)
            .materialize(context);
        final var paramTypes = instantiatedSignature.getFunctionParameters().stream()
            .map(type -> type.evaluateAsConst(context, Type.class).materialize(context))
            .toArray(org.objectweb.asm.Type[]::new);
        // @formatter:on
        final var bsmHandle = evaluateInvokeHandle(getBSMInstruction(), context);
        final var targetHandle = evaluateInvokeHandle(getTargetInstruction(), context);
        final var samType = org.objectweb.asm.Type.getMethodType(samReturnType, samParamTypes);
        final var instantiatedType = org.objectweb.asm.Type.getMethodType(returnType, paramTypes);
        // @formatter:off
        final var arguments = getArguments().stream()
            .map(expr -> expr.evaluateAsConst(context, Object.class))
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
}
