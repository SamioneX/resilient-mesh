package com.resilientmesh.user;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping("/{id}")
    Map<String, Object> getUser(
            @PathVariable @NotBlank String id,
            @RequestParam(name = "delayMs", defaultValue = "0") int delayMs) throws InterruptedException {
        if (delayMs > 0) {
            TimeUnit.MILLISECONDS.sleep(delayMs);
        }

        return Map.of(
                "id", id,
                "name", "user-" + id,
                "tier", "STANDARD",
                "fetchedAt", Instant.now().toString());
    }
}
