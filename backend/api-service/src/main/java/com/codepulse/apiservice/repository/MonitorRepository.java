package com.codepulse.apiservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.codepulse.apiservice.entity.Monitor;

/**
 * Repository for Monitor entity operations.
 */
@Repository
public interface MonitorRepository extends JpaRepository<Monitor, UUID> {

    /**
     * Find all active monitors across all projects.
     */
    @Query("SELECT m FROM Monitor m WHERE m.active = true AND m.project.active = true")
    List<Monitor> findAllActiveMonitors();
}