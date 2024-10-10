package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.entity.Timestamped;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "logs")
public class Log extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long requestUserId;

    @Column(nullable = false)
    Long todoId;

    @Column(nullable = false)
    Long managerUserId;

    @Builder
    public Log(Long id, Long requestUserId, Long todoId, Long managerUserId) {
        this.id = id;
        this.requestUserId = requestUserId;
        this.todoId = todoId;
        this.managerUserId = managerUserId;
    }
}
