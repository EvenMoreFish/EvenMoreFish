package com.oheers.fish.gui;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads InventoryGui formats and maps each character to a slot for use with TriumphGui.
 */
public class GuiReader {

    private static final int MAX_LINE_LENGTH = 9;

    private final Map<Character, ArrayList<Integer>> mappedSlots;

    public static void test() {
        // [EvenMoreFish] [STDOUT] a: [18, 21, 24]
        // [EvenMoreFish] [STDOUT] b: [19, 22, 25]
        // [EvenMoreFish] [STDOUT] c: [20, 23, 26]
        // [EvenMoreFish] [STDOUT] x: [0, 3, 6, 11]
        // [EvenMoreFish] [STDOUT] y: [1, 4, 7]
        // [EvenMoreFish] [STDOUT] z: [2, 5, 8]
        List<String> layout = List.of(
            "xyzxyzxyz",
            "  x",
            "abcabcabc"
        );
        new GuiReader(layout);
    }

    public GuiReader(@NotNull List<@NotNull String> layout) {
        this.mappedSlots = read(layout);
    }

    // I have to comment each line so I can keep track of what it's doing, I may be able to clean this up at some point.
    private Map<Character, ArrayList<Integer>> read(List<String> layout) {
        Map<Character, ArrayList<Integer>> mappedSlots = new HashMap<>();
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
                // If the character is air, increment slot and skip.
                if (character == ' ') {
                    slot++;
                    continue;
                }
                // Fetch the list related to this character, insert the slot, and increment slot.
                ArrayList<Integer> list = mappedSlots.computeIfAbsent(character, ArrayList::new);
                list.add(slot);
                slot++;
            }
            // Increment slot by the air/filler character amount.
            slot += airChars;
        }
        return mappedSlots;
    }

    private Map<Character, GuiItem<Player, ItemStack>> readItems() {
        return this.section.getRoutesAsStrings(false).stream()
            .map(this.section::getSection)
            .filter(section -> section != null && section.contains("item"))
            .map(section -> EMFGuiItem.create(this.gui, section))
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(EMFGuiItem::character, EMFGuiItem::item));
    }

}
