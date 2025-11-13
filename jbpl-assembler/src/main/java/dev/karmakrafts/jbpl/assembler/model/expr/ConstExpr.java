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

package dev.karmakrafts.jbpl.assembler.model.expr;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.type.BuiltinType;
import dev.karmakrafts.jbpl.assembler.model.type.TypeMapper;
import dev.karmakrafts.jbpl.assembler.source.TokenRange;
import dev.karmakrafts.jbpl.assembler.util.Unit;
import org.jetbrains.annotations.NotNull;

public interface ConstExpr extends Expr {
    static @NotNull ConstExpr unit() {
        final var expr = new LiteralExpr(BuiltinType.VOID, Unit.INSTANCE);
        expr.setTokenRange(TokenRange.SYNTHETIC);
        return expr;
    }

    static @NotNull ConstExpr unit(final @NotNull TokenRange tokenRange) {
        final var expr = new LiteralExpr(BuiltinType.VOID, Unit.INSTANCE);
        expr.setTokenRange(tokenRange);
        return expr;
    }

    static @NotNull ConstExpr of(final @NotNull Object value) {
        if (value.getClass().isArray()) {
            return ArrayExpr.fromArrayRef(value);
        }
        return of(value, TokenRange.SYNTHETIC);
    }

    static @NotNull ConstExpr of(final @NotNull Object value, final @NotNull TokenRange tokenRange) {
        if (value.getClass().isArray()) {
            return ArrayExpr.fromArrayRef(value);
        }
        final var type = TypeMapper.map(value.getClass(), true);
        final var expr = new LiteralExpr(type, value);
        expr.setTokenRange(tokenRange);
        return expr;
    }

    default void ensureLazyConstValue(final @NotNull EvaluationContext context) throws EvaluationException {
    }

    @NotNull Object getConstValue();
}
