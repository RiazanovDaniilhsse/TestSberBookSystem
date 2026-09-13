package com.project.BookSystem.dto;

import com.project.BookSystem.model.BookingStatus;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long roomId,
        String userName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BookingStatus status
) {
}