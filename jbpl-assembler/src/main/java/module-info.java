module dev.karmakrafts.jbpl.assembler {
    requires org.objectweb.asm;
    requires org.objectweb.asm.tree;
    requires org.jetbrains.annotations;
    requires org.antlr.antlr4.runtime;
    requires dev.karmakrafts.jbpl.frontend;

    exports dev.karmakrafts.jbpl.assembler;
    exports dev.karmakrafts.jbpl.assembler.parser;
    exports dev.karmakrafts.jbpl.assembler.util;
    exports dev.karmakrafts.jbpl.assembler.lower;
    exports dev.karmakrafts.jbpl.assembler.validation;
    exports dev.karmakrafts.jbpl.assembler.model;
    exports dev.karmakrafts.jbpl.assembler.model.decl;
    exports dev.karmakrafts.jbpl.assembler.model.expr;
    exports dev.karmakrafts.jbpl.assembler.model.source;
    exports dev.karmakrafts.jbpl.assembler.model.statement;
    exports dev.karmakrafts.jbpl.assembler.model.statement.instruction;
    exports dev.karmakrafts.jbpl.assembler.model.type;
    exports dev.karmakrafts.jbpl.assembler.model.element;
}