package com.nadavramon.job_tracker.service;

import com.nadavramon.job_tracker.dto.JobMatchRequest;
import com.nadavramon.job_tracker.dto.JobMatchResponse;
import com.nadavramon.job_tracker.entity.Application;
import com.nadavramon.job_tracker.entity.User;
import com.nadavramon.job_tracker.enums.JobType;
import com.nadavramon.job_tracker.exception.ResourceNotFoundException;
import com.nadavramon.job_tracker.exception.ResourceOwnershipException;
import com.nadavramon.job_tracker.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobMatchServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CurrentUserService currentUserService;

    private JobMatchService jobMatchService;

    private User owner;
    private Application application;

    @BeforeEach
    void setUp() {
        jobMatchService = new JobMatchService(applicationRepository, currentUserService);
        owner = new User();
        owner.setId(UUID.randomUUID());
        application = new Application();
        application.setId(UUID.randomUUID());
        application.setUser(owner);
        application.setJobRole("全栈工程师");
        application.setLocation("杭州");
        application.setCompanyName("云帆科技");
        application.setJobType(JobType.FULL_TIME);
    }

    private JobMatchRequest request(String jd, List<String> extras) {
        return new JobMatchRequest(jd, extras);
    }

    private void stubOwned() {
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
    }

    @Test
    void perfectChineseJdScores100() {
        stubOwned();
        String jd = "岗位职责：负责全栈工程师相关工作。工作地点：杭州。云帆科技直招。全职，五险一金。";
        JobMatchResponse response = jobMatchService.match(application.getId(), request(jd, null));
        assertEquals(100, response.getScore());
        assertTrue(response.getMatchedKeywords().contains("全栈工程师"));
        assertTrue(response.getMatchedKeywords().contains("杭州"));
        assertEquals(0, response.getMissingKeywords().size());
    }

    @Test
    void unrelatedJdScoresZero() {
        stubOwned();
        String jd = "急聘现场厨师，包吃住，工作地点北京。";
        JobMatchResponse response = jobMatchService.match(application.getId(), request(jd, null));
        assertEquals(0, response.getScore());
        assertTrue(response.getMissingKeywords().contains("全栈工程师"));
    }

    @Test
    void partialMatchScoresProportionally() {
        stubOwned();
        // only jobRole (40) matches; location 25 + company 15 + jobType 10 missing => 40%
        String jd = "诚聘全栈工程师若干名。";
        JobMatchResponse response = jobMatchService.match(application.getId(), request(jd, null));
        // 40/90 = 44.4% -> rounds to 44
        assertEquals(44, response.getScore());
        assertEquals(40, response.getMatchedWeight());
        assertEquals(90, response.getTotalWeight());
    }

    @Test
    void extraKeywordsAddWeightWithCap() {
        stubOwned();
        String jd = "全栈工程师，React，Spring Boot，TypeScript，Kubernetes，GraphQL，杭州，云帆科技，全职。";
        // 4 base weights (90) all match + 4 extra keywords (40) => 130/130 = 100
        JobMatchResponse response = jobMatchService.match(application.getId(),
                request(jd, List.of("React", "Spring Boot", "TypeScript", "Kubernetes", "GraphQL", "Rust")));
        assertEquals(100, response.getScore());
        assertEquals(130, response.getTotalWeight());
    }

    @Test
    void englishKeywordsAreCaseInsensitive() {
        stubOwned();
        String jd = "FULL STACK engineer wanted in hangzhou. React experience required.";
        JobMatchResponse response = jobMatchService.match(application.getId(),
                request(jd, List.of("react")));
        assertTrue(response.getMatchedKeywords().contains("react"));
    }

    @Test
    void blankOrNullFieldsDoNotContributeWeight() {
        stubOwned();
        application.setCompanyName(null);
        application.setJobType(null);
        String jd = "全栈工程师，杭州。";
        JobMatchResponse response = jobMatchService.match(application.getId(), request(jd, null));
        assertEquals(100, response.getScore());
        assertEquals(65, response.getTotalWeight());
    }

    @Test
    void applicationNotFoundThrows404() {
        when(applicationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> jobMatchService.match(UUID.randomUUID(), request("jd", null)));
    }

    @Test
    void otherUsersApplicationThrowsOwnership() {
        User stranger = new User();
        stranger.setId(UUID.randomUUID());
        application.setUser(stranger);
        stubOwned();
        assertThrows(ResourceOwnershipException.class,
                () -> jobMatchService.match(application.getId(), request("jd", null)));
    }

    @Test
    void jobTypeLabelIsChinese() {
        assertEquals("全职", JobMatchService.jobTypeLabel(JobType.FULL_TIME));
        assertEquals("实习", JobMatchService.jobTypeLabel(JobType.INTERNSHIP));
        assertNull(JobMatchService.jobTypeLabel(null));
    }
}
