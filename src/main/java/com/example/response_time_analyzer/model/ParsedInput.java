package com.example.response_time_analyzer.model;

import java.util.List;
import java.util.Map;

public class ParsedInput {
    private List<Task> tasks;
    private String mode;
    private Map<String, Resource> resourceMap; // puede ser null

    public ParsedInput(List<Task> tasks, String mode) {
        this(tasks, mode, null);
    }

    public ParsedInput(List<Task> tasks, String mode, Map<String, Resource> resourceMap) {
        this.tasks = tasks;
        this.mode = mode;
        this.resourceMap = resourceMap;
    }

    public List<Task> getTasks() { return tasks; }
    public String getMode() { return mode; }
    public Map<String, Resource> getResourceMap() { return resourceMap; }
}

