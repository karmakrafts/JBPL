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

package dev.karmakrafts.jbpl.intellij.action;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts.Command;
import com.intellij.psi.PsiDirectory;
import dev.karmakrafts.jbpl.intellij.util.Icons;
import dev.karmakrafts.jbpl.intellij.util.JBPLBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class CreateJBPLFileAction extends CreateFileFromTemplateAction {
    public CreateJBPLFileAction() {
        super(JBPLBundle.INSTANCE.getMessage("action.createJBPLFile.text"),
            JBPLBundle.INSTANCE.getMessage("action.createJBPLFile.description"),
            Icons.FILE);
    }

    @Override
    protected void buildDialog(final @NotNull Project project,
                               final @NotNull PsiDirectory directory,
                               final @NotNull CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(JBPLBundle.INSTANCE.getMessage("action.createJBPLFile.title")).addKind(JBPLBundle.INSTANCE.getMessage(
            "action.createJBPLFile.text"), Icons.FILE, "JBPL");
    }

    @Override
    protected @Command String getActionName(final PsiDirectory directory,
                                            final @NonNls @NotNull String s,
                                            final @NonNls String s1) {
        return JBPLBundle.INSTANCE.getMessage("action.createJBPLFile.text");
    }

    @Override
    protected @NonNls @NotNull String getDefaultTemplateProperty() {
        return "JBPL";
    }
}
