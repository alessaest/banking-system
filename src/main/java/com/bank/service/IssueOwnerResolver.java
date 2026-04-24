package com.bank.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IssueOwnerResolver {

    public String resolveOwnerEmail(String filePath, String fallbackCsv) {
//        if (filePath == null) return first(fallbackCsv);
//
//        if (filePath.contains("/resource/")) return "api-team@bankingsystem.com";
//        if (filePath.contains("/service/")) return "backend-team@bankingsystem.com";
//        if (filePath.contains("/repository/")) return "data-team@bankingsystem.com";

        return first(fallbackCsv);
    }

    private String first(String csv) {
        if (csv == null || csv.isBlank()) return "dev-team@bankingsystem.com";
        return csv.split(",")[0].trim();
    }
}