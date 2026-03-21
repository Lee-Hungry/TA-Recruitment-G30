package com.group30.tarecruitment.profile;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaProfileServiceTest {

    @Test
    void shouldSaveInitialProfile() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-profile");
        CsvTaProfileRepository repository = new CsvTaProfileRepository(tempDir.resolve("ta_profile.csv"));
        TaProfileService service = new TaProfileService(repository);

        TaProfile profile = service.createFirstProfile("ta-user-1", "Alice", "231222001", "Computer Science",
                "3.80", "Java;CSV", "Mon-Fri");

        assertEquals("ta-user-1", profile.userId());
        assertEquals(1, repository.readAll().size());
    }

    @Test
    void shouldUpdateExistingProfileAndKeepStudentId() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-profile-update");
        CsvTaProfileRepository repository = new CsvTaProfileRepository(tempDir.resolve("ta_profile.csv"));
        TaProfileService service = new TaProfileService(repository);

        service.createFirstProfile("ta-user-1", "Alice", "231222001", "Computer Science",
                "3.80", "Java;CSV", "Mon-Fri");

        TaProfile updated = service.saveOrUpdate("ta-user-1", "Alice Zhang", "231222001", "Software Engineering",
                "3.90", "Java;CSV;Swing", "Tue-Thu");

        assertEquals("231222001", updated.studentId());
        assertEquals("Alice Zhang", service.loadByUserId("ta-user-1").fullName());
    }

    @Test
    void shouldRejectInvalidGpa() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-profile-gpa");
        CsvTaProfileRepository repository = new CsvTaProfileRepository(tempDir.resolve("ta_profile.csv"));
        TaProfileService service = new TaProfileService(repository);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createFirstProfile("ta-user-1", "Alice", "231222001", "Computer Science",
                        "4.80", "Java", "Mon-Fri"));

        assertEquals("GPA_FORMAT_INVALID", ex.getMessage());
    }
}
