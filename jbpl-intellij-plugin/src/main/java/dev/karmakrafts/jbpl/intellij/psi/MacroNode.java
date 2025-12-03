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
import com.intellij.psi.PsiElement;
import dev.karmakrafts.jbpl.intellij.util.Icons;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import dev.karmakrafts.jbpl.intellij.util.TextAttributeKeys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.stream.Collectors;

public final class MacroNode extends JBPLPsiNode implements Annotated, StructuralPsiElement, Foldable {
    public MacroNode(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull String getFoldedText() {
        // @formatter:off
        var mods = PsiUtils.findAll(this, "/macro/macroModifier")
            .map(PsiElement::getText)
            .collect(Collectors.joining(" "));
        // @formatter:on
        if (mods.isEmpty()) {
            return String.format("macro %s {...}", getDetailedStructureText());
        }
        return String.format("%s macro %s {...}", mods, getDetailedStructureText());
    }

    @Override
    public void annotate(final @NotNull AnnotationHolder holder) {
        // @formatter:off
        PsiUtils.find(this, "/macro/macroSignature/exprOrName", ExprOrNameNode.class)
            .ifPresent(refOrName -> refOrName.annotateNameWith(TextAttributeKeys.MACRO, holder));
        // @formatter:on
    }

    @Override
    public @Nullable String getName() { // @formatter:off
        return PsiUtils.find(this, "/macro/macroSignature/exprOrName", ExprOrNameNode.class)
            .map(ExprOrNameNode::getName)
            .orElseGet(super::getName);
    } // @formatter:on

    @Override
    public @NotNull String getDetailedStructureText() { // @formatter:off
        return PsiUtils.find(this, "/macro/macroSignature")
            .map(PsiUtils::toSingleLine)
            .orElse("Unknown");
    } // @formatter:on

    @Override
    public @NotNull Icon getIcon(final int flags) {
        return Icons.getIconWithModifier(Icons.MACRO, PsiUtils.findAll(this, "/macro/macroModifier").toList());
    }
}
