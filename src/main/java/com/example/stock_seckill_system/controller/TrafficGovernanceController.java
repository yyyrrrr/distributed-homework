package com.example.stock_seckill_system.controller;

import com.example.stock_seckill_system.service.TrafficGovernanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/traffic")
public class TrafficGovernanceController {

    private final TrafficGovernanceService trafficGovernanceService;

    public TrafficGovernanceController(TrafficGovernanceService trafficGovernanceService) {
        this.trafficGovernanceService = trafficGovernanceService;
    }

    @GetMapping("/governance")
    public ResponseEntity<Map<String, Object>> governance(
            @RequestParam(defaultValue = "false") boolean fail,
            @RequestParam(defaultValue = "0") long sleepMs
    ) throws InterruptedException {
        return ResponseEntity.ok(trafficGovernanceService.guardedCall(fail, sleepMs));
    }
}
