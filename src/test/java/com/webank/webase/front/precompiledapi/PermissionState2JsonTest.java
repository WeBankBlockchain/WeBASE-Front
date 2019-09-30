package com.webank.webase.front.precompiledapi;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

public class PermissionState2JsonTest{

    @Test
    public void whenParsingJsonStringIntoJsonNode_thenCorrect()
            throws JsonParseException, IOException {
        String jsonString = "{\"a\": 1, \"b\": 2}";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(jsonString);
        System.out.println("a: " + actualObj.get("a"));

//        assertNotNull(actualObj);
    }
}
