package com.arthursouto.dto;

import com.arthursouto.domain.PersonalInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record PersonalInfoRequest(
        @NotBlank @Size(max = 150) String fullName,
        @Size(max = 150) String headline,
        @Email @Size(max = 150) String email,
        @Size(max = 30) String phone,
        @Size(max = 255) String address,
        @Size(max = 100) String city,
        @Size(max = 100) String state,
        @Size(max = 100) String country,
        @Size(max = 20) String postalCode,
        @URL @Size(max = 500) String photoUrl,
        String summary,
        @URL @Size(max = 500) String linkedinUrl,
        @URL @Size(max = 500) String githubUrl,
        @URL @Size(max = 500) String portfolioUrl
) {
    public static PersonalInfo from(PersonalInfoRequest req) {
        return PersonalInfo.builder()
                .fullName(req.fullName())
                .headline(req.headline())
                .email(req.email())
                .phone(req.phone())
                .address(req.address())
                .city(req.city())
                .state(req.state())
                .country(req.country())
                .postalCode(req.postalCode())
                .photoUrl(req.photoUrl())
                .summary(req.summary())
                .linkedinUrl(req.linkedinUrl())
                .githubUrl(req.githubUrl())
                .portfolioUrl(req.portfolioUrl())
                .build();
    }
}
