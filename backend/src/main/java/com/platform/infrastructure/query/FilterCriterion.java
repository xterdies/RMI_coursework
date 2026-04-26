package com.platform.infrastructure.query;

public record FilterCriterion(String field, FilterOperator operator, String value) {
}

