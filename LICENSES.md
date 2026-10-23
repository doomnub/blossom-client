# Third-party notices

Blossom original source: MIT, see LICENSE.

Minecraft and its libraries are downloaded to the ignored .deps directory only
for compilation and inspection. They are NOT included in Blossom distributions.
Minecraft remains the property of Mojang/Microsoft. Use a legitimate account and
launcher. The standalone Blossom launcher uses Microsoft device-code sign-in and
checks Minecraft entitlement through the official services.

MCP 1.8.9 SRG names were consulted to identify method/field signatures. No MCP
sources, decompiled Minecraft sources, or mappings archive are redistributed.

Eclipse ECJ 3.33.0 (EPL-2.0) is downloaded as a build tool, not redistributed.

ASM 9.7.1 is included in the agent JAR under its BSD-3-Clause license:

Copyright (c) 2000-2011 INRIA, France Telecom
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
3. Neither the name of the copyright holders nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

Gson 2.11.0 is included only in the launcher JAR under Apache License 2.0.
Copyright 2008 Google Inc. Full license: licenses/Apache-2.0.txt.

The start scripts and launcher download Azul Zulu Community Java 8u504 directly
from Azul's CDN. Java binaries are not embedded in the Blossom release. Original
runtime license files remain in the installed runtime. Azul Zulu/OpenJDK licensing
and source information: https://www.azul.com/downloads/?package=jre and
https://www.azul.com/products/core/openjdk-terms-of-use/.
The OS-specific URLs and vendor SHA-256 values are pinned in
launcher/src/main/resources/runtimes.json, obtained from the official metadata API:
https://docs.azul.com/core/install/metadata-api.
