package com.group30.tarecruitment.admin;

import com.group30.tarecruitment.auth.AuthRole;
import com.group30.tarecruitment.auth.UserAccount;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfileDraft;
import com.group30.tarecruitment.profile.TaProfileService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminUserAccountServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void shouldListTaAndMoAccountsAndDeactivateSelectedUser() throws Exception {
        Path tempDir = Files.createTempDirectory("admin-user-accounts");
        Path userCsv = tempDir.resolve("user_account.csv");
        Path profileCsv = tempDir.resolve("ta_profile.csv");

        CsvUserAccountRepository userRepository = new CsvUserAccountRepository(userCsv);
        userRepository.append(new UserAccount("ta-001", "ta@g30.local", "ta123456", AuthRole.TA,
                "ACTIVE", "2026-03-20T11:10:00+08:00", "2026-03-20T11:10:00+08:00"));
        userRepository.append(new UserAccount("mo-001", "mo@g30.local", "mo123456", AuthRole.MO,
                "ACTIVE", "2026-03-20T11:20:00+08:00", "2026-03-20T11:20:00+08:00"));
        userRepository.append(new UserAccount("admin-001", "admin@g30.local", "admin123", AuthRole.ADMIN,
                "ACTIVE", "2026-03-20T11:30:00+08:00", "2026-03-20T11:30:00+08:00"));

        TaProfileService profileService = new TaProfileService(new CsvTaProfileRepository(profileCsv));
        profileService.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Software Engineering",
                "3.82",
                "Java,Communication",
                "Weekdays after 2pm",
                ""
        ));

        AdminUserAccountService service = new AdminUserAccountService(
                userRepository,
                new CsvTaProfileRepository(profileCsv),
                fixedClock
        );

        List<ManagedUserAccount> accounts = service.listManageableAccounts();
        service.deactivateAccount("ta@g30.local");

        assertEquals(2, accounts.size());
        assertEquals("Alice Zhang", accounts.stream()
                .filter(account -> account.email().equals("ta@g30.local"))
                .findFirst()
                .orElseThrow()
                .displayName());
        assertEquals("INACTIVE", userRepository.findByEmail("ta@g30.local").orElseThrow().status());
    }
}
