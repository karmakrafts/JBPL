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
    public static final int VALUE_INDEX = 0;
    public String name;
    public Type type;

    public DefineStatement(final @NotNull String name, final @NotNull Type type, final @NotNull Expr value) {
        this.name = name;
        this.type = type;
        addExpression(value);
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getExpressions().set(VALUE_INDEX, value);
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) {
        return name;
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue();
        final var type = value.getType(context);
        if (!this.type.isAssignableFrom(type)) {
            final var message = String.format("Cannot assign value of type %s to define of type %s", type, this.type);
            final var diagnostic = SourceDiagnostic.from(this, value, message);
            throw new EvaluationException("Incompatible define value type", diagnostic, context.createStackTrace());
        }
        context.pushValue(value.evaluateAsConst(context));
    }

    @Override
    public @NotNull DefineStatement copy() {
        return copyParentAndSourceTo(new DefineStatement(name, type, getValue().copy()));
    }

    @Override
    public boolean isEvaluatedDirectly() {
        return false;
    }
}
