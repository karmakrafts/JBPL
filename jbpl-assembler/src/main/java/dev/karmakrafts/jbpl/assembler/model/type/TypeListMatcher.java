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

package dev.karmakrafts.jbpl.assembler.model.type;

import dev.karmakrafts.jbpl.assembler.eval.EvaluationContext;
import dev.karmakrafts.jbpl.assembler.eval.EvaluationException;
import dev.karmakrafts.jbpl.assembler.util.ExceptionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class TypeListMatcher {
    private TypeListMatcher() {
    }

    /**
     * Compute a simple numeric heuristic based on how well the given list of types is
     * assignable to its counterpart.
     * <p>
     * If the lists are of different sizes, the score is always 0.<br>
     * Otherwise, each type is checked for its {@link TypeConversion} and
     * the obtained type of conversion is converted into a numeric score.
     * <p>
     * - <b>For every {@link TypeConversion#DIRECT} match, 2 points are added to the total score</b><br>
     * - <b>For every {@link TypeConversion#COERCE} match, 1 point is added to the total score</b>
     * <p>
     * <h3>Example with call and declaration:</h3><br>
     * {@code foo(2, 3, 4)}, so out argument types are {@code i32}, {@code i32} and {@code i32}<br>
     * Since integers may be implicitly upcasted to larger byte size or promoted to floating point values,<br>
     * this algorithm takes into account implicit conversions.
     * <p>
     * - {@code macro foo(x: i32, y: i32, z: i32)} would be rated <b>2 + 2 + 2 = 6</b><br>
     * - {@code macro foo(x: i64, y: i64, z: i32)} would be rated <b>1 + 2 + 2 = 5</b><br>
     * - {@code macro foo(s: string)} would be rated <b>0</b>
     */
    public static int computeRating(final @NotNull List<Type> dstTypes,
                                    final @NotNull List<Type> srcTypes,
                                    final @NotNull EvaluationContext context) throws EvaluationException {
        if (dstTypes.size() != srcTypes.size()) {
            return 0;
        }
        var rating = 0;
        for (var i = 0; i < srcTypes.size(); i++) {
            final var srcType = srcTypes.get(i);
            final var dstType = dstTypes.get(i);
            final var conversion = dstType.conversionTypeFrom(srcType, context);
            if (conversion == TypeConversion.NONE) {
                return 0; // A single mismatch means a rating of 0
            }
            rating += conversion.ordinal(); // 1 for COERCE, 2 for DIRECT
        }
        return rating;
    }

    public static <D> @NotNull RatedTypeList<D> rate(final @NotNull TypeList<D> dstTypes,
                                                     final @NotNull List<Type> srcTypes,
                                                     final @NotNull EvaluationContext context) throws EvaluationException {
        return new RatedTypeList<>(srcTypes, computeRating(dstTypes.types, srcTypes, context), dstTypes.data);
    }

    public static <D> @NotNull List<RatedTypeList<D>> rate(final @NotNull List<TypeList<D>> srcTypeLists,
                                                           final @NotNull List<Type> dstTypes,
                                                           final @NotNull EvaluationContext context) {
        // @formatter:off
        return srcTypeLists.stream()
            .map(ExceptionUtils.unsafeFunction(list -> rate(list, dstTypes, context)))
            .filter(list -> list.rating > 0)
            .sorted()
            .toList();
        // @formatter:on
    }

    public static <D> @NotNull Optional<Result<D>> match(final @NotNull List<TypeList<D>> srcTypeLists,
                                                         final @NotNull List<Type> dstTypes,
                                                         final @NotNull EvaluationContext context) {
        final var ratedLists = rate(srcTypeLists, dstTypes, context);
        if (ratedLists.isEmpty()) {
            return Optional.empty();
        }
        final var list = ratedLists.get(0);
        final var highestRating = list.rating; // First entry should be the highest rating
        var matchType = MatchType.DIRECT;
        if (ratedLists.stream().filter(l -> l.rating == highestRating).count() > 1) {
            matchType = MatchType.AMBIGUOUS;
        }
        return Optional.of(new Result<>(list, matchType));
    }

    public enum MatchType {
        DIRECT, AMBIGUOUS
    }

    public record Result<D>(@NotNull RatedTypeList<D> types, MatchType matchType) {
    }

    public record TypeList<D>(@NotNull List<Type> types, D data) {
    }

    public record RatedTypeList<D>(List<Type> types, int rating, D data) implements Comparable<RatedTypeList<D>> {
        @Override
        public int compareTo(final @NotNull TypeListMatcher.RatedTypeList o) {
            return Integer.compare(rating, o.rating);
        }
    }
}
