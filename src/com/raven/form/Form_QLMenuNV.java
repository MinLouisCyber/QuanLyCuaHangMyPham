package com.raven.form;

import com.raven.database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Form_QLMenuNV extends JPanel {

    private JTable tblMenu;
    private DefaultTableModel modelMenu;
    private JButton btnReload;

    public Form_QLMenuNV() {
        initComponents();
        loadMenuData();
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
                // ❌ Không cho sửa bất kỳ cột nào
                return false;
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
                "Danh sách giá bán (MENU) - Chỉ xem"
        ));
        add(scroll, BorderLayout.CENTER);

        // ================== PANEL BUTTON ==================
        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBottom.setOpaque(false);
        panelBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnReload = new JButton("Tải lại");
        btnReload.setToolTipText("Tải lại dữ liệu từ CSDL");
        btnReload.addActionListener(e -> loadMenuData());

        panelBottom.add(btnReload);

        add(panelBottom, BorderLayout.SOUTH);
    }

    // ================== LOAD DỮ LIỆU MENU ==================
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
}
