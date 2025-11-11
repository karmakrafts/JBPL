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
import dev.karmakrafts.jbpl.assembler.model.type.*;
import dev.karmakrafts.jbpl.assembler.source.SourceDiagnostic;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.stream.Collectors;

public final class InExpr extends AbstractExprContainer implements Expr {
    public static final int LHS_INDEX = 0;
    public static final int RHS_INDEX = 1;

    public InExpr(final @NotNull Expr lhs, final @NotNull Expr rhs) {
        addExpression(lhs);
        addExpression(rhs);
    }

    public void setLhs(final @NotNull Expr lhs) {
        getLhs().setParent(null);
        lhs.setParent(this);
        getExpressions().set(LHS_INDEX, lhs);
    }

    public @NotNull Expr getLhs() {
        return getExpressions().get(LHS_INDEX);
    }

    public void setRhs(final @NotNull Expr rhs) {
        getRhs().setParent(null);
        rhs.setParent(this);
        getExpressions().set(RHS_INDEX, rhs);
    }

    public @NotNull Expr getRhs() {
        return getExpressions().get(RHS_INDEX);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var lhsValue = getLhs().evaluateAsConst(context, Object.class);
        final var lhsType = getLhs().getType(context);
        final var rhsValue = getRhs().evaluateAsConst(context, Object.class);
        final var rhsType = getRhs().getType(context);
        if (rhsType == BuiltinType.STRING) {
            context.pushValue(LiteralExpr.of(rhsValue.toString().contains(lhsValue.toString())));
            return;
        }
        else if (rhsType == PreproType.FUNCTION_SIGNATURE) {
            final var signature = (FunctionSignatureExpr) rhsValue;
            if (lhsType == BuiltinType.STRING) {
                context.pushValue(LiteralExpr.of(signature.getFunctionName().evaluateAsConst(context,
                    String.class).equals(lhsValue)));
                return;
            }
            else if (lhsType == PreproType.TYPE) {
                // @formatter:off
                final var types = signature.getExpressions().stream()
                    .map(ExceptionUtils.unsafeFunction(expr -> expr.getType(context)))
                    .collect(Collectors.toSet());
                // @formatter:on
                context.pushValue(LiteralExpr.of(types.contains((Type) lhsValue)));
                return;
            }
        }
        else if (rhsType == PreproType.FIELD_SIGNATURE) {
            final var signature = (FieldSignatureExpr) rhsValue;
            if (lhsType == BuiltinType.STRING) {
                // @formatter:off
                final var name = signature.getExpressions().stream()
                    .filter(ExceptionUtils.unsafePredicate(expr -> expr.getType(context) == BuiltinType.STRING))
                    .findFirst();
                // @formatter:on
                context.pushValue(LiteralExpr.of(name.isPresent() && name.get().evaluateAsConst(context,
                    String.class).equals(lhsValue)));
                return;
            }
            else if (lhsType == PreproType.TYPE) {
                // @formatter:off
                final var types = signature.getExpressions().stream()
                    .map(ExceptionUtils.unsafeFunction(expr -> expr.getType(context)))
                    .collect(Collectors.toSet());
                // @formatter:on
                context.pushValue(LiteralExpr.of(types.contains((Type) lhsValue)));
            }
        }
        else if (rhsValue instanceof IntersectionType rhsIntersectionType) {
            if (lhsValue instanceof IntersectionType lhsIntersectionType) {
                context.pushValue(LiteralExpr.of(new HashSet<>(rhsIntersectionType.alternatives()).containsAll(
                    lhsIntersectionType.alternatives()), getTokenRange()));
                return;
            }
            else if (lhsValue instanceof Type lhsTypeValue) {
                context.pushValue(LiteralExpr.of(rhsIntersectionType.alternatives().contains(lhsTypeValue),
                    getTokenRange()));
                return;
            }
            context.pushValue(LiteralExpr.of(false, getTokenRange()));
            return;
        }
        else if (rhsType instanceof ArrayType arrayType) {
            final var elementType = arrayType.elementType();
            if (!elementType.isAssignableFrom(lhsType)) {
                throw new EvaluationException(String.format("Element type %s cannot appear in array of type %s",
                    lhsType,
                    elementType), SourceDiagnostic.from(this), context.createStackTrace());
            }
            final var arrayLength = Array.getLength(rhsValue);
            for (var i = 0; i < arrayLength; i++) {
                final var value = Array.get(rhsValue, i);
                if (!lhsValue.equals(value)) {
                    continue;
                }
                context.pushValue(LiteralExpr.of(true, getTokenRange()));
                return;
            }
            context.pushValue(LiteralExpr.of(false, getTokenRange()));
            return;
        }
        throw new EvaluationException("Incompatible types in in-expression",
            SourceDiagnostic.from(this),
            context.createStackTrace());
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        return BuiltinType.BOOL;
    }

    @Override
    public @NotNull InExpr copy() {
        return copyParentAndSourceTo(new InExpr(getLhs().copy(), getRhs().copy()));
    }
}
