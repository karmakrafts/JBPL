package dev.karmakrafts.jbpl.assembler.model;

import dev.karmakrafts.jbpl.assembler.model.decl.*;
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl.InstructionCondition;
import dev.karmakrafts.jbpl.assembler.model.decl.SelectorDecl.OpcodeCondition;
import dev.karmakrafts.jbpl.assembler.model.expr.*;
import dev.karmakrafts.jbpl.assembler.model.statement.*;
import dev.karmakrafts.jbpl.assembler.model.statement.instruction.*;
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
        final var transformedExpressions = container.getExpressions().stream().map(this::visitExpr).toList();
        container.clearExpressions();
        container.addExpressions(transformedExpressions);
        return container;
    }

    default <E extends ElementContainer> @NotNull E visitElementContainer(final @NotNull E container) {
        final var transformedElements = container.getElements().stream().map(this::visitElement).toList();
        container.clearElements();
        container.addElements(transformedElements);
        return container;
    }

    default <E extends StatementContainer> @NotNull E visitStatementContainer(final @NotNull E container) {
        final var transformedStatements = container.getStatements().stream().map(this::visitStatement).toList();
        container.clearStatements();
        container.addStatements(transformedStatements);
        return container;
    }

    default @NotNull Declaration visitDeclaration(final @NotNull Declaration declaration) {
        if (declaration instanceof InjectorDecl injectorDecl) {
            return visitInjector(injectorDecl);
        }
        else if (declaration instanceof SelectorDecl selectorDecl) {
            return visitSelector(selectorDecl);
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
        injectorDecl.setTarget(visitFunctionSignatureExpr(injectorDecl.getTarget()));
        injectorDecl.setSelector(visitExpr(injectorDecl.getSelector()));
        return visitStatementContainer(injectorDecl);
    }

    default @NotNull Declaration visitSelector(final @NotNull SelectorDecl selectorDecl) {
        selectorDecl.offset = visitExpr(selectorDecl.offset);
        // @formatter:off
        final var transformedConditions = selectorDecl.conditions.stream()
            .map(this::visitSelectorCondition)
            .toList();
        // @formatter:on
        selectorDecl.conditions.clear();
        selectorDecl.conditions.addAll(transformedConditions);
        return selectorDecl;
    }

    default @NotNull SelectorDecl.Condition visitSelectorCondition(final @NotNull SelectorDecl.Condition condition) {
        if (condition instanceof InstructionCondition instructionCondition) {
            return visitInstructionSelectorCondition(instructionCondition);
        }
        else if (condition instanceof OpcodeCondition opcodeCondition) {
            return visitOpcodeSelectorCondition(opcodeCondition);
        }
        throw new IllegalStateException("Unsupported selector condition type");
    }

    default @NotNull SelectorDecl.InstructionCondition visitInstructionSelectorCondition(final @NotNull SelectorDecl.InstructionCondition instructionCondition) {
        instructionCondition.instruction = visitInstruction(instructionCondition.instruction);
        return instructionCondition;
    }

    default @NotNull SelectorDecl.OpcodeCondition visitOpcodeSelectorCondition(final @NotNull SelectorDecl.OpcodeCondition opcodeCondition) {
        return opcodeCondition;
    }

    default @NotNull Declaration visitFunction(final @NotNull FunctionDecl functionDecl) {
        functionDecl.setSignature(visitFunctionSignatureExpr(functionDecl.getSignature()));
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
        else if (expr instanceof ClassInstantiationExpr classInstantiationExpr) {
            return visitClassInstantiationExpr(classInstantiationExpr);
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
        else if (expr instanceof SelectorReferenceExpr selectorReferenceExpr) {
            return visitSelectorReferenceExpr(selectorReferenceExpr);
        }
        throw new IllegalStateException("Unsupported expression type");
    }

    default @NotNull Expr visitSelectorReferenceExpr(final @NotNull SelectorReferenceExpr selectorReferenceExpr) {
        return selectorReferenceExpr;
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

    default @NotNull SignatureExpr visitSignatureExpr(final @NotNull SignatureExpr signatureExpr) {
        if (signatureExpr instanceof FunctionSignatureExpr functionSignatureExpr) {
            return visitFunctionSignatureExpr(functionSignatureExpr);
        }
        else if (signatureExpr instanceof FieldSignatureExpr fieldSignatureExpr) {
            return visitFieldSignatureExpr(fieldSignatureExpr);
        }
        throw new IllegalStateException("Unsupported signature expression type");
    }

    default @NotNull FunctionSignatureExpr visitFunctionSignatureExpr(final @NotNull FunctionSignatureExpr functionSignatureExpr) {
        return functionSignatureExpr;
    }

    default @NotNull FieldSignatureExpr visitFieldSignatureExpr(final @NotNull FieldSignatureExpr fieldSignatureExpr) {
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
        return visitExprContainer(macroCallExpr);
    }

    default @NotNull Expr visitClassInstantiationExpr(final @NotNull ClassInstantiationExpr classInstantiationExpr) {
        return visitExprContainer(classInstantiationExpr);
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
        else if (statement instanceof Expr expr) {
            return visitExpr(expr);
        }
        throw new IllegalStateException(String.format("Unsupported statement type %s", statement.getClass()));
    }

    default @NotNull Statement visitVersionStatement(final @NotNull VersionStatement versionStatement) {
        return versionStatement;
    }

    default @NotNull Statement visitCompoundStatement(final @NotNull CompoundStatement compoundStatement) {
        return visitElementContainer(compoundStatement);
    }

    default @NotNull Statement visitLocal(final @NotNull LocalStatement localStatement) {
        return localStatement;
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
}
