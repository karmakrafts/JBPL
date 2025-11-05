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

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.antlr.intellij.adaptor.psi.ScopeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JBPLFile extends PsiFileBase implements ScopeNode {
    public JBPLFile(final @NotNull FileViewProvider viewProvider) {
        super(viewProvider, JBPLanguage.INSTANCE);
    }

    @Override
    public @Nullable ScopeNode getContext() {
        return null;
    }

    @Override
    public @NotNull FileType getFileType() {
        return JBPLFileType.INSTANCE;
    }

    @Override
    public @Nullable PsiElement resolve(final @NotNull PsiNamedElement element) {
        return null;
    }
}
