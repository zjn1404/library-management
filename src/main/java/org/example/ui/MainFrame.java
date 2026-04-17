package org.example.ui;

import org.example.util.AppContext;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Hệ Thống Quản Lý Thư Viện");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        String fullName = AppContext.getCurrentAccount().getFullName();
        JLabel greetLabel = new JLabel("Xin chào, " + fullName);
        greetLabel.setFont(greetLabel.getFont().deriveFont(Font.BOLD, 14f));
        JButton logoutBtn = new JButton("Đăng xuất");
        topBar.add(greetLabel, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Độc Giả", new ReaderPanel());
        tabs.addTab("Sách", new BookPanel());
        tabs.addTab("Mượn Sách", new BorrowPanel());
        tabs.addTab("Trả Sách", new ReturnPanel());
        tabs.addTab("Thống Kê", new StatisticsPanel());
        add(tabs, BorderLayout.CENTER);

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn đăng xuất?", "Đăng xuất", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                AppContext.setCurrentAccount(null);
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
    }
}
