package com.github.upperbound.secret_santa.web.rest;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.util.StaticContext;
import com.github.upperbound.secret_santa.web.dto.DTOMapper;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.web.dto.ParticipantDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/participant")
public class ParticipantRestController {
    private final DTOMapper dtoMapper;
    private final ParticipantService participantService;

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @Operation(
            description = "Retrieves the total number of participants in all groups",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The total number of participants within groups",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = "123"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/participants-within-groups-count")
    public ResponseEntity<Long> participantsWithinGroups() {
        return ResponseEntity.ok(participantService.participantsWithinGroups());
    }

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @Operation(
            description = "Get information about current participant",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Success result",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ParticipantDTO.class),
                                    examples = @ExampleObject(
                                            value = "{\"email\":\"user@example.org\"," +
                                                    "\"password\":null," +
                                                    "\"info\":\"user info\"," +
                                                    "\"locale\":\"en\"," +
                                                    "\"timezoneOffset\":10800000," +
                                                    "\"timezoneId\":\"Europe/Moscow\"," +
                                                    "\"receiveNotifications\":true," +
                                                    "\"participantGroups\":[" +
                                                    "{" +
                                                    "\"groupUuid\":\"933187f2-4420-4789-82dc-2c170f1a6ede\"," +
                                                    "\"participantUuid\":\"99bd192a-39d7-4af6-ba9d-86f0b6ea2dda\"," +
                                                    "\"role\":\"USER\"," +
                                                    "\"wishes\":null,\"giftee\":null,\"gifteeWishes\":null" +
                                                    "}" +
                                                    "]" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/profile")
    public ResponseEntity<ParticipantDTO> getCurrentParticipantInfo() {
        return ResponseEntity.ok(dtoMapper.toParticipantDTO(participantService.getCurrentAuthentication()));
    }
}
