package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoQueryRepository {
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    Page<Todo> findTodosByKeywordAndCreatedAtBetweenAndManagerName(String title, LocalDateTime startAt, LocalDateTime endAt, String managerName, Pageable pageable);
}
