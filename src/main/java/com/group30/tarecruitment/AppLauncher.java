package com.group30.tarecruitment;

import com.group30.tarecruitment.job.CsvJobPostingRepository;
import com.group30.tarecruitment.job.JobPostingService;
import com.group30.tarecruitment.ui.MoJobPostingPanel;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.nio.file.Path;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MO Job Posting");
            frame.setSize(620, 380);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new MoJobPostingPanel(
                    new JobPostingService(new CsvJobPostingRepository(Path.of("data", "job_posting.csv")))
            ));
            frame.setVisible(true);
        });
    }
}
