package com.project.demo.support;

import com.project.demo.web.ApiException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** 조회 API 공통: 기본 시간범위(최근 24h) 해석 + 정렬 alias(API 필드 → 엔티티 경로) 변환. */
public final class QuerySupport {

    private QuerySupport() {
    }

    /** startAt/endAt 미지정 시 최근 24시간으로 기본 설정. start > end 면 INVALID_DATE_RANGE. */
    public static LocalDateTime[] resolveRange(LocalDateTime startAt, LocalDateTime endAt) {
        LocalDateTime end = endAt != null ? endAt : LocalDateTime.now();
        LocalDateTime start = startAt != null ? startAt : end.minusHours(24);
        if (start.isAfter(end)) {
            throw ApiException.invalidDateRange();
        }
        return new LocalDateTime[] {start, end};
    }

    /**
     * Pageable 의 정렬 프로퍼티를 API 필드명에서 엔티티 경로명으로 변환.
     * 정렬이 비어 있으면 defaultSort 적용.
     */
    public static Pageable withSort(Pageable pageable, Map<String, String> alias, Sort defaultSort) {
        Sort sort = pageable.getSort();
        Sort resolved;
        if (sort.isUnsorted()) {
            resolved = defaultSort;
        } else {
            List<Sort.Order> orders = sort.stream()
                    .map(o -> o.withProperty(alias.getOrDefault(o.getProperty(), o.getProperty())))
                    .toList();
            resolved = Sort.by(orders);
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), resolved);
    }
}
