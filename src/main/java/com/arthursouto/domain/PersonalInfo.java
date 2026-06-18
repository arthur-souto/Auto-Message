package com.arthursouto.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "personal_info")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false, unique = true)
    private Resume resume;

    @Column(nullable = false)
    private String fullName;

    private String headline;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String photoUrl;
    private String summary;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
}
