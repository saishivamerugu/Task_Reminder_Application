package com.taskreminder.app.enums;

public enum TaskPriority {

    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low");

    public final String label;

    TaskPriority(String label) {
        this.label = label;
    }
}