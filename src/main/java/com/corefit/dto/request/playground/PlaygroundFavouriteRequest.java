package com.corefit.dto.request.playground;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaygroundFavouriteRequest {
    @NotNull(message = "Playground ID is required")
    private long playgroundId;
    private String type; // add or remove
}
