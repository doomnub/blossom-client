# Now Playing HUD (unreleased)

Enable **Now Playing** in Blossom's HUD modules. It displays album artwork, title,
artist, elapsed/total playback time and a progress bar in a rounded translucent
panel. Drag and scale it with the HUD editor. The existing module opacity,
background color and text color controls affect the panel/progress presentation.
No active media means the panel hides; the editor displays a placeholder.

Linux reads MPRIS through the logged-in session's D-Bus, using system GLib/GIO and
Python 3 (no pip modules, playerctl or Spotify credentials). Windows reads the
Windows 10/11 Global System Media Transport Controls API through a persistent
Windows PowerShell helper. Apps must publish their media session to the OS;
this does not inspect arbitrary application windows or browser pages. Playing
sessions take priority over paused sessions. There is no macOS adapter.

One background helper runs only while enabled and in a world. Disabling the module
or leaving the world stops it; a shutdown hook also terminates it. API failures
hide stale metadata and retry with a delay. Reads, thumbnail decoding and artwork
fetching do not run on the render thread. Artwork has a 2 MiB download limit,
dimension checks, timeouts and a single retained thumbnail. The render thread
replaces/deletes the previous texture when artwork changes or the module stops.
Only the current track is retained; no listening history is saved. For MPRIS art
URLs, the client reads the player-provided local file or HTTP(S) URL.

Progress interpolates between snapshots, respects pause and seek updates, and
shows an unknown duration when the application does not provide one. Session API
quality depends on the player. Artwork formats unsupported by Java ImageIO show
a placeholder. The helper does not control playback.

## Validation

The Java client/launcher build, JVM linkage, disabled-module guard and MPRIS
GVariant decoding tests pass locally. This environment forbids creating the
D-Bus session socket, so real desktop/player behavior needs OS acceptance testing.
Windows script/API startup and Linux D-Bus startup smoke tests are included for CI.
A blank-session CI check is not a test with Spotify/browser playback or album art.

Reference APIs:
- https://specifications.freedesktop.org/mpris/latest/Player_Interface.html
- https://learn.microsoft.com/en-us/uwp/api/windows.media.control.globalsystemmediatransportcontrolssessionmanager
