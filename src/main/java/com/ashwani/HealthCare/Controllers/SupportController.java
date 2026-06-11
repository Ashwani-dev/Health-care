package com.ashwani.HealthCare.Controllers;

import com.ashwani.HealthCare.DTO.ContactRequest;
import com.ashwani.HealthCare.Service.Communication.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/support")
@RequiredArgsConstructor
public class SupportController {
    private final EmailService emailService;

    /**
     * Endpoint to receive support/contact form submissions and email them to support inbox.
     * @param request Validated ContactRequest record
     * @return ResponseEntity with success status
     */
    @PostMapping("/message")
    public ResponseEntity<?> submitContactMessage(@Valid @RequestBody ContactRequest request) {
        emailService.sendSupportMessageEmail(
            request.name(),
            request.email(),
            request.subject(),
            request.message()
        );
        return ResponseEntity.ok(Map.of("message", "Support message sent successfully"));
    }
}
