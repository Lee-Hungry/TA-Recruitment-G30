package com.group30.tarecruitment.mo;

public class MoRouteGuard {

    public boolean canAccessMoDashboard(String sessionRole) {
        return "MO".equalsIgnoreCase(sessionRole);
    }

    public boolean canAccessMoDashboard(MoSession session) {
        if (session == null) {
            return false;
        }
        return canAccessMoDashboard(session.role()) && !session.expired();
    }
}
