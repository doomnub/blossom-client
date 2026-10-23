# Windows bootstrap; no admin rights, registry updates or system Java changes.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
function Test-Java8([string]$Executable) {
    if (!(Test-Path -LiteralPath $Executable)) { return $false }
    $info = New-Object Diagnostics.ProcessStartInfo
    $info.FileName = $Executable
    $info.Arguments = '-XshowSettings:properties -version'
    $info.UseShellExecute = $false
    $info.RedirectStandardError = $true
    $info.RedirectStandardOutput = $true
    $info.CreateNoWindow = $true
    $process = [Diagnostics.Process]::Start($info)
    $readError = $process.StandardError.ReadToEndAsync()
    $readOutput = $process.StandardOutput.ReadToEndAsync()
    if (!$process.WaitForExit(10000)) { $process.Kill(); $process.Dispose(); return $false }
    $output = $readError.Result + $readOutput.Result
    $ok = $process.ExitCode -eq 0 -and $output.Contains('"1.8.') -and $output.Contains('sun.arch.data.model = 64')
    $process.Dispose()
    return $ok
}
try {
    if ($env:PROCESSOR_ARCHITECTURE -ne 'AMD64' -and $env:PROCESSOR_ARCHITEW6432 -ne 'AMD64') { throw 'Windows x86_64 is required.' }
    $spec = (Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot 'runtimes.json') | ConvertFrom-Json).windows
    $root = Join-Path $env:USERPROFILE '.blossom'
    $runtimes = Join-Path $root 'runtimes'
    $target = Join-Path $runtimes $spec.folder
    $java = Join-Path $target $spec.executable
    $marker = Join-Path $target '.blossom-ready'
    $ready = (Test-Path -LiteralPath $marker) -and ((Get-Content -Raw -LiteralPath $marker).Trim() -eq $spec.sha256) -and (Test-Java8 $java)
    if (!$ready) {
        Write-Host "Installing $($spec.name) to $target"
        $stage = Join-Path $runtimes ('bootstrap-' + [Guid]::NewGuid())
        New-Item -ItemType Directory -Path $stage -Force | Out-Null
        try {
            $archive = Join-Path $stage 'runtime.zip'
            $client = New-Object Net.WebClient
            try { $client.DownloadFile($spec.url, $archive) } finally { $client.Dispose() }
            if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $spec.sha256) { throw 'Java download checksum mismatch.' }
            $zip = [IO.Compression.ZipFile]::OpenRead($archive)
            try {
                if ($zip.Entries.Count -gt 10000) { throw 'Too many runtime files.' }
                [long]$size = 0
                foreach ($entry in $zip.Entries) {
                    $name = $entry.FullName
                    if (!$name.StartsWith($spec.folder + '/') -or $name.Contains('\') -or $name.Contains(':') -or ($name.Split('/') -contains '..')) { throw 'Invalid runtime archive path.' }
                    $size += $entry.Length
                    if ($size -gt 1GB) { throw 'Runtime archive too large.' }
                }
            } finally { $zip.Dispose() }
            [IO.Compression.ZipFile]::ExtractToDirectory($archive, $stage)
            $installed = Join-Path $stage $spec.folder
            if (!(Test-Java8 (Join-Path $installed $spec.executable))) { throw 'Downloaded Java failed 64-bit Java 8 verification.' }
            [IO.File]::WriteAllText((Join-Path $installed '.blossom-ready'), $spec.sha256 + "`n")
            $backup = $null
            if (Test-Path -LiteralPath $target) { $backup = $target + '-previous-' + [Guid]::NewGuid(); Move-Item -LiteralPath $target -Destination $backup }
            try { Move-Item -LiteralPath $installed -Destination $target }
            catch { if ($backup) { Move-Item -LiteralPath $backup -Destination $target }; throw }
            if ($backup) { Remove-Item -LiteralPath $backup -Recurse -Force -ErrorAction SilentlyContinue }
        } finally { Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue }
    }
    Write-Host "Using $($spec.name): $java"
    if ($args -contains '--install-only') { exit 0 }
    $version = (Get-Content -Raw -LiteralPath (Join-Path $PSScriptRoot 'VERSION')).Trim()
    & $java -jar (Join-Path $PSScriptRoot "blossom-launcher-$version.jar")
    exit $LASTEXITCODE
} catch { Write-Error $_; exit 1 }
