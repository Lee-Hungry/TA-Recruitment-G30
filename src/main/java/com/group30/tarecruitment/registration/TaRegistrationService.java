package com.group30.tarecruitment.registration;

import java.util.regex.Pattern;

public class TaRegistrationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^\\d{9}$");
    private final CsvUserRepository userRepository;

    public TaRegistrationService(CsvUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(TaRegistrationRequest request) {
        validate(request);
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("EMAIL_ALREADY_EXISTS");
        }
        userRepository.saveTaAccount(request);
    }

    private void validate(TaRegistrationRequest request) {
        if (isBlank(request.fullName())) {
            throw new IllegalArgumentException("FULL_NAME_REQUIRED");
        }
        if (!EMAIL_PATTERN.matcher(request.email()).matches()) {
            throw new IllegalArgumentException("EMAIL_FORMAT_INVALID");
        }
        if (isBlank(request.studentId())) {
            throw new IllegalArgumentException("STUDENT_ID_REQUIRED");
        }
        if (!STUDENT_ID_PATTERN.matcher(request.studentId()).matches()) {
            throw new IllegalArgumentException("STUDENT_ID_FORMAT_INVALID");
        }
        if (isBlank(request.password()) || request.password().length() < 8) {
            throw new IllegalArgumentException("PASSWORD_TOO_SHORT");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
