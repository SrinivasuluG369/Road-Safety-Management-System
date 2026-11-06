
package com.roadsafety.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.roadsafety.model.RoadInspection;
import com.roadsafety.repository.RoadInspectionRepository;

@Service
public class RoadInspectionService {

    private final RoadInspectionRepository repository;
    private final ImageProcessingService imageProcessingService;

    public RoadInspectionService(
            RoadInspectionRepository repository,
            ImageProcessingService imageProcessingService) {

        this.repository = repository;
        this.imageProcessingService = imageProcessingService;
    }

    // Process image and save inspection
    public Map<String, Object> inspectRoad(
            String imagePath, String imageName) {

        Map<String, Object> result =
                imageProcessingService.processImage(imagePath);

        if (Boolean.TRUE.equals(result.get("success"))) {

            String condition = (String) result.get("condition");

            double damagePercentage =
                    ((Number) result.get("damagePercentage")).doubleValue();

            RoadInspection inspection = new RoadInspection(
                    imageName,
                    condition,
                    damagePercentage,
                    LocalDateTime.now(),
                    "COMPLETED"
            );

            RoadInspection savedInspection =
                    repository.save(inspection);

            result.put("inspectionId", savedInspection.getId());
            result.put("inspectionDate", savedInspection.getInspectionDate());
        }

        return result;
    }

    // Get all inspections
    public List<RoadInspection> getAllInspections() {
        return repository.findAll();
    }

    // Get inspection by ID
    public RoadInspection getInspectionById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // Delete inspection
    public void deleteInspection(Long id) {
        repository.deleteById(id);
    }

    // Dashboard statistics
    public Map<String, Object> getDashboardStatistics() {

        Map<String, Object> statistics = new HashMap<>();

        long total = repository.count();
        long good = repository.countByCondition("GOOD");
        long fair = repository.countByCondition("FAIR");
        long poor = repository.countByCondition("POOR");

        statistics.put("totalInspections", total);
        statistics.put("goodRoads", good);
        statistics.put("fairRoads", fair);
        statistics.put("poorRoads", poor);

        return statistics;
    }
}
