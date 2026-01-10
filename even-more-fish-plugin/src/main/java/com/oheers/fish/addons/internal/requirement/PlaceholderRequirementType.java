package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderRequirementType extends RequirementType {

    private final Pattern pattern = Pattern.compile("^(.+?)(>=|<=|==|=|!=|<|>)(.+)$");

    @Override
    public boolean checkRequirement(@NonNull RequirementContext requirementContext, @NonNull List<String> values) {
        Player player = requirementContext.getPlayer();
        for (String value : values) {
            Matcher matcher = this.pattern.matcher(value);
            if (!matcher.matches()) continue;
            String placeholder = PlaceholderAPI.setPlaceholders(player, matcher.group(1)).trim();
            String operator = matcher.group(2);
            String requiredValue = matcher.group(3).trim();
            boolean pass = evaluate(placeholder, operator, requiredValue);
            if (!pass) return false;
        }
        return true;
    }

    @NonNull
    @Override
    public String getIdentifier() {
        return "PLACEHOLDER";
    }

    @NonNull
    @Override
    public String getAuthor() {
        return "KitterKatter";
    }

    @Override
    public @NonNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

    private boolean evaluate(String placeholder, String operator, String requiredValue) {
        try {
            double left = Double.parseDouble(placeholder);
            double right = Double.parseDouble(requiredValue);

            return switch (operator) {
                case ">=" -> left >= right;
                case "<=" -> left <= right;
                case ">"  -> left > right;
                case "<"  -> left < right;
                case "=", "==" -> Double.compare(left, right) == 0;
                case "!=" -> Double.compare(left, right) != 0;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return switch (operator) {
                case "=", "==" -> placeholder.equalsIgnoreCase(requiredValue);
                case "!=" -> !placeholder.equalsIgnoreCase(requiredValue);
                default -> false;
            };
        }
    }
}
