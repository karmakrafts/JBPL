package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodInsnNode;

public final class InvokeInstruction extends AbstractExprContainer implements Instruction {
    public static final int SIGNATURE_INDEX = 0;
    public Opcode opcode;

    public InvokeInstruction(final @NotNull Opcode opcode, final @NotNull Expr signature) {
        this.opcode = opcode;
        addExpression(signature);
    }

    public @NotNull Expr getSignature() {
        return getExpressions().get(SIGNATURE_INDEX);
    }

    public void setSignature(final @NotNull Expr signature) {
        getExpressions().set(SIGNATURE_INDEX, signature);
    }

    @Override
    public @NotNull Opcode getOpcode(final @NotNull AssemblerContext context) {
        return opcode;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        final var encodedOpcode = opcode.encodedValue;
        final var signature = getSignature().evaluateAsConst(context, FunctionSignatureExpr.class);
        final var owner = signature.getFunctionOwner().evaluateAsConst(context, ClassType.class);
        final var descriptor = signature.evaluateAsConstDescriptor(context);
        final var name = signature.getFunctionName().evaluateAsConst(context, String.class);
        context.emit(new MethodInsnNode(encodedOpcode, owner.name(), name, descriptor));
    }
}
