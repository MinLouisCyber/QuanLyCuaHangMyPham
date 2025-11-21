package com.raven.form;

import com.raven.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Form_QLTonKho extends JPanel {

    private JTable tblTonKho;
    private JTextField txtSearch;
    private JButton btnTim, btnLamMoi;
    private JComboBox<String> cbSort;

    public Form_QLTonKho() {
        initComponents();
        loadTonKho();
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));

        // ====== BẢNG TỒN KHO (VIEW-ONLY, CÓ SẮP XẾP) ======
        DefaultTableModel modelTonKho = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Hàng", "Tên Hàng Tồn", "Số Lượng Tồn", "Ngày Nhận", "Ngày Hết Hạn", "Tên Nhà Cung Cấp"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTonKho = new JTable(modelTonKho);
        tblTonKho.setRowHeight(24);
        tblTonKho.setAutoCreateRowSorter(true); // cho phép sort theo cột (click tiêu đề)

        JScrollPane scroll = new JScrollPane(tblTonKho);
        scroll.setBorder(BorderFactory.createTitledBorder(
                "Báo cáo tồn kho theo lô hàng và nhà cung cấp"));
        add(scroll, BorderLayout.CENTER);

        // ====== PANEL DƯỚI: TÌM KIẾM + LÀM MỚI + SẮP XẾP ======
        JPanel panelBottom = new JPanel();
        panelBottom.setOpaque(false);
        panelBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbSearch = new JLabel("Tìm kiếm (Mã hàng / Tên hàng / Nhà cung cấp):");
        txtSearch = new JTextField(25);

        btnTim = new JButton("Tìm");
        btnLamMoi = new JButton("Làm mới");

        // ComboBox sắp xếp
        cbSort = new JComboBox<>(new String[]{
                "Không sắp xếp",
                "Số lượng tồn tăng dần",
                "Số lượng tồn giảm dần"
        });

        // sự kiện
        btnTim.addActionListener((ActionEvent e) -> timTonKho());
        btnLamMoi.addActionListener((ActionEvent e) -> {
            txtSearch.setText("");
            loadTonKho();
        });
        cbSort.addActionListener(e -> loadTonKho()); // đổi lựa chọn là reload

        GroupLayout layout = new GroupLayout(panelBottom);
        panelBottom.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(lbSearch)
                        .addComponent(txtSearch)
                        .addComponent(btnTim)
                        .addComponent(btnLamMoi)
                        .addComponent(cbSort)
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lbSearch)
                        .addComponent(txtSearch)
                        .addComponent(btnTim)
                        .addComponent(btnLamMoi)
                        .addComponent(cbSort)
        );

        add(panelBottom, BorderLayout.SOUTH);
    }

    // ============= TẠO CHUỖI ORDER BY THEO COMBOBOX =============
    private String buildOrderByClause() {
        if (cbSort == null || cbSort.getSelectedItem() == null) {
            return "";
        }

        String sort = cbSort.getSelectedItem().toString();
        switch (sort) {
            case "Số lượng tồn tăng dần":
                return " ORDER BY tonkho.SoLuongTon ASC";
            case "Số lượng tồn giảm dần":
                return " ORDER BY tonkho.SoLuongTon DESC";
            default:
                return "";
        }
    }

    // ============= HÀM LOAD DỮ LIỆU =============
    public void loadTonKho() {
        DefaultTableModel model = (DefaultTableModel) tblTonKho.getModel();
        model.setRowCount(0);

        String orderBy = buildOrderByClause();

        String sql =
                "SELECT tonkho.MaHang, tonkho.TenHangTon, tonkho.SoLuongTon, " +
                "       loaihang.NgayNhan, loaihang.NgayHetHan, nhacungcap.TenNCC " +
                "FROM tonkho " +
                "JOIN loaihang ON tonkho.MaHang = loaihang.MaHang " +
                "JOIN nhacungcap ON loaihang.MaNCC = nhacungcap.MaNCC" +
                orderBy;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("MaHang"),
                        rs.getString("TenHangTon"),
                        rs.getInt("SoLuongTon"),
                        rs.getDate("NgayNhan"),
                        rs.getDate("NgayHetHan"),
                        rs.getString("TenNCC")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải báo cáo tồn kho!");
        }
    }

    // ============= TÌM KIẾM =============
    private void timTonKho() {
        String key = txtSearch.getText().trim();
        if (key.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập nội dung cần tìm!");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblTonKho.getModel();
        model.setRowCount(0);

        String orderBy = buildOrderByClause();

        String sql =
                "SELECT tonkho.MaHang, tonkho.TenHangTon, tonkho.SoLuongTon, " +
                "       loaihang.NgayNhan, loaihang.NgayHetHan, nhacungcap.TenNCC " +
                "FROM tonkho " +
                "JOIN loaihang ON tonkho.MaHang = loaihang.MaHang " +
                "JOIN nhacungcap ON loaihang.MaNCC = nhacungcap.MaNCC " +
                "WHERE tonkho.MaHang LIKE ? " +
                "   OR tonkho.TenHangTon LIKE ? " +
                "   OR nhacungcap.TenNCC LIKE ? " +
                orderBy;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + key + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("MaHang"),
                            rs.getString("TenHangTon"),
                            rs.getInt("SoLuongTon"),
                            rs.getDate("NgayNhan"),
                            rs.getDate("NgayHetHan"),
                            rs.getString("TenNCC")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lôi tìm kiếm tồn kho!");
        }
    }
}
