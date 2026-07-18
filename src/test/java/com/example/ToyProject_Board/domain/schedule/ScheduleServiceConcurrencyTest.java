package com.example.ToyProject_Board.domain.schedule;

import com.example.ToyProject_Board.domain.performance.Performance;
import com.example.ToyProject_Board.domain.performance.PerformanceFixture;
import com.example.ToyProject_Board.domain.performance.repository.PerformanceRepository;
import com.example.ToyProject_Board.domain.schedule.dto.request.ScheduleCreateRequest;
import com.example.ToyProject_Board.domain.schedule.repository.ScheduleRequestRepository;
import com.example.ToyProject_Board.domain.schedule.service.ScheduleService;
import com.example.ToyProject_Board.domain.team.Team;
import com.example.ToyProject_Board.domain.team.TeamFixture;
import com.example.ToyProject_Board.domain.team.TeamMember;
import com.example.ToyProject_Board.domain.team.TeamMemberRole;
import com.example.ToyProject_Board.domain.team.repository.TeamMemberRepository;
import com.example.ToyProject_Board.domain.team.repository.TeamRepository;
import com.example.ToyProject_Board.domain.user.SignupStatus;
import com.example.ToyProject_Board.domain.user.User;
import com.example.ToyProject_Board.domain.user.UserFixture;
import com.example.ToyProject_Board.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ScheduleServiceTest는 MockitoExtension 기반 순수 단위 테스트라 실제 DB 트랜잭션 경합을
 * 재현할 수 없어, 실제 스레드풀 + 실제 DB(H2)를 쓰는 통합 테스트로 별도 작성.
 */
@SpringBootTest
class ScheduleServiceConcurrencyTest {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private PerformanceRepository performanceRepository;
    @Autowired
    private ScheduleRequestRepository scheduleRequestRepository;

    @Test
    void 동시에_100개의_요청_성공() throws InterruptedException {
        // given
        Performance performance = performanceRepository.save(PerformanceFixture.create());
        Team team = teamRepository.save(TeamFixture.create());
        User leader = userRepository.save(UserFixture.create("leader@test.com", "팀장", SignupStatus.APPROVED));
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i <threadCount; i++)  {
            executorService.submit(() -> {
               try {
                   ScheduleRequest scheduleRequest = ScheduleRequest.builder()
                           .performance(performance)
                           .team(team)
                           .submittedBy(leader)
                           .practiceDate(LocalDate.now().plusWeeks(1))
                           .startTime(LocalTime.of(18, 0))
                           .endTime(LocalTime.of(20, 0))
                           .alternativeRoom(RoomType.EXTERNAL)
                           .build();
                   scheduleRequestRepository.save(scheduleRequest);
               } finally {
                   latch.countDown();
               }
            });
        }
        latch.await();

        // then
        assertEquals(threadCount, scheduleRequestRepository.count());
    }
}