package com.group30.tarecruitment.mo;

import java.util.Optional;

public class MoLoginService {

    private final CsvMoAccountRepository accountRepository;
    private final CsvSessionRepository sessionRepository;

    public MoLoginService(CsvMoAccountRepository accountRepository, CsvSessionRepository sessionRepository) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
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
}
