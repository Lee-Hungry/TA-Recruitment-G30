package com.group30.tarecruitment.matching;

import com.group30.tarecruitment.ai.TestRecruitmentAiGateway;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillMatchingServiceTest {

    private final SkillMatchingService service = new SkillMatchingService(new TestRecruitmentAiGateway());

    @Test
    void shouldIdentifyMatchedAndMissingSkills() {
        SkillMatchResult result = service.analyze(
                "Java, Deep Learning, Communication",
                "Java, Python, DeepLearning"
        );

        assertEquals(67, result.matchScore());
        assertTrue(result.matchedSkills().contains("Java"));
        assertTrue(result.matchedSkills().contains("DeepLearning"));
        assertEquals(1, result.missingSkills().size());
        assertTrue(result.missingSkills().contains("Python"));
    }
}
