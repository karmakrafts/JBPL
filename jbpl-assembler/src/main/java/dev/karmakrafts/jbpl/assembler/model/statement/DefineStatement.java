package dev.karmakrafts.jbpl.assembler.model.statement;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.AbstractExprContainer;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.model.expr.UnitExpr;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class DefineStatement extends AbstractExprContainer implements Statement {
    public String name;
    public Type type;

    public DefineStatement(final @NotNull String name, final @NotNull Type type, final @NotNull Expr value) {
        this.name = name;
        this.type = type;
        addExpression(value);
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Expr evaluate(final @NotNull AssemblerContext context) {
        return new UnitExpr();
    }
}
