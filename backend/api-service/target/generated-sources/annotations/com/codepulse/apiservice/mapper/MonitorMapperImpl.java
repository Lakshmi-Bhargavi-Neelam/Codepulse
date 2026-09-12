package com.codepulse.apiservice.mapper;

import com.codepulse.apiservice.dto.MonitorConfigResponse;
import com.codepulse.apiservice.entity.Monitor;
import com.codepulse.apiservice.entity.Project;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-12T15:12:37+0000",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.12 (Ubuntu)"
)
@Component
public class MonitorMapperImpl implements MonitorMapper {

    @Override
    public MonitorConfigResponse toMonitorConfigResponse(Monitor monitor) {
        if ( monitor == null ) {
            return null;
        }

        MonitorConfigResponse.MonitorConfigResponseBuilder monitorConfigResponse = MonitorConfigResponse.builder();

        monitorConfigResponse.projectId( monitorProjectId( monitor ) );
        monitorConfigResponse.id( monitor.getId() );
        monitorConfigResponse.name( monitor.getName() );
        monitorConfigResponse.url( monitor.getUrl() );
        monitorConfigResponse.httpMethod( monitor.getHttpMethod() );
        monitorConfigResponse.timeoutMs( monitor.getTimeoutMs() );
        monitorConfigResponse.expectedStatusCode( monitor.getExpectedStatusCode() );
        monitorConfigResponse.active( monitor.getActive() );

        return monitorConfigResponse.build();
    }

    private UUID monitorProjectId(Monitor monitor) {
        Project project = monitor.getProject();
        if ( project == null ) {
            return null;
        }
        return project.getId();
    }
}
