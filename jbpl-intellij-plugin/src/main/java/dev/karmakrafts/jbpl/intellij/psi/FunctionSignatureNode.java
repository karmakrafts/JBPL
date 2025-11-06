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
import com.intellij.psi.PsiElement;
import dev.karmakrafts.jbpl.intellij.util.PsiUtils;
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public final class FunctionSignatureNode extends ANTLRPsiNode {
    public FunctionSignatureNode(final @NotNull ASTNode node) {
        super(node);
    }

    public @NotNull String getOwnerName() { // @formatter:off
        return PsiUtils.find(this, "/functionSignature/signatureOwner/classType")
            .map(type -> PsiUtils.findAll(type, "/classType/nameSegment")
                .map(PsiElement::getText)
                .collect(Collectors.joining(".")))
            .or(() -> PsiUtils.find(this, "/functionSignature/signatureOwner/reference", ReferenceNode.class).map(ReferenceNode::getName))
            .orElse("Unknown");
    } // @formatter:on

    @Override
    public @NotNull String getName() { // @formatter:off
        return PsiUtils.find(this, "/functionSignature/functionName", FunctionNameNode.class)
            .map(FunctionNameNode::getName)
            .orElse("Unknown");
    } // @formatter:on
}
