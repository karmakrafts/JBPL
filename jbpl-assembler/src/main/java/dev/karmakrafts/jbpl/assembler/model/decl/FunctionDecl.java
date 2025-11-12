package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import dev.karmakrafts.jbpl.assembler.model.statement.Statement;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;

import java.util.EnumSet;
import java.util.stream.Collectors;

public final class FunctionDecl extends AbstractStatementContainer implements Declaration, ScopeOwner {
    public final EnumSet<AccessModifier> accessModifiers = EnumSet.noneOf(AccessModifier.class);
    private Expr signature;

    public @NotNull Expr getSignature() {
        return signature;
    }

    public void setSignature(final @NotNull Expr signature) {
        if (this.signature != null) {
            this.signature.setParent(null);
        }
        signature.setParent(this);
        this.signature = signature;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var access = AccessModifier.combine(accessModifiers);
        final var signature = this.signature.evaluateAs(context, FunctionSignatureExpr.class);
        final var owner = signature.getFunctionOwner().evaluateAs(context, ClassType.class);
        final var name = signature.getFunctionName().evaluateAs(context, String.class);
        final var descriptor = signature.evaluateAsConstDescriptor(context);
        final var method = new MethodNode(context.bytecodeApi, access, name, descriptor, descriptor, null);
        super.evaluate(context); // Evaluate child statements
        method.instructions.add(context.getInstructionBuffer());
        context.addFunction(owner.name(), method);
    }

    @Override
    public @NotNull FunctionDecl copy() {
        final var function = copyParentAndSourceTo(new FunctionDecl());
        function.setSignature(getSignature().copy());
        function.accessModifiers.addAll(accessModifiers);
        function.addStatements(getStatements().stream().map(Statement::copy).toList());
        return copyParentAndSourceTo(function);
    }

    @Override
    public @NotNull String toString() {
        // @formatter:off
        final var mods = accessModifiers.stream()
            .map(AccessModifier::toString)
            .collect(Collectors.joining(" ", "", " "));
        // @formatter:on
        return String.format("%s fun %s", mods, getSignature());
    }
}
