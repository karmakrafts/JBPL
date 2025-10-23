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
import dev.karmakrafts.jbpl.assembler.EvaluationException;
import dev.karmakrafts.jbpl.assembler.model.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.model.statement.AbstractStatementContainer;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.stream.Collectors;

public final class IfExpr extends AbstractStatementContainer implements Expr, ScopeOwner {
    private final ArrayList<ElseIfBranch> elseIfBranches = new ArrayList<>();
    private Expr condition;
    private ElseBranch elseBranch;

    public IfExpr(final @NotNull Expr condition) {
        this.condition = condition;
    }

    public @NotNull Expr getCondition() {
        return condition;
    }

    public void setCondition(final @NotNull Expr condition) {
        this.condition.setParent(null);
        condition.setParent(this);
        this.condition = condition;
    }

    public void addElseIfBranch(final @NotNull ElseIfBranch branch) {
        branch.setParent(this);
        elseIfBranches.add(branch);
    }

    public void removeElseIfBranch(final @NotNull ElseIfBranch branch) {
        elseIfBranches.remove(branch);
        branch.setParent(null);
    }

    public @Nullable ElseBranch getElseBranch() {
        return elseBranch;
    }

    public void setElseBranch(final @Nullable ElseBranch branch) {
        if (elseBranch != null) {
            elseBranch.setParent(null);
        }
        if (branch != null) {
            branch.setParent(this);
        }
        this.elseBranch = branch;
    }

    @Override
    public @NotNull Type getType(final @NotNull AssemblerContext context) throws EvaluationException {
        // @formatter:off
        final var types = elseIfBranches.stream()
            .map(ExceptionUtils.propagateUnchecked(branch -> branch.getType(context)))
            .collect(Collectors.toCollection(ArrayList::new));
        // @formatter:on
        types.add(TypeCommonizer.getCommonType(this, context).orElseThrow());
        if (elseBranch != null) {
            types.add(elseBranch.getType(context));
        }
        return TypeCommonizer.getCommonType(types).orElseThrow();
    }

    @Override
    public void evaluate(final @NotNull AssemblerContext context) throws EvaluationException {
        final var condition = this.condition.evaluateAsConst(context, Boolean.class);
        if (!condition) {
            // If the condition of this if() expression didn't evaluate to true, attempt to take one of the else if() branches..
            for (final var branch : elseIfBranches) {
                branch.evaluate(context);
                if (context.clearRet()) { // If the previous branch was evaluated, break the loop
                    return;
                }
            }
            // ..if those fall through as well, try to evaluate the else branch if present.
            if (elseBranch != null) {
                elseBranch.evaluate(context);
                context.clearRet(); // Make sure the return flag is always cleared after this was reached
            }
            return;
        }
        context.pushFrame(this); // If-scope gets its own stack frame
        super.evaluate(context); // Only evaluate children when condition is true
        context.popFrame();
    }

    @Override
    public @NotNull LiteralExpr evaluateAsConst(final @NotNull AssemblerContext context) {
        if (elseBranch == null) {
            throw new IllegalStateException("If expression can only be const evaluated when it has a default else case");
        }
        return LiteralExpr.unit(); // TODO: implement
    }

    public static final class ElseIfBranch extends AbstractStatementContainer implements ScopeOwner {
        public Expr condition;

        public ElseIfBranch(final @NotNull Expr condition) {
            this.condition = condition;
        }

        public @NotNull Type getType(final @NotNull AssemblerContext context) throws EvaluationException {
            return TypeCommonizer.getCommonType(this, context).orElseThrow();
        }

        @Override
        public void evaluate(@NotNull AssemblerContext context) throws EvaluationException {
            final var condition = this.condition.evaluateAsConst(context, Boolean.class);
            if (!condition) {
                return;
            }
            context.pushFrame(this);
            super.evaluate(context);
            context.popFrame();
            context.ret(); // Mark this branch as the one we return from
        }
    }

    public static final class ElseBranch extends AbstractStatementContainer implements ScopeOwner {
        public @NotNull Type getType(final @NotNull AssemblerContext context) throws EvaluationException {
            return TypeCommonizer.getCommonType(this, context).orElseThrow();
        }

        @Override
        public void evaluate(final @NotNull AssemblerContext context) throws EvaluationException {
            context.pushFrame(this);
            super.evaluate(context);
            context.popFrame();
            context.ret(); // Mark this branch as the one we return from
        }
    }
}
