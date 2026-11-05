package com.ellora.application.common.dto;

import java.util.List;

/**
 * Standard pagination response wrapper DTO.
 *
 * @param items      Page data elements
 * @param page       Current zero-based or one-based page index
 * @param pageSize   Requested size per page
 * @param totalItems Total number of matching elements across all pages
 * @param totalPages Total calculated pages
 * @param <T>        Data element type
 */
public record PagedResult<T>(List<T> items, int page, int pageSize, long totalItems, int totalPages) {
    public static <T> PagedResult<T> of(List<T> items, int page, int pageSize, long totalItems) {
        int calculatedPages = pageSize > 0 ? (int) Math.ceil((double) totalItems / pageSize) : 0;
        return new PagedResult<>(items, page, pageSize, totalItems, calculatedPages);
    }
}
