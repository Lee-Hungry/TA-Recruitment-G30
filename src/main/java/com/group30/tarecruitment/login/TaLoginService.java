package com.group30.tarecruitment.login;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public class TaLoginService {

    private final CsvUserCredentialRepository userRepository;
    private final CsvSessionTokenRepository sessionRepository;

    public TaLoginService(CsvUserCredentialRepository userRepository, CsvSessionTokenRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public TaLoginResult login(String email, String password, String clientIp) {
        Optional<UserCredential> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return TaLoginResult.fail(LoginErrorCode.ACCOUNT_NOT_FOUND);
        }

        UserCredential user = userOpt.get();
        if (!"TA".equalsIgnoreCase(user.role())) {
            return TaLoginResult.fail(LoginErrorCode.ROLE_NOT_TA);
        }
        if (!"ACTIVE".equalsIgnoreCase(user.status())) {
            return TaLoginResult.fail(LoginErrorCode.ACCOUNT_DISABLED);
        }
        if (!user.password().equals(password)) {
            return TaLoginResult.fail(LoginErrorCode.INVALID_CREDENTIAL);
        }

        String sessionId = UUID.randomUUID().toString();
        sessionRepository.append(sessionId, user.userId(), OffsetDateTime.now().toString(), clientIp);
        return TaLoginResult.ok(sessionId);
    }

    public boolean logout(String sessionId) {
        return sessionRepository.revoke(sessionId, OffsetDateTime.now().toString());
    }
}
