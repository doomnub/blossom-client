# Blossom 0.2.7.1 validation

The published 0.2.7 agent reproduces the exact reported `NoSuchMethodError: java.nio.ByteBuffer.flip()Ljava/nio/ByteBuffer;` under managed Azul Java 8u504, before a display is needed. The previous verifier-only test loaded methods without executing icon generation; it could not catch this missing runtime method.

The hotfix explicitly invokes `Buffer.flip()` and compiles the game agent and legacy launcher with ECJ `--release 8`. The build therefore restricts the Java platform API as well as emitting classfile version 52. A JDK 17+ with platform signatures is required for building; the game still uses Java 8.

`StartupSmoke` executes the packaged agent's real PNG decoding, resizing and direct RGBA buffer preparation on Java 8. It checks both icon sizes, position/limit, alpha visibility and exact RGBA channel order. It initializes the installed Log4j libraries/configuration, verifies both appenders and their layouts, logs a message and exception, shuts down the logger, rejects status warnings/errors, and verifies console and file output. No Jackson libraries are added and the updated Log4j version is retained.

The legacy installer writes the packaged shared XML configuration. The native installer embeds that same source and exports its installed Java/classpath/logging paths to the smoke test. Both native CI platforms execute this test before packaging; the legacy runtime test also runs it. Existing core, cloth, transformer, JVM verification, launcher, browser and native Linux desktop checks remain enabled.

This exercises the reported failing startup paths without requiring an OpenGL window, Minecraft account or server. It does not claim full graphical gameplay acceptance on Fedora or Windows.
