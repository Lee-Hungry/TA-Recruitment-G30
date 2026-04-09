package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.jobs.JobPosting;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TaDashboardFrameTest {

    @Test
    void jobListItemTextShouldIncludeRequiredSkills() {
        JobPosting posting = new JobPosting(
                "job-001",
                "mo@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support tutorials and marking",
                "Java,JUnit,Scrum",
                10,
                LocalDate.of(2026, 4, 15),
                "OPEN",
                "2026-03-29T10:00:00+08:00",
                "2026-03-29T10:00:00+08:00"
        );

        String itemText = TaDashboardFrame.jobListItemText(posting);

        assertTrue(itemText.contains("Java,JUnit,Scrum"));
    }
}
