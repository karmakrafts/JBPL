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
import dev.karmakrafts.jbpl.intellij.util.Annotated;
import dev.karmakrafts.jbpl.intellij.util.TextAttributeKeys;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

public final class FunctionNameNode extends ANTLRPsiNode implements Annotated {
    public FunctionNameNode(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void annotate(final @NotNull PsiElement element, final @NotNull AnnotationHolder holder) {
        final var firstChild = element.getFirstChild();
        if (!(firstChild instanceof RefOrNameNode refOrName)) {
            return;
        }
        final var firstNameChild = refOrName.getFirstChild();
        if (!(firstNameChild instanceof NameSegmentNode nameSegment)) {
            return;
        }
        // @formatter:off
        holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(nameSegment)
            .textAttributes(TextAttributeKeys.FUNCTION)
            .create();
        // @formatter:on
    }
}
