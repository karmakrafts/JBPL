package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public interface Type {
    @NotNull TypeCategory getCategory();

    @NotNull Expr createDefaultValue(final @NotNull AssemblerContext context);

    @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context);

    default @NotNull ArrayType array(final int dimensions) {
        return new ArrayType(this, dimensions);
    }
}
