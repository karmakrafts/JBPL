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

import dev.karmakrafts.jbpl.assembler.model.expr.PreproClassExpr;
import org.jetbrains.annotations.NotNull;

public final class TypeMapper {
    private TypeMapper() {
    }

    public static @NotNull Class<?> map(final @NotNull Type type, final boolean box) {
        if (type instanceof IntersectionType) {
            throw new IllegalStateException("Intersection types cannot be mapped to the runtime");
        }
        if (type instanceof ArrayType arrayType) {
            return map(arrayType.elementType(), box).arrayType();
        }
        else if (type instanceof BuiltinType builtinType) {
            if (box) {
                return builtinType.boxedType;
            }
            return builtinType.type;
        }
        else if (type instanceof PreproType preproType) {
            return preproType.type;
        }
        else if (type instanceof ClassType classType) {
            try {
                return classType.loadClass();
            }
            catch (final ClassNotFoundException error) {
                throw new IllegalStateException(String.format("Could not load runtime class '%s'", classType.name()),
                    error);
            }
        }
        throw new IllegalStateException(String.format("Could not map type '%s' to runtime", type));
    }

    public static @NotNull Type map(final @NotNull Class<?> type, final boolean unbox) {
        if (type == PreproClassExpr.class) {
            throw new IllegalStateException("Preprocessor class instances cannot be mapped to a runtime type");
        }
        if (type.isArray()) {
            return map(type.componentType(), unbox).array();
        }
        Type mappedType = BuiltinType.findByType(type).orElse(null);
        if (mappedType == null && unbox) {
            mappedType = BuiltinType.findByBoxedType(type).orElse(null);
        }
        if (mappedType == null) {
            mappedType = PreproType.findByType(type).orElse(null);
        }
        return mappedType != null ? mappedType : new ClassType(type);
    }
}
