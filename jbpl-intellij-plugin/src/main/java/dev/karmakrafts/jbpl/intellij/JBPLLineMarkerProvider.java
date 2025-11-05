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

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer.Alignment;
import com.intellij.psi.PsiElement;
import com.intellij.util.PlatformIcons;
import dev.karmakrafts.jbpl.intellij.psi.FieldNode;
import dev.karmakrafts.jbpl.intellij.psi.FunctionNode;
import dev.karmakrafts.jbpl.intellij.psi.InjectorNode;
import dev.karmakrafts.jbpl.intellij.psi.SelectorNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JBPLLineMarkerProvider implements LineMarkerProvider {
    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(final @NotNull PsiElement element) {
        if (element instanceof InjectorNode injector) {
            return new LineMarkerInfo<>(injector,
                injector.getTextRange(),
                Icons.INJECTOR,
                null,
                null,
                Alignment.RIGHT,
                () -> "Injector");
        }
        else if (element instanceof SelectorNode selector) {
            return new LineMarkerInfo<PsiElement>(selector,
                selector.getTextRange(),
                Icons.SELECTOR,
                null,
                null,
                Alignment.RIGHT,
                () -> "Selector");
        }
        else if (element instanceof FunctionNode function) {
            return new LineMarkerInfo<PsiElement>(function,
                function.getTextRange(),
                PlatformIcons.FUNCTION_ICON,
                null,
                null,
                Alignment.RIGHT,
                () -> "Function");
        }
        else if (element instanceof FieldNode field) {
            return new LineMarkerInfo<PsiElement>(field,
                field.getTextRange(),
                PlatformIcons.FIELD_ICON,
                null,
                null,
                Alignment.RIGHT,
                () -> "Field");
        }
        return null;
    }
}
