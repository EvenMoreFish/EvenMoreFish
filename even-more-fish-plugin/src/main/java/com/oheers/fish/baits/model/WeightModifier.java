package com.oheers.fish.baits.model;

import org.jspecify.annotations.NonNull;

import java.util.Locale;

public record WeightModifier(@NonNull Operation operation, double value, @NonNull String expression) {

    public static final WeightModifier IDENTITY = new WeightModifier(Operation.ADD, 0.0D, "+0");

    public static @NonNull WeightModifier parse(Object rawValue) {
        if (rawValue == null) {
            return IDENTITY;
        }

        if (rawValue instanceof Number number) {
            return add(number.doubleValue());
        }

        String input = rawValue.toString().trim();
        if (input.isEmpty()) {
            return IDENTITY;
        }

        char prefix = input.charAt(0);
        if (prefix == '+' || prefix == '-' || prefix == '*' || prefix == '/') {
            double value = parseDouble(input.substring(1).trim(), input);
            return switch (prefix) {
                case '+' -> new WeightModifier(Operation.ADD, value, input);
                case '-' -> new WeightModifier(Operation.SUBTRACT, value, input);
                case '*' -> new WeightModifier(Operation.MULTIPLY, value, input);
                case '/' -> new WeightModifier(Operation.DIVIDE, value, input);
                default -> throw new IllegalStateException("Unexpected value: " + prefix);
            };
        }

        return add(parseDouble(input, input));
    }

    public static @NonNull WeightModifier add(double value) {
        return new WeightModifier(Operation.ADD, value, formatExpression('+', value));
    }

    public static @NonNull WeightModifier multiply(double value) {
        return new WeightModifier(Operation.MULTIPLY, value, formatExpression('*', value));
    }

    public double apply(double baseWeight) {
        double adjustedWeight = switch (operation) {
            case ADD -> baseWeight + value;
            case SUBTRACT -> baseWeight - value;
            case MULTIPLY -> baseWeight * value;
            case DIVIDE -> value == 0.0D ? 0.0D : baseWeight / value;
        };
        return Math.max(0.0D, adjustedWeight);
    }

    public boolean isIdentity() {
        return operation == Operation.ADD && value == 0.0D;
    }

    public @NonNull String describe() {
        return expression;
    }

    private static double parseDouble(@NonNull String value, @NonNull String originalInput) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid weight modifier: " + originalInput, exception);
        }
    }

    private static @NonNull String formatExpression(char operator, double value) {
        return ("%c%s").formatted(operator, formatNumber(value));
    }

    private static @NonNull String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.3f", value)
            .replaceAll("0+$", "")
            .replaceAll("\\.$", "");
    }

    public enum Operation {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }
}
