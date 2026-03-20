package com.group30.tarecruitment.auth;

public class AuthGuard {

    public boolean canAccess(AuthRole sessionRole, AuthRole requiredRole) {
        return sessionRole == requiredRole;
    }

    public void assertAccess(AuthRole sessionRole, AuthRole requiredRole) {
        if (!canAccess(sessionRole, requiredRole)) {
            throw new SecurityException("Access denied for role: " + sessionRole);
        }
    }
}
