package com.group30.tarecruitment.profile;

import java.util.UUID;
import java.util.regex.Pattern;

public class TaProfileService {

    private static final Pattern GPA_PATTERN = Pattern.compile("^(?:[0-3](?:\\.\\d{1,2})?|4(?:\\.0{1,2})?)$");
    private final CsvTaProfileRepository repository;

    public TaProfileService(CsvTaProfileRepository repository) {
        this.repository = repository;
    }

    public TaProfile createFirstProfile(String userId, String fullName, String studentId, String degreeProgramme,
                                        String gpa, String skills, String availability) {
        validateBase(userId, fullName, studentId, gpa);
        if (repository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("PROFILE_ALREADY_EXISTS");
        }

        TaProfile profile = new TaProfile(
                "profile-" + UUID.randomUUID(),
                userId,
                fullName,
                studentId,
                degreeProgramme,
                gpa,
                skills,
                availability,
                ""
        );
        repository.save(profile);
        return profile;
    }

    public TaProfile loadByUserId(String userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("PROFILE_NOT_FOUND"));
    }

    public TaProfile saveOrUpdate(String userId, String fullName, String studentId, String degreeProgramme,
                                  String gpa, String skills, String availability) {
        validateBase(userId, fullName, studentId, gpa);

        return repository.findByUserId(userId)
                .map(existing -> {
                    if (!existing.studentId().equals(studentId)) {
                        throw new IllegalArgumentException("STUDENT_ID_READ_ONLY");
                    }
                    TaProfile updated = new TaProfile(
                            existing.taProfileId(),
                            existing.userId(),
                            fullName,
                            existing.studentId(),
                            degreeProgramme,
                            gpa,
                            skills,
                            availability,
                            existing.cvFilePath()
                    );
                    repository.updateByUserId(updated);
                    return updated;
                })
                .orElseGet(() -> createFirstProfile(userId, fullName, studentId, degreeProgramme, gpa, skills, availability));
    }

    private void validateBase(String userId, String fullName, String studentId, String gpa) {
        if (isBlank(userId) || isBlank(fullName) || isBlank(studentId)) {
            throw new IllegalArgumentException("REQUIRED_FIELD_MISSING");
        }
        if (isBlank(gpa) || !GPA_PATTERN.matcher(gpa).matches()) {
            throw new IllegalArgumentException("GPA_FORMAT_INVALID");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
