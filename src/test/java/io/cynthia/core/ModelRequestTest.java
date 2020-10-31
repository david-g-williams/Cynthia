package io.cynthia.core;

import io.cynthia.server.request.ModelRequest;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;
import java.util.Map;

public class ModelRequestTest {
    private static final String TEST_KEY_A = "A";
    private static final String TEST_MODEL_ID = "testModelId";

    @Test
    public void Given_TestData_When_BuildModelRequest_Then_ModelRequestIsVerified() {
        final List<Map<String, Object>> data = List.of(Map.of(TEST_KEY_A, true));
        ModelRequest modelRequest = ModelRequest.of(data, TEST_MODEL_ID);
        Assert.assertEquals(TEST_MODEL_ID, modelRequest.modelId());
        Assert.assertEquals(1, modelRequest.data().size());
        Assert.assertEquals(1, modelRequest.data().get(0).size());
        Assert.assertTrue((boolean) modelRequest.data().get(0).get(TEST_KEY_A));
    }
}
