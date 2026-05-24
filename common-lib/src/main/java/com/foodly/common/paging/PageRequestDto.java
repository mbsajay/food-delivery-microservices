package com.foodly.common.paging;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    @Min(0)
    private int page = DEFAULT_PAGE;

    @Min(1)
    @Max(MAX_SIZE)
    private int size = DEFAULT_SIZE;

    private String sort;

    private SortDirection direction = SortDirection.ASC;

    public enum SortDirection {
        ASC, DESC
    }
}
