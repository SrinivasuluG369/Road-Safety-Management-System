
package com.roadsafety.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageViewController {

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    @GetMapping("/api/images/{fileName}")
    public ResponseEntity<Resource> viewImage(
            @PathVariable String fileName) {

        try {
            Path imagePath =
                    uploadDirectory.resolve(fileName).normalize();

            if (!imagePath.startsWith(uploadDirectory)
                    || !Files.isRegularFile(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource =
                    new UrlResource(imagePath.toUri());

            String contentType =
                    Files.probeContentType(imagePath);

            if (contentType == null
                    || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
