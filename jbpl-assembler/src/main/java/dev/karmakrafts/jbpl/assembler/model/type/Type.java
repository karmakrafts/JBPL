package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public sealed interface Type permits ArrayType, BuiltinType, ClassType, IntersectionType, PreproClassType, PreproType {
    /**
     * Attempt to parse a type from the given string value.
     * This will assume the input values are in one of the following formats:
     * <ul>
     *     <li>A builtin type by its keyword like <b>i8</b>, <b>f32</b>, <b>string</b> etc.</li>
     *     <li>A preprocessor type by its name (or keyword) like <b>type</b>, <b>opcode</b> etc.</li>
     *     <li>A JVM class type in the form of <b>&lt;fully/qualified/Name&gt;</b></li>
     *     <li>An intersection type in the form of <b>(OneType | AnotherType)</b></li>
     *     <li>An array type in the form of <b>[type]</b>, <b>[[type]]</b> etc.</li>
     * </ul>
     */
    static @NotNull Optional<Type> tryParse(final @Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }
        // @formatter:off
        return BuiltinType.findByName(value)
            .map(Type.class::cast)
            .or(() -> PreproType.findByName(value))
            .or(() -> ArrayType.tryParse(value))
            .or(() -> ClassType.tryParse(value))
            .or(() -> IntersectionType.tryParse(value));
        // @formatter:on
    }

    @NotNull TypeCategory getCategory();

    @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) throws EvaluationException;

    @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) throws EvaluationException;

    default boolean isAssignableFrom(final @NotNull Type other) {
        return equals(other);
    }

    default @NotNull ArrayType array() {
        return new ArrayType(this);
    }
}
