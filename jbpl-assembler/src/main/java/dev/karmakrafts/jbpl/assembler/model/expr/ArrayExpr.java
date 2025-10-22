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

import dev.karmakrafts.jbpl.assembler.AssemblerContext;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

public final class ArrayExpr extends AbstractExprContainer implements Expr {
    public static final int TYPE_INDEX = 0;
    private final BiFunction<AssemblerContext, Optional<? extends Type>, Type> elementTypeResolver;
    public final boolean hasInferredType;
    private boolean hasDynamicType = false; // Whether the type is an Expr or not

    private ArrayExpr(final @NotNull BiFunction<AssemblerContext, Optional<? extends Type>, Type> elementTypeResolver,
                      final boolean hasInferredType) {
        this.elementTypeResolver = elementTypeResolver;
        this.hasInferredType = hasInferredType;
    }

    public ArrayExpr() {
        this((ctx, commonType) -> commonType.orElseThrow(), true);
    }

    public ArrayExpr(final @NotNull Expr type) {
        this((ctx, commonType) -> type.evaluateAsConst(ctx, Type.class), false);
        hasDynamicType = true;
        addExpression(type);
    }

    public ArrayExpr(final @NotNull Type type) {
        this(LiteralExpr.of(type));
    }

    /**
     * The offset into the expression list until array values start
     */
    public int getValueOffset() {
        return hasDynamicType ? 1 : 0;
    }

    public boolean hasDynamicType() {
        return hasDynamicType;
    }

    public void setType(final @NotNull Expr type) {
        assert hasDynamicType;
        getExpressions().set(TYPE_INDEX, type);
    }

    public @NotNull Expr getType() {
        assert hasDynamicType;
        return getExpressions().get(TYPE_INDEX);
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) { // @formatter:off
        return elementTypeResolver.apply(context, TypeCommonizer.commonize(getExpressions().stream()
            .map(expr -> expr.getType(context))
            .toList()));
    } // @formatter:on

    @Override
    public void evaluate(final @NotNull AssemblerContext context) {
        super.evaluate(context);
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        return null; // TODO: ...
    }
}
