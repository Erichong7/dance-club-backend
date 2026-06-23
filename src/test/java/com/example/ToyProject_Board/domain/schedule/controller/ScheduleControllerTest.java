package com.example.ToyProject_Board.domain.schedule.controller;

import com.example.ToyProject_Board.domain.performance.PerformanceFixture;
import com.example.ToyProject_Board.domain.schedule.RoomType;
import com.example.ToyProject_Board.domain.schedule.ScheduleRequest;
import com.example.ToyProject_Board.domain.schedule.ScheduleStatus;
import com.example.ToyProject_Board.domain.schedule.dto.response.ScheduleResponse;
import com.example.ToyProject_Board.domain.schedule.service.ScheduleService;
import com.example.ToyProject_Board.domain.support.ControllerTestSupport;
import com.example.ToyProject_Board.domain.team.TeamFixture;
import com.example.ToyProject_Board.domain.user.UserFixture;
import com.example.ToyProject_Board.global.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduleController.class)
@Import(SecurityConfig.class)
class ScheduleControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService scheduleService;

    @Test
    void createScheduleSuccess() throws Exception {
        ScheduleResponse response = buildScheduleResponse();
        given(scheduleService.create(any(), eq(1L))).willReturn(response);

        LocalDate futureDate = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.THURSDAY);

        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "performanceId", 5,
                                "teamId", 10,
                                "practiceDate", futureDate.toString(),
                                "startTime", "18:00:00",
                                "endTime", "20:00:00"
                        )))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createScheduleFail_missingFields() throws Exception {
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "performanceId", 5
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByWeekSuccess() throws Exception {
        given(scheduleService.getByWeek(any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/schedules")
                        .param("performanceId", "5")
                        .param("weekStart", "2024-12-02"))
                .andExpect(status().isOk());
    }

    @Test
    void assignWeekSuccess() throws Exception {
        given(scheduleService.assignWeek(eq(5L), any(), eq(1L)))
                .willReturn(List.of());

        mockMvc.perform(post("/api/schedules/assign")
                        .param("performanceId", "5")
                        .param("weekStart", "2024-12-02")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void cancelSuccess() throws Exception {
        mockMvc.perform(post("/api/schedules/100/cancel")
                        .requestAttr("userId", 1L))
                .andExpect(status().isNoContent());
    }

    private ScheduleResponse buildScheduleResponse() {
        var performance = PerformanceFixture.createWithId(5L);
        var team = TeamFixture.createWithId(10L);
        var user = UserFixture.createWithId(1L);

        ScheduleRequest request = ScheduleRequest.builder()
                .performance(performance).team(team).submittedBy(user)
                .practiceDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .build();
        ReflectionTestUtils.setField(request, "id", 100L);
        ReflectionTestUtils.setField(request, "status", ScheduleStatus.PENDING);
        return new ScheduleResponse(request);
    }
}
