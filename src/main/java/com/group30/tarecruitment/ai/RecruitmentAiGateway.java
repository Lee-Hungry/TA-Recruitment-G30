package com.group30.tarecruitment.ai;

import com.group30.tarecruitment.admin.TaWorkloadSummary;
import com.group30.tarecruitment.admin.WorkloadCandidateOption;
import com.group30.tarecruitment.admin.WorkloadSuggestion;
import com.group30.tarecruitment.applications.MoApplicantView;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.matching.SkillMatchResult;
import com.group30.tarecruitment.profile.TaProfile;

import java.util.List;
import java.util.Map;

public interface RecruitmentAiGateway {

    SkillMatchResult analyzeSkillMatch(TaProfile profile, JobPosting posting);

    Map<String, SkillMatchResult> analyzeApplicants(JobPosting posting, List<MoApplicantView> applicants);

    WorkloadSuggestion suggestWorkloadAdjustment(
            TaWorkloadSummary overloaded,
            int threshold,
            List<JobPosting> acceptedJobs,
            Map<String, List<WorkloadCandidateOption>> candidatesByJobId
    );
}
