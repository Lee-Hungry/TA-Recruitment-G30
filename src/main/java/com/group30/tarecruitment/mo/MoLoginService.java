package com.group30.tarecruitment.mo;

import java.util.Optional;

public class MoLoginService {

    private final CsvMoAccountRepository accountRepository;
    private final CsvSessionRepository sessionRepository;
    private final MoRouteGuard routeGuard;

    public MoLoginService(CsvMoAccountRepository accountRepository, CsvSessionRepository sessionRepository) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.routeGuard = new MoRouteGuard();
    }

    public MoLoginResult login(String email, String password) {
        Optional<MoAccount> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isEmpty()) {
            return MoLoginResult.fail("AUTH_FAILED_ACCOUNT_NOT_FOUND");
        }

        MoAccount account = accountOpt.get();
        if (!"MO".equalsIgnoreCase(account.role())) {
            return MoLoginResult.fail("AUTH_FAILED_MO_ONLY");
        }
        if (!"ACTIVE".equalsIgnoreCase(account.status())) {
            return MoLoginResult.fail("AUTH_FAILED_ACCOUNT_DISABLED");
        }
        if (!account.password().equals(password)) {
            return MoLoginResult.fail("AUTH_FAILED_INVALID_CREDENTIAL");
        }

        String sessionId = sessionRepository.createMoSession(account.userId());
        return MoLoginResult.ok(sessionId);
    }

    public boolean canAccessMoDashboard(String sessionId, String sessionRole) {
        if (!routeGuard.canAccessMoDashboard(sessionRole)) {
            return false;
        }
        return sessionRepository.findBySessionId(sessionId)
                .map(routeGuard::canAccessMoDashboard)
                .orElse(false);
    }

    public boolean expireSession(String sessionId) {
        return sessionRepository.expireSessionNow(sessionId);
    }
}
