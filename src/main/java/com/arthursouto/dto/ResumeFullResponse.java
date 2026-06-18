package com.arthursouto.dto;

import com.arthursouto.domain.Certification;
import com.arthursouto.domain.Education;
import com.arthursouto.domain.Language;
import com.arthursouto.domain.PersonalInfo;
import com.arthursouto.domain.Project;
import com.arthursouto.domain.Resume;
import com.arthursouto.domain.Skill;
import com.arthursouto.domain.WorkExperience;

import java.time.Instant;
import java.util.List;

public record ResumeFullResponse(
        Long id,
        String title,
        String language,
        String templateId,
        boolean isPrimary,
        Instant createdAt,
        Instant updatedAt,
        PersonalInfoResponse personalInfo,
        List<WorkExperienceResponse> workExperiences,
        List<EducationResponse> education,
        List<SkillResponse> skills,
        List<LanguageResponse> languages,
        List<CertificationResponse> certifications,
        List<ProjectResponse> projects
) {
    public static ResumeFullResponse from(
            Resume resume,
            PersonalInfo personalInfo,
            List<WorkExperience> workExperiences,
            List<Education> education,
            List<Skill> skills,
            List<Language> languages,
            List<Certification> certifications,
            List<Project> projects
    ) {
        return new ResumeFullResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getLanguage(),
                resume.getTemplateId(),
                resume.isPrimary(),
                resume.getCreatedAt(),
                resume.getUpdatedAt(),
                personalInfo != null ? PersonalInfoResponse.from(personalInfo) : null,
                workExperiences.stream().map(WorkExperienceResponse::from).toList(),
                education.stream().map(EducationResponse::from).toList(),
                skills.stream().map(SkillResponse::from).toList(),
                languages.stream().map(LanguageResponse::from).toList(),
                certifications.stream().map(CertificationResponse::from).toList(),
                projects.stream().map(ProjectResponse::from).toList()
        );
    }
}
