package com.planeo.planeo_admin.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InvitationTokenGeneratorTest {

    private final InvitationTokenGenerator generator = new InvitationTokenGenerator();

    @Test
    void generatesHighEntropyUrlSafeTokens() {
        String token = generator.generateToken();

        assertThat(token).doesNotContain("+", "/", "=");
        assertThat(token.length()).isGreaterThanOrEqualTo(40);
    }

    @Test
    void generatesUniqueTokensAcrossManyCalls() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            tokens.add(generator.generateToken());
        }
        assertThat(tokens).hasSize(1000);
    }

    @Test
    void hashIsDeterministicForTheSameToken() {
        String token = generator.generateToken();
        assertThat(generator.hash(token)).isEqualTo(generator.hash(token));
    }

    @Test
    void hashDiffersForDifferentTokens() {
        String tokenA = generator.generateToken();
        String tokenB = generator.generateToken();
        assertThat(generator.hash(tokenA)).isNotEqualTo(generator.hash(tokenB));
    }

    @Test
    void hashNeverEqualsTheRawToken() {
        String token = generator.generateToken();
        assertThat(generator.hash(token)).isNotEqualTo(token);
    }
}
