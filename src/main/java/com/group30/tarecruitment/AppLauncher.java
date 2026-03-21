package com.group30.tarecruitment;

import com.group30.tarecruitment.login.CsvSessionTokenRepository;
import com.group30.tarecruitment.login.CsvUserCredentialRepository;
import com.group30.tarecruitment.login.TaLoginService;
import com.group30.tarecruitment.mo.CsvMoAccountRepository;
import com.group30.tarecruitment.mo.CsvSessionRepository;
import com.group30.tarecruitment.mo.MoLoginService;
import com.group30.tarecruitment.registration.CsvUserRepository;
import com.group30.tarecruitment.registration.TaRegistrationService;
import com.group30.tarecruitment.ui.MoLoginFrame;
import com.group30.tarecruitment.ui.TaLoginPanel;
import com.group30.tarecruitment.ui.TaRegistrationPanel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.nio.file.Path;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TA Recruitment");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(620, 360);
            frame.setLocationRelativeTo(null);

            Path userCsv = Path.of("data", "user_account.csv");
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("TA Registration", new TaRegistrationPanel(
                    new TaRegistrationService(new CsvUserRepository(userCsv))
            ));
            tabs.addTab("TA Login", new TaLoginPanel(new TaLoginService(
                    new CsvUserCredentialRepository(userCsv),
                    new CsvSessionTokenRepository(Path.of("data", "session_token.csv"))
            )));

            MoLoginService moLoginService = new MoLoginService(
                    new CsvMoAccountRepository(userCsv),
                    new CsvSessionRepository(Path.of("data", "session_token.csv"))
            );
            JPanel moTab = new JPanel(new BorderLayout());
            JButton openMoLogin = new JButton("Open MO Login Window");
            openMoLogin.addActionListener(e -> new MoLoginFrame(moLoginService).setVisible(true));
            moTab.add(openMoLogin, BorderLayout.CENTER);
            tabs.addTab("MO Login", moTab);

            frame.setContentPane(tabs);
            frame.setVisible(true);
        });
    }
}