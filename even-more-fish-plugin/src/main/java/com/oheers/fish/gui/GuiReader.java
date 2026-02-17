package com.oheers.fish.gui;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.builder.gui.BaseGuiBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Reads InventoryGui formats and maps each character to a slot for use with TriumphGui.
 */
public class GuiReader {

    private static final int MAX_LINE_LENGTH = 9;

    private final EMFGui gui;
    private final BaseGui base;
    private final Section section;

    protected GuiReader(@NotNull EMFGui gui, @NotNull BaseGui base) {
        this.gui = gui;
        this.base = base;
        this.section = gui.getConfig();
    }

    protected void addItems() {
        Map<Character, List<Integer>> mappedSlots = readSlots();
        Map<Character, GuiItem> items = readItems();

        mappedSlots.forEach((character, slots) -> {
            GuiItem item = items.get(character);
            if (item == null) {
                return;
            }
            slots.forEach(slot -> this.base.setItem(slot, item));
        });
    }

    // I have to comment each line so I can keep track of what it's doing, I may be able to clean this up at some point.
    private Map<Character, List<Integer>> readSlots() {
        List<String> layout = section.getStringList("layout");
        if (layout.isEmpty()) {
            throw new EMFGuiException("No layout is configured.");
        }

        Map<Character, List<Integer>> mappedSlots = new HashMap<>();
        int slot = 0;

        // Loop over every line
        for (String line : layout) {
            int length = line.length();
            // Check if the line length goes over the max
            if (length > MAX_LINE_LENGTH) {
                throw new EMFGuiException("Invalid line length: " + length);
            }
            // Calculate characters that should be air/filler
            int airChars = MAX_LINE_LENGTH - line.length();
            for (char character : line.toCharArray()) {
                // If the character is air or hashtag, increment slot and skip.
                if (character == ' ' || character == '#') {
                    slot++;
                    continue;
                }
                // Fetch the list related to this character, insert the slot, and increment slot.
                List<Integer> list = mappedSlots.computeIfAbsent(character, ArrayList::new);
                list.add(slot);
                slot++;
            }
            // Increment slot by the air/filler character amount.
            slot += airChars;
        }
        return mappedSlots;
    }

    private Map<Character, GuiItem> readItems() {
        return this.section.getRoutesAsStrings(false).stream()
            .map(this.section::getSection)
            .filter(section -> section != null && section.contains("item"))
            .map(section -> EMFGuiItem.create(this.gui, section))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(EMFGuiItem::character, EMFGuiItem::item));
    }

}
