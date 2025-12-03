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

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import dev.karmakrafts.jbpl.intellij.psi.Foldable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class JBPLFoldingBuilder extends FoldingBuilderEx {
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(final @NotNull PsiElement root,
                                                          final @NotNull Document document,
                                                          final boolean quick) {
        final var descriptors = new ArrayList<FoldingDescriptor>();
        root.accept(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(final @NotNull PsiElement element) {
                super.visitElement(element);
                if (element instanceof Foldable) {
                    descriptors.add(new FoldingDescriptor(element.getNode(), element.getTextRange()));
                }
            }
        });
        return descriptors.toArray(FoldingDescriptor[]::new);
    }

    @Override
    public boolean isCollapsedByDefault(final @NotNull ASTNode node) {
        return false;
    }

    @Override
    public @NotNull String getPlaceholderText(final @NotNull ASTNode node) {
        if (node.getPsi() instanceof Foldable foldable) {
            return foldable.getFoldedText();
        }
        return "...";
    }
}
