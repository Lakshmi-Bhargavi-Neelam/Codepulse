package com.codepulse.apiservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.codepulse.apiservice.dto.MonitorConfigResponse;
import com.codepulse.apiservice.entity.Monitor;

/**
 * MapStruct mapper for Monitor entity to DTOs.
 */
@Mapper(componentModel = "spring")
public interface MonitorMapper {

    @Mapping(source = "project.id", target = "projectId")
    MonitorConfigResponse toMonitorConfigResponse(Monitor monitor);
}