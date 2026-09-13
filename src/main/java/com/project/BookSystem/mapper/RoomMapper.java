package com.project.BookSystem.mapper;

import com.project.BookSystem.dto.RoomResponse;
import com.project.BookSystem.model.Room;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomResponse toDto(Room room);
    List<RoomResponse> toResponseList(List<Room> rooms);
}