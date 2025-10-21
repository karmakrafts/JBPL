package dev.karmakrafts.jbpl.assembler.model.decl;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.Order;
import dev.karmakrafts.jbpl.assembler.model.ReturnTarget;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.SourceOwner;
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.LiteralExpr;
import dev.karmakrafts.jbpl.assembler.model.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.Opcode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class SelectorDecl extends AbstractElement implements Declaration, ScopeOwner, ReturnTarget {
    public final ArrayList<Condition> conditions = new ArrayList<>();
    public String name;
    public Expr offset = LiteralExpr.of(0);

    public SelectorDecl(final @NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public void evaluate(@NotNull AssemblerContext context) {

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
