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
    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("JBPL_KEYWORD",
        DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey NUMBER = TextAttributesKey.createTextAttributesKey("JBPL_NUMBER",
        DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING = TextAttributesKey.createTextAttributesKey("JBPL_STRING",
        DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey CHAR = TextAttributesKey.createTextAttributesKey("JBPL_CHAR",
        DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey BRACE = TextAttributesKey.createTextAttributesKey("JBPL_BRACE",
        DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey PAREN = TextAttributesKey.createTextAttributesKey("JBPL_PAREN",
        DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACKET = TextAttributesKey.createTextAttributesKey("JBPL_BRACKET",
        DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey ANGLE_BRACKET = TextAttributesKey.createTextAttributesKey("JBPL_ANGLE_BRACKET",
        DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey INSTRUCTION = TextAttributesKey.createTextAttributesKey("JBPL_INSTRUCTION",
        DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey DOT = TextAttributesKey.createTextAttributesKey("JBPL_DOT",
        DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey LINE_COMMENT = TextAttributesKey.createTextAttributesKey("JBPL_LINE_COMMENT",
        DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("JBPL_BLOCK_COMMENT",
        DefaultLanguageHighlighterColors.BLOCK_COMMENT);

    public static final TextAttributesKey DEFINE = TextAttributesKey.createTextAttributesKey("JBPL_DEFINE",
        DefaultLanguageHighlighterColors.STATIC_FIELD);
    public static final TextAttributesKey MACRO = TextAttributesKey.createTextAttributesKey("JBPL_MACRO",
        DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey CLASS = TextAttributesKey.createTextAttributesKey("JBPL_CLASS",
        DefaultLanguageHighlighterColors.INTERFACE_NAME);
    public static final TextAttributesKey INTERPOLATION = TextAttributesKey.createTextAttributesKey("JBPL_INTERPOLATION",
        DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey FIELD = TextAttributesKey.createTextAttributesKey("JBPL_FIELD",
        DefaultLanguageHighlighterColors.INSTANCE_FIELD);
    public static final TextAttributesKey FUNCTION = TextAttributesKey.createTextAttributesKey("JBPL_FUNCTION",
        DefaultLanguageHighlighterColors.INSTANCE_METHOD);
    public static final TextAttributesKey PARAMETER = TextAttributesKey.createTextAttributesKey("JBPL_PARAMETER",
        DefaultLanguageHighlighterColors.PARAMETER);
    public static final TextAttributesKey OPERATOR = TextAttributesKey.createTextAttributesKey("JBPL_OPERATOR",
        DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PREPRO_CLASS = TextAttributesKey.createTextAttributesKey("JBPL_PREPRO_CLASS",
        DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey VARIABLE = TextAttributesKey.createTextAttributesKey("JBPL_VARIABLE",
        DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey TYPE_PARAMETER = TextAttributesKey.createTextAttributesKey(
        "JBPL_TYPE_PARAMETER",
        DefaultLanguageHighlighterColors.PARAMETER);

    private TextAttributeKeys() {
    }
}
