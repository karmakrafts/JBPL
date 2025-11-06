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

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts.Label;
import dev.karmakrafts.jbpl.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class JBPLFileType extends LanguageFileType {
    public static final JBPLFileType INSTANCE = new JBPLFileType();

    private JBPLFileType() {
        super(JBPLanguage.INSTANCE);
    }

    @Override
    public @NotNull String getName() {
        return "JBPL";
    }

    @Override
    public @Label @NotNull String getDescription() {
        return "JBPL assembly file";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "jbpl";
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE;
    }
}
