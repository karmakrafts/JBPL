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

import com.intellij.icons.AllIcons.Nodes;
import com.intellij.lang.ASTNode;
import dev.karmakrafts.jbpl.intellij.util.Icons;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class FunctionNode extends JBPLPsiNode implements StructuralPsiElement {
    public FunctionNode(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Icon getIcon(final int flags) {
        return Icons.getIconWithModifier(Nodes.Method, PsiUtils.findAll(this, "/function/accessModifier").toList());
    }

    @Override
    public String getName() { // @formatter:off
        return PsiUtils.find(this, "/function/functionSignature", FunctionSignatureNode.class)
            .map(FunctionSignatureNode::getName)
            .orElse("Unknown");
    } // @formatter:on

    @Override
    public @NotNull String getDetailedStructureText() { // @formatter:off
        return PsiUtils.find(this, "/function/functionSignature")
            .map(PsiUtils::toSingleLine)
            .orElse("Unknown");
    } // @formatter:on
}
