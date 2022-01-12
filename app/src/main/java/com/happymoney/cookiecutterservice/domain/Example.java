package com.happymoney.cookiecutterservice.domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Example {

    UUID id;

    String content;

    Long favoriteNumber;

}
