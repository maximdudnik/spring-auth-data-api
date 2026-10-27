package com.example.dataapi.transform;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransformController {

    @PostMapping("/transform")
    public TransformResponse transform(@Valid @RequestBody TransformRequest request) {
        return new TransformResponse(TextTransformer.transform(request.text()));
    }
}
