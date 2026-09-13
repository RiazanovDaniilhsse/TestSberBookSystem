package com.project.BookSystem.service;

import com.project.BookSystem.dto.RoomResponse;
import com.project.BookSystem.mapper.RoomMapper;
import com.project.BookSystem.model.Room;
import com.project.BookSystem.repository.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public List<RoomResponse> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return roomMapper.toResponseList(rooms);
    }

}
