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

package dev.karmakrafts.jbpl.intellij.insight;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.psi.JBPLPsiLeafNode;
import dev.karmakrafts.jbpl.intellij.psi.JBPLPsiNode;
import dev.karmakrafts.jbpl.intellij.psi.NameSegmentNode;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JBPLGoToDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement element,
                                                             final int offset,
                                                             final @NotNull Editor editor) {
        if (element == null) {
            return null;
        }
        // Resolve raw identifiers to their segment parent if present
        if (element.getNode().getElementType() instanceof TokenIElementType tokenType && tokenType.getANTLRTokenType() == JBPLLexer.IDENT) {
            final var segment = PsiTreeUtil.findFirstParent(element, NameSegmentNode.class::isInstance);
            if (segment != null) {
                element = segment;
            }
        }
        if (element instanceof JBPLPsiNode || element instanceof JBPLPsiLeafNode) {
            final var reference = element.getReference();
            if (reference == null) {
                return null;
            }
            final var target = reference.resolve();
            if (target == null) {
                return null;
            }
            return new PsiElement[]{target};
        }
        return null;
    }
}
