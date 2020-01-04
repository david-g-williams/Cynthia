package io.cynthia.core;

import org.junit.Test;
import java.util.Collections;
import java.util.Map;
import static org.junit.Assert.*;

public class RequestTest {

    @Test
    public void CreateRequest_WithValidData_IsSuccessful() {
        Request request = new Request().setModelId("testId")
            .setData(Collections.singletonList(
                Map.ofEntries(Map.entry("a", true),
                              Map.entry("b", false))));
        assertEquals("testId", request.getModelId());
        assertEquals(1, request.getData().size());
        assertEquals(2, request.getData().get(0).size());
        assertTrue((boolean)request.getData().get(0).get("a"));
        assertFalse((boolean)request.getData().get(0).get("b"));
    }
}
