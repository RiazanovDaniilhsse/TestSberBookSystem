package com.project.BookSystem.controller;

import com.project.BookSystem.dto.AvailabilityResponse;
import com.project.BookSystem.dto.RoomResponse;
import com.project.BookSystem.service.BookingService;
import com.project.BookSystem.service.RoomService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final BookingService bookingService;
    private final RoomService roomService;

    @GetMapping("/{roomId}/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        AvailabilityResponse response = bookingService.checkAvailability(roomId, from, to);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }
}