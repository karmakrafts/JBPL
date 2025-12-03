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

package dev.karmakrafts.jbpl.intellij.copyright;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdatePsiFileCopyright;
import dev.karmakrafts.jbpl.intellij.JBPLFileType;
import org.jetbrains.annotations.NotNull;

public final class UpdateJBPLFileCopyright extends UpdatePsiFileCopyright {
    UpdateJBPLFileCopyright(final @NotNull Project project,
                            final @NotNull Module module,
                            final @NotNull VirtualFile root,
                            final @NotNull CopyrightProfile options) {
        super(project, module, root, options);
    }

    @Override
    protected boolean accept() {
        return getFile().getFileType() == JBPLFileType.INSTANCE;
    }

    @Override
    protected void scanFile() {
        final var location = getLanguageOptions().getFileLocation();
        final var file = getFile();
        checkComments(file.getFirstChild(), file.getLastChild(), location == 1);
    }
}
