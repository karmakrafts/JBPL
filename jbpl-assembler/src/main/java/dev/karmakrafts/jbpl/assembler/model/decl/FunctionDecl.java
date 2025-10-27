package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumSet;

public final class FunctionDecl extends AbstractStatementContainer implements Declaration, ScopeOwner {
    public final EnumSet<AccessModifier> accessModifiers = EnumSet.noneOf(AccessModifier.class);
    private FunctionSignatureExpr signature;

    public FunctionDecl(final @NotNull FunctionSignatureExpr signature) {
        signature.setParent(this);
        this.signature = signature;
    }

    public @NotNull FunctionSignatureExpr getSignature() {
        return signature;
    }

    public void setSignature(final @NotNull FunctionSignatureExpr signature) {
        if (this.signature != null) {
            this.signature.setParent(null);
        }
        signature.setParent(this);
        this.signature = signature;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var access = AccessModifier.combine(accessModifiers);
        final var signature = this.signature.evaluateAsConst(context, FunctionSignatureExpr.class);
        final var owner = signature.getFunctionOwner().evaluateAsConst(context, ClassType.class);
        final var name = signature.getFunctionName().evaluateAsConst(context, String.class);
        final var descriptor = signature.evaluateAsConstDescriptor(context);
        final var method = new MethodNode(context.bytecodeApi, access, name, descriptor, descriptor, null);
        super.evaluate(context); // Evaluate child statements
        method.instructions.add(context.getInstructionBuffer());
        context.addFunction(owner.name(), method);
    }

    @Override
    public @NotNull FunctionDecl copy() {
        final var function = copyParentAndSourceTo(new FunctionDecl(getSignature().copy()));
        function.accessModifiers.addAll(accessModifiers);
        function.addStatements(getStatements().stream().map(Statement::copy).toList());
        return copyParentAndSourceTo(function);
    }
}
