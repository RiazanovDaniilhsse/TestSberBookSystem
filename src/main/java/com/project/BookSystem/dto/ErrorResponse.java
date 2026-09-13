package com.project.BookSystem.dto;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime time, int status, String message) {

}
