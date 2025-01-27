package io.evan.balance.ops.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import io.evan.balance.ops.service.OpsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;

class OpsControllerTest {

    @Mock
    private OpsService opsService;

    @InjectMocks
    private OpsController opsController;

    @Test
    void clean_database_success() {
        doNothing().when(opsService).cleanDatabase();

        ResponseEntity<String> response = opsController.cleanDatabase();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Database cleaned successfully", response.getBody());
    }

    @Test
    void create_sample_accounts_custom_total() {
        int customTotal = 500;
        doNothing().when(opsService).createSampleAccounts(customTotal);

        ResponseEntity<String> response = opsController.createSampleAccounts(customTotal);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("500 sample accounts created successfully", response.getBody());
    }

    @Test
    void create_sample_accounts_default_total() {
        doNothing().when(opsService).createSampleAccounts(1000);

        ResponseEntity<String> response = opsController.createSampleAccounts(null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("1000 sample accounts created successfully", response.getBody());
    }

    @Test
    void create_sample_accounts_total_exceeds_maximum() {
        ResponseEntity<String> response = opsController.createSampleAccounts(10001);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Maximum allowed accounts is 10000", response.getBody());
    }

    @Test
    void create_sample_accounts_total_less_than_zero() {
        ResponseEntity<String> response = opsController.createSampleAccounts(-1);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Total must be greater than 0", response.getBody());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
}