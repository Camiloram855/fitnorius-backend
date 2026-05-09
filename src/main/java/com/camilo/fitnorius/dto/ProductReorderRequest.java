package com.camilo.fitnorius.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductReorderRequest {
    private List<Long> orderedIds;
}
