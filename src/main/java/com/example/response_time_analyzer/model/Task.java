package com.example.response_time_analyzer.model;

/**
 * Represents a real-time task with computation time, period, and optional deadline and priority.
 */
public class Task {
    private String name;          // Task name
    private Integer priority;     // Assigned priority (may be null initially)
    private int C;                // Computation time
    private int T;                // Period
    private Integer D;            // Deadline (can be equal to period or different)

    /**
     * Full constructor to initialize all fields.
     */
    public Task(String name, Integer priority, int c, int t, Integer d) {
        this.name = name;
        this.priority = priority;
        this.C = c;
        this.T = t;
        this.D = d;
    }

    /**
     * Constructor for simple tasks without specified priority or deadline.
     */
    public Task(String name, int C, int T) {
        this(name, null, C, T, null);
    }

    /**
     * Constructor for tasks with explicit deadline but no initial priority.
     */
    public Task(String name, int C, int T, int D) {
        this(name, null, C, T, D);
    }

    // Getters

    public String getName() {
        return name;
    }

    public Integer getPriority() {
        return priority;
    }

    public int getC() {
        return C;
    }

    public int getT() {
        return T;
    }

    public Integer getD() {
        return D;
    }

    // Setters

    /**
     * Set the priority for this task.
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
