package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
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
        final var signature = getSignature().evaluateAs(context, FunctionSignatureExpr.class);
        final var owner = signature.evaluateFunctionOwner(context);
        final var descriptor = signature.evaluateAsDescriptor(context);
        final var name = signature.evaluateFunctionName(context);
        context.emit(new MethodInsnNode(encodedOpcode, owner.name(), name, descriptor));
    }
}
