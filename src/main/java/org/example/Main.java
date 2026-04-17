package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
