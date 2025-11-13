/*
 * Copyright 2025 Karma Krafts & associates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.model.expr.ConstExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum BuiltinType implements Type {
    // @formatter:off
    VOID    (Void.TYPE,      Void.class,      TypeCategory.VOID,    org.objectweb.asm.Type.VOID_TYPE),
    I8      (Byte.TYPE,      Byte.class,      TypeCategory.INTEGER, org.objectweb.asm.Type.BYTE_TYPE),
    I16     (Short.TYPE,     Short.class,     TypeCategory.INTEGER, org.objectweb.asm.Type.SHORT_TYPE),
    I32     (Integer.TYPE,   Integer.class,   TypeCategory.INTEGER, org.objectweb.asm.Type.INT_TYPE),
    I64     (Long.TYPE,      Long.class,      TypeCategory.INTEGER, org.objectweb.asm.Type.LONG_TYPE),
    F32     (Float.TYPE,     Float.class,     TypeCategory.FLOAT,   org.objectweb.asm.Type.FLOAT_TYPE),
    F64     (Double.TYPE,    Double.class,    TypeCategory.FLOAT,   org.objectweb.asm.Type.DOUBLE_TYPE),
    CHAR    (Character.TYPE, Character.class, TypeCategory.CHAR,    org.objectweb.asm.Type.CHAR_TYPE),
    BOOL    (Boolean.TYPE,   Boolean.class,   TypeCategory.BOOL,    org.objectweb.asm.Type.BOOLEAN_TYPE),
    OBJECT  (Object.class,                    TypeCategory.OBJECT,  org.objectweb.asm.Type.getObjectType("java/lang/Object")),
    STRING  (String.class,                    TypeCategory.STRING,  org.objectweb.asm.Type.getObjectType("java/lang/String"));
    // @formatter:on

    public final Class<?> type;
    public final Class<?> boxedType;
    private final TypeCategory category;
    private final org.objectweb.asm.Type materialType;

    BuiltinType(final @NotNull Class<?> type,
                final @NotNull Class<?> boxedType,
                final @NotNull TypeCategory category,
                final @NotNull org.objectweb.asm.Type materialType) {
        this.type = type;
        this.boxedType = boxedType;
        this.category = category;
        this.materialType = materialType;
    }

    BuiltinType(final @NotNull Class<?> type,
                final @NotNull TypeCategory category,
                final @NotNull org.objectweb.asm.Type materialType) {
        this(type, type, category, materialType);
    }

    public static @NotNull Optional<BuiltinType> findByType(final @NotNull Class<?> type) {
        return Arrays.stream(values()).filter(t -> t.type == type).findFirst();
    }

    public static @NotNull Optional<BuiltinType> findByBoxedType(final @NotNull Class<?> boxedType) {
        return Arrays.stream(values()).filter(t -> t.boxedType == boxedType).findFirst();
    }

    public static @NotNull Optional<BuiltinType> findByName(final @NotNull String name) {
        return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public @NotNull TypeCategory getCategory() {
        return category;
    }

    @Override
    public @NotNull Expr createDefaultValue(final @NotNull EvaluationContext context) {
        return switch (this) {
            case VOID -> ConstExpr.unit();
            case I8 -> ConstExpr.of((byte) 0);
            case I16 -> ConstExpr.of((short) 0);
            case I32 -> ConstExpr.of(0);
            case I64 -> ConstExpr.of(0L);
            case F32 -> ConstExpr.of(0F);
            case F64 -> ConstExpr.of(0.0);
            case CHAR -> ConstExpr.of(' ');
            case BOOL -> ConstExpr.of(false);
            case STRING -> ConstExpr.of("");
            default -> throw new IllegalStateException(String.format("%s has no default value!", this));
        };
    }

    @Override
    public @NotNull org.objectweb.asm.Type materialize(final @NotNull EvaluationContext context) {
        return materialType;
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean isAssignableFrom(final @NotNull Type other) {
        return switch (category) {
            // We need to take into account implicit widening conversions here
            case INTEGER, FLOAT -> switch (other.getCategory()) {
                // If the incoming type is smaller than the type we assign to, we allow assignment
                case INTEGER, FLOAT -> ((BuiltinType) other).ordinal() <= ordinal();
                default -> Type.super.isAssignableFrom(other);
            };
            default -> Type.super.isAssignableFrom(other);
        };
    }
}
