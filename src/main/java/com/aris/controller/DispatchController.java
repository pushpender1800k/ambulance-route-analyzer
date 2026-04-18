package com.aris.controller;

import com.aris.dto.DispatchRequest;
import com.aris.model.Dispatch;
import com.aris.service.DispatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchController {

    private final DispatchService dispatchService;

    public DispatchController(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping
    public ResponseEntity<?> dispatch(@Valid @RequestBody DispatchRequest request) {
        try {
            Dispatch response = dispatchService.dispatch(request);
            return ResponseEntity.ok(response);
        } catch (com.aris.exception.AlreadyAssignedException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                .body(java.util.Map.of(
                    "error", "ALREADY_ASSIGNED",
                    "message", e.getMessage(),
                    "code", 409
                ));
        } catch (com.aris.exception.UnitNotAvailableException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.CONFLICT)
                .body(java.util.Map.of(
                    "error", "UNIT_NOT_AVAILABLE",
                    "message", e.getMessage(),
                    "code", 409
                ));
        }
    }
}
