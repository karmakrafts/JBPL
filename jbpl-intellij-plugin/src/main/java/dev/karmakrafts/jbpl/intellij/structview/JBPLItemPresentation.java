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

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import dev.karmakrafts.jbpl.intellij.psi.StructuralPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class JBPLItemPresentation implements ItemPresentation {
    private final PsiElement element;

    public JBPLItemPresentation(final @NotNull PsiElement element) {
        this.element = element;
    }

    @Override
    public @Nullable String getPresentableText() {
        if (element instanceof StructuralPsiElement structural) {
            return structural.getName();
        }
        return null;
    }

    @Override
    public @Nullable Icon getIcon(final boolean unused) {
        if (element instanceof StructuralPsiElement structural) {
            return structural.getStructureIcon();
        }
        return null;
    }

    @Override
    public @Nullable String getLocationString() {
        return null;
    }
}
