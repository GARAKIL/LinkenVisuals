package dev.linkenvisuals.autotest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AutoTestReportTest {
    @Test
    void countsResults() {
        AutoTestReport report = new AutoTestReport();
        report.pass();
        report.pass();
        report.skip("bind", "user data");
        report.fail("render", new IllegalStateException("boom"));

        assertEquals(2, report.passed());
        assertEquals(1, report.failed());
        assertEquals(1, report.skipped());
        assertFalse(report.ok());
        assertEquals("steps=4 ok=2 fail=1 skip=1", report.summary());
    }

    @Test
    void describesExceptionWithoutMessage() {
        AutoTestReport report = new AutoTestReport();
        report.fail("step", new IllegalArgumentException());

        assertEquals(1, report.failures().size());
        assertEquals("step: IllegalArgumentException", report.failures().get(0));
    }

    @Test
    void okWhenNothingFailed() {
        AutoTestReport report = new AutoTestReport();
        report.pass();
        report.skip("string", "text field");

        assertTrue(report.ok());
        assertEquals(0, report.failures().size());
    }

    @Test
    void capsFailureDetails() {
        AutoTestReport report = new AutoTestReport();
        for (int i = 0; i < 60; i++) {
            report.fail("step" + i, "failure");
        }

        assertEquals(60, report.failed());
        assertEquals(41, report.failures().size());
        assertEquals("... (details truncated)", report.failures().get(40));
    }
}
