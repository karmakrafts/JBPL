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
import dev.karmakrafts.jbpl.intellij.util.Icons;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class YeetStatementNode extends JBPLPsiNode implements StructuralPsiElement {
    public YeetStatementNode(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Icon getStructureIcon() {
        return Icons.YEET;
    }

    @Override
    public @NotNull String getName() { // @formatter:off
        return PsiUtils.find(this, "/yeetStatement/functionSignature", FunctionSignatureNode.class)
            .map(FunctionSignatureNode::getName)
            .or(() -> PsiUtils.find(this, "/yeetStatement/fieldSignature", FieldSignatureNode.class)
                .map(FieldSignatureNode::getName))
            .or(() -> PsiUtils.find(this, "/yeetStatement/classType", ClassTypeNode.class)
                .map(ClassTypeNode::getName))
            .orElse("Unknown");
    } // @formatter:on

    @Override
    public @NotNull String getDetailedStructureText() { // @formatter:off
        return PsiUtils.find(this, "/yeetStatement/functionSignature")
            .or(() -> PsiUtils.find(this, "/yeetStatement/fieldSignature"))
            .or(() -> PsiUtils.find(this, "/yeetStatement/classType"))
            .map(PsiUtils::toSingleLine)
            .orElse("Unknown");
    } // @formatter:on
}
