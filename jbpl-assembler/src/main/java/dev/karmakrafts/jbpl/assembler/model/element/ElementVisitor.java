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

package dev.karmakrafts.jbpl.assembler.model.element;

import dev.karmakrafts.jbpl.assembler.model.AssemblyFile;
import dev.karmakrafts.jbpl.assembler.model.decl.*;
import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.model.expr.IfExpr.ElseBranch;
import dev.karmakrafts.jbpl.assembler.model.expr.IfExpr.ElseIfBranch;
import dev.karmakrafts.jbpl.assembler.model.instruction.*;
import dev.karmakrafts.jbpl.assembler.model.statement.*;
import dev.karmakrafts.jbpl.assembler.util.Pair;
import org.jetbrains.annotations.NotNull;

public interface ElementVisitor {
    default @NotNull Element visitElement(final @NotNull Element element) {
        if (element instanceof AssemblyFile file) {
            return visitFile(file);
        }
        else if (element instanceof Declaration declaration) {
            return visitDeclaration(declaration);
        }
        else if (element instanceof Statement statement) {
            return visitStatement(statement);
        }
        throw new IllegalStateException("Unsupported element type");
    }

    default @NotNull AssemblyFile visitFile(final @NotNull AssemblyFile file) {
        final var transformedElements = file.transformChildren(this);
        file.clearElements();
        file.addElements(transformedElements);
        return file;
    }

    default <E extends ExprContainer> @NotNull E visitExprContainer(final @NotNull E container) {
        return visitElementContainer(container);
    }

    default <E extends ElementContainer> @NotNull E visitElementContainer(final @NotNull E container) {
        final var transformedElements = container.getElements().stream().map(this::visitElement).toList();
        container.clearElements();
        container.addElements(transformedElements);
        return container;
    }

    default <E extends StatementContainer> @NotNull E visitStatementContainer(final @NotNull E container) {
        return visitElementContainer(container);
    }

    default @NotNull Declaration visitDeclaration(final @NotNull Declaration declaration) {
        if (declaration instanceof InjectorDecl injectorDecl) {
            return visitInjector(injectorDecl);
        }
        else if (declaration instanceof FunctionDecl functionDecl) {
            return visitFunction(functionDecl);
        }
        else if (declaration instanceof FieldDecl fieldDecl) {
            return visitField(fieldDecl);
        }
        else if (declaration instanceof MacroDecl macroDecl) {
            return visitMacro(macroDecl);
        }
        else if (declaration instanceof PreproClassDecl preproClassDecl) {
            return visitPreproClass(preproClassDecl);
        }
        else if (declaration instanceof EmptyDecl emptyDecl) {
            return visitEmptyDeclaration(emptyDecl);
        }
        throw new IllegalStateException("Unsupported declaration type");
    }

    default @NotNull Declaration visitEmptyDeclaration(final @NotNull EmptyDecl declaration) {
        return declaration;
    }

    default @NotNull Declaration visitPreproClass(final @NotNull PreproClassDecl preproClassDecl) {
        return visitExprContainer(preproClassDecl);
    }

    default @NotNull Declaration visitMacro(final @NotNull MacroDecl macroDecl) {
        return visitElementContainer(macroDecl);
    }

    default @NotNull Declaration visitInjector(final @NotNull InjectorDecl injectorDecl) {
        injectorDecl.setTarget(visitExpr(injectorDecl.getTarget()));
        injectorDecl.setSelector(visitExpr(injectorDecl.getSelector()));
        return visitStatementContainer(injectorDecl);
    }

    default @NotNull Declaration visitFunction(final @NotNull FunctionDecl functionDecl) {
        functionDecl.setSignature(visitExpr(functionDecl.getSignature()));
        return visitStatementContainer(functionDecl);
    }

    default @NotNull Declaration visitField(final @NotNull FieldDecl fieldDecl) {
        return visitExprContainer(fieldDecl);
    }

