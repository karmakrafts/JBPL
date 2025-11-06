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
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class FieldNode extends ANTLRPsiNode implements StructuralPsiElement {
    public FieldNode(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Icon getStructureIcon() {
        return Nodes.Field;
    }

    @Override
    public @NotNull String getName() { // @formatter:off
        return PsiUtils.find(this, "/field/fieldSignature", FieldSignatureNode.class)
            .map(FieldSignatureNode::getName)
            .orElse("Unknown");
    } // @formatter:on

    @Override
    public @NotNull String getDetailedStructureText() {
        // @formatter:off
        final var signature = PsiUtils.find(this, "/field/fieldSignature")
            .map(PsiUtils::toSingleLine)
            .or(() -> PsiUtils.find(this, "/field/explicitReference", ExplicitReferenceNode.class)
                .map(ExplicitReferenceNode::getName))
            .orElse("Unknown");
        final var value = PsiUtils.find(this, "/field/expr")
            .map(PsiUtils::toSingleLine)
            .orElse("<uninitialized>");
        // @formatter:on
        return String.format("%s = %s", signature, value);
    }
}
