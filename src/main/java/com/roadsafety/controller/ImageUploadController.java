
package com.roadsafety.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.roadsafety.model.RoadInspection;
import com.roadsafety.service.RoadInspectionService;

@RestController
public class ImageUploadController {

    private final RoadInspectionService inspectionService;

    private final String uploadDir = "uploads";

    public ImageUploadController(
            RoadInspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    // Dashboard statistics API
    @GetMapping("/api/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {

        return ResponseEntity.ok(
                inspectionService.getDashboardStatistics()
        );
    }

    // Upload and inspect road image
    @PostMapping("/api/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("image") MultipartFile image) {

        try {

            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Please select an image."
                        )
                );
            }

            String contentType = image.getContentType();

            if (contentType == null ||
                    !contentType.startsWith("image/")) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Please upload a valid image."
                        )
                );
            }

            Path uploadPath = Paths.get(uploadDir);

            Files.createDirectories(uploadPath);

            String originalName = Paths.get(
                    image.getOriginalFilename() == null
                            ? "road-image"
                            : image.getOriginalFilename()
            ).getFileName().toString();

            String fileName =
                    UUID.randomUUID() + "_" + originalName;

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(image.getInputStream(), filePath);

            Map<String, Object> result =
                    inspectionService.inspectRoad(
                            filePath.toAbsolutePath().toString(),
                            fileName
                    );

            // Original uploaded image
            result.put("imageName", fileName);

            // Processed image filename is returned by
            // ImageProcessingService
            if (Boolean.TRUE.equals(result.get("success"))) {
                result.put(
                        "processedImageUrl",
                        "/api/images/" +
                                result.get("processedImageName")
                );
            }

            return ResponseEntity.ok(result);

        } catch (IOException e) {

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of(
                            "success", false,
                            "message", "Image upload failed: " +
                                    e.getMessage()
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of(
                            "success", false,
                            "message", "Image processing failed: " +
                                    e.getMessage()
                    )
            );
        }
    }

    // Get all inspection records
    @GetMapping("/api/inspections")
    public ResponseEntity<?> getAllInspections() {

        return ResponseEntity.ok(
                inspectionService.getAllInspections()
        );
    }

    // Get inspection by ID
    @GetMapping("/api/inspections/{id}")
    public ResponseEntity<?> getInspectionById(
            @PathVariable Long id) {

        RoadInspection inspection =
                inspectionService.getInspectionById(id);

        if (inspection == null) {
            return ResponseEntity.status(
                    HttpStatus.NOT_FOUND
            ).body(
                    Map.of("message", "Inspection not found.")
            );
        }

        return ResponseEntity.ok(inspection);
    }

    // Delete inspection
    @DeleteMapping("/api/inspections/{id}")
    public ResponseEntity<?> deleteInspection(
            @PathVariable Long id) {

        if (inspectionService.getInspectionById(id) == null) {
            return ResponseEntity.status(
                    HttpStatus.NOT_FOUND
            ).body(
                    Map.of("message", "Inspection not found.")
            );
        }

        inspectionService.deleteInspection(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Inspection deleted successfully."
                )
        );
    }
}
