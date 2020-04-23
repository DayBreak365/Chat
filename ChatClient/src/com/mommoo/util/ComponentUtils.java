package com.mommoo.util;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

public class ComponentUtils {
    private ComponentUtils() {

    }

    public static Dimension getAvailableSize(Container comp) {
        Insets insets = comp.getInsets();
        return new ComputableDimension(comp.getSize())
                .subDimension(insets.left, insets.top)
                .subDimension(insets.right, insets.bottom);
    }
}
