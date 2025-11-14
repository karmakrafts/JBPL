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
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

public final class ExprOrNameNode extends JBPLPsiNode {
    public ExprOrNameNode(final @NotNull ASTNode node) {
        super(node);
    }

    public void annotateNameWith(final @NotNull TextAttributesKey key, final @NotNull AnnotationHolder holder) {
        PsiUtils.find(this, "/exprOrName/nameSegment").ifPresent(name -> { // @formatter:off
            holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(name)
                .textAttributes(key)
                .create();
        }); // @formatter:on
    }

    @Override
    public @NotNull String getName() { // @formatter:off
        return PsiUtils.find(this, "/exprOrName/nameSegment")
            .map(PsiElement::getText)
            .orElseGet(this::getText);
    } // @formatter:on
}
