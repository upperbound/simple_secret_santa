package com.github.upperbound.secret_santa.web.rest;

import com.github.upperbound.secret_santa.aop.MDCLog;
import com.github.upperbound.secret_santa.service.ParticipantService;
import com.github.upperbound.secret_santa.util.StaticContext;
import com.github.upperbound.secret_santa.web.dto.AvailableMessageBundle;
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

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/common")
public class CommonRestController {
    private final StaticContext staticContext;
    private final ParticipantService participantService;

    @MDCLog(mdcKey = StaticContext.MDC_SESSION_USER, executionTime = true)
    @Operation(
            description = "Retrieves an array of all available i18n bundles",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "All available bundles",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AvailableMessageBundle.class),
                                    examples = @ExampleObject(
                                            value = "[" +
                                                    "{\"lang\":\"en\",\"imageUrl\":\"/images/en.png\"}," +
                                                    "{\"lang\":\"ru\",\"imageUrl\":\"/images/ru.png\"}" +
                                                    "]"
                                    )
                            )
                    )
            }
    )
    @GetMapping("/available-message-bundles")
    public ResponseEntity<List<AvailableMessageBundle>> availableMessageBundles() {
        return ResponseEntity.ok(staticContext.getAvailableMessageBundles());
    }
}
