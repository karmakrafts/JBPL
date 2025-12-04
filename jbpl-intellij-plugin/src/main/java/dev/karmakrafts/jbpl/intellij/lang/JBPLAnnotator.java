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

package dev.karmakrafts.jbpl.intellij.lang;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.psi.Annotated;
import dev.karmakrafts.jbpl.intellij.util.TextAttributeKeys;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

public final class JBPLAnnotator implements Annotator {
    private static final IntSet STRING_ESCAPE_TOKENS = IntSet.of(JBPLLexer.M_STRING_ESCAPED_CHAR,
        JBPLLexer.M_STRING_ESCAPED_QUOTE);

    private static void highlightEscapedLiteralChar(final @NotNull PsiElement element,
                                                    final @NotNull AnnotationHolder holder) {
        // Literal escaped characters need special treatment, as we only highlight a subrange of a single token;
        // we highlight all chars within the token except the first and last one, being the single quotes
        final var range = element.getTextRange();
        // Opening single quote
        var start = range.getStartOffset();
        var end = range.getStartOffset() + 1;
        // @formatter:off
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(TextRange.create(start, end))
            .textAttributes(TextAttributeKeys.CHAR)
            .create();
        // @formatter:on
        // Escaped character
        start = range.getStartOffset() + 1;
        end = range.getEndOffset() - 1;
        // @formatter:off
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(TextRange.create(start, end))
            .textAttributes(TextAttributeKeys.ESCAPED_CHAR)
            .create();
        // @formatter:on
        // Closing single quote
        start = range.getEndOffset() - 1;
        end = range.getEndOffset();
        // @formatter:off
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(TextRange.create(start, end))
            .textAttributes(TextAttributeKeys.CHAR)
            .create();
        // @formatter:on
    }

    private static void highlightEscapedChar(final @NotNull PsiElement element,
                                             final @NotNull AnnotationHolder holder) {
        // Escaped chars in strings are handled as one unit
        // @formatter:off
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(element)
            .textAttributes(TextAttributeKeys.ESCAPED_CHAR)
            .create();
        // @formatter:on
    }

    @Override
    public void annotate(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
        if (element.getNode().getElementType() instanceof TokenIElementType tokenType) {
            final var type = tokenType.getANTLRTokenType();
            if (STRING_ESCAPE_TOKENS.contains(type)) {
                highlightEscapedChar(element, holder);
                return;
            }
            else if (type == JBPLLexer.LITERAL_ESCAPED_CHAR) {
                highlightEscapedLiteralChar(element, holder);
                return;
            }
        }
        if (element instanceof Annotated annotated) {
            annotated.annotate(holder);
        }
    }
}
