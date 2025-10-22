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

import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl;
import dev.karmakrafts.jbpl.assembler.model.expr.FieldSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.expr.FunctionSignatureExpr;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.Instruction;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.Opcode;
import org.jetbrains.annotations.NotNull;

public final class TypeMapper {
    private TypeMapper() {
    }

    public static @NotNull Type map(final @NotNull Class<?> type, final boolean unbox) {
        // @formatter:off
        if ((unbox && type == Void.class) || type == Void.TYPE) return BuiltinType.VOID;
        else if (type.isArray()) return map(type.getComponentType(), unbox).array(1);
        else if (type.isPrimitive()) {
            if ((unbox && type == Byte.class) || type == Byte.TYPE) return BuiltinType.I8;
            else if ((unbox && type == Short.class) || type == Short.TYPE) return BuiltinType.I16;
            else if ((unbox && type == Integer.class) || type == Integer.TYPE) return BuiltinType.I32;
            else if ((unbox && type == Long.class) || type == Long.TYPE) return BuiltinType.I64;
            else if ((unbox && type == Float.class) || type == Float.TYPE) return BuiltinType.F32;
            else if ((unbox && type == Double.class) || type == Double.TYPE) return BuiltinType.F64;
            else if ((unbox && type == Boolean.class) || type == Boolean.TYPE) return BuiltinType.BOOL;
            else if ((unbox && type == Character.class) || type == Character.TYPE) return BuiltinType.CHAR;
        }
        else if (type == String.class) return BuiltinType.STRING;
        else if (Instruction.class.isAssignableFrom(type)) return PreproType.INSTRUCTION;
        else if (type == Opcode.class) return PreproType.OPCODE;
        else if (Type.class.isAssignableFrom(type)) return PreproType.TYPE;
        else if (type == FieldSignatureExpr.class) return PreproType.FIELD_SIGNATURE;
        else if (type == FunctionSignatureExpr.class) return PreproType.FUNCTION_SIGNATURE;
        else if (type == SelectorDecl.class) return PreproType.SELECTOR;
        // @formatter:on
        return new ClassType(type);
    }
}
