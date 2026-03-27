package com.oheers.fish.utils;

import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public record DurationFormatter(@NotNull TimeUnit timeUnit) {

    public Component format(long value) {
        long seconds = timeUnit.toSeconds(value);

        // Convert seconds to a Duration
        Duration duration = Duration.ofSeconds(seconds);

        // Calculate hours, minutes, and remaining seconds
        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long remainingSeconds = duration.toSecondsPart();

        // Format the result
        List<Component> list = new ArrayList<>();

        appendUnit(list, days, Unit.DAY);
        appendUnit(list, hours, Unit.HOUR);
        appendUnit(list, minutes, Unit.MINUTE);
        appendUnit(list, remainingSeconds, Unit.SECOND);

        return Component.join(JoinConfiguration.separator(Component.space()), list);
    }

    private static void appendUnit(@NotNull List<Component> list, long value, @NotNull DurationFormatter.Unit timeUnit) {
        if (value <= 0) {
            return;
        }
        list.add(timeUnit.getFormat(value).getComponentMessage());
    }

    private enum Unit {
        DAY(
            ConfigMessage.DURATION_DAY::getMessage,
            "{day}"
        ),
        HOUR(
            ConfigMessage.DURATION_HOUR::getMessage,
            "{hour}"
        ),
        MINUTE(
            ConfigMessage.DURATION_MINUTE::getMessage,
            "{minute}"
        ),
        SECOND(
            ConfigMessage.DURATION_SECOND::getMessage,
            "{second}"
        );

        private final Supplier<EMFMessage> formatSupplier;
        private final String variable;

        Unit(@NotNull Supplier<EMFMessage> formatSupplier, @NotNull String variable) {
            this.formatSupplier = formatSupplier;
            this.variable = variable;
        }

        public EMFMessage getFormat(long value) {
            EMFMessage message = formatSupplier.get();
            message.setVariable(this.variable, value);
            return message;
        }

    }

}
