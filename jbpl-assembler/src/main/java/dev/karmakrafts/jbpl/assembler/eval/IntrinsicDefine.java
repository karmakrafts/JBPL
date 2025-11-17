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

package dev.karmakrafts.jbpl.assembler.eval;

import dev.karmakrafts.jbpl.assembler.model.expr.Expr;
import dev.karmakrafts.jbpl.assembler.util.XBiConsumer;
import dev.karmakrafts.jbpl.assembler.util.XFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record IntrinsicDefine( // @formatter:off
    @NotNull String name,
    @NotNull XFunction<EvaluationContext, Expr, EvaluationException> getter,
    @Nullable XBiConsumer<EvaluationContext, Expr, EvaluationException> setter
) { // @formatter:on
}
