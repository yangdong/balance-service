package io.evan.balance.ops.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.evan.balance.common.APIResponse;

@RestController
@RequestMapping("/v1/ops")
public class HealthCheckController {
    @GetMapping("/ping")
    public ResponseEntity<APIResponse<String>> ping() {
        return ResponseEntity.ok(APIResponse.success("pong"));
    }
}
