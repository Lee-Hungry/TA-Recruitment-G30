package com.group30.tarecruitment.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthGuardTest {

    @Test
    void shouldAllowSameRole() {
        AuthGuard guard = new AuthGuard();
        assertTrue(guard.canAccess(AuthRole.ADMIN, AuthRole.ADMIN));
    }

    @Test
    void shouldRejectDifferentRole() {
        AuthGuard guard = new AuthGuard();
        assertFalse(guard.canAccess(AuthRole.TA, AuthRole.MO));
        assertThrows(SecurityException.class, () -> guard.assertAccess(AuthRole.TA, AuthRole.MO));
    }
}
