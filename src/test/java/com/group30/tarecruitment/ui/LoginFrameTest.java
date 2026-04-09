package com.group30.tarecruitment.ui;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginFrameTest {

    @Test
    void loginCardPreferredSizeShouldReserveVisibleHeightForFormFields() {
        Dimension preferredSize = LoginFrame.loginCardPreferredSize();

        assertEquals(430, preferredSize.width);
        assertTrue(preferredSize.height >= 480);
    }
}
