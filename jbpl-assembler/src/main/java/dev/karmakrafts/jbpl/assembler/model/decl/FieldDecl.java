package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.AccessModifier;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.ClassType;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.FieldNode;

import java.util.EnumSet;
import java.util.stream.Collectors;

public final class FieldDecl extends AbstractExprContainer implements Declaration {
    public static final int SIGNATURE_INDEX = 0;
    public static final int INITIALIZER_INDEX = 1;

    public final EnumSet<AccessModifier> accessModifiers = EnumSet.noneOf(AccessModifier.class);

    public FieldDecl() {
        addExpression(LiteralExpr.UNIT); // Signature
        addExpression(LiteralExpr.UNIT); // Initializer
    }

    public @NotNull Expr getInitializer() {
        return getExpressions().get(INITIALIZER_INDEX);
    }

    public void setInitializer(final @NotNull Expr initializer) {
        getInitializer().setParent(null);
        initializer.setParent(this);
        getExpressions().set(INITIALIZER_INDEX, initializer);
    }

    public @NotNull Expr getSignature() {
        return getExpressions().get(SIGNATURE_INDEX);
    }

    public void setSignature(final @NotNull Expr signature) {
        getSignature().setParent(null);
        signature.setParent(this);
        getExpressions().set(SIGNATURE_INDEX, signature);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var signature = getSignature().evaluateAsConst(context, FieldSignatureExpr.class);
        final var owner = signature.getFieldOwner().evaluateAsConst(context, ClassType.class);
        final var modifier = AccessModifier.combine(accessModifiers);
        final var name = signature.getFieldName().evaluateAsConst(context, String.class);
        final var descriptor = signature.evaluateAsConstDescriptor(context);
        final var initializer = getInitializer();
        final var initializerType = initializer.getType(context);
        final var fieldType = signature.getFieldType().evaluateAsConst(context, Type.class);
        // @formatter:off
        // Handle cases where the initializer never gets updated from unit-expr by creating default value from field type
        final var initialValue = initializerType == BuiltinType.VOID
            ? fieldType.createDefaultValue(context).evaluateAsConstAndMaterialize(context)
            : initializer.evaluateAsConstAndMaterialize(context);
        // @formatter:on
        final var node = new FieldNode(context.bytecodeApi, modifier, name, descriptor, descriptor, initialValue);
        context.addField(owner.name(), node);
    }

    @Override
    public @NotNull FieldDecl copy() {
        final var field = copyParentAndSourceTo(new FieldDecl());
        field.setInitializer(getInitializer().copy());
        field.setSignature(getSignature().copy());
        field.accessModifiers.addAll(accessModifiers);
        field.addExpressions(getExpressions().stream().map(Expr::copy).toList());
        return copyParentAndSourceTo(field);
    }

    @Override
    public @NotNull String toString() {
        // @formatter:off
        final var mods = accessModifiers.stream()
            .map(AccessModifier::toString)
            .collect(Collectors.joining(" ", "", " "));
        // @formatter:on
        return String.format("%sfield %s = %s", mods, getSignature(), getInitializer());
    }
}
