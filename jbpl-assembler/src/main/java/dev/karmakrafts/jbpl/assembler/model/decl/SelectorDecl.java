package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.Order;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class SelectorDecl extends AbstractExprContainer implements Declaration, ScopeOwner, NamedElement {
    public static final int NAME_INDEX = 0;
    public static final int OFFSET_INDEX = 1;
    public final ArrayList<Condition> conditions = new ArrayList<>();

    public SelectorDecl(final @NotNull Expr name, final @NotNull Expr offset) {
        addExpression(name);
        addExpression(offset);
    }

    public @NotNull Expr getName() {
        return getExpressions().get(NAME_INDEX);
    }

    public void setName(final @NotNull Expr name) {
        getName().setParent(null);
        name.setParent(this);
        getExpressions().set(NAME_INDEX, name);
    }

    public @NotNull Expr getOffset() {
        return getExpressions().get(OFFSET_INDEX);
    }

    public void setOffset(final @NotNull Expr offset) {
        getOffset().setParent(null);
        offset.setParent(this);
        getExpressions().set(OFFSET_INDEX, offset);
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return getName().evaluateAsConst(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {
        // TODO: implement this
    }

    @Override
    public @NotNull SelectorDecl copy() {
        final var selector = copyParentAndSourceTo(new SelectorDecl(getName().copy(), getOffset().copy()));
        selector.conditions.addAll(conditions.stream().map(Condition::copy).toList());
        return selector;
    }

    public static final class Condition extends AbstractExprContainer {
        public static final int VALUE_INDEX = 0;

        public Order order;
        private TokenRange tokenRange = TokenRange.UNDEFINED;

        public Condition(final @NotNull Order order, final @NotNull Expr value) {
            this.order = order;
            addExpression(value);
        }

        public @NotNull Expr getValue() {
            return getExpressions().get(VALUE_INDEX);
        }

        public void setValue(final @NotNull Expr value) {
            getValue().setParent(null);
            value.setParent(this);
            getExpressions().set(VALUE_INDEX, value);
        }

        @Override
        public @NotNull TokenRange getTokenRange() {
            return tokenRange;
        }

        @Override
        public void setTokenRange(final @NotNull TokenRange tokenRange) {
            this.tokenRange = tokenRange;
        }

        public @NotNull Order getOrder() {
            return order;
        }

        @Override
        public @NotNull Condition copy() {
            return copySourcesTo(new Condition(order, getValue().copy()));
        }
    }
}
