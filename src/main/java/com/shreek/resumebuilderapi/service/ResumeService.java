package com.shreek.resumebuilderapi.service;


import com.shreek.resumebuilderapi.document.resume;
import com.shreek.resumebuilderapi.dto.AuthResponse;
import com.shreek.resumebuilderapi.dto.CreateResumeRequest;
import com.shreek.resumebuilderapi.repository.ResumeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AuthService authService;

    public resume createResume(CreateResumeRequest request, Object principalObject) {
        //Step 1: Create resume Object
        resume newResume = new resume();

        //Step 2: Get the current profile
        AuthResponse response = authService.getProfile(principalObject);

        //Step 3: update the resume object
        newResume.setUserId(response.getId());
        newResume.setTitle(request.getTitle());

        //Step 4: Set default data for resume
        setDefaultResumeData(newResume);

        //Step 5:save the resume data
        return resumeRepository.save(newResume);
    }

    private void setDefaultResumeData(resume newResume) {
        newResume.setProfileInfo(new resume.ProfileInfo());
        newResume.setContactInfo(new resume.ContactInfo());
        newResume.setWorkExperience(new ArrayList<>());
        newResume.setEducation(new ArrayList<>());
        newResume.setSkills(new ArrayList<>());
        newResume.setProjects(new ArrayList<>());
        newResume.setCertifications(new ArrayList<>());
        newResume.setLanguages(new ArrayList<>());
        newResume.setInterests(new ArrayList<>());
    }

    public List<resume> getUserResumes(Object principal) {
        //Step 1: Get the current profile
        AuthResponse response = authService.getProfile(principal);

        //Step 2: Call the repository finder method
        List<resume> resumes =  resumeRepository.findByUserIdOrderByUpdatedAtDesc(response.getId());

        //Step 3: return response
        return resumes;
    }

    public resume getResumeById(String resumeId, Object principal) {
        //Step 1: Get the current profile
        AuthResponse response = authService.getProfile(principal);

        //Step 2: call the repository finder method
        resume existingResume = resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        //Step 3: return result
        return existingResume;

    }

    public resume updatedResume(String resumeId, resume updatedData, Object principal) {
        //Step 1: call the service method
        AuthResponse response = authService.getProfile(principal);

        //Step 2: call the repository finder method
        resume existingResume = resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        //Step 3: update the new data
        existingResume.setTitle(updatedData.getTitle());
        existingResume.setTemplate(updatedData.getTemplate());
        existingResume.setThumbnailLink(updatedData.getThumbnailLink());
        existingResume.setProfileInfo(updatedData.getProfileInfo());
        existingResume.setContactInfo(updatedData.getContactInfo());
        existingResume.setWorkExperience(updatedData.getWorkExperience());
        existingResume.setEducation(updatedData.getEducation());
        existingResume.setSkills(updatedData.getSkills());
        existingResume.setProjects(updatedData.getProjects());
        existingResume.setCertifications(updatedData.getCertifications());
        existingResume.setLanguages(updatedData.getLanguages());
        existingResume.setInterests(updatedData.getInterests());

        //Step 4: save the updated data
        resumeRepository.save(existingResume);

        //Step 5: return new data
        return existingResume;

    }

    public void deleteResume(String resumeId, Object principal) {
        // Step 1: get the current profile
        AuthResponse response =authService.getProfile(principal);

        //step 2: call the repo finder methods
        resume existingResume = resumeRepository.findByUserIdAndId(response.getId(),resumeId)
                .orElseThrow(() ->  new RuntimeException("Resume not found"));

        resumeRepository.delete(existingResume);
    }
}
