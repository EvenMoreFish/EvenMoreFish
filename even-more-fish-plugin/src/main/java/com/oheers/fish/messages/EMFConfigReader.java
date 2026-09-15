package com.oheers.fish.messages;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import uk.firedev.daisylib.messages.config.ConfigReader;

import java.util.List;

public class EMFConfigReader implements ConfigReader<Section> {

    private final Section config;

    public EMFConfigReader(@NonNull Section section) {
        this.config = section;
    }
    
    @Override
    public @Nullable Object getObject(String path) {
        return config.get(path);
    }

    @Override
    public @Nullable String getString(String path) {
        return config.getString(path);
    }

    @Override
    public @NonNull List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    @Override
    public @NonNull Section getConfig() {
        return config;
    }

    @Override
    public @Nullable ConfigReader<Section> getSection(@NonNull String path) {
        Section section = config.getSection(path);
        return section == null ? null : new EMFConfigReader(section);
    }
    
}
