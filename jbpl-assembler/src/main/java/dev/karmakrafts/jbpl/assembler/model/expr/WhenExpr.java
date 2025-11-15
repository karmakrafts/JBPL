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
import dev.karmakrafts.jbpl.assembler.model.element.ElementContainer;
import dev.karmakrafts.jbpl.assembler.model.type.Type;
import dev.karmakrafts.jbpl.assembler.model.type.TypeCommonizer;
import dev.karmakrafts.jbpl.assembler.scope.ScopeOwner;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class WhenExpr extends AbstractExprContainer implements Expr, ScopeOwner {
    public static final int VALUE_INDEX = 0;
    private final ArrayList<Branch> branches = new ArrayList<>();

    public WhenExpr(final @NotNull Expr value) {
        addExpression(value);
    }

    public void clearBranches() {
        branches.clear();
    }

    public void addBranches(final @NotNull Collection<? extends Branch> branches) {
        this.branches.addAll(branches);
    }

    public void addBranch(final @NotNull Branch branch) {
        branches.add(branch);
    }

    public @NotNull List<Branch> getBranches() {
        return branches;
    }

    public @NotNull Expr getValue() {
        return getExpressions().get(VALUE_INDEX);
    }

    public void setValue(final @NotNull Expr value) {
        getValue().setParent(null);
        value.setParent(this);
        getExpressions().set(VALUE_INDEX, value);
    }

    @Override
    public void evaluate(final @NotNull EvaluationContext context) throws EvaluationException {
        final var value = getValue().evaluateAs(context, Object.class);
        // Attempt evaluating branches in order
        for (final var branch : branches) {
            if (!(branch instanceof ConditionalBranch conditionalBranch)) {
                continue;
            }
            if (!conditionalBranch.getValue().evaluateAs(context, Object.class).equals(value)) {
                continue;
            }
            branch.evaluate(context);
            return;
        }
        // If no branch was matched, try to find a default branch
        final var defaultBranch = branches.stream().filter(branch -> !(branch instanceof ConditionalBranch)).findFirst();
        if (defaultBranch.isEmpty()) {
            return;
        }
        defaultBranch.get().evaluate(context);
    }

    @Override
    public @NotNull Type getType(final @NotNull EvaluationContext context) throws EvaluationException { // @formatter:off
        return TypeCommonizer.getCommonType(branches.stream()
            .map(branch -> TypeCommonizer.getCommonReturnType(branch.getElements(), context).orElseThrow())
            .toList(), context).orElseThrow();
    } // @formatter:on

    @Override
    public @NotNull WhenExpr copy() {
        final var expr = copyParentAndSourceTo(new WhenExpr(getValue().copy()));
        expr.addBranches(branches.stream().map(Branch::copy).toList());
        return expr;
    }

    public interface Branch extends ElementContainer {
        @Override
        Branch copy();
    }

    public interface ConditionalBranch extends Branch {
        @NotNull Expr getValue();

        void setValue(final @NotNull Expr condition);

        @Override
        ConditionalBranch copy();
    }

    public static sealed abstract class AbstractConditionalBranch extends AbstractElementContainer
        implements ConditionalBranch {
        protected Expr value;

        protected AbstractConditionalBranch(final @NotNull Expr value) {
            setValue(value);
        }

        @Override
        public @NotNull Expr getValue() {
            return value;
        }

        @Override
        public void setValue(final @NotNull Expr condition) {
            if (this.value != null) {
                this.value.setParent(null);
            }
            condition.setParent(this);
            this.value = condition;
        }
    }

    public static final class ScopedBranch extends AbstractConditionalBranch implements ScopeOwner {
        public ScopedBranch(final @NotNull Expr condition) {
            super(condition);
        }

        @Override
        public ScopedBranch copy() {
            final var branch = copyParentAndSourceTo(new ScopedBranch(value.copy()));
            branch.addElements(getElements().stream().map(Element::copy).toList());
            return branch;
        }
    }

    public static final class ScopelessBranch extends AbstractConditionalBranch {
        public ScopelessBranch(final @NotNull Expr condition) {
            super(condition);
        }

        @Override
        public ScopelessBranch copy() {
            final var branch = copyParentAndSourceTo(new ScopelessBranch(value.copy()));
            branch.addElements(getElements().stream().map(Element::copy).toList());
            return branch;
        }
    }

    public static final class ScopedDefaultBranch extends AbstractElementContainer implements Branch, ScopeOwner {
        @Override
        public ScopedDefaultBranch copy() {
            final var branch = copyParentAndSourceTo(new ScopedDefaultBranch());
            branch.addElements(getElements().stream().map(Element::copy).toList());
            return branch;
        }
    }

    public static final class ScopelessDefaultBranch extends AbstractElementContainer implements Branch {
        @Override
        public ScopelessDefaultBranch copy() {
            final var branch = copyParentAndSourceTo(new ScopelessDefaultBranch());
            branch.addElements(getElements().stream().map(Element::copy).toList());
            return branch;
        }
    }
}
