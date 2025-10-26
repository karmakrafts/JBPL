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
import dev.karmakrafts.jbpl.assembler.model.element.AbstractElementContainer;
import dev.karmakrafts.jbpl.assembler.model.element.Element;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class IfExpr extends AbstractElementContainer implements Expr, ScopeOwner {
    private final ArrayList<ElseIfBranch> elseIfBranches = new ArrayList<>();
    private Expr condition;
    private ElseBranch elseBranch;

    public IfExpr(final @NotNull Expr condition) {
        setCondition(condition);
    }

    public @NotNull Expr getCondition() {
        return condition;
    }

    public void setCondition(final @NotNull Expr condition) {
        if (this.condition != null) {
            this.condition.setParent(null);
        }
        condition.setParent(this);
        this.condition = condition;
    }

    public void addElseIfBranch(final @NotNull ElseIfBranch branch) {
        branch.setParent(this);
        elseIfBranches.add(branch);
    }

    public void addElseIfBranches(final @NotNull Collection<ElseIfBranch> branches) {
        for (final var branch : branches) {
            addElseIfBranch(branch);
        }
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

    public @NotNull List<ElseIfBranch> getElseIfBranches() {
        return elseIfBranches;
    }

    public void clearElseIfBranches() {
        elseIfBranches.clear();
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
        // @formatter:off
        final var types = elseIfBranches.stream()
            .map(ExceptionUtils.unsafeFunction(branch -> branch.getType(context)))
            .collect(Collectors.toCollection(ArrayList::new));
        // @formatter:on
        types.add(TypeCommonizer.getCommonType(getElements(), context).orElseThrow());
        if (elseBranch != null) {
            types.add(elseBranch.getType(context));
        }
        return TypeCommonizer.getCommonType(types).orElseThrow();
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var condition = this.condition.evaluateAsConst(context, Boolean.class);
        if (!condition) {
            // If the condition of this if() expression didn't evaluate to true, attempt to take one of the else if() branches..
            for (final var branch : elseIfBranches) {
                branch.evaluate(context);
                if (context.hasRet()) { // If the previous branch has returned, break the loop and propagate return flag
                    return;
                }
            }
            // ..if those fall through as well, try to evaluate the else branch if present.
            if (elseBranch != null) {
                elseBranch.evaluate(context);
            }
            return;
        }
        context.pushFrame(this); // If-scope gets its own stack frame
        super.evaluate(context); // Only evaluate children when condition is true
        context.popFrame();
    }

    @Override
    public @NotNull IfExpr copy() {
        final var result = copyParentAndSourceTo(new IfExpr(condition.copy()));
        result.addElseIfBranches(getElseIfBranches().stream().map(ElseIfBranch::copy).toList());
        result.setElseBranch(getElseBranch().copy());
        result.addElements(getElements().stream().map(Element::copy).toList());
        return result;
    }

    @Override
    public boolean mergeFrameDataOnFrameExit() {
        return true;
    }

    public static final class ElseIfBranch extends AbstractElementContainer implements ScopeOwner {
        private Expr condition;

        public ElseIfBranch(final @NotNull Expr condition) {
            setCondition(condition);
        }

        public void setCondition(final @NotNull Expr condition) {
            if (this.condition != null) {
                this.condition.setParent(null);
            }
            condition.setParent(this);
            this.condition = condition;
        }

        public @NotNull Expr getCondition() {
            return condition;
        }

        public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
            return TypeCommonizer.getCommonType(getElements(), context).orElseThrow();
        }

        @Override
        public void evaluate(@NotNull EvaluationContext context) throws EvaluationException {
            final var condition = this.condition.evaluateAsConst(context, Boolean.class);
            if (!condition) {
                return;
            }
            context.pushFrame(this);
            super.evaluate(context);
            context.popFrame();
            context.ret(); // Mark this branch as the one we return from
        }

        @Override
        public @NotNull ElseIfBranch copy() {
            final var result = copyParentAndSourceTo(new ElseIfBranch(condition.copy()));
            result.addElements(getElements().stream().map(Element::copy).toList());
            return result;
        }

        @Override
        public boolean mergeFrameDataOnFrameExit() {
            return true;
        }
    }

    public static final class ElseBranch extends AbstractElementContainer implements ScopeOwner {
        public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException {
            return TypeCommonizer.getCommonType(getElements(), context).orElseThrow();
        }

        @Override
        public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
            context.pushFrame(this);
            super.evaluate(context);
            context.popFrame();
            context.ret(); // Mark this branch as the one we return from
        }

        @Override
        public @NotNull ElseBranch copy() {
            final var result = copyParentAndSourceTo(new ElseBranch());
            result.addElements(getElements().stream().map(Element::copy).toList());
            return result;
        }

        @Override
        public boolean mergeFrameDataOnFrameExit() {
            return true;
        }
    }
}
