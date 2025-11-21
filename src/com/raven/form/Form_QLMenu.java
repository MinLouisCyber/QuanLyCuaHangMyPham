package com.raven.form;

import com.raven.database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Form_QLMenu extends JPanel {

    private JTable tblMenu;
    private DefaultTableModel modelMenu;
    private JButton btnReload;
    private JButton btnSave;

    public Form_QLMenu() {
        initComponents();
        loadMenuData();   // Lần đầu mở form -> load dữ liệu từ DB
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));

        // ================== TABLE MENU ==================
        modelMenu = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã hàng", "Tên hàng", "Đơn giá bán"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // CHỈ cho sửa cột Đơn giá (index = 2)
                return column == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {
                    return Double.class;
                }
                return String.class;
            }
        };

        tblMenu = new JTable(modelMenu);
        tblMenu.setRowHeight(24);

        JScrollPane scroll = new JScrollPane(tblMenu);
        scroll.setBorder(BorderFactory.createTitledBorder(
                "Danh sách giá bán (MENU)"
        ));
        add(scroll, BorderLayout.CENTER);

        // ================== PANEL BUTTON ==================
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBottom.setOpaque(false);
        panelBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnReload = new JButton("Tải lại");
        btnReload.setToolTipText("Tải lại dữ liệu từ CSDL (các giá chưa bấm Lưu sẽ bị mất)");
        btnReload.addActionListener(e -> loadMenuData());

        btnSave = new JButton("Lưu giá");
        btnSave.setToolTipText("Lưu các giá đã chỉnh xuống bảng MENU");
        btnSave.addActionListener(e -> saveMenuPrices());

        panelBottom.add(btnReload);
        panelBottom.add(btnSave);

        add(panelBottom, BorderLayout.SOUTH);
    }

    // ================== LOAD DỮ LIỆU MENU ==================
    // Lấy tất cả hàng từ LOAIHANG, join sang MENU để xem có giá chưa
    public void loadMenuData() {
        modelMenu.setRowCount(0);

        String sql =
                "SELECT l.MaHang, l.TenHangNhap, IFNULL(m.DonGia, 0) AS DonGia " +
                "FROM LOAIHANG l " +
                "LEFT JOIN MENU m ON l.MaHang = m.MaHang " +
                "ORDER BY l.MaHang";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maHang = rs.getString("MaHang");
                String tenHang = rs.getString("TenHangNhap");
                double donGia = rs.getDouble("DonGia");

                modelMenu.addRow(new Object[]{maHang, tenHang, donGia});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu MENU từ bảng Hàng hóa!");
        }
    }

    // ================== LƯU GIÁ XUỐNG BẢNG MENU ==================
    private void saveMenuPrices() {
        Connection conn = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psInsert = null;

        String sqlUpdate =
                "UPDATE MENU SET DonGia = ?, TenHangBan = ? WHERE MaHang = ?";

        String sqlInsert =
                "INSERT INTO MENU (MaMN, MaHang, TenHangBan, DonGia, SoLuongBan) " +
                "VALUES (?, ?, ?, ?, 0)";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);   // Transaction

            psUpdate = conn.prepareStatement(sqlUpdate);
            psInsert = conn.prepareStatement(sqlInsert);

            for (int i = 0; i < modelMenu.getRowCount(); i++) {
                String maHang = modelMenu.getValueAt(i, 0).toString();
                String tenHang = modelMenu.getValueAt(i, 1).toString();
                Object donGiaObj = modelMenu.getValueAt(i, 2);

                // Kiểm tra giá có hợp lệ không
                double donGia;
                try {
                    donGia = Double.parseDouble(donGiaObj.toString());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Đơn giá không hợp lệ tại dòng " + (i + 1) +
                                    ". Vui lòng nhập số.");
                    conn.rollback();
                    return;
                }

                if (donGia < 0) {
                    JOptionPane.showMessageDialog(this,
                            "Đơn giá phải >= 0 tại dòng " + (i + 1));
                    conn.rollback();
                    return;
                }

                // 1. UPDATE trước
                psUpdate.setDouble(1, donGia);
                psUpdate.setString(2, tenHang);
                psUpdate.setString(3, maHang);
                int affected = psUpdate.executeUpdate();

                // 2. Nếu chưa có MENU cho mã hàng này -> INSERT
                if (affected == 0) {
                    // Ở đây dùng MaMN = MaHang cho đơn giản (1-1)
                    // Nếu MaMN trong DB của em là kiểu khác (MN001, MN002...), thì đổi logic sinh mã tại đây.
                    String maMN = maHang;

                    psInsert.setString(1, maMN);
                    psInsert.setString(2, maHang);
                    psInsert.setString(3, tenHang);
                    psInsert.setDouble(4, donGia);
                    psInsert.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Lưu giá bán thành công!");

            // Sau khi lưu xong -> load lại từ DB để đảm bảo hiển thị đúng dữ liệu đã commit
            loadMenuData();

        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu MENU: " + ex.getMessage());
        } finally {
            try {
                if (psUpdate != null) psUpdate.close();
                if (psInsert != null) psInsert.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
