package com.example.ToyProject_Board.domain.performance.controller;

import com.example.ToyProject_Board.domain.performance.dto.request.PerformanceCreateRequest;
import com.example.ToyProject_Board.domain.performance.dto.response.PerformanceResponse;
import com.example.ToyProject_Board.domain.performance.service.PerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @PostMapping
    public ResponseEntity<PerformanceResponse> create(
            @Valid @RequestBody PerformanceCreateRequest request,
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(performanceService.create(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<PerformanceResponse>> getAll() {
        return ResponseEntity.ok(performanceService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(performanceService.getOne(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId) {
        performanceService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
