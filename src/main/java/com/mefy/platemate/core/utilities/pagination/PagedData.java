package com.mefy.platemate.core.utilities.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedData<T> {
    private List<T> items;
    private PaginationMeta meta;
}
