package com.shreek.resumebuilderapi.controller;

import com.shreek.resumebuilderapi.document.resume;
import com.shreek.resumebuilderapi.dto.CreateResumeRequest;
import com.shreek.resumebuilderapi.service.ResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.shreek.resumebuilderapi.utils.AppConstants.*;

@RestController
@RequestMapping(RESUME)
@RequiredArgsConstructor
@Slf4j
public class ResumeController {
    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<?> createResume(@Valid @RequestBody CreateResumeRequest request,
                                          Authentication authentication) {
        //Step 1: Call the service method
        resume newResume = resumeService.createResume(request ,authentication.getPrincipal());

        //Step 2: return response
        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);
    }

    @GetMapping
    public ResponseEntity<?> getUserResumes(Authentication authentication) {
        //Step 1: Call the service method
        List<resume> resumes = resumeService.getUserResumes(authentication.getPrincipal());

        //Step 2: return the response
        return ResponseEntity.ok(resumes);

    }

    @GetMapping(ID)
    public ResponseEntity<?> getResumeById(@PathVariable String id,
                                           Authentication authentication) {
        //Step 1: Call the Service method
        resume existingResume = resumeService.getResumeById(id, authentication.getPrincipal());

        //Step 2: return the response
        return ResponseEntity.ok(existingResume);
    }

    @PutMapping(ID)
    public ResponseEntity<?> updateResume(@PathVariable String id,
                                          @RequestBody resume updatedData,
                                          Authentication authentication) {
        //Step 1: Call the Service method
        resume updatedResume =  resumeService.updatedResume(id, updatedData, authentication.getPrincipal());

        //Step 2: return the response
        return ResponseEntity.ok(updatedResume);


    }

    @PutMapping(UPLOAD_IMAGES)
    public ResponseEntity<?> uploadResumeImages(@PathVariable String id,
                                                @RequestPart(value = "thumbnail" , required = false) MultipartFile thumbnail,
                                                @RequestPart(value = "profileImage", required = false) MultipartFile profileImage){
        return null;
    }

    @DeleteMapping(ID)
    public ResponseEntity<?> deleteResume(@PathVariable String id){
        return null;
    }

}
