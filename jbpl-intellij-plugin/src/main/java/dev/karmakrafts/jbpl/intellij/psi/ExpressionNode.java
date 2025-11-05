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

package dev.karmakrafts.jbpl.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.util.Annotated;
import dev.karmakrafts.jbpl.intellij.util.TextAttributeKeys;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

public final class ExpressionNode extends ANTLRPsiNode implements Annotated {
    private static final IntSet BINARY_OPS = IntSet.of(JBPLLexer.AMP,
        JBPLLexer.AMPAMP,
        JBPLLexer.PIPE,
        JBPLLexer.PIPEPIPE,
        JBPLLexer.EQEQ,
        JBPLLexer.CARET,
        JBPLLexer.NEQ,
        JBPLLexer.L_ABRACKET,
        JBPLLexer.R_ABRACKET,
        JBPLLexer.LSH,
        JBPLLexer.RSH,
        JBPLLexer.PLUS,
        JBPLLexer.MINUS,
        JBPLLexer.ASTERISK,
        JBPLLexer.SLASH,
        JBPLLexer.REM,
        JBPLLexer.SPACESHIP,
        JBPLLexer.GEQ,
        JBPLLexer.LEQ);
    private static final IntSet UNARY_OPS = IntSet.of(JBPLLexer.PLUS, JBPLLexer.MINUS, JBPLLexer.TILDE);

    public ExpressionNode(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void annotate(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
        final var children = element.getChildren();
        if (children.length == 3) {
            // Handle semantic highlighting for operators
            final var opNode = children[1];
            if (opNode.getNode().getElementType() instanceof TokenIElementType tokenType && BINARY_OPS.contains(tokenType.getANTLRTokenType())) { // @formatter:off
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                    .range(opNode)
                    .textAttributes(TextAttributeKeys.OPERATOR)
                    .create();
            } // @formatter:on
        }
        else if (children.length == 2) {
            // Handle semantic highlighting for unary operators
            final var opNode = children[0];
            if(opNode.getNode().getElementType() instanceof TokenIElementType tokenType && UNARY_OPS.contains(tokenType.getANTLRTokenType())) { // @formatter:off
                holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                    .range(opNode)
                    .textAttributes(TextAttributeKeys.OPERATOR)
                    .create();
            } // @formatter:on
        }
    }
}
