package com.happymoney.cookiecutterservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.happymoney.cookiecutterservice.domain.Example;
import com.happymoney.cookiecutterservice.entity.ExampleMapper;
import com.happymoney.cookiecutterservice.entity.web.ExampleDto;
import com.happymoney.cookiecutterservice.response.ResponseDTO;
import com.happymoney.cookiecutterservice.service.ExampleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static com.happymoney.cookiecutterservice.response.ResponseDTO.response;

@Api
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;
    private final ExampleMapper exampleMapper;

    @ApiOperation(value = "Example", notes = "Create Example")
    @PostMapping(path = "example", produces = "application/json")
//    @PreAuthorize("hasAuthority('SCOPE_Example')") // Remove if you don't need Scope based authorization
    @ResponseBody
    public ResponseEntity<ResponseDTO<ExampleDto>> create(@RequestBody ExampleDto exampleDto)
            throws JsonProcessingException {

        Example example = exampleMapper.toDomain(exampleDto);
        exampleService.saveExample(example);

        return new ResponseEntity<>(response(exampleDto), HttpStatus.OK);
    }

    @ApiOperation(value = "Example", notes = "Get Example")
    @GetMapping(path = "example/{id}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<ResponseDTO<ExampleDto>> getExample(@PathVariable String id) {

        Optional<Example> Example = exampleService.findExample(id);
        return Example.map(this::okResponse).orElseGet(this::notFoundResponse);

    }

    private ResponseEntity<ResponseDTO<ExampleDto>> notFoundResponse() {
        return new ResponseEntity<>(response(null), HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<ResponseDTO<ExampleDto>> okResponse(final Example domainModel) {
        return new ResponseEntity<>(response(exampleMapper.toJson(domainModel)), HttpStatus.OK);
    }

}
