package org.example.ui;

import org.example.model.Reader;
import org.example.service.ReaderService;
import org.example.util.FileUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;

public class ReaderPanel extends JPanel {

    private final ReaderService readerService = new ReaderService();
    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final String[] COLUMNS = {
        "STT", "Mã độc giả", "Họ tên", "CMND/CCCD",
        "Ngày sinh", "Giới tính", "Email", "Địa chỉ",
        "Ngày lập thẻ", "Ngày hết hạn"
    };

    public ReaderPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Tìm theo họ tên", "Tìm theo CMND/CCCD"});
        JTextField searchField = new JTextField(18);
        JButton searchBtn = new JButton("Tìm kiếm");
        JButton refreshBtn = new JButton("Làm mới");
        northPanel.add(searchType);
        northPanel.add(searchField);
        northPanel.add(searchBtn);
        northPanel.add(refreshBtn);
        add(northPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        configureColumnWidths();
        attachCopyMenu(table, 1, "Sao chép mã độc giả");
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton addBtn = new JButton("Thêm");
        JButton editBtn = new JButton("Sửa");
        JButton deleteBtn = new JButton("Xóa");
        southPanel.add(addBtn);
        southPanel.add(editBtn);
        southPanel.add(deleteBtn);
        add(southPanel, BorderLayout.SOUTH);

        loadAll();

        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) { loadAll(); return; }
            List<Reader> result = searchType.getSelectedIndex() == 0
                    ? readerService.findByName(keyword)
                    : readerService.findByIdCard(keyword);
            populateTable(result);
        });

        refreshBtn.addActionListener(e -> { searchField.setText(""); loadAll(); });

        addBtn.addActionListener(e -> openReaderDialog(null));

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một độc giả.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String readerId = (String) tableModel.getValueAt(row, 1);
            openReaderDialog(readerService.findById(readerId));
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một độc giả.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String readerId = (String) tableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa độc giả này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                readerService.delete(readerId);
                loadAll();
            }
        });
    }

    private void attachCopyMenu(JTable tbl, int col, String label) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem(label);
        menu.add(item);
        item.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            String val = tbl.getValueAt(row, col).toString();
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(val), null);
        });
        tbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) show(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) show(e);
            }
            private void show(MouseEvent e) {
                int row = tbl.rowAtPoint(e.getPoint());
                if (row >= 0) tbl.setRowSelectionInterval(row, row);
                menu.show(tbl, e.getX(), e.getY());
            }
        });
    }

    private void configureColumnWidths() {
        int[] widths = {40, 120, 150, 120, 90, 70, 160, 180, 100, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void loadAll() {
        populateTable(readerService.getAll());
    }

    private void populateTable(List<Reader> readers) {
        tableModel.setRowCount(0);
        int stt = 1;
        for (Reader r : readers) {
            tableModel.addRow(new Object[]{
                stt++,
                r.getReaderId(),
                r.getFullName(),
                r.getIdCard(),
                r.getDob().format(FileUtil.DATE_FMT),
                r.getGender(),
                r.getEmail(),
                r.getAddress(),
                r.getCardDate().format(FileUtil.DATE_FMT),
                r.getExpiryDate().format(FileUtil.DATE_FMT)
            });
        }
    }

    private void openReaderDialog(Reader existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                isEdit ? "Sửa độc giả" : "Thêm độc giả",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 400);
        dialog.setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);

        JTextField fullNameField  = new JTextField(20);
        JTextField idCardField    = new JTextField(20);
        JTextField dobField       = new JTextField(20);
        dobField.setToolTipText("dd/MM/yyyy");
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Nam", "Nữ"});
        JTextField emailField     = new JTextField(20);
        JTextField addressField   = new JTextField(20);
        JTextField cardDateField  = new JTextField(20);
        cardDateField.setToolTipText("dd/MM/yyyy");

        if (isEdit) {
            fullNameField.setText(existing.getFullName());
            idCardField.setText(existing.getIdCard());
            dobField.setText(existing.getDob().format(FileUtil.DATE_FMT));
            genderBox.setSelectedItem(existing.getGender());
            emailField.setText(existing.getEmail());
            addressField.setText(existing.getAddress());
            cardDateField.setText(existing.getCardDate().format(FileUtil.DATE_FMT));
        }

        String[] labels = {"Họ và tên:", "CMND/CCCD:", "Ngày sinh:", "Giới tính:", "Email:", "Địa chỉ:", "Ngày lập thẻ:"};
        JComponent[] fields = {fullNameField, idCardField, dobField, genderBox, emailField, addressField, cardDateField};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add(fields[i], gbc);
        }

        JButton saveBtn = new JButton("Lưu");
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2; gbc.weightx = 1.0;
        gbc.insets = new Insets(12, 4, 4, 4);
        form.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            try {
                String fullName = fullNameField.getText().trim();
                String idCard   = idCardField.getText().trim();
                String email    = emailField.getText().trim();
                String address  = addressField.getText().trim();
                String gender   = (String) genderBox.getSelectedItem();

                if (fullName.isEmpty() || idCard.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog,
                            "Vui lòng điền đầy đủ họ tên và CMND/CCCD.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LocalDate dob      = LocalDate.parse(dobField.getText().trim(), FileUtil.DATE_FMT);
                LocalDate cardDate = LocalDate.parse(cardDateField.getText().trim(), FileUtil.DATE_FMT);

                if (isEdit) {
                    Reader updated = new Reader(
                            existing.getReaderId(), fullName, idCard, dob, gender, email, address, cardDate);
                    readerService.update(updated);
                } else {
                    Reader newReader = new Reader(null, fullName, idCard, dob, gender, email, address, cardDate);
                    readerService.add(newReader);
                }
                loadAll();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Lỗi nhập liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(new JScrollPane(form));
        dialog.setVisible(true);
    }
}
