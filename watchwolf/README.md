# WatchWolf real-server tests

This suite uses the published `v0.4.3` images and tester JAR from the
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

The Paper 1.21.1 plugin-load check has passed. The suite does not configure a
client because it only runs server commands. Catch and reconnect scenarios need
a separate client-enabled test and a working ClientsManager. The current suite
cannot provide Folia, Geyser, or matched rarity-attempt evidence.

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
temporary Paper 1.21.1 server, checks plugin load, reload, and competition
start/end, then stops the server afterward.
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
