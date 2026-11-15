package com.nadavramon.job_tracker.service;

import com.nadavramon.job_tracker.dto.JobMatchRequest;
import com.nadavramon.job_tracker.dto.JobMatchResponse;
import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.enums.JobType;
import com.nadavramon.job_tracker.exception.ResourceNotFoundException;
import com.nadavramon.job_tracker.exception.ResourceOwnershipException;
import com.nadavramon.job_tracker.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Lightweight bilingual (Chinese/English) JD-to-application matching score.
 * Pure Java: keyword weights over case-insensitive containment matching, no
 * external tokenizer dependency. Weights: jobRole 40, location 25, companyName 15,
 * jobType 10, each extra keyword 10 (capped at 40).
 */
@Service
public class JobMatchService {

    static final int WEIGHT_JOB_ROLE = 40;
    static final int WEIGHT_LOCATION = 25;
    static final int WEIGHT_COMPANY = 15;
    static final int WEIGHT_JOB_TYPE = 10;
    static final int WEIGHT_EXTRA_KEYWORD = 10;
    static final int EXTRA_KEYWORD_CAP = 40;

    private final ApplicationRepository applicationRepository;
    private final CurrentUserService currentUserService;

    public JobMatchService(ApplicationRepository applicationRepository, CurrentUserService currentUserService) {
        this.applicationRepository = applicationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public JobMatchResponse match(UUID applicationId, JobMatchRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        User user = currentUserService.getCurrentUser();
        if (!application.getUser().getId().equals(user.getId()))
            throw new ResourceOwnershipException("Access denied");
        return score(application, request);
    }

    /** Pure scoring function, package-private for unit tests. */
    JobMatchResponse score(Application application, JobMatchRequest request) {
        String jd = request.getJdText() == null ? "" : request.getJdText().toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        int[] matchedWeight = {0};
        int[] totalWeight = {0};

        check(jd, application.getJobRole(), WEIGHT_JOB_ROLE, matched, missing, matchedWeight, totalWeight);
        check(jd, application.getLocation(), WEIGHT_LOCATION, matched, missing, matchedWeight, totalWeight);
        check(jd, application.getCompanyName(), WEIGHT_COMPANY, matched, missing, matchedWeight, totalWeight);
        check(jd, jobTypeLabel(application.getJobType()), WEIGHT_JOB_TYPE, matched, missing, matchedWeight, totalWeight);

        List<String> extras = request.getExtraKeywords() == null ? List.of() : request.getExtraKeywords();
        int extraBudget = EXTRA_KEYWORD_CAP;
        for (String keyword : extras) {
            if (keyword == null || keyword.isBlank() || extraBudget <= 0) continue;
            int weight = Math.min(WEIGHT_EXTRA_KEYWORD, extraBudget);
            extraBudget -= weight;
            check(jd, keyword.trim(), weight, matched, missing, matchedWeight, totalWeight);
        }

        int score = totalWeight[0] == 0 ? 0 : Math.round(matchedWeight[0] * 100f / totalWeight[0]);
        return new JobMatchResponse(score, matchedWeight[0], totalWeight[0], matched, missing);
    }

    private void check(String jd, String keyword, int weight, List<String> matched, List<String> missing,
                       int[] matchedWeight, int[] totalWeight) {
        if (keyword == null || keyword.isBlank()) return;
        totalWeight[0] += weight;
        boolean hit = jd.contains(keyword.toLowerCase(Locale.ROOT).trim());
        if (hit) {
            matched.add(keyword.trim());
            matchedWeight[0] += weight;
        } else {
            missing.add(keyword.trim());
        }
    }

    static String jobTypeLabel(JobType jobType) {
        if (jobType == null) return null;
        return switch (jobType) {
            case FULL_TIME -> "全职";
            case PART_TIME -> "兼职";
            case CONTRACT -> "合同";
            case INTERNSHIP -> "实习";
        };
    }
}
