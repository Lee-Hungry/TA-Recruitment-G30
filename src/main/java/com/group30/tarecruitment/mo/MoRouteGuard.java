package com.group30.tarecruitment.mo;

public class MoRouteGuard {

    public boolean canAccessMoDashboard(String sessionRole) {
        return "MO".equalsIgnoreCase(sessionRole);
    }
}
