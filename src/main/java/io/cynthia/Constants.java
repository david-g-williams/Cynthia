package io.cynthia;

public class Constants {
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final double EPSILON = Math.ulp(0.5);
    public static final double INFINITY = Double.POSITIVE_INFINITY;
    public static final long TOTAL_MEMORY = Runtime.getRuntime().totalMemory();
    public static final String COMPONENT_SCAN = "io.cynthia";
    public static final String CYNTHIA = "cynthia";
    public static final String MODEL_BUNDLE = "model.bundle";
    public static final String MODEL_LAMBDA = "model.lambda";
    public static final String MODEL = "model";
    public static final String MODELS = "models";
    public static final String MODELS_YAML = "/models.yaml";
    public static final String PROCESS = "/process";
    public static final String SERVE = "serve";
    public static final String SHUTDOWN_MESSAGE = "Closing all SavedModelBundles.";
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    public static final String MODEL_PROPERTIES = "model.properties";
    public static final String INDEX_JSON = "index.json";
    public static final String MODEL_INDEX = "model.index";
}
