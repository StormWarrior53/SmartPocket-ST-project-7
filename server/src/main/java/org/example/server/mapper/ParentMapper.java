package org.example.server.mapper;

import org.example.server.dto.ParentResponse;
import org.example.server.model.Parent;
import org.springframework.stereotype.Component;

public interface ParentMapper {

    ParentResponse toResponse(Parent parent);

    @Component
    class Impl implements ParentMapper {

        @Override
        public ParentResponse toResponse(Parent parent) {
            if (parent == null) return null;
            return new ParentResponse(
                    parent.getId(),
                    parent.getEmail(),
                    parent.getFirstName(),
                    parent.getLastName(),
                    parent.getCreatedAt()
            );
        }
    }
}
