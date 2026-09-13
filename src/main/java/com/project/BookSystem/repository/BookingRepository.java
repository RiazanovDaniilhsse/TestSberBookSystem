package com.project.BookSystem.repository;

import com.project.BookSystem.model.Booking;
import com.project.BookSystem.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
                SELECT COUNT(b) > 0 FROM Booking b
                WHERE b.roomId = :roomId
                  AND b.status = com.project.BookSystem.model.BookingStatus.ACTIVE
                  AND b.startTime < :endTime
                  AND b.endTime > :startTime
            """)
    boolean hasOverlap(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Booking> findByStatus(BookingStatus status);
}