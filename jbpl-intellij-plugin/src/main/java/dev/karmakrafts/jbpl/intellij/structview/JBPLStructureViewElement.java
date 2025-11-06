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

package dev.karmakrafts.jbpl.intellij.structview;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import dev.karmakrafts.jbpl.intellij.psi.StructuralPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public final class JBPLStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
    private final PsiElement element;
    private final ItemPresentation presentation;

    public JBPLStructureViewElement(final @NotNull PsiElement element) {
        this.element = element;
        presentation = new JBPLItemPresentation(element);
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        final var s = element instanceof StructuralPsiElement structural ? structural.getName() : null;
        if (s == null) {
            return "unknown";
        }
        return s;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return presentation;
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        final var treeElements = new ArrayList<TreeElement>();
        new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(final @NotNull PsiElement element) {
                if (element == JBPLStructureViewElement.this.element) {
                    super.visitElement(element);
                    return;
                }
                if (element instanceof StructuralPsiElement) {
                    treeElements.add(new JBPLStructureViewElement(element));
                    return;
                }
                super.visitElement(element);
            }
        }.visitElement(element);
        return treeElements.toArray(TreeElement[]::new);
    }
}
