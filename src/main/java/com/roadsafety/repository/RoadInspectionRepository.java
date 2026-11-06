
package com.roadsafety.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadsafety.model.RoadInspection;

@Repository
public interface RoadInspectionRepository
        extends JpaRepository<RoadInspection, Long> {

    long countByCondition(String condition);

}
