package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.type.PreproClassTypeRef;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import org.jetbrains.annotations.NotNull;

public final class ClassInstantiationExpr extends AbstractCallExpr implements Expr {
    public final PreproClassTypeRef type;

    public ClassInstantiationExpr(final @NotNull PreproClassTypeRef type) {
        super(new UnitExpr()); // Class instantiations don't have a receiver
        this.type = type;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) {
        return type;
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        // Calls evaluate to themselves since they are only argument storage
    }
}
