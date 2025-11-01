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

package dev.karmakrafts.jbpl.assembler.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static <T> T rethrowUnchecked(final @NotNull XSupplier<T, ?> supplier) {
        try {
            return supplier.get();
        }
        catch (Throwable error) {
            if (error instanceof RuntimeException runtimeError) {
                throw runtimeError;
            }
            throw new RuntimeException(error);
        }
    }

    public static <T> @NotNull Predicate<T> unsafePredicate(final @NotNull XPredicate<T, ?> predicate) {
        return value -> rethrowUnchecked(() -> predicate.test(value));
    }

    public static <T, R> @NotNull Function<T, R> unsafeFunction(final @NotNull XFunction<T, R, ?> function) {
        return value -> rethrowUnchecked(() -> function.apply(value));
    }
}
