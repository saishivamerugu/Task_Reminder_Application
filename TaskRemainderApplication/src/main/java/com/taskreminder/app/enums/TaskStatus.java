package com.taskreminder.app.enums;

public enum TaskStatus {

    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    public final String label;

    TaskStatus(String label) {
        this.label = label;
    }
}