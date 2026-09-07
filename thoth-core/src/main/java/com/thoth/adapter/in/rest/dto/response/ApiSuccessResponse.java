package com.thoth.adapter.in.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiSuccessResponse<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}