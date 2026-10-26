package dev.blossom.agent;
import java.lang.instrument.Instrumentation;
import java.io.*;
import java.security.MessageDigest;
public final class BlossomAgent {
    private static final String[][] EXPECTED={
        {"bjh","f294e78abc95e1258785c14dbc9ecea05d0da576e5fc6914a59d92fff22023a5"},
        {"bkp","ce4c2d9099e9a8f3b590834c99dad5907bd72cda9994c699991652b6936847f3"},
        {"bjl","f67bc86bd68414401a5c6fed53fc1ceccdb22cf68f95e6a2f137ac55cf91d214"},
        {"bec","2e9425b1999cf93869b2c5bf62d2cb591d8b636ff6ba2d3fdaf3c92301914b12"},
        {"bky","d07d3c2af04d26181bf5cf8a21f03e22b6e0bf3b711540a4a92addaed808ffa7"},
        {"bbj","2414f0a93e7bbf8128b079976a80615b2c62cff2d7383a6285adff23f848b015"},
        {"bfn","c9770ea5f79c956e6be5983f081660c2e140c4f94cf2be60d81cd092dc08099b"},
        {"axp","99e54411523cebfbfc9322c80f507e4f0b2e160e4eb180db151125fbe49b3d17"},
        {"avf","45dcd6790540b7e12c0342f9b80ae4ba9d2c0bd1e0b2bf780a0280d90c2713a8"},

        {"bfk","8a4ce93bbf272398ea298d2ed363f75211dd469fcd3b66682a387e4738a419a2"},
        {"aya","459cbafe9aacb8afa13c1427a332dc6bc35f4f11d71339447cc703fe08f735fe"},
        {"avb","4e4885a7bfeeb54bfedcf3bfe1b2a3c9330fba4527d0dc44110c00b397d10ab3"},
        {"ave","430a17aa1911bcd0251aa4dcecb013b6c5b251bb5d0122c7305de0e36dc33e82"},
        {"avo","e883bdbfc9170fffba120e7915f62a7224116e5259dba7b91129757323509e78"},
        {"bet","f1e1848125e253d0c0b8b519321a29cb8f9fe762c8269a72dcacd849d5a5378f"}
    };
    public static void premain(String options, Instrumentation instrumentation) {
        try {
            for(String[] expected:EXPECTED) {
                MessageDigest digest=MessageDigest.getInstance("SHA-256");
                try(InputStream in=ClassLoader.getSystemResourceAsStream(expected[0]+".class")) {
                    if(in==null)throw new IOException("Minecraft 1.8.9 not on the application classpath");
                    byte[] buffer=new byte[8192];int n;
                    while((n=in.read(buffer))!=-1)digest.update(buffer,0,n);
                }
                StringBuilder hex=new StringBuilder();for(byte b:digest.digest())hex.append(String.format("%02x",b&255));
                if(!expected[1].equals(hex.toString()))throw new IOException("Unsupported game class: "+expected[0]);
            }
            instrumentation.addTransformer(new BlossomTransformer());
            System.out.println("[Blossom] Verified vanilla 1.8.9; startup hooks registered");
        } catch(Exception e) {
            System.err.println("[Blossom] NOT ENABLED: "+e.getMessage()+". Use unmodified vanilla 1.8.9, without OptiFine/Forge/Lunar.");
        }
    }
}
