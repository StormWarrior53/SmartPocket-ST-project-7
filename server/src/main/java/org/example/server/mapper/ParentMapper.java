package org.example.server.mapper;

import org.example.server.dto.ParentResponse;
import org.example.server.model.Parent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParentMapper {

    ParentResponse toResponse(Parent parent);
}
