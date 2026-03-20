package com.group30.tarecruitment.profile;

import java.util.UUID;

public class TaProfileService {

    private final CsvTaProfileRepository repository;

    public TaProfileService(CsvTaProfileRepository repository) {
        this.repository = repository;
    }

    public TaProfile createFirstProfile(String userId, String fullName, String studentId, String degreeProgramme,
                                        String gpa, String skills, String availability) {
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
}
