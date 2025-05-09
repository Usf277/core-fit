package com.corefit.controller.playground;

import com.corefit.dto.request.playground.PlaygroundRateRequest;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.exceptions.GeneralException;
import com.corefit.service.playground.PlaygroundRateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/playground_rates")
public class PlaygroundRateController {

    @Autowired
    private PlaygroundRateService playgroundRateService;

    @PostMapping("")
    public ResponseEntity<GeneralResponse<?>> addRate(@RequestBody PlaygroundRateRequest request, HttpServletRequest httpRequest) {
        GeneralResponse<?> response = playgroundRateService.insert(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("")
    public ResponseEntity<GeneralResponse<?>> getRatesByPlayground(
            @RequestParam Long playgroundId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        GeneralResponse<?> response = playgroundRateService.getRatesByPlayground(playgroundId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
