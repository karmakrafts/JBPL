package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

public sealed interface Type permits ArrayType, BuiltinType, ClassType, IntersectionType, PreproClassType, PreproType {
    @NotNull TypeCategory getCategory();

    @NotNull Expr createDefaultValue(final @NotNull AssemblerContext context);

    @NotNull org.objectweb.asm.Type materialize(final @NotNull AssemblerContext context);

    default @NotNull ArrayType array() {
        return new ArrayType(this);
    }
}
