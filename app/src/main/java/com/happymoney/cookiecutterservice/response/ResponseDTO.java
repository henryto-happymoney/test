package com.happymoney.cookiecutterservice.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
public class ResponseDTO<T> implements Serializable {

    private static final long serialVersionUID = -3016256894889290713L;

    @JsonProperty(value = "data")
    private transient T data;

    @JsonProperty(value = "errors")
    private transient List<Error> errorList;

    @JsonProperty(value = "meta")
    private transient MetaDataModel total;

    public static <T> ResponseDTO<T> response(T data) {
        ResponseDTO<T> responseDTO = new ResponseDTO<>();
        responseDTO.setData(data);
        return responseDTO;
    }

}
