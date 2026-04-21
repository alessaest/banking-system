package com.bank.service;

import com.bank.util.DeprecatedIssueRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class DeprecatedCodeCollectorService {

    private static final String OPEN_DEPRECATED = "openDeprecated";
    private static final String COUNTS = "counts";
    private static final String COUNT = "count";
    private static final String ISSUES = "issues";


    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public Path collectAndWriteSnapshot(
            String sonarBaseUrl,
            String organization,
            String projectKey,
            String sonarToken,
            String rules,
            String severities
    ) throws IOException, InterruptedException {

        List<DeprecatedIssueRecord> openIssues = fetchDeprecatedIssues(
                sonarBaseUrl, organization, projectKey, sonarToken, false, rules, severities
        );

        List<DeprecatedIssueRecord> resolvedIssues = fetchDeprecatedIssues(
                sonarBaseUrl, organization, projectKey, sonarToken, true, rules, severities
        );

        String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put(OPEN_DEPRECATED, openIssues.size());
        counts.put("resolvedDeprecated", resolvedIssues.size());
        counts.put("totalDeprecatedObserved", openIssues.size() + resolvedIssues.size());

        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("organization", organization);
        filters.put("rules", rules);
        filters.put("severities", severities);

        Map<String, Object> groupedIssues = new LinkedHashMap<>();
        groupedIssues.put("open", openIssues);
        groupedIssues.put("resolved", resolvedIssues);

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("projectKey", projectKey);
        snapshot.put("generatedAt", now);
        snapshot.put("filters", filters);
        snapshot.put(COUNTS, counts);
        snapshot.put(ISSUES, groupedIssues);

        // Backward-compatible fields
        snapshot.put(COUNT, openIssues.size());

        Path outDir = Paths.get("target", "deprecated-reports");
        Files.createDirectories(outDir);

        String safeTs = now.replace(":", "-");
        Path outFile = outDir.resolve("deprecated-snapshot-" + safeTs + ".json");
        MAPPER.writeValue(outFile.toFile(), snapshot);

        return outFile;
    }

    public Map<String, Object> compareSnapshots(Path oldSnapshot, Path newSnapshot) throws IOException {
        JsonNode oldRoot = MAPPER.readTree(oldSnapshot.toFile());
        JsonNode newRoot = MAPPER.readTree(newSnapshot.toFile());

        // Compare OPEN issues only for regression tracking
        JsonNode oldOpen = oldRoot.path(ISSUES).path("open");
        JsonNode newOpen = newRoot.path(ISSUES).path("open");

        Set<String> oldKeys = extractStableIssueKeys(oldOpen);
        Set<String> newKeys = extractStableIssueKeys(newOpen);

        Set<String> resolved = new HashSet<>(oldKeys);
        resolved.removeAll(newKeys);

        Set<String> introduced = new HashSet<>(newKeys);
        introduced.removeAll(oldKeys);

        int oldCount = oldRoot.path(COUNTS).path(OPEN_DEPRECATED).asInt(oldRoot.path(COUNT).asInt(0));
        int newCount = newRoot.path(COUNTS).path(OPEN_DEPRECATED).asInt(newRoot.path(COUNT).asInt(0));

        Map<String, Object> diff = new LinkedHashMap<>();
        diff.put("oldOpenCount", oldCount);
        diff.put("newOpenCount", newCount);
        diff.put("resolvedCount", resolved.size());
        diff.put("introducedCount", introduced.size());
        diff.put("netChange", newCount - oldCount);
        diff.put("status", newCount <= oldCount ? "improved_or_equal" : "regressed");
        return diff;
    }

    private List<DeprecatedIssueRecord> fetchDeprecatedIssues(
            String sonarBaseUrl,
            String organization,
            String projectKey,
            String sonarToken,
            boolean resolved,
            String rules,
            String severities
    ) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        String url = buildIssuesUrl(sonarBaseUrl, organization, projectKey, resolved, rules, severities);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", basicAuthHeader(sonarToken))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 300) {
            throw new IOException("Sonar API failed HTTP " + response.statusCode() + " url=" + url + " body=" + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode issues = root.path(ISSUES);

        List<DeprecatedIssueRecord> out = new ArrayList<>();
        String now = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        for (JsonNode i : issues) {
            DeprecatedIssueRecord r = new DeprecatedIssueRecord();
            r.issueKey = i.path("key").asText();
            r.rule = i.path("rule").asText();
            r.severity = i.path("severity").asText();
            r.message = i.path("message").asText();
            r.file = i.path("file").asText(i.path("component").asText(""));
            r.line = i.hasNonNull("line") ? i.path("line").asInt() : null;
            r.status = i.path("status").asText();
            r.resolution = i.path("resolution").asText();
            r.symbol = i.path("message").asText();
            r.timestamp = now;
            out.add(r);
        }

        return out;
    }

    private String buildIssuesUrl(
            String sonarBaseUrl,
            String organization,
            String projectKey,
            boolean resolved,
            String rules,
            String severities
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(trimTrailingSlash(sonarBaseUrl)).append("/api/issues/search")
                .append("?organization=").append(enc(organization))
                .append("&componentKeys=").append(enc(projectKey))
                .append("&resolved=").append(resolved)
                .append("&types=CODE_SMELL, BUG, VULNERABILITY")
                .append("&ps=100");

        if (notBlank(rules)) {
            sb.append("&rules=").append(enc(rules));
        }
        if (notBlank(severities)) {
            sb.append("&severities=").append(enc(severities));
        }

        return sb.toString();
    }

    private Set<String> extractStableIssueKeys(JsonNode issuesNode) {
        Set<String> keys = new HashSet<>();
        for (JsonNode i : issuesNode) {
            String file = i.path("file").asText("");
            int line = i.path("line").asInt(-1);
            String rule = i.path("rule").asText("");
            String msg = i.path("message").asText("");
            keys.add(file + "|" + line + "|" + rule + "|" + msg);
        }
        return keys;
    }

    private String basicAuthHeader(String sonarToken) {
        String raw = sonarToken + ":";
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private String trimTrailingSlash(String url) {
        return url != null && url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}