package io.evan.balance.ops.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.evan.balance.ops.service.OpsService;

@RestController
@RequestMapping("/v1/ops")
public class OpsController {
    private final OpsService opsService;

    public OpsController(OpsService opsService) {
        this.opsService = opsService;
    }

    @PostMapping("/databases/cleanup")
    public ResponseEntity<String> cleanDatabase() {
        opsService.cleanDatabase();
        return ResponseEntity.ok("Database cleaned successfully");
    }

    @PostMapping("/accounts/samples")
    public ResponseEntity<String> createSampleAccounts(@RequestParam(value = "total", required = false) Integer total) {
        int actualTotal = (total == null) ? 1000 : total;

        if (actualTotal <= 0) {
            return ResponseEntity.badRequest().body("Total must be greater than 0");
        }

        if (actualTotal > 10000) {
            return ResponseEntity.badRequest().body("Maximum allowed accounts is 10000");
        }

        opsService.createSampleAccounts(actualTotal);
        return ResponseEntity.ok(String.format("%d sample accounts created successfully", actualTotal));
    }
}
