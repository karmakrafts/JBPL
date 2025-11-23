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

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import com.intellij.lang.Language;
import dev.karmakrafts.jbpl.intellij.psi.StructuralPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class JBPLStructureAwareNavBarModelExtension extends StructureAwareNavBarModelExtension {
    @Override
    protected @NotNull Language getLanguage() {
        return JBPLanguage.INSTANCE;
    }

    @Override
    public @Nullable String getPresentableText(final @Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof StructuralPsiElement structural) {
            return structural.getStructureText();
        }
        return null;
    }

    @Override
    public @Nullable Icon getIcon(final @Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof StructuralPsiElement structural) {
            return structural.getIcon(0);
        }
        return null;
    }
}
