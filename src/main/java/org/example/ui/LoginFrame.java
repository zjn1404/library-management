package org.example.ui;

import org.example.model.Account;
import org.example.service.AccountService;
import org.example.util.AppContext;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AccountService accountService = new AccountService();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    public LoginFrame() {
        setTitle("Thư Viện Quản Lý");
        setSize(420, 340);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        cardPanel.add(buildLoginPanel(), "login");
        cardPanel.add(buildRegisterPanel(), "register");
        setContentPane(cardPanel);

        cardLayout.show(cardPanel, accountService.hasAccounts() ? "login" : "register");
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(24, 48, 24, 48));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JLabel title = new JLabel("Đăng Nhập", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        JTextField usernameField = new JTextField(16);
        JPasswordField passwordField = new JPasswordField(16);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.35;
        panel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.35;
        panel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(passwordField, gbc);

        JButton loginBtn = new JButton("Đăng nhập");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            Account account = accountService.login(username, password);
            if (account == null) {
                JOptionPane.showMessageDialog(this,
                        "Tên đăng nhập hoặc mật khẩu không đúng.", "Lỗi đăng nhập",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                AppContext.setCurrentAccount(account);
                new MainFrame().setVisible(true);
                dispose();
            }
        });

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 48, 20, 48));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        JLabel title = new JLabel("Tạo Tài Khoản", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        JTextField usernameField = new JTextField(16);
        JPasswordField passwordField = new JPasswordField(16);
        JPasswordField confirmField = new JPasswordField(16);
        JTextField fullNameField = new JTextField(16);

        String[] labels = {"Tên đăng nhập:", "Mật khẩu:", "Xác nhận mật khẩu:", "Họ và tên:"};
        JComponent[] fields = {usernameField, passwordField, confirmField, fullNameField};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.4;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.6;
            panel.add(fields[i], gbc);
        }

        JButton registerBtn = new JButton("Tạo tài khoản");
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            String fullName = fullNameField.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng điền đầy đủ thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this,
                        "Mật khẩu xác nhận không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!accountService.register(username, password, fullName)) {
                JOptionPane.showMessageDialog(this,
                        "Tên đăng nhập đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this,
                    "Tạo tài khoản thành công! Vui lòng đăng nhập.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(cardPanel, "login");
        });

        return panel;
    }
}
