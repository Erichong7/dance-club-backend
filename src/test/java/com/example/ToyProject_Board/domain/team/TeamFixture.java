package com.example.ToyProject_Board.domain.team;

import org.springframework.test.util.ReflectionTestUtils;

public class TeamFixture {

    public static Team create() {
        return Team.builder()
                .name("테스트팀")
                .build();
    }

    public static Team createWithId(Long id) {
        Team team = create();
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    public static Team createWithName(String name) {
        return Team.builder().name(name).build();
    }

    public static Team createWithNameAndId(String name, Long id) {
        Team team = createWithName(name);
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }
}
