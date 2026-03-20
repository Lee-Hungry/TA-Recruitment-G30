package com.group30.tarecruitment;

import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfileService;
import com.group30.tarecruitment.ui.TaProfilePanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TA Profile Editor");
            frame.setSize(560, 360);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new TaProfilePanel(
                    new TaProfileService(new CsvTaProfileRepository(Path.of("data", "ta_profile.csv")))
            ));
            frame.setVisible(true);
        });
    }
}
