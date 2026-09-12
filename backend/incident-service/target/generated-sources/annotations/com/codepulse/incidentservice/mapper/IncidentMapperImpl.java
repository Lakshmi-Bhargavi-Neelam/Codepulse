package com.codepulse.incidentservice.mapper;

import com.codepulse.incidentservice.dto.IncidentRequest;
import com.codepulse.incidentservice.dto.IncidentResponse;
import com.codepulse.incidentservice.entity.Incident;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-12T15:12:42+0000",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.12 (Ubuntu)"
)
@Component
public class IncidentMapperImpl implements IncidentMapper {

    @Override
    public IncidentResponse toResponse(Incident incident) {
        if ( incident == null ) {
            return null;
        }

        IncidentResponse.IncidentResponseBuilder incidentResponse = IncidentResponse.builder();

        incidentResponse.id( incident.getId() );
        incidentResponse.monitorId( incident.getMonitorId() );
        incidentResponse.projectId( incident.getProjectId() );
        incidentResponse.monitorName( incident.getMonitorName() );
        incidentResponse.url( incident.getUrl() );
        incidentResponse.status( incident.getStatus() );
        incidentResponse.startedAt( incident.getStartedAt() );
        incidentResponse.resolvedAt( incident.getResolvedAt() );
        incidentResponse.durationSeconds( incident.getDurationSeconds() );
        incidentResponse.failureCount( incident.getFailureCount() );
        incidentResponse.lastErrorMessage( incident.getLastErrorMessage() );

        return incidentResponse.build();
    }

    @Override
    public Incident toEntity(IncidentRequest request) {
        if ( request == null ) {
            return null;
        }

        Incident incident = new Incident();

        incident.setMonitorId( request.getMonitorId() );
        incident.setProjectId( request.getProjectId() );
        incident.setMonitorName( request.getMonitorName() );
        incident.setUrl( request.getUrl() );

        return incident;
    }
}
