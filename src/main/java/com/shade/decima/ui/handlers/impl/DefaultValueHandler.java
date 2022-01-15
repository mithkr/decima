package com.shade.decima.ui.handlers.impl;

import com.shade.decima.rtti.RTTIType;
import com.shade.decima.ui.handlers.ValueHandler;
import com.shade.decima.util.NotNull;
import com.shade.decima.util.Nullable;

public class DefaultValueHandler implements ValueHandler {
    public static final DefaultValueHandler INSTANCE = new DefaultValueHandler();

    private DefaultValueHandler() {
    }

    @Nullable
    @Override
    public String getInlineValue(@NotNull RTTIType<?> type, @NotNull Object value) {
        return String.valueOf(value);
    }
}