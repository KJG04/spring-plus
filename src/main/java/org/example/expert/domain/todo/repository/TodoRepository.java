package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE COALESCE(t.weather, 1) LIKE " +
            "CASE " +
            "   WHEN :weather IS NULL " +
            "   THEN '%' " +
            "   ELSE :weather " +
            "END " +
            "AND (:startAt IS NULL OR t.modifiedAt >= :startAt) " +
            "AND (:endAt IS NULL OR t.modifiedAt <= :endAt) " +
            "ORDER BY t.modifiedAt DESC")
    Page<Todo> findTodosByWeatherAndModifiedAtBetween(@Param("weather") String weather, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt, Pageable pageable);

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
