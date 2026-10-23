# Build

Requires Python 3.9+ and a Java 17+ runtime on PATH. No IDE or installed Java
compiler is required; the build downloads a checksum-pinned Eclipse compiler.
Output bytecode targets Java 8. Network is needed on the first build only.

Linux (Fedora/openSUSE): `python3 scripts/build.py`

Windows PowerShell: `py scripts/build.py`

With Gradle 8.x installed: `gradle build` delegates to exactly the same pipeline.
There is no Gradle wrapper in this checkpoint. Gradle invocation itself has not
been tested in the current environment; the Python pipeline has.

Pipeline: verify/download dependencies, compile, relocate our integration classes,
run core tests, transform actual 1.8.9 bytecode, package a deterministic JAR, then
run JVM `-Xverify:all` with the packaged startup agent. Test/build classes and all
Minecraft libraries are excluded from the output. ASM core and license notices
are included. Build-time ASM Commons and ECJ are excluded.

Output: `build/libs/blossom-client-0.2.2.jar`.

CI runs this pipeline on Linux and Windows and uploads the compiled JAR. A green
CI build verifies compilation and non-graphical tests, not gameplay.

The original spec is in docs/SPECIFICATION.md. Commit coherent working checkpoints
and push each checkpoint; never represent planned matrix rows as completed.

The pipeline also builds `build/libs/blossom-launcher-0.2.2.jar`, embedding
the tested agent. Run `python scripts/package_release.py` after building to create
the release ZIP and checksums under `build/release`.

Run `python scripts/test_runtime.py` for an actual host Java 8 install, offline
reuse and tests under the managed JVM. The Windows CI job also checks the
PowerShell bootstrap from the packaged ZIP. Linux startup uses Python 3; Windows
startup uses Windows PowerShell. No system Java installation is required to use
the ZIP's startup scripts.

Release numbering advances one patch by default: 0.2.2, 0.2.3, 0.2.4.
