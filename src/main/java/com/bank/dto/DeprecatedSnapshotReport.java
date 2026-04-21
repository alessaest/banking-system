package com.bank.dto;

import com.bank.util.DeprecatedIssueRecord;

import java.util.List;

public class DeprecatedSnapshotReport {
    public String projectKey;
    public String organization;
    public String generatedAt;
    public Filters filters;
    public Counts counts;
    public Issues issues;

    public static class Filters {
        public List<String> ruleKeys;
        public String branch;
    }

    public static class Counts {
        public int openDeprecated;
        public int resolvedDeprecated;
        public int totalDeprecatedObserved;
    }

    public static class Issues {
        public List<DeprecatedIssueRecord> open;
        public List<DeprecatedIssueRecord> resolved;
    }
}