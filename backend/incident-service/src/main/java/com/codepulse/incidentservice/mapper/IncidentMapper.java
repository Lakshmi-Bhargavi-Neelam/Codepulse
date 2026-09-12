package com.codepulse.incidentservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codepulse.incidentservice.dto.IncidentRequest;
import com.codepulse.incidentservice.dto.IncidentResponse;
import com.codepulse.incidentservice.entity.Incident;

/**
 * Maps between the Incident entity and its DTOs.
 *
 * IncidentRequest -> Incident only carries over the fields the caller
 * (worker-service) actually knows about (monitorId, projectId, monitorName,
 * url). Everything lifecycle-related (id, status, startedAt, resolvedAt,
 * durationSeconds, failureCount, lastErrorMessage) is business logic owned
 * by IncidentServiceImpl and is therefore ignored here, not derived from
 * the request.
 */
@Mapper(componentModel = "spring")
public interface IncidentMapper {

    IncidentResponse toResponse(Incident incident);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "durationSeconds", ignore = true)
    @Mapping(target = "failureCount", ignore = true)
    @Mapping(target = "lastErrorMessage", ignore = true)
    Incident toEntity(IncidentRequest request);
}
