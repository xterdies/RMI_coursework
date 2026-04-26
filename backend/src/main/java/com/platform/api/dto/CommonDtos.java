package com.platform.api.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public final class CommonDtos {

    private CommonDtos() {}

    public record PagedResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
        public static <T> PagedResponse<T> from(Page<T> page) {
            return new PagedResponse<>(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isLast()
            );
        }
    }

    public record ErrorResponse(
            int status,
            String error,
            String message,
            String path,
            java.time.LocalDateTime timestamp
    ) {}

    public record MessageResponse(String message) {}
}