    default @NotNull Expr visitExpr(final @NotNull Expr expr) {
        if (expr instanceof LiteralExpr literalExpr) {
            return visitLiteralExpr(literalExpr);
        }
        else if (expr instanceof SignatureExpr signatureExpr) {
            return visitSignatureExpr(signatureExpr);
        }
        else if (expr instanceof BinaryExpr binaryExpr) {
            return visitBinaryExpr(binaryExpr);
        }
        else if (expr instanceof UnaryExpr unaryExpr) {
            return visitUnaryExpr(unaryExpr);
        }
        else if (expr instanceof MacroCallExpr macroCallExpr) {
            return visitMacroCallExpr(macroCallExpr);
        }
        else if (expr instanceof StringLerpExpr stringLerpExpr) {
            return visitStringLerpExpr(stringLerpExpr);
        }
        else if (expr instanceof PreproClassExpr preproClassExpr) {
            return visitClassInstantiationExpr(preproClassExpr);
        }
        else if (expr instanceof ReferenceExpr referenceExpr) {
            return visitReferenceExpr(referenceExpr);
        }
        else if (expr instanceof TypeOfExpr typeOfExpr) {
            return visitTypeOfExpr(typeOfExpr);
        }
        else if (expr instanceof OpcodeOfExpr opcodeOfExpr) {
            return visitOpcodeOfExpr(opcodeOfExpr);
        }
        else if (expr instanceof IsExpr isExpr) {
            return visitIsExpr(isExpr);
        }
        else if (expr instanceof ArrayExpr arrayExpr) {
            return visitArrayExpr(arrayExpr);
        }
        else if (expr instanceof ArrayAccessExpr arrayGetExpr) {
            return visitArrayAccessExpr(arrayGetExpr);
        }
        else if (expr instanceof DefaultExpr defaultExpr) {
            return visitDefaultExpr(defaultExpr);
        }
        else if (expr instanceof AsExpr asExpr) {
            return visitAsExpr(asExpr);
        }
        else if (expr instanceof IfExpr ifExpr) {
            return visitIfExpr(ifExpr);
        }
        else if (expr instanceof InExpr inExpr) {
            return visitInExpr(inExpr);
        }
        else if (expr instanceof SizeOfExpr sizeOfExpr) {
            return visitSizeOfExpr(sizeOfExpr);
        }
        else if (expr instanceof WhenExpr whenExpr) {
            return visitWhenExpr(whenExpr);
        }
        else if (expr instanceof RangeExpr rangeExpr) {
            return visitRangeExpr(rangeExpr);
        }
        throw new IllegalStateException("Unsupported expression type");
    }

    default @NotNull Expr visitRangeExpr(final @NotNull RangeExpr rangeExpr) {
        return visitExprContainer(rangeExpr);
    }

    default @NotNull Expr visitWhenExpr(final @NotNull WhenExpr whenExpr) {
        final var transformedBranches = whenExpr.getBranches().stream().map(this::visitWhenBranch).toList();
        whenExpr.clearBranches();
        whenExpr.addBranches(transformedBranches);
        return visitExprContainer(whenExpr);
    }

    default @NotNull WhenExpr.Branch visitWhenBranch(final @NotNull WhenExpr.Branch branch) {
        if (branch instanceof WhenExpr.ConditionalBranch conditionalBranch) {
            return visitConditionalWhenBranch(conditionalBranch);
        }
        return visitElementContainer(branch);
    }

    default @NotNull WhenExpr.ConditionalBranch visitConditionalWhenBranch(final @NotNull WhenExpr.ConditionalBranch branch) {
        branch.setValue(visitExpr(branch.getValue()));
        return visitElementContainer(branch);
    }

    default @NotNull Expr visitSizeOfExpr(final @NotNull SizeOfExpr sizeOfExpr) {
        return visitExprContainer(sizeOfExpr);
    }

    default @NotNull Expr visitInExpr(final @NotNull InExpr inExpr) {
        return visitExprContainer(inExpr);
    }

    // For macro calls & prepro class instantiations
    default @NotNull Expr visitCallExpr(final @NotNull AbstractCallExpr callExpr) {
        // @formatter:off
        final var arguments = callExpr.getArguments().stream()
            .map(pair -> {
                final var name = pair.left();
                final var argument = pair.right();
                if(name != null) {
                    return new Pair<>(visitExpr(name), visitExpr(argument));
                }
                return new Pair<>((Expr)null, visitExpr(argument));
            })
            .toList();
        // @formatter:on
        callExpr.clearArguments();
        callExpr.addArguments(arguments);
        return visitExprContainer(callExpr);
    }

    default @NotNull Expr visitAsExpr(final @NotNull AsExpr asExpr) {
        return visitExprContainer(asExpr);
    }

    default @NotNull Expr visitIfExpr(final @NotNull IfExpr ifExpr) {
        final var elseIfBranches = ifExpr.getElseIfBranches().stream().map(this::visitElseIfBranch).toList();
        ifExpr.clearElseIfBranches();
        ifExpr.addElseIfBranches(elseIfBranches);
        final var elseBranch = ifExpr.getElseBranch();
        if (elseBranch != null) {
            ifExpr.setElseBranch(visitElseBranch(elseBranch));
        }
        return visitElementContainer(ifExpr);
    }

    default @NotNull ElseIfBranch visitElseIfBranch(final @NotNull ElseIfBranch branch) {
        return branch;
    }

    default @NotNull ElseBranch visitElseBranch(final @NotNull ElseBranch branch) {
        return branch;
    }

