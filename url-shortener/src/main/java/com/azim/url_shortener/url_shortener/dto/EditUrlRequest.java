package com.azim.url_shortener.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EditUrlRequest {

    @NotBlank(message = "URL is required")
    @Pattern(
        regexp = "^(https?://).*",
        message = "URL must start with http:// or https://"
    )
    private String newOriginalUrl;
}