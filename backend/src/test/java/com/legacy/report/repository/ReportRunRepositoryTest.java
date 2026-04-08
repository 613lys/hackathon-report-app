package com.legacy.report.repository;

import com.legacy.report.model.ReportRun;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReportRunRepositoryTest {

    @Autowired
    private ReportRunRepository reportRunRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByMakerUsernameOrderByGeneratedAtDesc_returnsLatestFirst() {
        ReportRun older = persistRun("maker-alpha", "Generated", LocalDateTime.now().minusHours(2));
        ReportRun newer = persistRun("maker-alpha", "Generated", LocalDateTime.now());

        List<ReportRun> results = reportRunRepository.findByMakerUsernameOrderByGeneratedAtDesc("maker-alpha");

        assertThat(results)
                .hasSize(2)
                .extracting(ReportRun::getId)
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    void findByStatusOrderBySubmittedAtAsc_returnsChronologicalList() {
        ReportRun first = persistSubmittedRun(LocalDateTime.now().minusMinutes(20));
        ReportRun second = persistSubmittedRun(LocalDateTime.now().minusMinutes(5));

        List<ReportRun> results = reportRunRepository.findByStatusOrderBySubmittedAtAsc("Submitted");

        List<ReportRun> matching = results.stream()
                .filter(run -> run.getId().equals(first.getId()) || run.getId().equals(second.getId()))
                .toList();

        assertThat(matching)
                .hasSize(2)
                .isSortedAccordingTo((a, b) -> a.getSubmittedAt().compareTo(b.getSubmittedAt()))
                .extracting(ReportRun::getId)
                .containsExactly(first.getId(), second.getId());
    }

    @Test
    void findByCheckerUsernameOrderByDecidedAtDesc_returnsLatestDecisionFirst() {
        ReportRun olderDecision = persistDecidedRun("checker-one", LocalDateTime.now().minusHours(1));
        ReportRun latestDecision = persistDecidedRun("checker-one", LocalDateTime.now());

        List<ReportRun> results = reportRunRepository.findByCheckerUsernameOrderByDecidedAtDesc("checker-one");

        assertThat(results)
                .isNotEmpty()
                .extracting(ReportRun::getId)
                .contains(latestDecision.getId(), olderDecision.getId())
                .startsWith(latestDecision.getId());
    }

    private ReportRun persistRun(String maker, String status, LocalDateTime generatedAt) {
        ReportRun run = new ReportRun();
        run.setReportId(999L);
        run.setReportName("Fixture");
        run.setMakerUsername(maker);
        run.setStatus(status);
        run.setGeneratedAt(generatedAt);
        entityManager.persist(run);
        return run;
    }

    private ReportRun persistSubmittedRun(LocalDateTime submittedAt) {
        ReportRun run = persistRun("maker-alpha", "Submitted", submittedAt.minusMinutes(1));
        run.setSubmittedAt(submittedAt);
        return entityManager.merge(run);
    }

    private ReportRun persistDecidedRun(String checkerUsername, LocalDateTime decidedAt) {
        ReportRun run = persistRun("maker-alpha", "Submitted", decidedAt.minusMinutes(10));
        run.setStatus("Approved");
        run.setSubmittedAt(decidedAt.minusMinutes(5));
        run.setCheckerUsername(checkerUsername);
        run.setDecidedAt(decidedAt);
        return entityManager.merge(run);
    }
}
