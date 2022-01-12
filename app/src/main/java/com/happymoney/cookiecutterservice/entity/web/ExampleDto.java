package com.happymoney.cookiecutterservice.entity.web;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExampleDto {

    String id;

    String content;

    Long favoriteNumber;

}
