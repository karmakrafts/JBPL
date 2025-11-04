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

package dev.karmakrafts.jbpl.intellij.util;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public final class TextAttributeKeys {
    public static final TextAttributesKey DEFINE_NAME = TextAttributesKey.createTextAttributesKey("JBPL_DEFINE_NAME",
        DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey MACRO_NAME = TextAttributesKey.createTextAttributesKey("JBPL_MACRO_NAME",
        DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey CLASS_TYPE = TextAttributesKey.createTextAttributesKey("JBPL_CLASS_TYPE",
        DefaultLanguageHighlighterColors.INTERFACE_NAME);
    public static final TextAttributesKey INTERPOLATION = TextAttributesKey.createTextAttributesKey("JBPL_INTERPOLATION",
        DefaultLanguageHighlighterColors.KEYWORD);

    private TextAttributeKeys() {
    }
}
