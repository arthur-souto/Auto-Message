package com.arthursouto.controller;

import com.arthursouto.dto.CertificationRequest;
import com.arthursouto.dto.EducationRequest;
import com.arthursouto.dto.LanguageRequest;
import com.arthursouto.dto.PersonalInfoRequest;
import com.arthursouto.dto.ProjectRequest;
import com.arthursouto.dto.ResumeFullResponse;
import com.arthursouto.dto.ResumeRequest;
import com.arthursouto.dto.SkillRequest;
import com.arthursouto.dto.WorkExperienceRequest;
import com.arthursouto.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createResume(@Valid @RequestBody ResumeRequest req) {
        resumeService.createResume(req);
    }

    @PostMapping("/{resumeId}/personal-info")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPersonalInfo(@PathVariable Long resumeId, @Valid @RequestBody PersonalInfoRequest req) {
        resumeService.createPersonalInfo(resumeId, req);
    }

    @PostMapping("/{resumeId}/work-experiences")
    @ResponseStatus(HttpStatus.CREATED)
    public void createWorkExperience(@PathVariable Long resumeId, @Valid @RequestBody WorkExperienceRequest req) {
        resumeService.createWorkExperience(resumeId, req);
    }

    @PostMapping("/{resumeId}/education")
    @ResponseStatus(HttpStatus.CREATED)
    public void createEducation(@PathVariable Long resumeId, @Valid @RequestBody EducationRequest req) {
        resumeService.createEducation(resumeId, req);
    }

    @PostMapping("/{resumeId}/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public void createSkill(@PathVariable Long resumeId, @Valid @RequestBody SkillRequest req) {
        resumeService.createSkill(resumeId, req);
    }

    @PostMapping("/{resumeId}/languages")
    @ResponseStatus(HttpStatus.CREATED)
    public void createLanguage(@PathVariable Long resumeId, @Valid @RequestBody LanguageRequest req) {
        resumeService.createLanguage(resumeId, req);
    }

    @PostMapping("/{resumeId}/certifications")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCertification(@PathVariable Long resumeId, @Valid @RequestBody CertificationRequest req) {
        resumeService.createCertification(resumeId, req);
    }

    @PostMapping("/{resumeId}/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public void createProject(@PathVariable Long resumeId, @Valid @RequestBody ProjectRequest req) {
        resumeService.createProject(resumeId, req);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResumeFullResponse getFullResume(@PathVariable Long id) {
        return resumeService.getFullResume(id);
    }
}
