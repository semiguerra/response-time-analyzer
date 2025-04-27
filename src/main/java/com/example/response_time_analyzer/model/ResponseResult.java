package com.example.response_time_analyzer.model;

public class ResponseResult {
    private final String taskName;
    private final int period;
    private final int deadline;
    private final int computation;
    private final int priority; // Campo añadido
    private final Integer blockingTime;
    private final Integer ceilingBlockingTime;
    private final int responseTime;
    private final Integer inheritedResponseTime;
    private final Integer ceilingResponseTime;
    private final boolean schedulable;

    // Constructor para análisis básico
    public ResponseResult(String taskName, int period, int deadline,
            int computation, int priority, int responseTime,
            boolean schedulable) {
        this.taskName = taskName;
        this.period = period;
        this.deadline = deadline;
        this.computation = computation;
        this.priority = priority;
        this.blockingTime = null;
        this.ceilingBlockingTime = null;
        this.responseTime = responseTime;
        this.inheritedResponseTime = null;
        this.ceilingResponseTime = null;
        this.schedulable = schedulable;
    }

    // Constructor para análisis con recursos
    public ResponseResult(String taskName, int period, int deadline,
            int computation, int priority, int blockingTime,
            int ceilingBlockingTime, int responseTime,
            int inheritedResponseTime, int ceilingResponseTime) {
        this.taskName = taskName;
        this.period = period;
        this.deadline = deadline;
        this.computation = computation;
        this.priority = priority;
        this.blockingTime = blockingTime;
        this.ceilingBlockingTime = ceilingBlockingTime;
        this.responseTime = responseTime;
        this.inheritedResponseTime = inheritedResponseTime;
        this.ceilingResponseTime = ceilingResponseTime;
        this.schedulable = responseTime <= deadline;
    }


    // Getters
    public int getPriority() {
        return priority;
    }

    public String getTaskName() {
        return taskName;
    }

    public int getPeriod() {
        return period;
    }

    public int getDeadline() {
        return deadline;
    }

    public int getComputation() {
        return computation;
    }

    public Integer getBlockingTime() {
        return blockingTime;
    }

    public Integer getCeilingBlockingTime() {
        return ceilingBlockingTime;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public Integer getInheritedResponseTime() {
        return inheritedResponseTime;
    }

    public Integer getCeilingResponseTime() {
        return ceilingResponseTime;
    }

    public boolean isSchedulable() {
        return schedulable;
    }

    public boolean isInheritedSchedulable() {
        return inheritedResponseTime != null && inheritedResponseTime <= deadline;
    }

    public boolean isCeilingSchedulable() {
        return ceilingResponseTime != null && ceilingResponseTime <= deadline;
    }

    public boolean hasResourceData() {
        return blockingTime != null && ceilingBlockingTime != null &&
                inheritedResponseTime != null && ceilingResponseTime != null;
    }
}