package com.group30.tarecruitment.profile;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
