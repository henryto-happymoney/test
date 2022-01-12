package com.happymoney.cookiecutterservice.controller;

import com.happymoney.cookiecutterservice.domain.Example;
import com.happymoney.cookiecutterservice.entity.ExampleMapper;
import com.happymoney.cookiecutterservice.entity.web.ExampleDto;
import com.happymoney.cookiecutterservice.response.ResponseDTO;
import com.happymoney.cookiecutterservice.service.ExampleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("Example Controller Test")
public class ExampleControllerTest {
    @Mock
    private ExampleService exampleService;

    @Mock
    private ExampleMapper exampleMapper;

    @InjectMocks
    private ExampleController exampleController;

    @Test
    @DisplayName("Given a request to create example, should call service to save")
    public void createExample() throws Exception {
        ExampleDto exampleDto = new ExampleDto();
        exampleDto.setId(UUID.randomUUID().toString());
        exampleDto.setContent("test 1");
        exampleDto.setFavoriteNumber(100L);

        ResponseEntity<ResponseDTO<ExampleDto>> responseEntity = exampleController.create(exampleDto);
        assertThat(responseEntity).isNotNull();
        ResponseDTO<ExampleDto> responseDto = responseEntity.getBody();
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getData()).isEqualTo(exampleDto);
        assertThat(responseDto.getErrorList()).isNull();
        assertThat(responseDto.getTotal()).isNull();

        then(exampleService).should().saveExample(any());
    }

    @Test
    @DisplayName("Given a request to get an existing example, should return OK response")
    public void getExampleExist() {
        UUID id = UUID.randomUUID();
        String idString = id.toString();
        Example example = new Example();
        example.setId(id);
        example.setContent("test 2");
        example.setFavoriteNumber(200L);

        given(exampleService.findExample(idString)).willReturn(Optional.of(example));
        ResponseEntity<ResponseDTO<ExampleDto>> responseEntity = exampleController.getExample(idString);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        ResponseDTO<ExampleDto> responseDto = responseEntity.getBody();
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getErrorList()).isNull();
        assertThat(responseDto.getTotal()).isNull();
    }

    @Test
    @DisplayName("Given a request to get a unknown example, should return NOT_FOUND response")
    public void getExampleNone() {
        String id = UUID.randomUUID().toString();

        given(exampleService.findExample(id)).willReturn(Optional.empty());
        ResponseEntity<ResponseDTO<ExampleDto>> responseEntity = exampleController.getExample(id);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
