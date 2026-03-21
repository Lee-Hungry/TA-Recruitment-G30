package com.group30.tarecruitment;

import com.group30.tarecruitment.registration.CsvUserRepository;
import com.group30.tarecruitment.registration.TaRegistrationService;
import com.group30.tarecruitment.ui.TaRegistrationPanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TA Registration");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 260);
            frame.setLocationRelativeTo(null);
            frame.setContentPane(new TaRegistrationPanel(
                    new TaRegistrationService(new CsvUserRepository(Path.of("data", "user_account.csv")))
            ));
            frame.setVisible(true);
        });
    }
}
