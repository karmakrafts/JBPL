package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.element.NamedElement;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.instruction.Opcode;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.source.SourceOwner;
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
        getExpressions().set(NAME_INDEX, name);
    }

    public @NotNull Expr getOffset() {
        return getExpressions().get(OFFSET_INDEX);
    }

    public void setOffset(final @NotNull Expr offset) {
        getExpressions().set(OFFSET_INDEX, offset);
    }

    @Override
    public @NotNull String getName(final @NotNull EvaluationContext context) throws EvaluationException {
        return getName().evaluateAsConst(context, String.class);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {
    }

    @Override
    public @NotNull SelectorDecl copy() {
        return copyParentAndSourceTo(new SelectorDecl(getName().copy(), getOffset().copy()));
    }

    public sealed interface Condition extends SourceOwner {
        @NotNull Order order();
    }

    public static final class OpcodeCondition implements Condition {
        public Order order;
        public Opcode opcode;
        public TokenRange tokenRange = TokenRange.UNDEFINED;

        public OpcodeCondition(final @NotNull Order order, final @NotNull Opcode opcode) {
            this.order = order;
            this.opcode = opcode;
        }

        @Override
        public @NotNull TokenRange getTokenRange() {
            return tokenRange;
        }

        @Override
        public void setTokenRange(final @NotNull TokenRange tokenRange) {
            this.tokenRange = tokenRange;
        }

        @Override
        public @NotNull Order order() {
            return order;
        }
    }

    public static final class InstructionCondition implements Condition {
        public Order order;
        public Instruction instruction;
        public TokenRange tokenRange = TokenRange.UNDEFINED;

        public InstructionCondition(final @NotNull Order order, final @NotNull Instruction instruction) {
            this.order = order;
            this.instruction = instruction;
        }

        @Override
        public @NotNull TokenRange getTokenRange() {
            return tokenRange;
        }

        @Override
        public void setTokenRange(final @NotNull TokenRange tokenRange) {
            this.tokenRange = tokenRange;
        }

        @Override
        public @NotNull Order order() {
            return order;
        }
    }
}
