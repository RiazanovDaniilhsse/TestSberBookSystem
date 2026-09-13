package com.project.BookSystem.service;

import com.project.BookSystem.dto.AvailabilityResponse;
import com.project.BookSystem.dto.BookingResponse;
import com.project.BookSystem.dto.CreateBookingRequest;
import com.project.BookSystem.exception.BookingConflictException;
import com.project.BookSystem.exception.BookingNotFoundException;
import com.project.BookSystem.mapper.BookingMapper;
import com.project.BookSystem.model.Booking;
import com.project.BookSystem.model.BookingStatus;
import com.project.BookSystem.repository.BookingRepository;
import com.project.BookSystem.repository.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    @Test
    @DisplayName("Успешное создание бронирования, если нет пересечений")
    void createBooking_Success() {
        LocalDateTime now = LocalDateTime.now();
        CreateBookingRequest request = new CreateBookingRequest(1L, "Иван", now.plusHours(1),
                now.plusHours(2));
        Booking entity = new Booking();
        Booking savedEntity = new Booking();
        BookingResponse expectedResponse = new BookingResponse(1L, 1L, "Иван", now.plusHours(1),
                now.plusHours(2), BookingStatus.ACTIVE);
        when(roomRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.hasOverlap(eq(1L), any(), any())).thenReturn(false);
        when(bookingMapper.toEntity(request)).thenReturn(entity);
        when(bookingRepository.save(entity)).thenReturn(savedEntity);
        when(bookingMapper.toResponse(savedEntity)).thenReturn(expectedResponse);
        BookingResponse actualResponse = bookingService.createBooking(request);
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(bookingRepository, times(1)).save(entity);
        verify(bookingMapper, times(1)).toResponse(savedEntity);
    }

    @Test
    @DisplayName("Ошибка создания: выброс BookingConflictException при пересечении времени")
    void createBooking_ThrowsConflictException_WhenOverlapExists() {
        LocalDateTime now = LocalDateTime.now();
        CreateBookingRequest request = new CreateBookingRequest(1L, "Иван", now.plusHours(1),
                now.plusHours(2));
        when(roomRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.hasOverlap(eq(1L), any(), any())).thenReturn(true);
        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка создания: выброс IllegalArgumentException, если endTime раньше startTime")
    void createBooking_ThrowsIllegalArgumentException_WhenTimeIsInvalid() {
        LocalDateTime now = LocalDateTime.now();
        CreateBookingRequest request = new CreateBookingRequest(1L, "Иван", now.plusHours(2),
                now.plusHours(1));
        when(roomRepository.existsById(1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).hasOverlap(any(), any(), any());
    }

    @Test
    @DisplayName("Успешное получение списка всех бронирований")
    void getAllBookings_Success() {
        Booking booking = new Booking();
        BookingResponse responseDto = new BookingResponse(1L, 1L, "Иван", LocalDateTime.now(),
                LocalDateTime.now().plusHours(1), BookingStatus.ACTIVE);
        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        when(bookingMapper.toResponseList(anyList())).thenReturn(List.of(responseDto));
        List<BookingResponse> result = bookingService.getAllBookings();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Успешная отмена существующего бронирования")
    void cancelBooking_Success() {
        Long bookingId = 10L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.ACTIVE);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        bookingService.cancelBooking(bookingId);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Ошибка отмены: выброс BookingNotFoundException, если бронь не найдена")
    void cancelBooking_ThrowsNotFoundException_WhenBookingDoesNotExist() {
        Long bookingId = 99L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> bookingService.cancelBooking(bookingId));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Проверка доступности: комната свободна")
    void checkAvailability_RoomIsAvailable() {
        Long roomId = 1L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(bookingRepository.hasOverlap(eq(roomId), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(false);
        AvailabilityResponse response = bookingService.checkAvailability(roomId, start, end);
        assertTrue(response.isAvailable());
    }

    @Test
    @DisplayName("Проверка доступности: комната занята")
    void checkAvailability_RoomIsNotAvailable() {
        Long roomId = 1L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        when(roomRepository.existsById(roomId)).thenReturn(true);
        when(bookingRepository.hasOverlap(eq(roomId), any(LocalDateTime.class),
                any(LocalDateTime.class))).thenReturn(true);
        AvailabilityResponse response = bookingService.checkAvailability(roomId, start, end);
        assertFalse(response.isAvailable());
    }
}