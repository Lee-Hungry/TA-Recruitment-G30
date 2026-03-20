package com.group30.tarecruitment;

import com.group30.tarecruitment.login.CsvSessionTokenRepository;
import com.group30.tarecruitment.login.CsvUserCredentialRepository;
import com.group30.tarecruitment.login.TaLoginService;
import com.group30.tarecruitment.ui.TaLoginPanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TA Login");
            frame.setSize(460, 220);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new TaLoginPanel(new TaLoginService(
                    new CsvUserCredentialRepository(Path.of("data", "user_account.csv")),
                    new CsvSessionTokenRepository(Path.of("data", "session_token.csv"))
            )));
            frame.setVisible(true);
        });
    }
}
