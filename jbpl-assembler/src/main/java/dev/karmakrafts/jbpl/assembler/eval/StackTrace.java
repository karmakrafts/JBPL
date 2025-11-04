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

import dev.karmakrafts.jbpl.assembler.model.element.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record StackTrace(@NotNull List<StackFrame> frames) {
    @Override
    public @NotNull String toString() {
        if (frames.isEmpty()) {
            return "";
        }
        final var builder = new StringBuilder();
        final var lastFrameIndex = frames.size() - 1;
        for (var i = 0; i < frames.size(); i++) {
            final var frame = frames.get(lastFrameIndex - i);
            final var owner = frame.scope.owner();
            if (owner instanceof Element element) {
                final var file = element.getContainingFile();
                final var range = file.getSourceRange(element.getTokenRange());
                final var line = range.startLine();
                final var column = range.startColumn();
                final var parent = element.getParent();
                if (parent != null) {
                    builder.append(String.format("%s.%s(%s:%s:%s)", parent, element, file.path, line, column));
                }
                else {
                    builder.append(String.format("%s(%s:%s:%s)", element, file.path, line, column));
                }
                if (i < lastFrameIndex) {
                    builder.append('\n');
                }
                continue;
            }
            builder.append(owner);
            if (i < lastFrameIndex) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }
}
