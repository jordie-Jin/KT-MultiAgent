package com.project.demo.dto.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Spring {@code PageImpl} 을 그대로 직렬화하면 {@code pageable} 객체가 지저분하게 노출되고
 * 버전에 따라 불안정하므로, API 명세(§1)에 맞춘 안정적인 페이지 응답 래퍼.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isFirst(),
                p.isLast());
    }
}
