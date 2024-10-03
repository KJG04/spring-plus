package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.expert.domain.todo.entity.Todo;

import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;

public class TodoQueryRepositoryImpl implements TodoQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public TodoQueryRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(todo).join(todo.user).fetchJoin().where(todo.id.eq(todoId)).fetchOne());
    }
}
