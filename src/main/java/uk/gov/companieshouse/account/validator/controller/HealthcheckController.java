package uk.gov.companieshouse.account.validator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/account-validator/healthcheck")
public class HealthcheckController {

    @GetMapping
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok().body("OK");
    }
}
