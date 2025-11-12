package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import org.jetbrains.annotations.NotNull;

public final class DefineStatement extends AbstractExprContainer implements Statement, NamedElement {
    public static final int NAME_INDEX = 0;
    public static final int TYPE_INDEX = 1;
    public static final int VALUE_INDEX = 2;

    public DefineStatement(final @NotNull Expr name, final @NotNull Expr type, final @NotNull Expr value) {
        addExpression(name);
        addExpression(type);
        addExpression(value);
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        name.setParent(this);
        getExpressions().set(NAME_INDEX, name);
    }

    public @NotNull Expr getType() {
        return getExpressions().get(TYPE_INDEX);
    }

    public void setType(final @NotNull Expr type) {
        type.setParent(this);
        getExpressions().set(TYPE_INDEX, type);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        value.setParent(this);
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return getName().evaluateAsConst(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue();
        final var type = getType().evaluateAsConst(context, Type.class);
        final var otherType = value.getType(context);
        if (!type.isAssignableFrom(otherType)) {
            final var message = String.format("Cannot assign value of type %s to define of type %s", otherType, type);
            final var diagnostic = SourceDiagnostic.from(this, value, message);
            throw new EvaluationException("Incompatible define value type", diagnostic, context.createStackTrace());
        }
        context.pushValue(value.evaluateAsConst(context));
    }

    @Override
    public @NotNull DefineStatement copy() {
        return copyParentAndSourceTo(new DefineStatement(getName().copy(), getType().copy(), getValue().copy()));
    }

    @Override
    public boolean isEvaluatedDirectly() {
        return false;
    }

    @Override
    public @NotNull String toString() {
        return String.format("define %s: %s = %s", getName(), getType(), getValue());
    }
}
