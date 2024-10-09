package org.example.expert.domain.todo.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class AdvancedTodoResponse {
    private final Long id;
    private final String title;
    private final Integer managerCount;
    private final Integer commentCount;
}
