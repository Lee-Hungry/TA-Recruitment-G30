package com.group30.tarecruitment.profile;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaProfileServiceTest {

    @Test
    void shouldCreateAndReloadSavedProfile() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-profile-create");
        Path profileCsv = tempDir.resolve("ta_profile.csv");

        TaProfileService service = new TaProfileService(new CsvTaProfileRepository(profileCsv));

        TaProfile saved = service.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Software Engineering",
                "3.85",
                "Java,Python,Communication",
                "Weekdays after 2pm"
        ));

        TaProfile loaded = service.loadProfile("ta@g30.local");

        assertEquals(saved, loaded);
        assertEquals("MSc Software Engineering", loaded.degreeProgramme());
        assertEquals("Java,Python,Communication", loaded.skills());
    }

    @Test
    void shouldReplaceExistingProfileInsteadOfAppendingDuplicateRows() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-profile-update");
        Path profileCsv = tempDir.resolve("ta_profile.csv");

        TaProfileService service = new TaProfileService(new CsvTaProfileRepository(profileCsv));
        service.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Software Engineering",
                "3.80",
                "Java",
                "Monday"
        ));

        TaProfile updated = service.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Artificial Intelligence",
                "3.92",
                "Java,Python",
                "Monday,Wednesday"
        ));

        assertEquals("MSc Artificial Intelligence", updated.degreeProgramme());
        assertEquals(2, Files.readAllLines(profileCsv).size());
    }
}
