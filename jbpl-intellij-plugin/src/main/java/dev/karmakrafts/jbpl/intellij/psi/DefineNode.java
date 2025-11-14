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

package dev.karmakrafts.jbpl.intellij.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.EffectType;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.intellij.util.Icons;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import dev.karmakrafts.jbpl.intellij.util.TextAttributeKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public final class DefineNode extends JBPLPsiNode implements StructuralPsiElement, Annotated {
    public DefineNode(final @NotNull ASTNode node) {
        super(node);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void annotate(final @NotNull AnnotationHolder holder) {
        final var isFinal = PsiUtils.hasToken(this, JBPLLexer.KW_FINAL);
        if (isFinal) {
            // @formatter:off
            PsiUtils.find(this, "/define/exprOrName/nameSegment").ifPresent(name -> holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
                .range(name)
                .textAttributes(TextAttributeKeys.DEFINE)
                .create());
            // @formatter:on
            return;
        }
        final var scheme = EditorColorsManager.getInstance().getGlobalScheme();
        final var reAssignAttribs = scheme.getAttributes(DefaultLanguageHighlighterColors.REASSIGNED_LOCAL_VARIABLE);
        final var attribs = scheme.getAttributes(TextAttributeKeys.DEFINE).clone();
        attribs.withAdditionalEffect(EffectType.LINE_UNDERSCORE, reAssignAttribs.getEffectColor());
        // @formatter:off
        PsiUtils.find(this, "/define/exprOrName/nameSegment").ifPresent(name -> holder.newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
            .range(name)
            .enforcedTextAttributes(attribs)
            .create());
        // @formatter:on
    }

    @Override
    public @Nullable String getName() {
        final var children = getChildren();
        if (children.length < 2) {
            return null;
        }
        return PsiUtils.toSingleLine(children[1]);
    }

    // Defines should be rendered as <name>: <type> = <value>
    @Override
    public @NotNull String getDetailedStructureText() {
        // @formatter:off
        final var name = getName();
        final var type = PsiUtils.find(this, "/define/exprOrType")
            .map(PsiUtils::toSingleLine)
            .orElse("Unknown");
        final var value = PsiUtils.find(this, "/define/expr", ExpressionNode.class)
            .map(PsiUtils::toSingleLine)
            .orElse("<uninitialized>");
        // @formatter:on
        return String.format("%s: %s = %s", name, type, value);
    }

    @Override
    public @NotNull Icon getStructureIcon() {
        return Icons.DEFINE;
    }
}