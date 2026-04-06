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
                "Weekdays after 2pm",
                ""
        ));

        TaProfile loaded = service.loadProfile("ta@g30.local");

        assertEquals(saved, loaded);
        assertEquals("MSc Software Engineering", loaded.degreeProgramme());
        assertEquals("Java,Python,Communication", loaded.skills());
        assertEquals("", loaded.cvFilePath());
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
                "Monday",
                ""
        ));

        TaProfile updated = service.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Artificial Intelligence",
                "3.92",
                "Java,Python",
                "Monday,Wednesday",
                ""
        ));

        assertEquals("MSc Artificial Intelligence", updated.degreeProgramme());
        assertEquals(2, Files.readAllLines(profileCsv).size());
    }

    @Test
    void shouldPersistUploadedCvPathForExistingProfile() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-profile-cv");
        Path profileCsv = tempDir.resolve("ta_profile.csv");

        TaProfileService service = new TaProfileService(new CsvTaProfileRepository(profileCsv));
        service.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Software Engineering",
                "3.80",
                "Java",
                "Monday",
                ""
        ));

        TaProfile updated = service.attachCv("ta@g30.local", "C:/Users/alice/Documents/alice_cv.pdf");

        assertEquals("C:/Users/alice/Documents/alice_cv.pdf", updated.cvFilePath());
        assertEquals("C:/Users/alice/Documents/alice_cv.pdf", service.loadProfile("ta@g30.local").cvFilePath());
    }

    @Test
    void shouldLoadLegacyCsvRowsWithoutCvPath() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-profile-legacy");
        Path profileCsv = tempDir.resolve("ta_profile.csv");
        Files.writeString(
                profileCsv,
                """
                email,full_name,student_id,contact_email,degree_programme,gpa,skills,availability,updated_at
                ta@g30.local,Alice Zhang,231222001,alice@g30.local,MSc Software Engineering,3.85,"Java,Python",Weekdays,2026-03-29T16:00:00+08:00
                """
        );

        TaProfileService service = new TaProfileService(new CsvTaProfileRepository(profileCsv));
        TaProfile loaded = service.loadProfile("ta@g30.local");

        assertEquals("Alice Zhang", loaded.fullName());
        assertEquals("", loaded.cvFilePath());
    }
}
