package dev.karmakrafts.jbpl.assembler.model;

public interface ScopeOwner {
    default boolean mergeFrameInstructionsOnFrameExit() {
        return false;
    }
}
