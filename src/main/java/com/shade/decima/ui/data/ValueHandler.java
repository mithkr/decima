package com.shade.decima.ui.data;

import com.shade.decima.model.rtti.RTTIType;
import com.shade.platform.ui.controls.ColoredComponent;
import com.shade.util.NotNull;
import com.shade.util.Nullable;

import javax.swing.*;

public interface ValueHandler {
    /**
     * Appends the value inlined in the node's name, or none (name of the type is used instead)
     */
    void appendInlineValue(@NotNull RTTIType<?> type, @NotNull Object value, @NotNull ColoredComponent component);

    boolean hasInlineValue();

    @Nullable
    Icon getIcon(@NotNull RTTIType<?> type);
}
