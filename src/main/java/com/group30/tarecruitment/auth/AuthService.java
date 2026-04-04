package com.group30.tarecruitment.auth;

import com.group30.tarecruitment.auth.repository.CsvSessionTokenRepository;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private final CsvUserAccountRepository userRepository;
    private final CsvSessionTokenRepository sessionRepository;

    public AuthService(CsvUserAccountRepository userRepository, CsvSessionTokenRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    public AuthResult login(String email, String rawPassword, AuthRole expectedRole, String clientIp) {
        Optional<UserAccount> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return AuthResult.fail("ACCOUNT_NOT_FOUND");
        }
        UserAccount user = userOpt.get();

        if (!"ACTIVE".equalsIgnoreCase(user.status())) {
            return AuthResult.fail("ACCOUNT_DISABLED");
        }
        if (!user.passwordHash().equals(rawPassword)) {
            return AuthResult.fail("INVALID_CREDENTIAL");
        }
        if (user.role() != expectedRole) {
            return AuthResult.fail("ROLE_MISMATCH");
        }

        String now = OffsetDateTime.now().toString();
        String sessionId = UUID.randomUUID().toString();
        sessionRepository.append(new SessionToken(sessionId, user.userId(), now, "", "", clientIp));

        Map<AuthRole, String> routeMap = Map.of(
                AuthRole.TA, "TA_DASHBOARD",
                AuthRole.MO, "MO_DASHBOARD",
                AuthRole.ADMIN, "ADMIN_DASHBOARD"
        );
        return AuthResult.ok(sessionId, routeMap.get(expectedRole));
    }
}
