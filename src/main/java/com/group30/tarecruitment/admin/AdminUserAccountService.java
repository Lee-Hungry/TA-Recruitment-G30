package com.group30.tarecruitment.admin;

import com.group30.tarecruitment.auth.AuthRole;
import com.group30.tarecruitment.auth.UserAccount;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfile;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AdminUserAccountService {

    private final CsvUserAccountRepository userRepository;
    private final CsvTaProfileRepository profileRepository;
    private final Clock clock;

    public AdminUserAccountService(
            CsvUserAccountRepository userRepository,
            CsvTaProfileRepository profileRepository,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.clock = clock;
    }

    public List<ManagedUserAccount> listManageableAccounts() {
        return userRepository.readAll().stream()
                .filter(user -> user.role() == AuthRole.TA || user.role() == AuthRole.MO)
                .map(this::toManagedAccount)
                .sorted(Comparator.comparing(ManagedUserAccount::role)
                        .thenComparing(ManagedUserAccount::displayName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ManagedUserAccount::email, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public UserAccount deactivateAccount(String email) {
        UserAccount account = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ACCOUNT_NOT_FOUND"));
        if (account.role() == AuthRole.ADMIN) {
            throw new IllegalArgumentException("ACCOUNT_DEACTIVATE_FORBIDDEN");
        }
        if (!"ACTIVE".equalsIgnoreCase(account.status())) {
            return account;
        }
        UserAccount updated = account.withStatus("INACTIVE", OffsetDateTime.now(clock).toString());
        userRepository.replace(updated);
        return updated;
    }

    private ManagedUserAccount toManagedAccount(UserAccount user) {
        Optional<TaProfile> profile = user.role() == AuthRole.TA
                ? profileRepository.findByEmail(user.email())
                : Optional.empty();
        String displayName = profile.map(TaProfile::fullName)
                .filter(value -> value != null && !value.isBlank())
                .orElse(user.email());
        String studentId = profile.map(TaProfile::studentId).orElse("");
        return new ManagedUserAccount(
                user.userId(),
                user.email(),
                displayName,
                user.role().name(),
                user.status(),
                studentId,
                user.updatedAt()
        );
    }
}
