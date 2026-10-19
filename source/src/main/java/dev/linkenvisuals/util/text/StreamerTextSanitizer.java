package dev.linkenvisuals.util.text;

import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;

/**
 * Санитизация vanilla {@link Text} для стример-режима: обход компонент с сохранением
 * стилей, плюс маскирование текста поля ввода парольных команд.
 * <p>
 * Донор (MC 1.21.4) вешал всё на DrawContext-хуки; в 1.21.11 чат рисуется через
 * {@code DrawnTextConsumer}, поэтому тексты чистятся в источниках
 * (ChatHud.addMessage, tab, scoreboard, actionbar) — см. D36.
 */
public final class StreamerTextSanitizer {

    private StreamerTextSanitizer() {
    }

    /** @return исходный {@code text}, если модуль выключен или замен нет. */
    public static Text sanitize(Text text, StreamerRules rules) {
        if (text == null || rules == null) {
            return text;
        }
        String plain = text.getString();
        if (plain.isEmpty() || !rules.couldMatch(plain)) {
            return text;
        }
        return sanitizeComponent(text, rules);
    }

    /** Вариант для методов, возвращающих {@link MutableText} (ScoreboardEntry.formatted). */
    public static MutableText sanitizeMutable(Text text, StreamerRules rules) {
        Text result = sanitize(text, rules);
        if (result instanceof MutableText mutable) {
            return mutable;
        }
        MutableText wrapped = Text.literal(result.getString());
        wrapped.setStyle(result.getStyle());
        return wrapped;
    }

    /**
     * Маскирует видимый фрагмент поля ввода, если полный текст начинается с парольной
     * команды. Индексы сохраняются (длина не меняется), поэтому каретка/выделение не сбиваются.
     *
     * @return маскированный текст или {@code null}, если маскировать нечего
     */
    public static OrderedText maskFieldText(String full, OrderedText rendered, StreamerRules rules) {
        if (full == null || full.isEmpty() || rendered == null || rules == null) {
            return null;
        }
        int commandEnd = StreamerRules.sensitiveCommandEnd(full);
        if (commandEnd < 0) {
            return null;
        }
        String plain = plainOf(rendered);
        if (plain.isEmpty()) {
            return null;
        }
        String maskedFull = StreamerRules.maskFromIndex(full, commandEnd);
        int offset = full.length() - plain.length();
        if (offset < 0 || !full.regionMatches(offset, plain, 0, plain.length())) {
            offset = full.indexOf(plain);
        }
        String masked = offset < 0
            ? StreamerRules.maskFromIndex(plain, 0)
            : maskedFull.substring(offset, offset + plain.length());
        if (masked.equals(plain)) {
            return null;
        }
        return OrderedText.styledForwardsVisitedString(masked, Style.EMPTY);
    }

    /** Plain-строка из {@link OrderedText} (для оконного сопоставления с полным текстом поля). */
    public static String plainOf(OrderedText text) {
        StringBuilder plain = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            plain.appendCodePoint(codePoint);
            return true;
        });
        return plain.toString();
    }

    private static Text sanitizeComponent(Text text, StreamerRules rules) {
        boolean changed = false;
        MutableText out;
        TextContent content = text.getContent();
        if (content instanceof PlainTextContent literal) {
            String replaced = rules.sanitize(literal.string());
            if (replaced.equals(literal.string())) {
                out = text.copy();
            } else {
                changed = true;
                out = Text.literal(replaced);
            }
        } else if (content instanceof TranslatableTextContent translatable) {
            Object[] args = translatable.getArgs();
            Object[] replacedArgs = args;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Text arg) {
                    Text replaced = sanitize(arg, rules);
                    if (replaced != arg) {
                        if (replacedArgs == args) {
                            replacedArgs = args.clone();
                        }
                        replacedArgs[i] = replaced;
                        changed = true;
                    }
                }
            }
            out = replacedArgs == args ? text.copy()
                : MutableText.of(new TranslatableTextContent(translatable.getKey(),
                    translatable.getFallback(), replacedArgs));
        } else {
            out = text.copy();
        }
        out.setStyle(text.getStyle());
        out.getSiblings().clear();
        for (Text sibling : text.getSiblings()) {
            Text replaced = sanitize(sibling, rules);
            if (replaced != sibling) {
                changed = true;
            }
            out.append(replaced);
        }
        return changed ? out : text;
    }
}
