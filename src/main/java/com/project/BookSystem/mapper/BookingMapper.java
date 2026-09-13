package com.project.BookSystem.mapper;

import com.project.BookSystem.dto.BookingResponse;
import com.project.BookSystem.dto.CreateBookingRequest;
import com.project.BookSystem.model.Booking;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingMapper {

    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toEntity(CreateBookingRequest request);
}