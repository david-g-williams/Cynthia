package io.cynthia.lambdas;

import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;

import org.junit.Test;

import javax.script.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import static io.cynthia.Constants.AVAILABLE_PROCESSORS;
import static io.cynthia.utils.Serialization.objectToJSON;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LogicGatesTest {

    static ExecutorService executorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);

    private String genBool(final String expr,
                           final List<String> vars,
                           final int depth,
                           final int maxDepth,
                           final Random random) {
        if (depth >= maxDepth) {
            return expr;
        }

        final String var = vars.get(random.nextInt(vars.size()));

        final List<Supplier<String>> unarySuppliers = List.of(
            () -> genBool(String.format("(%s)", expr), vars, depth + 1, maxDepth, random),
            () -> genBool(String.format("!%s", expr), vars, depth + 1, maxDepth, random),
            () -> String.format("(%s)", genBool(expr, vars, depth + 1, maxDepth, random)),
            () -> String.format("!%s", genBool(expr, vars, depth + 1, maxDepth, random))
        );

        final List<Supplier<String>> binarySuppliers = List.of(
            () -> String.format("%s && %s", genBool(var, vars, depth + 1, maxDepth, random), expr),
            () -> String.format("%s || %s", genBool(var, vars, depth + 1, maxDepth, random), expr),
            () -> String.format("%s && %s", genBool(expr, vars, depth + 1, maxDepth, random), var),
            () -> String.format("%s || %s", genBool(expr, vars, depth + 1, maxDepth, random), var),
            () -> String.format("%s && %s", var, genBool(expr, vars, depth + 1, maxDepth, random)),
            () -> String.format("%s || %s", var, genBool(expr, vars, depth + 1, maxDepth, random)),
            () -> String.format("%s && %s", expr, genBool(var, vars, depth + 1, maxDepth, random)),
            () -> String.format("%s || %s", expr, genBool(var, vars, depth + 1, maxDepth, random)),
            () -> genBool(String.format("%s && %s", var, expr), vars, depth + 1, maxDepth, random),
            () -> genBool(String.format("%s || %s", var, expr), vars, depth + 1, maxDepth, random),
            () -> genBool(String.format("%s && %s", expr, var), vars, depth + 1, maxDepth, random),
            () -> genBool(String.format("%s || %s", expr, var), vars, depth + 1, maxDepth, random)
        );

        final List<List<Supplier<String>>> supplierLists = List.of(unarySuppliers, binarySuppliers);
        final List<Supplier<String>> suppliers = supplierLists.get(random.nextInt(supplierLists.size()));
        return suppliers.get(random.nextInt(suppliers.size())).get();
    }

    @SneakyThrows
    private void generateBooleanExpressions() {
        final byte[] randomSeed = new byte[256];

        ThreadLocalRandom.current().nextBytes(randomSeed);

        final Random random = new SecureRandom(randomSeed);

        final Path outputDirectory = Path.of("/home/david/data/boolean");

        final Path outputFile = Path.of(outputDirectory.toString(), "expressions.json");

        final ScriptEngineManager engineManager = new ScriptEngineManager();
        final ScriptEngine engine = engineManager.getEngineByName("JavaScript");

        final List<Map<String, Boolean>> bindingValues = List.of(
            Map.of("A", true, "B", true),
            Map.of("A", true, "B", false),
            Map.of("A", false, "B", true),
            Map.of("A", false, "B", false)
        );

        final List<String> symbols = List.of("A", "B");

        final int totalToGenerate = (int) 1e8;

        int totalCount = 0;
        for (int i = 0; i <= totalToGenerate; i ++) {
            final int randomDepth = random.nextInt(30);
            final String symbol = symbols.get(random.nextInt(symbols.size()));
            final String genBool = genBool(symbol, List.of("A", "B"), 0, randomDepth, random);
            final Bindings vars = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            for (final Map<String, Boolean> values : bindingValues) {
                vars.put("A", values.get("A"));
                vars.put("B", values.get("B"));
                final boolean result = (boolean) engine.eval(genBool);
                final Map<String, Object> data = Map.of(
                    "X", genBool,
                    "A", values.get("A"),
                    "B", values.get("B"),
                    "R", result
                );
                Files.writeString(outputFile, objectToJSON(data) + "\n",
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
                );
            }
            if (totalCount % 100000 == 0) {
                final double progressPercentage = 100.0 * (i + 1) / totalToGenerate;
                System.out.println(
                    String.format("i: %-10s generated: %-10s progress: %-3.3f %%",
                        i, totalCount, progressPercentage));
            }
            totalCount += 1;
        }
    }

    @Test
    public void testGenerateBooleanExpressions() {
        final Supplier<Boolean> booleanSupplier = () -> {
            generateBooleanExpressions();
            return true;
        };

        final List<CompletableFuture<Boolean>> completableFutures = new ArrayList<>();

        for (int i = 0; i < AVAILABLE_PROCESSORS; i++) {
            completableFutures.add(CompletableFuture.supplyAsync(booleanSupplier, executorService));
        }

        final CompletableFuture<?>[] futureArray = completableFutures.toArray(new CompletableFuture[0]);

        CompletableFuture.allOf(futureArray).join();
    }
}
