package com.group30.tarecruitment;

import com.group30.tarecruitment.auth.AuthService;
import com.group30.tarecruitment.auth.repository.CsvSessionTokenRepository;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;
import com.group30.tarecruitment.ui.LoginFrame;

import javax.swing.SwingUtilities;
import java.nio.file.Path;

public class AppLauncher {

    public static void main(String[] args) {
        CsvUserAccountRepository userRepository = new CsvUserAccountRepository(Path.of("data", "user_account.csv"));
        CsvSessionTokenRepository sessionRepository = new CsvSessionTokenRepository(Path.of("data", "session_token.csv"));
        AuthService authService = new AuthService(userRepository, sessionRepository);

        SwingUtilities.invokeLater(() -> new LoginFrame(authService).setVisible(true));
    }
}
