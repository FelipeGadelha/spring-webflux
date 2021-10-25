package br.com.felipe.gadelha.webflux.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JacksonParse {

    @Autowired
    private ObjectMapper mapper;
    private JacksonJsonParser jackson = new JacksonJsonParser();

    public <T> String toJson(T t) throws JsonProcessingException {
        return mapper.writeValueAsString(t);
    }

    public Map<String, Object> toMap(String json) {
        return jackson.parseMap(json);
    }



}