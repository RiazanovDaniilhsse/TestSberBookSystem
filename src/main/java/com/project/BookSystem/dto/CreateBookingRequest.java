package com.project.BookSystem.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CreateBookingRequest(
        @NotNull(message = "ID комнаты обязателен для заполнения")
        Long roomId,

        @NotBlank(message = "Имя пользователя не должно быть пустым")
        @Size(max = 100, message = "Имя пользователя не должно превышать 100 символов")
        String userName,

        @NotNull(message = "Время начала бронирования обязательно")
        @Future(message = "Время начала бронирования должно быть в будущем")
        LocalDateTime startTime,

        @NotNull(message = "Время окончания бронирования обязательно")
        @Future(message = "Время окончания бронирования должно быть в будущем")
        LocalDateTime endTime
) {}
