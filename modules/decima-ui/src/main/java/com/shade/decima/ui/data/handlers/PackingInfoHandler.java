package com.shade.decima.ui.data.handlers;

import com.shade.decima.model.rtti.RTTIType;
import com.shade.decima.ui.data.registry.ValueHandlerRegistration;
import com.shade.decima.ui.data.registry.ValueHandlerRegistration.Field;
import com.shade.decima.ui.data.registry.ValueHandlerRegistration.Selector;
import com.shade.decima.ui.data.viewer.texture.util.Channel;
import com.shade.platform.ui.controls.TextAttributes;
import com.shade.util.NotNull;

import java.util.EnumSet;

@ValueHandlerRegistration(id = "packingInfo", name = "Packing Info", value = {
    @Selector(field = @Field(type = "TextureSetEntry", field = "PackingInfo"))
})
public class PackingInfoHandler extends NumberValueHandler {
    public static final PackingInfoHandler INSTANCE = new PackingInfoHandler();

    @NotNull
    @Override
    public Decorator getDecorator(@NotNull RTTIType<?> type) {
        return (value, component) -> {
            final int data = ((Number) value).intValue();

            component.append("R=", TextAttributes.REGULAR_BOLD_ATTRIBUTES);
            component.append(getInfo(data & 0xff) + ", ", TextAttributes.REGULAR_ATTRIBUTES);

            component.append("G=", TextAttributes.REGULAR_BOLD_ATTRIBUTES);
            component.append(getInfo(data >>> 8 & 0xff) + ", ", TextAttributes.REGULAR_ATTRIBUTES);

            component.append("B=", TextAttributes.REGULAR_BOLD_ATTRIBUTES);
            component.append(getInfo(data >>> 16 & 0xff) + ", ", TextAttributes.REGULAR_ATTRIBUTES);

            component.append("A=", TextAttributes.REGULAR_BOLD_ATTRIBUTES);
            component.append(getInfo(data >>> 24 & 0xff), TextAttributes.REGULAR_ATTRIBUTES);
        };
    }

    @NotNull
    public static EnumSet<Channel> getChannels(int packedData, int packingInfo) {
        final int usage = packedData >> 2 & 15;
        final EnumSet<Channel> channels = EnumSet.noneOf(Channel.class);

        if ((packingInfo & 15) == usage)
            channels.add(Channel.R);
        if ((packingInfo >> 8 & 15) == usage)
            channels.add(Channel.G);
        if ((packingInfo >> 16 & 15) == usage)
            channels.add(Channel.B);
        if ((packingInfo >> 24 & 15) == usage)
            channels.add(Channel.A);

        return channels;
    }

    @NotNull
    public static String getInfo(int value) {
        if (value == 0x80) {
            return "N/A";
        } else {
            return getChannel(value >>> 4) + " " + getUsage(value & 0xf);
        }
    }

    @NotNull
    private static String getChannel(int value) {
        return switch (value) {
            case 0 -> "R";
            case 1 -> "G";
            case 2 -> "B";
            case 3 -> "A";
            case 8 -> "RGB";
            default -> "{" + value + "}";
        };
    }

    @NotNull
    public static String getUsage(int value) {
        return switch (value) {
            case 0 -> "Invalid";
            case 1 -> "Color";
            case 2 -> "Alpha";
            case 3 -> "Normal";
            case 4 -> "Reflectance";
            case 5 -> "AO";
            case 6 -> "Roughness";
            case 7 -> "Height";
            case 8 -> "Mask";
            case 9 -> "Mask_Alpha";
            case 10 -> "Incandescence";
            case 11 -> "Translucency_Diffusion";
            case 12 -> "Translucency_Amount";
            case 13 -> "Misc_01";
            case 14 -> "Curvature";
            case 15 -> "Luminance";
            default -> "{" + value + "}";
        };
    }
}