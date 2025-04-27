package com.example.response_time_analyzer.model;

import java.util.*;

public class Resource {
    private String name; // Resource name
    private Map<Task, Integer> usageTimes; // Task -> usage time of this resource by that task
    private int ceilingPriority; // Calculated from tasks that use the resource

    public Resource(String name) {
        this.name = name;
        this.usageTimes = new HashMap<>();
        this.ceilingPriority = -1; // Default value
    }

    /**
     * Add or update the usage time of the resource by a specific task.
     */
    public void addUsage(Task task, int usageTime) {
        usageTimes.put(task, usageTime);
    }

    /**
     * Get the resource name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the usage times mapping.
     */
    public Map<Task, Integer> getUsageTimes() {
        return usageTimes;
    }

    /**
     * Get the maximum usage time among all tasks using this resource.
     */
    public int getMaxUsageTime() {
        return usageTimes.values().stream().max(Integer::compareTo).orElse(0);
    }

    /**
     * Calculate the ceiling priority of the resource.
     * The ceiling is the highest priority among all tasks that use this resource.
     */
    public void calculateCeiling() {
        this.ceilingPriority = 0; // Start assuming no tasks are using it

        for (Map.Entry<Task, Integer> entry : usageTimes.entrySet()) {
            Task task = entry.getKey();
            // Update ceiling if a higher priority is found
            if (task.getPriority() > this.ceilingPriority) {
                this.ceilingPriority = task.getPriority();
            }
        }
    }

    /**
     * Get the calculated ceiling priority of the resource.
     */
    public int getCeilingPriority() {
        return ceilingPriority;
    }
}
