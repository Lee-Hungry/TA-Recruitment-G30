package com.group30.tarecruitment;

import com.group30.tarecruitment.mo.CsvMoAccountRepository;
import com.group30.tarecruitment.mo.CsvSessionRepository;
import com.group30.tarecruitment.mo.MoLoginService;
import com.group30.tarecruitment.ui.MoLoginFrame;

import javax.swing.SwingUtilities;
import java.nio.file.Path;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MoLoginService loginService = new MoLoginService(
                    new CsvMoAccountRepository(Path.of("data", "user_account.csv")),
                    new CsvSessionRepository(Path.of("data", "session_token.csv"))
            );
            new MoLoginFrame(loginService).setVisible(true);
        });
    }
}
