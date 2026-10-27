package com.example.dataapi.transform;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TransformRequest(@NotBlank @Size(max = 10000) String text) {
}
