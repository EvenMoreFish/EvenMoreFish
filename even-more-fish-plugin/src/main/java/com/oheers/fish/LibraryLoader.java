package com.oheers.fish;

import com.google.gson.Gson;
import net.byteflux.libby.Library;
import net.byteflux.libby.BukkitLibraryManager;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class LibraryLoader {

    private final EvenMoreFish plugin;

    public LibraryLoader(@NotNull EvenMoreFish plugin) {
        this.plugin = plugin;
    }

    public void loadLibraries() throws IOException {
        PluginLibraries libraries = read();
        if (libraries == null) {
            throw new IOException("Failed to read bukkit-libraries.json");
        }
        BukkitLibraryManager manager = new BukkitLibraryManager(plugin, "libraries");
        libraries.asRepositories().forEach(repo -> manager.addRepository(repo.getUrl()));
        libraries.asDependencies().forEach(dep -> {
            Artifact artifact = dep.getArtifact();
            Library library = Library.builder()
                .artifactId(artifact.getArtifactId())
                .groupId(artifact.getGroupId())
                .version(artifact.getVersion())
                .build();
            manager.loadLibrary(library);
        });
    }

    private PluginLibraries read() throws IOException {
        try (var in = getClass().getResourceAsStream("/bukkit-libraries.json")) {
            return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), PluginLibraries.class);
        }
    }

    private record PluginLibraries(Map<String, String> repositories, List<String> dependencies) {
        public Stream<Dependency> asDependencies() {
            if (dependencies == null) {
                return Stream.empty();
            }
            return dependencies.stream()
                .map(d -> new Dependency(new DefaultArtifact(d), null));
        }

        public Stream<RemoteRepository> asRepositories() {
            if (repositories == null) {
                return Stream.empty();
            }
            return repositories.entrySet().stream()
                .map(e -> new RemoteRepository.Builder(e.getKey(), "default", e.getValue()).build());
        }
    }
}