    default @NotNull Expr visitArrayAccessExpr(final @NotNull ArrayAccessExpr arrayAccessExpr) {
        return visitExprContainer(arrayAccessExpr);
    }

    default @NotNull Expr visitDefaultExpr(final @NotNull DefaultExpr defaultExpr) {
        return visitExprContainer(defaultExpr);
    }

    default @NotNull Expr visitArrayExpr(final @NotNull ArrayExpr arrayExpr) {
        return visitExprContainer(arrayExpr);
    }

    default @NotNull Expr visitIsExpr(final @NotNull IsExpr isExpr) {
        return visitExprContainer(isExpr);
    }

    default @NotNull Expr visitTypeOfExpr(final @NotNull TypeOfExpr typeOfExpr) {
        return visitExprContainer(typeOfExpr);
    }

    default @NotNull Expr visitOpcodeOfExpr(final @NotNull OpcodeOfExpr opcodeOfExpr) {
        return visitExprContainer(opcodeOfExpr);
    }

    default @NotNull Expr visitSignatureExpr(final @NotNull SignatureExpr signatureExpr) {
        if (signatureExpr instanceof FunctionSignatureExpr functionSignatureExpr) {
            return visitFunctionSignatureExpr(functionSignatureExpr);
        }
        else if (signatureExpr instanceof FieldSignatureExpr fieldSignatureExpr) {
            return visitFieldSignatureExpr(fieldSignatureExpr);
        }
        throw new IllegalStateException("Unsupported signature expression type");
    }

    default @NotNull Expr visitFunctionSignatureExpr(final @NotNull FunctionSignatureExpr functionSignatureExpr) {
        return functionSignatureExpr;
    }

    default @NotNull Expr visitFieldSignatureExpr(final @NotNull FieldSignatureExpr fieldSignatureExpr) {
        return fieldSignatureExpr;
    }

    default @NotNull Expr visitBinaryExpr(final @NotNull BinaryExpr binaryExpr) {
        return visitExprContainer(binaryExpr);
    }

    default @NotNull Expr visitUnaryExpr(final @NotNull UnaryExpr unaryExpr) {
        return visitExprContainer(unaryExpr);
    }

    default @NotNull Expr visitLiteralExpr(final @NotNull LiteralExpr literalExpr) {
        return literalExpr;
    }

    default @NotNull Expr visitStringLerpExpr(final @NotNull StringLerpExpr stringLerpExpr) {
        return visitExprContainer(stringLerpExpr);
    }

    default @NotNull Expr visitMacroCallExpr(final @NotNull MacroCallExpr macroCallExpr) {
        return visitCallExpr(macroCallExpr);
    }

    default @NotNull Expr visitClassInstantiationExpr(final @NotNull PreproClassExpr preproClassExpr) {
        return visitCallExpr(preproClassExpr);
    }

    default @NotNull Expr visitReferenceExpr(final @NotNull ReferenceExpr referenceExpr) {
        return visitExprContainer(referenceExpr);
    }

    default @NotNull Statement visitStatement(final @NotNull Statement statement) {
        if (statement instanceof IncludeStatement includeStatement) {
            return visitInclude(includeStatement);
        }
        else if (statement instanceof DefineStatement defineStatement) {
            return visitDefine(defineStatement);
        }
        else if (statement instanceof ReturnStatement returnStatement) {
            return visitReturnStatement(returnStatement);
        }
        else if (statement instanceof BreakStatement breakStatement) {
            return visitBreakStatement(breakStatement);
        }
        else if (statement instanceof AssertStatement assertStatement) {
            return visitAssertStatement(assertStatement);
        }
        else if (statement instanceof Instruction instruction) {
            return visitInstruction(instruction);
        }
        else if (statement instanceof NoopStatement noopStatement) {
            return visitNoopStatement(noopStatement);
        }
        else if (statement instanceof LabelStatement labelStatement) {
            return visitLabel(labelStatement);
        }
        else if (statement instanceof LocalStatement localStatement) {
            return visitLocal(localStatement);
        }
        else if (statement instanceof CompoundStatement compoundStatement) {
            return visitCompoundStatement(compoundStatement);
        }
        else if (statement instanceof YeetStatement yeetStatement) {
            return visitYeetStatement(yeetStatement);
        }
        else if (statement instanceof VersionStatement versionStatement) {
            return visitVersionStatement(versionStatement);
        }
        else if (statement instanceof InfoStatement infoStatement) {
            return visitInfoStatement(infoStatement);
        }
        else if (statement instanceof ErrorStatement errorStatement) {
            return visitErrorStatement(errorStatement);
        }
        else if (statement instanceof ForStatement forStatement) {
            return visitForStatement(forStatement);
        }
        else if (statement instanceof ContinueStatement continueStatement) {
            return visitContinueStatement(continueStatement);
        }
        else if (statement instanceof TypeAliasStatement typeAliasStatement) {
            return visitTypeAliasStatement(typeAliasStatement);
        }
        else if (statement instanceof Expr expr) {
            return visitExpr(expr);
        }
        throw new IllegalStateException(String.format("Unsupported statement type %s", statement.getClass()));
    }

