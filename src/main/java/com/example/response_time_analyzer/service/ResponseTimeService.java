package com.example.response_time_analyzer.service;

import com.example.response_time_analyzer.model.ParsedInput;
import com.example.response_time_analyzer.model.Resource;
import com.example.response_time_analyzer.model.ResponseResult;
import com.example.response_time_analyzer.model.Task;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class ResponseTimeService {

    /**
     * Main method to process the CSV input and dispatch to the correct calculation algorithm.
     */
    public List<ResponseResult> calculate(MultipartFile file) {
        ParsedInput input = parseCSV(file);
        List<Task> tasks = input.getTasks();
        String mode = input.getMode();

        // Select scheduling policy based on mode
        if (mode.equals("RESOURCES")) {
            // Sort tasks by deadline (DMS policy) if available, otherwise by period (ARM fallback)
            if (tasks.get(0).getD() != null) {
                tasks.sort(Comparator.comparingInt(Task::getD));
            } else {
                tasks.sort(Comparator.comparingInt(Task::getT));
            }
            // Assign priorities (higher value = higher priority)
            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).setPriority(tasks.size() - i);
            }
            return calculateWithResources(tasks, input.getResourceMap());
        }

        // If mode is ARM or DMS without resources
        if (mode.equals("DMS")) {
            tasks.sort(Comparator.comparingInt(Task::getD));
        } else { // ARM
            tasks.sort(Comparator.comparingInt(Task::getT));
        }

        // Assign priorities
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setPriority(tasks.size() - i);
        }

        return calculateResponseTimeWithoutResources(tasks, mode);
    }

    /**
     * Calculate response times for tasks without considering shared resources.
     */
    private List<ResponseResult> calculateResponseTimeWithoutResources(List<Task> tasks, String mode) {
        List<ResponseResult> results = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            Task currentTask = tasks.get(i);
            List<Task> higherPriorityTasks = tasks.subList(0, i);

            // Compute response time of the current task
            int responseTime = calculateResponseTime(currentTask, higherPriorityTasks);
            int deadline = currentTask.getD() != null ? currentTask.getD() : currentTask.getT();
            boolean schedulable = responseTime <= deadline;

            results.add(new ResponseResult(
                currentTask.getName(),
                currentTask.getT(),
                deadline,
                currentTask.getC(),
                currentTask.getPriority(),
                responseTime,
                schedulable
            ));
        }

        return results;
    }

    /**
     * Calculate the worst-case response time of a task under fixed priority preemptive scheduling.
     */
    private int calculateResponseTime(Task task, List<Task> higherPriorityTasks) {
        int currentResponseTime = task.getC();
        int nextResponseTime = currentResponseTime;
        boolean converged = false;
        final int MAX_ITERATIONS = 1000; // Prevent infinite loops

        int iterations = 0;
        while (!converged && iterations < MAX_ITERATIONS) {
            iterations++;
            int totalInterference = 0;

            // Sum interference from higher priority tasks
            for (Task hpTask : higherPriorityTasks) {
                totalInterference += (int) Math.ceil((double) currentResponseTime / hpTask.getT()) * hpTask.getC();
            }

            // Update response time
            nextResponseTime = task.getC() + totalInterference;

            // Check convergence
            if (nextResponseTime == currentResponseTime) {
                converged = true;
            } else {
                int deadlineOrPeriod = task.getD() != null ? task.getD() : task.getT();
                if (nextResponseTime > deadlineOrPeriod) {
                    converged = true; // Unschedulable case
                } else {
                    currentResponseTime = nextResponseTime; // Continue iterations
                }
            }
        }

        return nextResponseTime;
    }

    /**
     * Calculate response times considering blocking from shared resources.
     */
    private List<ResponseResult> calculateWithResources(List<Task> tasks, Map<String, Resource> resourceMap) {
        List<ResponseResult> results = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            Task currentTask = tasks.get(i);
            List<Task> higherPriorityTasks = tasks.subList(0, i);

            // Step 1: Base response time without blocking
            int baseResponseTime = calculateResponseTime(currentTask, higherPriorityTasks);

            // Step 2: Compute blocking under both protocols
            int blockingInheritance = calculateBlockingInheritance(currentTask, resourceMap);
            int blockingCeiling = calculateBlockingCeiling(currentTask, resourceMap);

            // Step 3: Response times with blocking
            int responseWithInheritance = calculateResponseWithBlocking(currentTask, higherPriorityTasks, blockingInheritance);
            int responseWithCeiling = calculateResponseWithBlocking(currentTask, higherPriorityTasks, blockingCeiling);

            results.add(new ResponseResult(
                currentTask.getName(),
                currentTask.getT(),
                currentTask.getD(),
                currentTask.getC(),
                currentTask.getPriority(),
                blockingInheritance,
                blockingCeiling,
                baseResponseTime,
                responseWithInheritance,
                responseWithCeiling
            ));
        }

        return results;
    }

    /**
     * Calculate blocking time under Priority Inheritance Protocol.
     */
    private int calculateBlockingInheritance(Task task, Map<String, Resource> resourceMap) {
        int totalBlocking = 0;
        int currentPriority = task.getPriority();

        if (currentPriority == 1) {
            return 0; // Lowest priority task experiences no blocking
        }

        // Analyze each resource
        for (Resource resource : resourceMap.values()) {
            Map<Task, Integer> usageTimes = resource.getUsageTimes();
            boolean hasLowerPriorityUser = false;
            boolean hasHigherOrEqualPriorityUser = false;
            int maxUsageTime = 0;

            // Check tasks using the resource
            for (Map.Entry<Task, Integer> entry : usageTimes.entrySet()) {
                Task userTask = entry.getKey();
                int usageTime = entry.getValue();

                maxUsageTime = Math.max(maxUsageTime, usageTime);

                if (userTask.getPriority() < currentPriority) {
                    hasLowerPriorityUser = true;
                }
                if (userTask.getPriority() >= currentPriority) {
                    hasHigherOrEqualPriorityUser = true;
                }
            }

            // If both conditions are satisfied, consider blocking
            if (hasLowerPriorityUser && hasHigherOrEqualPriorityUser) {
                totalBlocking += maxUsageTime;
            }
        }

        return totalBlocking;
    }

    /**
     * Calculate blocking time under Priority Ceiling Protocol.
     */
    private int calculateBlockingCeiling(Task task, Map<String, Resource> resourceMap) {
        int maxBlocking = 0;
        int currentPriority = task.getPriority();

        if (currentPriority == 1) {
            return 0;
        }

        for (Resource resource : resourceMap.values()) {
            resource.calculateCeiling();
            int ceilingPriority = resource.getCeilingPriority();

            if (ceilingPriority >= currentPriority) {
                int resourceBlocking = 0;
                boolean foundLowerPriorityUser = false;

                // Find maximum usage time among lower priority tasks
                for (Map.Entry<Task, Integer> entry : resource.getUsageTimes().entrySet()) {
                    Task userTask = entry.getKey();
                    int usageTime = entry.getValue();

                    if (userTask.getPriority() < currentPriority) {
                        foundLowerPriorityUser = true;
                        resourceBlocking = Math.max(resourceBlocking, usageTime);
                    }
                }
    
                if (foundLowerPriorityUser) {
                    // Blocking = worst use of ALL resource (not only minors)
                    Collection<Integer> usos = resource.getUsageTimes().values();
                    int tMax = usos.isEmpty() ? 0 : Collections.max(usos);
                    maxBlocking = Math.max(maxBlocking, tMax);
                }
            }
        }

        return maxBlocking;
    }

    /**
     * Calculate response time including blocking.
     */
    private int calculateResponseWithBlocking(Task task, List<Task> higherPriorityTasks, int blockingTime) {
        int currentResponseTime = task.getC() + blockingTime;
        int nextResponseTime = currentResponseTime;
        boolean converged = false;

        while (!converged) {
            int totalInterference = 0;

            // Sum interference from higher priority tasks
            for (Task hpTask : higherPriorityTasks) {
                totalInterference += Math.ceil((double) currentResponseTime / hpTask.getT()) * hpTask.getC();
            }

            nextResponseTime = task.getC() + blockingTime + totalInterference;

            // Stop if response time converges or exceeds the period
            if (nextResponseTime == currentResponseTime || nextResponseTime > task.getT()) {
                converged = true;
            } else {
                currentResponseTime = nextResponseTime;
            }
        }

        return nextResponseTime;
    }

    /**
     * Parse the input CSV file and extract tasks and resources information.
     */
    private ParsedInput parseCSV(MultipartFile file) {
        List<Task> tasks = new ArrayList<>();
        Map<String, Resource> resourceMap = new HashMap<>();
        String mode = "ARM";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            String header = headerLine.toLowerCase();
            boolean simpleFormat = header.contains("proceso") && header.contains("prioridad") 
                                && header.contains("c") && header.contains("t");
            boolean extendedFormat = header.contains("task") && (header.contains("period") || header.contains("deadline"));

            boolean hasDeadlines = false;
            boolean hasResources = false;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    throw new IllegalArgumentException("Invalid line format: " + line);
                }

                if (simpleFormat) {
                    // Simple format: Proceso, Prioridad, C, T
                    String name = parts[0].trim();
                    int priority = Integer.parseInt(parts[1].trim());
                    int C = Integer.parseInt(parts[2].trim());
                    int T = Integer.parseInt(parts[3].trim());

                    tasks.add(new Task(name, priority, C, T, T));
                    mode = "ARM";
                } else if (extendedFormat) {
                    // Extended format: Task, Period, Deadline, C, Resources
                    String name = parts[0].trim();
                    int T = Integer.parseInt(parts[1].trim());
                    int D = Integer.parseInt(parts[2].trim());
                    int C = Integer.parseInt(parts[3].trim());

                    if (D != T) {
                        hasDeadlines = true;
                    }

                    Task task = new Task(name, 0, C, T, D);

                    if (parts.length > 4 && !parts[4].trim().isEmpty()) {
                        hasResources = true;
                        String[] resourcePairs = parts[4].trim().split(";");

                        for (String pair : resourcePairs) {
                            String[] resourceSplit = pair.split(":");
                            if (resourceSplit.length == 2) {
                                String resourceName = resourceSplit[0].trim();
                                int usageTime = Integer.parseInt(resourceSplit[1].trim());

                                Resource resource = resourceMap.computeIfAbsent(resourceName, Resource::new);
                                resource.addUsage(task, usageTime);
                            }
                        }
                    }

                    tasks.add(task);
                } else {
                    throw new IllegalArgumentException("Unrecognized CSV format. Header: " + header);
                }
            }

            if (hasResources) {
                mode = "RESOURCES";
            } else if (hasDeadlines) {
                mode = "DMS";
            }

            // Assign priorities according to selected mode
            if (mode.equals("RESOURCES") || mode.equals("DMS")) {
                tasks.sort(Comparator.comparingInt(Task::getD));
                for (int i = 0; i < tasks.size(); i++) {
                    tasks.get(i).setPriority(tasks.size() - i);
                }
                if (hasResources) {
                    for (Resource r : resourceMap.values()) {
                        r.calculateCeiling();
                    }
                }
            } else {
                tasks.sort(Comparator.comparingInt(Task::getT));
                for (int i = 0; i < tasks.size(); i++) {
                    tasks.get(i).setPriority(tasks.size() - i);
                }
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Numeric format error in CSV", e);
        } catch (Exception e) {
            throw new RuntimeException("Error while processing CSV", e);
        }

        return new ParsedInput(tasks, mode, resourceMap.isEmpty() ? null : resourceMap);
    }

}
