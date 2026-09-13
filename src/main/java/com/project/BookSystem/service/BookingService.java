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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BookingMapper bookingMapper;

    private static final long MIN_BOOKING_MINUTES = 30;
    private static final long MAX_BOOKING_HOURS = 4;
    private static final long MAX_FUTURE_DAYS = 30;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        if (!roomRepository.existsById(request.roomId())) {
            throw new BookingNotFoundException("Комната с ID " + request.roomId() + " не найдена");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException(
                    "Время окончания бронирования должно быть позже времени начала");
        }
        Duration duration = Duration.between(request.startTime(), request.endTime());
        if (duration.toMinutes() < MIN_BOOKING_MINUTES) {
            throw new IllegalArgumentException(
                    "Минимальное время бронирования — " + MIN_BOOKING_MINUTES + " минут");
        }
        if (duration.toHours() > MAX_BOOKING_HOURS) {
            throw new IllegalArgumentException(
                    "Максимальное время бронирования — " + MAX_BOOKING_HOURS + " часа");
        }
        if (request.startTime().isAfter(now.plusDays(MAX_FUTURE_DAYS))) {
            throw new IllegalArgumentException(
                    "Нельзя бронировать комнату более чем на " + MAX_FUTURE_DAYS + " дней вперед");
        }
        boolean hasOverlap = bookingRepository.hasOverlap(request.roomId(), request.startTime(),
                request.endTime());
        if (hasOverlap) {
            throw new BookingConflictException(
                    "Комната уже забронирована на указанный интервал времени");
        }
        Booking booking = bookingMapper.toEntity(request);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllActiveBookings() {
        List<Booking> bookings = bookingRepository.findByStatus(BookingStatus.ACTIVE);
        return bookingMapper.toResponseList(bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookingMapper.toResponseList(bookings);
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Бронирование с ID " + bookingId + " не найдено"));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalArgumentException("Бронирование уже отменено");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(Long roomId, LocalDateTime from,
            LocalDateTime to) {
        if (!roomRepository.existsById(roomId)) {
            throw new BookingNotFoundException("Комната с ID " + roomId + " не найдена");
        }
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("Время начала должно быть раньше времени окончания");
        }
        boolean hasOverlap = bookingRepository.hasOverlap(roomId, from, to);
        if (hasOverlap) {
            return new AvailabilityResponse(false, "Комната уже забронирована на этот интервал");
        } else {
            return new AvailabilityResponse(true, "Комната свободна");
        }
    }
}