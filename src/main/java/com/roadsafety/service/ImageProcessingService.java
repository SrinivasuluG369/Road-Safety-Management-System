
package com.roadsafety.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ImageProcessingService {

    private final RoadDamageDetectionService detectionService;

    public ImageProcessingService(
            RoadDamageDetectionService detectionService) {
        this.detectionService = detectionService;
    }

    private final Path processedDirectory =
            Paths.get("uploads", "processed")
                    .toAbsolutePath()
                    .normalize();

    @PostConstruct
    public void loadOpenCV() {
        nu.pattern.OpenCV.loadLocally();
        System.out.println("OpenCV loaded successfully!");
    }

    public Map<String, Object> processImage(String imagePath) {

        Map<String, Object> result = new HashMap<>();

        Mat image = Imgcodecs.imread(imagePath);

        try {
            if (image.empty()) {
                result.put("success", false);
                result.put("message", "Unable to read image.");
                return result;
            }

            if (!detectionService.isModelLoaded()) {
                result.put("success", false);
                result.put("message", "AI model is not loaded.");
                return result;
            }

            Files.createDirectories(processedDirectory);

            // Run AI detection
            List<Map<String, Object>> detections =
                    detectionService.detect(image);

            int totalRegions = detections.size();

            // Calculate approximate bounding-box coverage
            double detectedArea = 0;

            for (Map<String, Object> detection : detections) {

                int x1 = ((Number) detection.get("x1")).intValue();
                int y1 = ((Number) detection.get("y1")).intValue();
                int x2 = ((Number) detection.get("x2")).intValue();
                int y2 = ((Number) detection.get("y2")).intValue();

                double area =
                        (double) (x2 - x1) * (y2 - y1);

                detectedArea += area;
            }

            double totalArea =
                    (double) image.rows() * image.cols();

            double damagePercentage = totalArea > 0
                    ? (detectedArea * 100.0) / totalArea
                    : 0;

            damagePercentage = Math.min(100,
                    Math.round(damagePercentage * 100.0) / 100.0);

            // Road condition based on approximate box coverage
            String condition;

            if (totalRegions == 0) {
                condition = "GOOD";
            } else if (damagePercentage <= 10) {
                condition = "FAIR";
            } else {
                condition = "POOR";
            }

            // Save processed image
            String processedFileName =
                    "processed_" + System.currentTimeMillis() + ".jpg";

            Path processedPath =
                    processedDirectory.resolve(processedFileName);

            boolean saved = Imgcodecs.imwrite(
                    processedPath.toString(), image);

            if (!saved) {
                throw new IllegalStateException(
                        "Unable to save processed image.");
            }

            // Prepare damage type summary
            Map<String, Integer> damageTypeCounts =
                    new HashMap<>();

            for (Map<String, Object> detection : detections) {

                String damageType =
                        (String) detection.get("damageType");

                damageTypeCounts.merge(
                        damageType, 1, Integer::sum);
            }

            // Response data
            result.put("success", true);
            result.put("damagePercentage", damagePercentage);
            result.put("condition", condition);
            result.put("detectedRegions", totalRegions);
            result.put("detections", detections);
            result.put("damageTypeCounts", damageTypeCounts);
            result.put("processedImageName", processedFileName);
            result.put("width", image.cols());
            result.put("height", image.rows());

            result.put("message",
                    "AI road damage detection completed successfully.");

            return result;

        } catch (Exception e) {

            e.printStackTrace();

            result.put("success", false);
            result.put("message", e.getMessage());

            return result;

        } finally {
            image.release();
        }
    }
}
