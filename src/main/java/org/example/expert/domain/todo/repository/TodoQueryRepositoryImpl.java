package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;


@RequiredArgsConstructor
public class TodoQueryRepositoryImpl implements TodoQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(todo).join(todo.user).fetchJoin().where(todo.id.eq(todoId)).fetchOne());
    }

    private BooleanExpression containsTitle(String title) {
        return title == null ? null : todo.title.contains(title);
    }

    private BooleanExpression afterCreatedAt(LocalDateTime startAt) {
        return startAt == null ? null : todo.createdAt.after(startAt);
    }

    private BooleanExpression beforeCreatedAt(LocalDateTime endAt) {
        return endAt == null ? null : todo.createdAt.before(endAt);
    }

    private BooleanExpression containsManagerNickname(String managerName) {
        return managerName == null ? null : manager.user.nickname.contains(managerName);
    }

    private List<Todo> getContents(String title, LocalDateTime startAt, LocalDateTime endAt, String managerName, Pageable pageable) {
        return jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user)
                .leftJoin(todo.comments)
                .leftJoin(todo.managers, manager)
                .fetchJoin()
                .leftJoin(manager.user)
                .fetchJoin()
                .where(
                        containsTitle(title),
                        afterCreatedAt(startAt),
                        beforeCreatedAt(endAt),
                        containsManagerNickname(managerName)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

    }

    private Long getCount(String title, LocalDateTime startAt, LocalDateTime endAt, String managerName) {
        return jpaQueryFactory
                .select(todo.count())
                .from(todo)
                .where(
                        containsTitle(title),
                        afterCreatedAt(startAt),
                        beforeCreatedAt(endAt),
                        containsManagerNickname(managerName)
                )
                .fetchOne();
    }

    @Override
    public Page<Todo> findTodosByKeywordAndCreatedAtBetweenAndManagerName(String title, LocalDateTime startAt, LocalDateTime endAt, String managerName, Pageable pageable) {
        List<Todo> contents = getContents(title, startAt, endAt, managerName, pageable);

        return PageableExecutionUtils.getPage(contents, pageable, () -> {
            Long count = getCount(title, startAt, endAt, managerName);
            return count == null ? 0 : count;
        });
    }

}
