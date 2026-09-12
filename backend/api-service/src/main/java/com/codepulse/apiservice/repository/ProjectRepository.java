package com.codepulse.apiservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codepulse.apiservice.entity.Project;

/**
 * Repository for Project entity operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
}