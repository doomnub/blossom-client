$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false)
Add-Type -AssemblyName System.Runtime.WindowsRuntime
$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]
$null = [Windows.Storage.Streams.DataReader,Windows.Storage.Streams,ContentType=WindowsRuntime]
$asTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.IsGenericMethod -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' } | Select-Object -First 1
function Await($operation, $type) {
    $task = $asTask.MakeGenericMethod($type).Invoke($null, @($operation))
    if (-not $task.Wait(5000)) { throw 'Media API timeout' }
    return $task.Result
}
$manager = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
$lastKey = ''; $art = ''
while ($true) {
    try {
        $session = $manager.GetCurrentSession()
        foreach ($candidate in $manager.GetSessions()) {
            if ($candidate.GetPlaybackInfo().PlaybackStatus.ToString() -eq 'Playing') { $session = $candidate; break }
        }
        if ($null -eq $session) { $lastKey=''; $art=''; [Console]::WriteLine('{}'); Start-Sleep -Milliseconds 1000; continue }
        $media = Await ($session.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
        $key = $session.SourceAppUserModelId + '|' + $media.Title + '|' + $media.Artist + '|' + $media.AlbumTitle
        $artChanged = $key -ne $lastKey
        if ($artChanged) {
            $lastKey = $key; $art = ''
            if ($null -ne $media.Thumbnail) {
                $stream = $null; $reader = $null
                try {
                    $stream = Await ($media.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])
                    if ($stream.Size -gt 0 -and $stream.Size -le 2097152) {
                        $reader = [Windows.Storage.Streams.DataReader]::new($stream.GetInputStreamAt(0))
                        $loaded = Await ($reader.LoadAsync([uint32]$stream.Size)) ([uint32])
                        $bytes = New-Object byte[] $loaded
                        $reader.ReadBytes($bytes)
                        $art = 'data:;base64,' + [Convert]::ToBase64String($bytes)
                    }
                } catch { $art = '' } finally {
                    if ($null -ne $reader) { $reader.Dispose() }
                    if ($null -ne $stream) { $stream.Dispose() }
                }
            }
        }
        $timeline = $session.GetTimelineProperties()
        $playing = $session.GetPlaybackInfo().PlaybackStatus.ToString() -eq 'Playing'
        $position = ($timeline.Position - $timeline.StartTime).TotalSeconds
        if ($playing) { $position += [Math]::Max(0, ([DateTimeOffset]::Now - $timeline.LastUpdatedTime).TotalSeconds) }
        $result = @{ title=$media.Title; artist=$media.Artist; position=[Math]::Max(0,$position); duration=[Math]::Max(0,($timeline.EndTime-$timeline.StartTime).TotalSeconds); playing=$playing; rate=1 }
        if ($artChanged) { $result.art = $art }
        [Console]::WriteLine(($result | ConvertTo-Json -Compress -Depth 3))
    } catch { $lastKey=''; $art=''; [Console]::WriteLine('{}') }
    Start-Sleep -Milliseconds 1000
}
