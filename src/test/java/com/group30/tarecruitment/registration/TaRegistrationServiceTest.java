package com.group30.tarecruitment.registration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaRegistrationServiceTest {

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        Path tempDir = Files.createTempDirectory("registration-test");
        Path userCsv = tempDir.resolve("user_account.csv");

        CsvUserRepository repository = new CsvUserRepository(userCsv);
        repository.saveTaAccount(new TaRegistrationRequest("Alice", "alice@g30.local", "231222001", "password1"));

        TaRegistrationService service = new TaRegistrationService(repository);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.register(new TaRegistrationRequest("Alice 2", "alice@g30.local", "231222002", "password2")));

        assertEquals("EMAIL_ALREADY_EXISTS", ex.getMessage());
    }
}
