package com.bank.resource;

import com.bank.service.DeprecatedCodeCollectorService;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/deprecated-collector")
@Produces(MediaType.APPLICATION_JSON)
public class DeprecatedCodeCollectorResource {

    private static final Logger LOGGER = Logger.getLogger(DeprecatedCodeCollectorResource.class.getName());
    private static final String ERROR = "error";


    private final DeprecatedCodeCollectorService service;

    DeprecatedCodeCollectorResource (DeprecatedCodeCollectorService service) {
        this.service = service;
    }

    @POST
    @Path("/snapshot")
    public Response snapshot(
            @QueryParam("sonarUrl") String sonarUrl,
            @QueryParam("organization") String organization,
            @QueryParam("projectKey") String projectKey,
            @QueryParam("rules") String rules,
            @QueryParam("severities") String severities,
            @QueryParam("types") String types,
            @HeaderParam("X-Sonar-Token") String sonarToken
    ) {
        try {
            if (isBlank(sonarUrl) || isBlank(organization) || isBlank(projectKey) || isBlank(sonarToken)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of(ERROR, "Required: sonarUrl, organization, projectKey, X-Sonar-Token"))
                        .build();
            }


            java.nio.file.Path out = service.collectAndWriteSnapshot(
                    sonarUrl, organization, projectKey, sonarToken, rules, severities, types
            );

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("snapshotPath", out.toString());
            body.put("projectKey", projectKey);
            body.put("organization", organization);
            body.put("rules", rules);
            body.put("severities", severities);
            body.put("types", types);

            return Response.ok(body).build();
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, "Interrupted while collecting deprecated code snapshot", e);
            Thread.currentThread().interrupt();
            return Response.serverError()
                    .entity(Map.of(ERROR, e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to collect deprecated code snapshot", e);
            return Response.serverError()
                    .entity(Map.of(ERROR, e.getMessage()))
                    .build();
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}