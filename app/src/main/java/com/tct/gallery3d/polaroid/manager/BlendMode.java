package com.tct.gallery3d.polaroid.manager;

import java.util.Locale;

public enum BlendMode {
    UNKNOWN, NORMAL, MULTIPLY, SCREEN, SOFTLIGHT, LINEAR_BURN, LINEAR_DODGE;

    public static BlendMode parseBlendMode(String string) {
        if (string != null) {
            String uc = string.toUpperCase(Locale.US);
            for (BlendMode blendMode : BlendMode.values()) {
                if (blendMode != BlendMode.UNKNOWN && blendMode.toString().equals(uc)) {
                    return blendMode;
                }
            }
        }

        return BlendMode.UNKNOWN;
    }
}

/* EOF */
