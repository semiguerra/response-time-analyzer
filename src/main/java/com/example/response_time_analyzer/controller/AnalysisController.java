package com.example.response_time_analyzer.controller;

import com.example.response_time_analyzer.model.ResponseResult;
import com.example.response_time_analyzer.service.ResponseTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("/analyze")
public class AnalysisController {

    @Autowired
    private ResponseTimeService responseTimeService;

    /**
     * Handles GET requests to show the upload form page.
     */
    @GetMapping
    public ModelAndView showUploadForm() {
        // Returns the view "index.html" for file upload
        return new ModelAndView("index");
    }

    /**
     * Handles POST requests to upload and analyze the CSV file.
     */
    @PostMapping
    public ModelAndView analyzeFile(@RequestParam("file") MultipartFile file) {
        try {
            // Parse and process the uploaded file
            List<ResponseResult> results = responseTimeService.calculate(file);
            ModelAndView modelAndView = new ModelAndView();

            if (results.isEmpty()) {
                throw new IllegalArgumentException("The file did not contain valid data.");
            }

            // Check whether the results include resource blocking analysis
            boolean hasResourceData = results.get(0).hasResourceData();

            if (hasResourceData) {
                modelAndView.setViewName("result-resources");

                // Global metrics across all tasks
                boolean allSchedulable = results.stream().allMatch(ResponseResult::isSchedulable);
                boolean allInheritedSchedulable = results.stream().allMatch(ResponseResult::isInheritedSchedulable);
                boolean allCeilingSchedulable = results.stream().allMatch(ResponseResult::isCeilingSchedulable);

                // Debugging log
                System.out.println("Results with resource analysis:");
                results.forEach(r -> System.out.println(
                    String.format("Task: %s, PIP Block: %d, PCP Block: %d",
                        r.getTaskName(), r.getBlockingTime(), r.getCeilingBlockingTime())
                ));

                modelAndView.addObject("allSchedulable", allSchedulable);
                modelAndView.addObject("allInheritedSchedulable", allInheritedSchedulable);
                modelAndView.addObject("allCeilingSchedulable", allCeilingSchedulable);

            } else {
                // Basic analysis (no resources involved)
                modelAndView.setViewName("result-basic");
                modelAndView.addObject("allSchedulable",
                    results.stream().allMatch(ResponseResult::isSchedulable));
            }

            modelAndView.addObject("results", results);
            return modelAndView;

        } catch (Exception e) {
            // Handle error by showing an error page
            ModelAndView errorView = new ModelAndView("error");
            errorView.addObject("message", "Error while processing the file: " + e.getMessage());

            // Debugging error log
            System.err.println("Error processing file:");
            e.printStackTrace();

            return errorView;
        }
    }
}
