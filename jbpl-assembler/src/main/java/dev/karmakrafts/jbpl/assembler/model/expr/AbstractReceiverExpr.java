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

import org.jetbrains.annotations.NotNull;

public abstract class AbstractReceiverExpr extends AbstractExprContainer {
    public static final int RECEIVER_INDEX = 0;

    public AbstractReceiverExpr() {
        addExpression(ConstExpr.unit()); // Receiver
    }

    public @NotNull Expr getReceiver() {
        return getExpressions().get(RECEIVER_INDEX);
    }

    public void setReceiver(final @NotNull Expr receiver) {
        getReceiver().setParent(null);
        receiver.setParent(this);
        if (elements.isEmpty()) {
            elements.add(receiver);
        }
        elements.set(RECEIVER_INDEX, receiver);
    }
}
