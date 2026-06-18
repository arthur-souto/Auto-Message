package com.arthursouto.dto;

import com.arthursouto.domain.PersonalInfo;

public record PersonalInfoResponse(
        Long id,
        Long resumeId,
        String fullName,
        String headline,
        String email,
        String phone,
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        String photoUrl,
        String summary,
        String linkedinUrl,
        String githubUrl,
        String portfolioUrl
) {
    public static PersonalInfoResponse from(PersonalInfo personalInfo) {
        return new PersonalInfoResponse(
                personalInfo.getId(),
                personalInfo.getResume().getId(),
                personalInfo.getFullName(),
                personalInfo.getHeadline(),
                personalInfo.getEmail(),
                personalInfo.getPhone(),
                personalInfo.getAddress(),
                personalInfo.getCity(),
                personalInfo.getState(),
                personalInfo.getCountry(),
                personalInfo.getPostalCode(),
                personalInfo.getPhotoUrl(),
                personalInfo.getSummary(),
                personalInfo.getLinkedinUrl(),
                personalInfo.getGithubUrl(),
                personalInfo.getPortfolioUrl()
        );
    }
}
