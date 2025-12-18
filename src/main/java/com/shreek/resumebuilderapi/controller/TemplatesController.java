package com.shreek.resumebuilderapi.controller;

import com.shreek.resumebuilderapi.service.TemplatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/templates")
@Slf4j
public class TemplatesController {
    public final TemplatesService templateService;

    @GetMapping
    public ResponseEntity<?> getTemplates(Authentication authentication) {
        //Step 1: Call the Service method
        Map<String , Object> response = templateService.getTemplates(authentication.getPrincipal());

        //Step 2: Return the Response
        return ResponseEntity.ok(response);
    }
}
