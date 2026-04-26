package com.platform.infrastructure.query;

import com.platform.service.exception.ValidationException;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class QueryParamParsers {

    private QueryParamParsers() {}

    /**
     * filter syntax: field:op:value
     * - op: eq | like
     * Multiple criteria: separate by ';'
     * Example: name:like:eu;countryCode:eq:UA
     */
    public static List<FilterCriterion> parseFilter(String filter) {
        if (filter == null || filter.isBlank()) return List.of();
        List<FilterCriterion> out = new ArrayList<>();
        String[] parts = filter.split(";");
        for (String p : parts) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) continue;
            String[] tokens = trimmed.split(":", 3);
            if (tokens.length != 3) {
                throw new ValidationException("Malformed filter criterion: " + trimmed);
            }
            String field = tokens[0].trim();
            String op = tokens[1].trim().toLowerCase(Locale.ROOT);
            String value = tokens[2].trim();
            if (field.isEmpty() || value.isEmpty()) {
                throw new ValidationException("Malformed filter criterion: " + trimmed);
            }
            FilterOperator operator = switch (op) {
                case "eq" -> FilterOperator.EQ;
                case "like" -> FilterOperator.LIKE;
                default -> throw new ValidationException("Unsupported filter operator: " + op);
            };
            out.add(new FilterCriterion(field, operator, value));
        }
        return out;
    }

    /**
     * sort syntax: field,asc|desc
     * Example: createdAt,desc
     */
    public static Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.unsorted();
        String[] tokens = sort.split(",", 2);
        String field = tokens[0].trim();
        if (field.isEmpty()) throw new ValidationException("Malformed sort parameter: " + sort);

        // If direction is omitted, default to ASC (backward compatible).
        if (tokens.length == 1 || tokens[1].isBlank()) {
            return Sort.by(Sort.Direction.ASC, field);
        }

        String dir = tokens[1].trim().toLowerCase(Locale.ROOT);
        Sort.Direction direction = switch (dir) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new ValidationException("Malformed sort direction: " + dir);
        };
        return Sort.by(direction, field);
    }
}

