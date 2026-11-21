# WatchWolf real-server tests

This suite uses the published `v0.4.1` images and tester JAR from the
[sarhatabaot WatchWolf fork](https://github.com/sarhatabaot/WatchWolf-Tester).
The setup follows the current [ChunkSpawnerLimiter `watchwolf/README.md`](https://github.com/sarhatabaot/ChunkSpawnerLimiter/blob/master/watchwolf/README.md).
The managers start only the Paper server requested by
`even-more-fish-plugin/src/testWatchWolf/resources/watchwolf.yaml` through
`itzg/minecraft-server`; no WatchWolf source checkout is needed.

## Start

Docker Engine must be running with Linux containers. From the repository root:

```bash
docker compose -f watchwolf/compose.yaml up --wait -d
```

The first run downloads the requested Paper version and can take several minutes.

**Current fork limitation:** the published ServersManager `v0.4.1` rejects Paper
1.21.1 before starting a server. Its `ItzgContainerSpec.SUPPORTED_VERSIONS`
contains only 1.19, 1.20.2 and 1.20.6. EvenMoreFish declares `api-version: 1.21`,
so those older servers are not a valid substitute. The fork already accepts
`WATCHWOLF_ITZG_IMAGE` and `WATCHWOLF_ITZG_TAG`; these select the *inner Minecraft
runtime image* and are exposed in `compose.yaml`. Set the image repository and tag
separately, since the manager joins them with `:`. The image must support the
itzg-style `TYPE=PAPER` and `VERSION` environment settings, `/data` volume, and
the shell commands the manager uses to seed that volume. Changing the runtime
image or tag does not bypass the manager's hard-coded version check.

A one-time ServersManager change should validate the server type and version
format without a fixed Paper-version list, then let the configured itzg image
download the requested version. With an explicit Java 21 tag, new compatible
Paper releases should only require a `watchwolf.yaml` version change, not a new
ServersManager image. Confirm the bundled WatchWolf Server control plugin works
on the new Paper version before relying on the test. Until that manager change is
published, `WATCHWOLF_SERVERS_MANAGER_IMAGE` can point Compose at a custom build.

## Test

From the repository root:

```bash
./gradlew :even-more-fish-plugin:testWatchWolf
```

On Windows:

```powershell
.\gradlew.bat :even-more-fish-plugin:testWatchWolf
```

Gradle builds the shaded plugin and copies it to
`even-more-fish-plugin/build/watchwolf/EvenMoreFish.jar`. WatchWolf uploads it to a
temporary Paper 1.21.1 server, runs the smoke test, and stops the server afterward.
The suite is separate from `build` and `check`, so Docker is not needed for unit tests.
The [manual GitHub Actions workflow](../.github/workflows/watchwolf.yml) runs the same
smoke test on a Linux runner and uploads its test report and manager logs.

## Stop

```bash
docker compose -f watchwolf/compose.yaml down
```

To remove the manager logs as well:

```bash
docker compose -f watchwolf/compose.yaml down -v
```
