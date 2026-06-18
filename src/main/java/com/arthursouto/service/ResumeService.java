package com.arthursouto.service;

import com.arthursouto.domain.Resume;
import com.arthursouto.dto.CertificationRequest;
import com.arthursouto.dto.EducationRequest;
import com.arthursouto.dto.LanguageRequest;
import com.arthursouto.dto.PersonalInfoRequest;
import com.arthursouto.dto.ProjectRequest;
import com.arthursouto.dto.ResumeFullResponse;
import com.arthursouto.dto.ResumeRequest;
import com.arthursouto.dto.SkillRequest;
import com.arthursouto.dto.WorkExperienceRequest;
import com.arthursouto.exception.ResourceNotFoundException;
import com.arthursouto.helper.AuthenticatedUser;
import com.arthursouto.repository.CertificationRepository;
import com.arthursouto.repository.EducationRepository;
import com.arthursouto.repository.LanguageRepository;
import com.arthursouto.repository.PersonalInfoRepository;
import com.arthursouto.repository.ProjectRepository;
import com.arthursouto.repository.ResumeRepository;
import com.arthursouto.repository.SkillRepository;
import com.arthursouto.repository.WorkExperienceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final WorkExperienceRepository workExperienceRepository;
    private final EducationRepository educationRepository;
    private final SkillRepository skillRepository;
    private final LanguageRepository languageRepository;
    private final CertificationRepository certificationRepository;
    private final ProjectRepository projectRepository;


    @Transactional
    public void createResume(ResumeRequest req) {
        final var resume =  ResumeRequest.from(req);
        final var user = AuthenticatedUser.get();

        resume.setUser(user);

        resumeRepository.save(resume);
    }


    @Transactional
    public void createPersonalInfo(Long resumeId, PersonalInfoRequest req) {
        final var resume = findResumeOrThrow(resumeId);
        final var personalInfo = PersonalInfoRequest.from(req);

        personalInfo.setResume(resume);

        personalInfoRepository.save(personalInfo);
    }

    @Transactional
    public void createWorkExperience(Long resumeId, WorkExperienceRequest req) {
        final var resume = findResumeOrThrow(resumeId);
        final var workExperience = WorkExperienceRequest.from(req);

        workExperience.setResume(resume);

        workExperienceRepository.save(workExperience);
    }

    @Transactional
    public void createEducation(Long resumeId, EducationRequest req) {
        final var resume = findResumeOrThrow(resumeId);
        final var education = EducationRequest.from(req);

        education.setResume(resume);

        educationRepository.save(education);
    }

    @Transactional
    public void createSkill(Long resumeId, SkillRequest req) {
        final var resume = findResumeOrThrow(resumeId);
        final var skill = SkillRequest.from(req);

        skill.setResume(resume);

        skillRepository.save(skill);
    }

    @Transactional
    public void createLanguage(Long resumeId, LanguageRequest req) {
        final var resume = findResumeOrThrow(resumeId);
        final var language = LanguageRequest.from(req);

        language.setResume(resume);

        languageRepository.save(language);
    }

    @Transactional
    public void createCertification(Long resumeId, CertificationRequest req) {
        final var resume = findResumeOrThrow(resumeId);
        final var certification = CertificationRequest.from(req);

        certification.setResume(resume);

        certificationRepository.save(certification);
    }

    @Transactional
    public void createProject(Long resumeId, ProjectRequest req) {
        final var resume = findResumeOrThrow(resumeId);
        final var project = ProjectRequest.from(req);

        project.setResume(resume);

        projectRepository.save(project);
    }

    // responses

    @Transactional(readOnly = true)
    public ResumeFullResponse getFullResume(Long resumeId) {
        final var resume = resumeRepository.findByIdAndUserId(resumeId, AuthenticatedUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        final var personalInfo = personalInfoRepository.findByResumeId(resumeId).orElse(null);
        final var workExperiences = workExperienceRepository.findByResumeIdOrderByOrderIndexAsc(resumeId);
        final var education = educationRepository.findByResumeIdOrderByOrderIndexAsc(resumeId);
        final var skills = skillRepository.findByResumeIdOrderByOrderIndexAsc(resumeId);
        final var languages = languageRepository.findByResumeIdOrderByOrderIndexAsc(resumeId);
        final var certifications = certificationRepository.findByResumeIdOrderByOrderIndexAsc(resumeId);
        final var projects = projectRepository.findByResumeIdOrderByOrderIndexAsc(resumeId);

        return ResumeFullResponse.from(
                resume,
                personalInfo,
                workExperiences,
                education,
                skills,
                languages,
                certifications,
                projects
        );
    }

    private Resume findResumeOrThrow(Long resumeId) {
        return resumeRepository.findByIdAndUserId(resumeId, AuthenticatedUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

}
