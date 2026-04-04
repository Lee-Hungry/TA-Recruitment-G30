# Sprint 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver every Sprint 1 backlog item for the Swing TA recruitment system using CSV-backed persistence and prototype-aligned screens.

**Architecture:** Keep authentication on top of the existing `user_account.csv` and `session_token.csv` files, then add focused CSV-backed modules for TA profiles and job postings. Build role-specific dashboard screens around small service classes so the UI stays thin and most behavior remains unit-testable.

**Tech Stack:** Java 21, Maven, JUnit 5, Java Swing, CSV file persistence

---

### Task 1: Lock down Sprint 1 domain behavior with tests

**Files:**
- Modify: `src/test/java/com/group30/tarecruitment/registration/TaRegistrationServiceTest.java`
- Create: `src/test/java/com/group30/tarecruitment/profile/TaProfileServiceTest.java`
- Create: `src/test/java/com/group30/tarecruitment/jobs/JobPostingServiceTest.java`

- [ ] Add a failing registration test that proves a successful TA registration also creates an initial TA profile row.
- [ ] Add failing TA profile tests for first-time profile creation, later profile updates, and reloading saved data.
- [ ] Add failing job posting tests for MO posting, MO viewing own postings, and TA browsing only open non-expired jobs.
- [ ] Run `mvn -q -Dtest=TaRegistrationServiceTest,TaProfileServiceTest,JobPostingServiceTest test` and confirm the new tests fail for missing profile/job functionality.

### Task 2: Implement CSV-backed TA profile support

**Files:**
- Create: `src/main/java/com/group30/tarecruitment/profile/TaProfile.java`
- Create: `src/main/java/com/group30/tarecruitment/profile/TaProfileDraft.java`
- Create: `src/main/java/com/group30/tarecruitment/profile/CsvTaProfileRepository.java`
- Create: `src/main/java/com/group30/tarecruitment/profile/TaProfileService.java`
- Modify: `src/main/java/com/group30/tarecruitment/registration/TaRegistrationService.java`
- Modify: `src/main/java/com/group30/tarecruitment/registration/CsvUserRepository.java`

- [ ] Implement a CSV repository that can read, insert, and replace TA profile rows by email.
- [ ] Implement a service that creates default profile data from registration and saves edited profile content.
- [ ] Update registration flow so successful TA registration writes both the auth account row and the initial profile row.
- [ ] Re-run `mvn -q -Dtest=TaRegistrationServiceTest,TaProfileServiceTest test` until the profile-related tests pass.

### Task 3: Implement CSV-backed MO job posting and TA job browsing

**Files:**
- Create: `src/main/java/com/group30/tarecruitment/jobs/JobPosting.java`
- Create: `src/main/java/com/group30/tarecruitment/jobs/JobPostingDraft.java`
- Create: `src/main/java/com/group30/tarecruitment/jobs/CsvJobPostingRepository.java`
- Create: `src/main/java/com/group30/tarecruitment/jobs/JobPostingService.java`

- [ ] Implement job posting persistence with fields for title, module code, description, required skills, hours per week, deadline, status, and timestamps.
- [ ] Implement service validation for required fields, positive hours, and date parsing.
- [ ] Add queries for “my postings” and “browse open jobs” with expired jobs filtered out for TA views.
- [ ] Re-run `mvn -q -Dtest=JobPostingServiceTest test` until the posting and browsing tests pass.

### Task 4: Replace placeholder Swing flows with prototype-aligned dashboards

**Files:**
- Modify: `src/main/java/com/group30/tarecruitment/AppLauncher.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/LoginFrame.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/TaDashboardFrame.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/MoDashboardFrame.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/DashboardFrame.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/MoLoginFrame.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/TaLoginPanel.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/TaRegistrationPanel.java`
- Create: `src/main/java/com/group30/tarecruitment/ui/AdminDashboardFrame.java`

- [ ] Convert app startup to a real landing/login experience with role selection and a registration path that matches the supplied login prototype.
- [ ] Build a TA dashboard with a sidebar, profile editor, and browse-jobs workspace using the TA prototype as the visual reference.
- [ ] Build an MO dashboard with job posting and my-postings views using the MO prototypes as the visual reference.
- [ ] Build an Admin dashboard shell that matches the prototype and clearly shows the admin login succeeded.

### Task 5: Verify the whole Sprint 1 slice

**Files:**
- Modify: `data/user_account.csv`
- Create: `data/ta_profile.csv`
- Create: `data/job_posting.csv`

- [ ] Seed any required CSV headers and example records so the app opens into a usable demo state.
- [ ] Run `mvn -q test` and confirm the full test suite passes.
- [ ] Run `mvn -q -DskipTests package` to verify the application compiles cleanly for the GUI flow.
