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
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElement;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.util.Copyable;
import dev.karmakrafts.jbpl.assembler.util.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class LiteralExpr extends AbstractElement implements ConstExpr {
    public Type type;
    public Object value;

    public LiteralExpr(final @NotNull Type type, final @NotNull Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public @NotNull Object getConstValue() {
        return value;
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return type.resolveIfNeeded(context);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) {
        context.pushValue(this); // Literals push themselves on the stack as ConstValue
    }

    @Override
    public boolean isUnit() {
        return value == Unit.INSTANCE;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (!(obj instanceof LiteralExpr literalExpr)) {
            return false;
        }
        return type.equals(literalExpr.type) && value.equals(literalExpr.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public @NotNull String toString() {
        return value.toString();
    }

    @Override
    public @NotNull LiteralExpr copy() {
        return copyParentAndSourceTo(new LiteralExpr(type, Copyable.copyIfPossible(value)));
    }
}
