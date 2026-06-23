package com.example.ToyProject_Board.domain.user;

import com.example.ToyProject_Board.domain.user.UserRole;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static User create() {
        return User.builder()
                .email("test@test.com")
                .password("encoded_password")
                .nickname("테스터")
                .build();
    }

    public static User createWithId(Long id) {
        User user = create();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    public static User createAdmin() {
        User user = User.builder()
                .email("admin@test.com")
                .password("encoded_password")
                .nickname("관리자")
                .build();
        ReflectionTestUtils.setField(user, "role", UserRole.ADMIN);
        return user;
    }

    public static User createAdminWithId(Long id) {
        User user = createAdmin();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}