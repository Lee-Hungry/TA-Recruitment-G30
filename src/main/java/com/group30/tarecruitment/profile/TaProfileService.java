package com.group30.tarecruitment.profile;

import java.time.OffsetDateTime;

public class TaProfileService {

    private final CsvTaProfileRepository repository;

    public TaProfileService(CsvTaProfileRepository repository) {
        this.repository = repository;
    }

    public TaProfile createInitialProfile(String fullName, String email, String studentId) {
        return saveProfile(normalizeEmail(email), new TaProfileDraft(
                fullName,
                studentId,
                normalizeEmail(email),
                "",
                "",
                "",
                ""
        ));
    }

    public TaProfile saveProfile(String email, TaProfileDraft draft) {
        String normalizedEmail = normalizeEmail(email);
        validate(normalizedEmail, draft);
        TaProfile profile = new TaProfile(
                normalizedEmail,
                draft.fullName().trim(),
                draft.studentId().trim(),
                draft.contactEmail().trim(),
                draft.degreeProgramme().trim(),
                draft.gpa().trim(),
                draft.skills().trim(),
                draft.availability().trim(),
                OffsetDateTime.now().toString()
        );
        repository.upsert(profile);
        return repository.findByEmail(normalizedEmail).orElse(profile);
    }

    public TaProfile loadProfile(String email) {
        String normalizedEmail = normalizeEmail(email);
        return repository.findByEmail(normalizedEmail).orElseGet(() -> new TaProfile(
                normalizedEmail,
                "",
                "",
                normalizedEmail,
                "",
                "",
                "",
                "",
                ""
        ));
    }

    private void validate(String email, TaProfileDraft draft) {
        if (email.isBlank()) {
            throw new IllegalArgumentException("EMAIL_REQUIRED");
        }
        if (isBlank(draft.fullName())) {
            throw new IllegalArgumentException("FULL_NAME_REQUIRED");
        }
        if (isBlank(draft.studentId())) {
            throw new IllegalArgumentException("STUDENT_ID_REQUIRED");
        }
        if (isBlank(draft.contactEmail())) {
            throw new IllegalArgumentException("CONTACT_EMAIL_REQUIRED");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
