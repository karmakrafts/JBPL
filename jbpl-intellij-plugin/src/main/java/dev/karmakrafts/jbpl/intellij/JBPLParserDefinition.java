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

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import dev.karmakrafts.jbpl.frontend.JBPLLexer;
import dev.karmakrafts.jbpl.frontend.JBPLParser;
import dev.karmakrafts.jbpl.intellij.psi.*;
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor;
import org.antlr.intellij.adaptor.lexer.PSIElementTypeFactory;
import org.antlr.intellij.adaptor.lexer.RuleIElementType;
import org.antlr.intellij.adaptor.lexer.TokenIElementType;
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class JBPLParserDefinition implements ParserDefinition {
    public static final IFileElementType FILE_TYPE = new IFileElementType(JBPLanguage.INSTANCE);
    private static final List<TokenIElementType> TOKEN_TYPES;
    private static final TokenSet COMMENTS;
    private static final TokenSet STRING_LITERALS;
    private static final TokenSet WHITESPACE;

    static {
        ensureTokenTypesRegistered();
        TOKEN_TYPES = PSIElementTypeFactory.getTokenIElementTypes(JBPLanguage.INSTANCE);
        COMMENTS = PSIElementTypeFactory.createTokenSet(JBPLanguage.INSTANCE,
            JBPLLexer.LINE_COMMENT,
            JBPLLexer.BLOCK_COMMENT);
        STRING_LITERALS = PSIElementTypeFactory.createTokenSet(JBPLanguage.INSTANCE, JBPLLexer.M_CONST_STR_TEXT);
        WHITESPACE = PSIElementTypeFactory.createTokenSet(JBPLanguage.INSTANCE, JBPLLexer.WS, JBPLLexer.NL);
    }

    public static @NotNull TokenIElementType getTokenType(final int index) {
        return TOKEN_TYPES.get(index);
    }

    public static void ensureTokenTypesRegistered() {
        PSIElementTypeFactory.defineLanguageIElementTypes(JBPLanguage.INSTANCE,
            JBPLParser.tokenNames,
            JBPLParser.ruleNames);
    }

    @Override
    public @NotNull Lexer createLexer(final @NotNull Project project) {
        return new ANTLRLexerAdaptor(JBPLanguage.INSTANCE, new JBPLLexer(null));
    }

    @Override
    public @NotNull PsiParser createParser(final @NotNull Project project) {
        return new ANTLRParserAdaptor(JBPLanguage.INSTANCE, new JBPLParser(null)) {
            @Override
            protected @NotNull ParseTree parse(final @NotNull Parser parser, final @NotNull IElementType root) {
                if (root instanceof IFileElementType) {
                    return ((JBPLParser) parser).file();
                }
                return ((JBPLParser) parser).statement(); // Assume everything else is a statement
            }
        };
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE_TYPE;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return STRING_LITERALS;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return WHITESPACE;
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(final @NotNull ASTNode left,
                                                                      final @NotNull ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @Override
    public @NotNull PsiElement createElement(final @NotNull ASTNode node) {
        final var elementType = node.getElementType();
        if (elementType instanceof TokenIElementType tokenType) {
            return new JBPLPsiLeafNode(tokenType, node.getText());
        }
        if (!(elementType instanceof RuleIElementType ruleType)) {
            return new JBPLPsiNode(node);
        }
        return switch (ruleType.getRuleIndex()) { // @formatter:off
            case JBPLParser.RULE_define -> new DefineNode(node);
            case JBPLParser.RULE_reference -> new ReferenceNode(node);
            case JBPLParser.RULE_classType -> new ClassTypeNode(node);
            case JBPLParser.RULE_macro -> new MacroNode(node);
            case JBPLParser.RULE_parameter -> new ParameterNode(node);
            case JBPLParser.RULE_macroCall -> new MacroCallNode(node);
            case JBPLParser.RULE_wrappedExpr -> new WrappedExprNode(node);
            case JBPLParser.RULE_stringSegment -> new StringSegmentNode(node);
            case JBPLParser.RULE_exprOrName -> new ExprOrNameNode(node);
            case JBPLParser.RULE_exprOrType -> new ExprOrTypeNode(node);
            case JBPLParser.RULE_fieldSignature -> new FieldSignatureNode(node);
            case JBPLParser.RULE_nameSegment -> new NameSegmentNode(node);
            case JBPLParser.RULE_functionName -> new FunctionNameNode(node);
            case JBPLParser.RULE_intLiteral, JBPLParser.RULE_floatLiteral -> new NumberNode(node);
            case JBPLParser.RULE_expr -> new ExpressionNode(node);
            case JBPLParser.RULE_statement -> new StatementNode(node);
            case JBPLParser.RULE_function -> new FunctionNode(node);
            case JBPLParser.RULE_field -> new FieldNode(node);
            case JBPLParser.RULE_injector -> new InjectorNode(node);
            case JBPLParser.RULE_selector -> new SelectorNode(node);
            case JBPLParser.RULE_functionSignature -> new FunctionSignatureNode(node);
            case JBPLParser.RULE_preproClass -> new PreproClassNode(node);
            case JBPLParser.RULE_yeetStatement -> new YeetStatementNode(node);
            case JBPLParser.RULE_signatureOwner -> new SignatureOwnerNode(node);
            case JBPLParser.RULE_type -> new TypeNode(node);
            case JBPLParser.RULE_typeLiteral -> new TypeLiteralNode(node);
            case JBPLParser.RULE_opcodeLiteral -> new OpcodeLiteralNode(node);
            case JBPLParser.RULE_versionStatement -> new VersionStatementNode(node);
            case JBPLParser.RULE_local -> new LocalStatement(node);
            case JBPLParser.RULE_functionSignatureParameter -> new FunctionSignatureParameterNode(node);
            default -> new JBPLPsiNode(node);
        }; // @formatter:on
    }

    @Override
    public @NotNull PsiFile createFile(final @NotNull FileViewProvider fileViewProvider) {
        return new JBPLFile(fileViewProvider);
    }
}
