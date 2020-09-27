package io.cynthia.core;

import io.cynthia.server.request.Request;
import org.junit.Test;
import java.util.Collections;
import java.util.Map;
import static org.junit.Assert.*;

public class RequestTest {

    @Test
    public void CreateRequest_WithValidData_IsSuccessful() {
        Request request = new Request().modelId("testId")
            .data(Collections.singletonList(
                Map.ofEntries(Map.entry("a", true),
                              Map.entry("b", false))));
        assertEquals("testId", request.modelId());
        assertEquals(1, request.data().size());
        assertEquals(2, request.data().get(0).size());
        assertTrue((boolean)request.data().get(0).get("a"));
        assertFalse((boolean)request.data().get(0).get("b"));
    }
}
