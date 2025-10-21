package dev.karmakrafts.jbpl.assembler.model.statement.instruction;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldInsnNode;

public final class FieldInstruction extends AbstractExprContainer implements Instruction {
    public static final int SIGNATURE_INDEX = 0;
    public final Opcode opcode;

    public FieldInstruction(final @NotNull Opcode opcode, final @NotNull Expr signature) {
        addExpression(signature);
        this.opcode = opcode;
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
        final var signature = getSignature().evaluateAs(context, FieldSignatureExpr.class);
        final var owner = signature.getFieldOwner().evaluateAsConst(context, ClassType.class);
        final var name = signature.getFieldName().evaluateAsConst(context, String.class);
        final var type = signature.getFieldType().evaluateAsConst(context, Type.class).materialize(context);
        context.emit(new FieldInsnNode(encodedOpcode, owner.name(), name, type.getDescriptor()));
    }
}
