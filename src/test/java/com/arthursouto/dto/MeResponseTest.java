package com.arthursouto.dto;

import com.arthursouto.factory.UserFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeResponseTest {

    @Test
    void fromMapsAllExpectedUserFields() {
        var user = UserFactory.user();

        MeResponse response = MeResponse.from(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.googleId()).isEqualTo(user.getGoogleId());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.profileImage()).isEqualTo(user.getProfileImage());
    }

    @Test
    void fromHandlesNullGoogleIdAndProfileImage() {
        var user = UserFactory.userBuilder()
                .googleId(null)
                .profileImage(null)
                .build();

        MeResponse response = MeResponse.from(user);

        assertThat(response.googleId()).isNull();
        assertThat(response.profileImage()).isNull();
    }
}
