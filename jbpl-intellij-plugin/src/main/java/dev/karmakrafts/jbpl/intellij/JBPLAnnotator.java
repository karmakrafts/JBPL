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

package dev.karmakrafts.jbpl.intellij;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.psi.DefineNode;
import dev.karmakrafts.jbpl.intellij.psi.ReferenceNode;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

public final class JBPLAnnotator implements Annotator {
    public static final TextAttributesKey DEFINE_NAME = TextAttributesKey.createTextAttributesKey("JBPL_DEFINE_NAME",
        DefaultLanguageHighlighterColors.CONSTANT);

    @Override
    public void annotate(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
        // For defines with literal names, we want the name to be highlighted like a constant
        if (element instanceof DefineNode define) {
            final var children = define.getChildren();
            if (children[1].getNode().getElementType() instanceof TokenIElementType tokenType && tokenType.getANTLRTokenType() == JBPLLexer.IDENT) { // @formatter:off
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                    .range(children[1])
                    .textAttributes(DEFINE_NAME)
                    .create();
            } // @formatter:on
        }
        // References are always colored like constants too, since they refer to defines
        if(element instanceof ReferenceNode reference) { // @formatter:off
            holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(reference)
                .textAttributes(DEFINE_NAME)
                .create();
        } // @formatter:on
    }
}