    default @NotNull Statement visitTypeAliasStatement(final @NotNull TypeAliasStatement typeAliasStatement) {
        return visitExprContainer(typeAliasStatement);
    }

    default @NotNull Statement visitContinueStatement(final @NotNull ContinueStatement continueStatement) {
        return continueStatement;
    }

    default @NotNull Statement visitBreakStatement(final @NotNull BreakStatement breakStatement) {
        return breakStatement;
    }

    default @NotNull Statement visitAssertStatement(final @NotNull AssertStatement assertStatement) {
        return visitExprContainer(assertStatement);
    }

    default @NotNull Statement visitInfoStatement(final @NotNull InfoStatement infoStatement) {
        return visitExprContainer(infoStatement);
    }

    default @NotNull Statement visitErrorStatement(final @NotNull ErrorStatement errorStatement) {
        return visitExprContainer(errorStatement);
    }

    default @NotNull Statement visitVersionStatement(final @NotNull VersionStatement versionStatement) {
        return visitExprContainer(versionStatement);
    }

    default @NotNull Statement visitCompoundStatement(final @NotNull CompoundStatement compoundStatement) {
        return visitElementContainer(compoundStatement);
    }

    default @NotNull Statement visitLocal(final @NotNull LocalStatement localStatement) {
        return visitExprContainer(localStatement);
    }

    default @NotNull Statement visitLabel(final @NotNull LabelStatement labelStatement) {
        return labelStatement;
    }

    default @NotNull Statement visitNoopStatement(final @NotNull NoopStatement statement) {
        return statement;
    }

    default @NotNull Statement visitYeetStatement(final @NotNull YeetStatement yeetStatement) {
        return visitExprContainer(yeetStatement);
    }

    default @NotNull Statement visitReturnStatement(final @NotNull ReturnStatement returnStatement) {
        return visitExprContainer(returnStatement);
    }

    default @NotNull Statement visitForStatement(final @NotNull ForStatement forStatement) {
        return visitElementContainer(forStatement);
    }

    default @NotNull Statement visitDefine(final @NotNull DefineStatement defineStatement) {
        return visitExprContainer(defineStatement);
    }

    default @NotNull Statement visitInclude(final @NotNull IncludeStatement includeStatement) {
        return includeStatement;
    }

    default @NotNull Instruction visitInstruction(final @NotNull Instruction instruction) {
        if (instruction instanceof StackInstruction stackInstruction) {
            return visitStackInstruction(stackInstruction);
        }
        else if (instruction instanceof LoadConstantInstruction loadConstantInstruction) {
            return visitLoadConstantInstruction(loadConstantInstruction);
        }
        else if (instruction instanceof OplessInstruction oplessInstruction) {
            return visitOplessInstruction(oplessInstruction);
        }
        else if (instruction instanceof InvokeInstruction invokeInstruction) {
            return visitInvokeInstruction(invokeInstruction);
        }
        else if (instruction instanceof InvokeDynamicInstruction invokeDynamicInstruction) {
            return visitInvokeDynamicInstruction(invokeDynamicInstruction);
        }
        else if (instruction instanceof FieldInstruction fieldInstruction) {
            return visitFieldInstruction(fieldInstruction);
        }
        else if (instruction instanceof JumpInstruction jumpInstruction) {
            return visitJumpInstruction(jumpInstruction);
        }
        throw new IllegalStateException("Unsupported instruction type");
    }

    default @NotNull Instruction visitInvokeDynamicInstruction(final @NotNull InvokeDynamicInstruction instruction) {
        return visitExprContainer(instruction);
    }

    default @NotNull Instruction visitInvokeInstruction(final @NotNull InvokeInstruction instruction) {
        return visitExprContainer(instruction);
    }

    default @NotNull Instruction visitOplessInstruction(final @NotNull OplessInstruction instruction) {
        return instruction;
    }

    default @NotNull Instruction visitStackInstruction(final @NotNull StackInstruction instruction) {
        return visitExprContainer(instruction);
    }

    default @NotNull Instruction visitLoadConstantInstruction(final @NotNull LoadConstantInstruction instruction) {
        return visitExprContainer(instruction);
    }

    default @NotNull Instruction visitFieldInstruction(final @NotNull FieldInstruction instruction) {
        return visitExprContainer(instruction);
    }

    default @NotNull Instruction visitJumpInstruction(final @NotNull JumpInstruction instruction) {
        return visitExprContainer(instruction);
    }
}
