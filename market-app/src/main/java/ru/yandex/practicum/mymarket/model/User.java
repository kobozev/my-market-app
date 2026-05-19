package ru.yandex.practicum.mymarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("users")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private boolean enabled;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
