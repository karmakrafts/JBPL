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

import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCallExpr extends AbstractReceiverExpr {
    private final ArrayList<Pair<@Nullable Expr, Expr>> arguments = new ArrayList<>();

    public AbstractCallExpr() {
        super();
    }

    public void clearArguments() {
        arguments.clear();
    }

    public @NotNull Pair<@Nullable Expr, Expr> getArgument(final int index) {
        return arguments.get(index + 1);
    }

    public void addArgument(final @Nullable Expr name, final @NotNull Expr argument) {
        if (name != null) {
            name.setParent(this);
        }
        argument.setParent(this);
        arguments.add(new Pair<>(name, argument));
    }

    public void addArguments(final @NotNull Iterable<Pair<@Nullable Expr, Expr>> arguments) {
        for (final var pair : arguments) {
            addArgument(pair.left(), pair.right());
        }
    }

    public @NotNull List<Pair<@Nullable Expr, Expr>> getArguments() {
        return arguments;
    }
}
