package com.platform.infrastructure.query;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class Specifications {

    private Specifications() {}

    public static <T> Specification<T> fromCriteria(List<FilterCriterion> criteria, List<String> allowedFields) {
        if (criteria == null || criteria.isEmpty()) {
            return Specification.where(null);
        }
        Specification<T> spec = Specification.where(null);
        for (FilterCriterion c : criteria) {
            if (!allowedFields.contains(c.field())) {
                throw new com.platform.service.exception.ValidationException("Filtering by field not allowed: " + c.field());
            }
            spec = spec.and(toSpec(c));
        }
        return spec;
    }

    private static <T> Specification<T> toSpec(FilterCriterion c) {
        return (root, query, cb) -> {
            Path<?> path = resolvePath(root, c.field());
            return switch (c.operator()) {
                case EQ -> cb.equal(path, castToPathType(path, c.value()));
                case LIKE -> cb.like(cb.lower(path.as(String.class)), "%" + c.value().toLowerCase() + "%");
            };
        };
    }

    private static <T> Path<?> resolvePath(Root<T> root, String field) {
        if (field.contains(".")) {
            Path<?> p = root;
            for (String part : field.split("\\.")) {
                p = p.get(part);
            }
            return p;
        }
        return root.get(field);
    }

    private static Object castToPathType(Path<?> path, String raw) {
        Class<?> javaType = path.getJavaType();
        try {
            if (javaType.equals(String.class)) return raw;
            if (javaType.equals(Long.class) || javaType.equals(long.class)) return Long.valueOf(raw);
            if (javaType.equals(Integer.class) || javaType.equals(int.class)) return Integer.valueOf(raw);
            if (javaType.equals(Boolean.class) || javaType.equals(boolean.class)) return Boolean.valueOf(raw);
            return raw;
        } catch (RuntimeException e) {
            throw new com.platform.service.exception.ValidationException("Invalid value for field type: " + raw);
        }
    }
}

