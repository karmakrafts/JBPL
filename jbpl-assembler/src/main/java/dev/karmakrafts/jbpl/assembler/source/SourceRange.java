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

package dev.karmakrafts.jbpl.assembler.source;

import org.jetbrains.annotations.NotNull;

public record SourceRange(int startLine, int startColumn, int endLine, int endColumn) {
    public static final int UNDEFINED_INDEX = -1;
    public static final int SYNTHETIC_INDEX = -2;
    public static final SourceRange UNDEFINED = new SourceRange(UNDEFINED_INDEX,
        UNDEFINED_INDEX,
        UNDEFINED_INDEX,
        UNDEFINED_INDEX);
    public static final SourceRange SYNTHETIC = new SourceRange(SYNTHETIC_INDEX,
        SYNTHETIC_INDEX,
        SYNTHETIC_INDEX,
        SYNTHETIC_INDEX);

    public static @NotNull SourceRange from(final int line, final int column) {
        return new SourceRange(line, column, line, column);
    }

    public int getLineCount() {
        return endLine - startLine;
    }

    public boolean containsLine(final int line) {
        return line >= startLine && line <= endLine;
    }

    public boolean containsColumn(final int line, final int column) {
        return line >= startLine && line <= endLine && column >= startColumn && column <= endColumn;
    }

    public boolean contains(final @NotNull SourceRange range) { // @formatter:off
        // If one of the line boundaries is outside this range, take a short path
        if(range.startLine < startLine || range.endLine > endLine) {
            return false;
        }
        // If the start line is equal, we early return false if the column doesn't fit within this range
        if(range.startLine == startLine && range.startColumn < startColumn) {
            return false;
        }
        // Otherwise check if end line or end column fall inside this range
        return range.endLine != endLine || range.endColumn <= endColumn;
    } // @formatter:on

    public boolean isUndefined() { // @formatter:off
        return startLine == UNDEFINED_INDEX || startColumn == UNDEFINED_INDEX
            || endLine == UNDEFINED_INDEX || endColumn == UNDEFINED_INDEX;
    } // @formatter:on

    public boolean isSynthetic() { // @formatter:off
        return startLine == SYNTHETIC_INDEX || startColumn == SYNTHETIC_INDEX
            || endLine == SYNTHETIC_INDEX || endColumn == SYNTHETIC_INDEX;
    } // @formatter:on
}
