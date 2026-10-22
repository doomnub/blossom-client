package dev.blossom.agent;
import java.lang.instrument.Instrumentation;
public final class BlossomAgent {
    public static void premain(String options, Instrumentation instrumentation) {
        instrumentation.addTransformer(new BlossomTransformer());
        System.out.println("[Blossom] Startup hooks registered (vanilla 1.8.9 only)");
    }
}
