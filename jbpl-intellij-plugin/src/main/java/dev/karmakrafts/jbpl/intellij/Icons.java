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

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public final class Icons {
    public static final Icon FILE = IconLoader.getIcon("/icons/dev/karmakrafts/jbpl/intellij/file.svg", Icons.class);
    public static final Icon INJECTOR = IconLoader.getIcon("/icons/dev/karmakrafts/jbpl/intellij/injector.svg",
        Icons.class);
    public static final Icon SELECTOR = IconLoader.getIcon("/icons/dev/karmakrafts/jbpl/intellij/selector.svg",
        Icons.class);

    private Icons() {
    }
}
