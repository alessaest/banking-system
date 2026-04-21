package com.bank.resource;

import com.bank.service.DeprecatedCodeCollectorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/deprecated-collector")
@Produces(MediaType.APPLICATION_JSON)
public class DeprecatedCodeCollectorResource {

    @Inject
    DeprecatedCodeCollectorService service;

    @POST
    @Path("/snapshot")
    public Response snapshot(
            @QueryParam("sonarUrl") String sonarUrl,
            @QueryParam("organization") String organization,
            @QueryParam("projectKey") String projectKey,
            @QueryParam("rules") String rules,
            @QueryParam("severities") String severities,
            @QueryParam("tags") String tags,
            @QueryParam("branch") String branch,
            @HeaderParam("X-Sonar-Token") String sonarToken
    ) {
        try {
            if (isBlank(sonarUrl) || isBlank(organization) || isBlank(projectKey) || isBlank(sonarToken)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Required: sonarUrl, organization, projectKey, X-Sonar-Token"))
                        .build();
            }

            if (isBlank(rules)) {
                rules = "java:S1874";
            }

            java.nio.file.Path out = service.collectAndWriteSnapshot(
                    sonarUrl, organization, projectKey, sonarToken, rules, severities, tags, branch
            );

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("snapshotPath", out.toString());
            body.put("projectKey", projectKey);
            body.put("organization", organization);
            body.put("rules", rules);
            body.put("severities", severities);
            body.put("tags", tags);
            body.put("branch", branch);

            return Response.ok(body).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}