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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Form_QLNV extends JPanel {

    private JTable tblNhanVien;
    private JTextField txtMaNV, txtTenNV, txtDiaChi, txtSDT, txtChucVu;
    private JButton btnThem, btnSua, btnXoa, btnTim, btnLamMoi;

    public Form_QLNV() {
        initComponents();
        loadNhanVien();
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));

        // ===== Panel trên: nhập liệu + nút =====
        JPanel panelTop = new JPanel();
        panelTop.setOpaque(false);
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GroupLayout layout = new GroupLayout(panelTop);
        panelTop.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel lbMaNV = new JLabel("Mã nhân viên:");
        JLabel lbTenNV = new JLabel("Tên nhân viên:");
        JLabel lbDiaChi = new JLabel("Địa chỉ:");
        JLabel lbSDT = new JLabel("Số điện thoại:");
        JLabel lbChucVu = new JLabel("Chức vụ:");

        txtMaNV = new JTextField();
        txtTenNV = new JTextField();
        txtDiaChi = new JTextField();
        txtSDT = new JTextField();
        txtChucVu = new JTextField();

        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnTim = new JButton("Tìm");
        btnLamMoi = new JButton("Làm mới");

        // Gán sự kiện
        btnThem.addActionListener((ActionEvent e) -> themNhanVien());
        btnSua.addActionListener((ActionEvent e) -> suaNhanVien());
        btnXoa.addActionListener((ActionEvent e) -> xoaNhanVien());
        btnTim.addActionListener((ActionEvent e) -> timNhanVien());
        btnLamMoi.addActionListener((ActionEvent e) -> lamMoi());

        // Layout ngang/dọc cho panelTop
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lbMaNV)
                                                .addComponent(txtMaNV))
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lbTenNV)
                                                .addComponent(txtTenNV))
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lbDiaChi)
                                                .addComponent(txtDiaChi))
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lbSDT)
                                                .addComponent(txtSDT))
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lbChucVu)
                                                .addComponent(txtChucVu))
                        )
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(btnThem)
                                        .addComponent(btnSua)
                                        .addComponent(btnXoa)
                                        .addComponent(btnTim)
                                        .addComponent(btnLamMoi)
                        )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lbMaNV)
                                        .addComponent(lbTenNV)
                                        .addComponent(lbDiaChi)
                        )
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtMaNV)
                                        .addComponent(txtTenNV)
                                        .addComponent(txtDiaChi)
                        )
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lbSDT)
                                        .addComponent(lbChucVu)
                        )
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtSDT)
                                        .addComponent(txtChucVu)
                        )
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnThem)
                                        .addComponent(btnSua)
                                        .addComponent(btnXoa)
                                        .addComponent(btnTim)
                                        .addComponent(btnLamMoi)
                        )
        );

        add(panelTop, BorderLayout.SOUTH);

        // ===== Panel dưới: bảng nhân viên =====
        tblNhanVien = new JTable();

        DefaultTableModel modelNV = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Nhân Viên", "Tên Nhân Viên", "Địa Chỉ Nhân Viên", "SĐT", "Chức Vụ"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblNhanVien.setModel(modelNV);
        tblNhanVien.setRowHeight(24);

        // Click lên bảng -> đổ dữ liệu lại vào textfield
        tblNhanVien.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblNhanVien.getSelectedRow();
                if (row >= 0) {
                    txtMaNV.setText(tblNhanVien.getValueAt(row, 0).toString());
                    txtTenNV.setText(tblNhanVien.getValueAt(row, 1).toString());
                    txtDiaChi.setText(tblNhanVien.getValueAt(row, 2).toString());
                    txtSDT.setText(tblNhanVien.getValueAt(row, 3).toString());
                    txtChucVu.setText(tblNhanVien.getValueAt(row, 4).toString());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblNhanVien);
        scroll.setBorder(BorderFactory.createTitledBorder("Danh sách nhân viên"));
        add(scroll, BorderLayout.CENTER);
    }

    // ================== CÁC HÀM XỬ LÝ DB ==================
    private void loadNhanVien() {
        DefaultTableModel model = (DefaultTableModel) tblNhanVien.getModel();
        model.setRowCount(0);

        String sql = "SELECT MaNV, TenNV, DiaChiNV, SDTNV, ChucVu FROM NHANVIEN";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("MaNV"),
                    rs.getString("TenNV"),
                    rs.getString("DiaChiNV"),
                    rs.getString("SDTNV"),
                    rs.getString("ChucVu")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu nhân viên!");
        }
    }

    private void themNhanVien() {
        String ma = txtMaNV.getText().trim();
        String ten = txtTenNV.getText().trim();
        String dc = txtDiaChi.getText().trim();
        String sdt = txtSDT.getText().trim();
        String cv = txtChucVu.getText().trim();

        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã và tên nhân viên không được để trống!");
            return;
        }

        String sql = "INSERT INTO NHANVIEN(MaNV, TenNV, DiaChiNV, SDTNV, ChucVu) VALUES (?,?,?,?,?)";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ma);
            ps.setString(2, ten);
            ps.setString(3, dc);
            ps.setString(4, sdt);
            ps.setString(5, cv);

            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                loadNhanVien();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi thêm nhân viên!");
        }
    }

    private void suaNhanVien() {
        String ma = txtMaNV.getText().trim();
        String ten = txtTenNV.getText().trim();
        String dc = txtDiaChi.getText().trim();
        String sdt = txtSDT.getText().trim();
        String cv = txtChucVu.getText().trim();

        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập mã nhân viên cần sửa!");
            return;
        }

        String sql = "UPDATE NHANVIEN SET TenNV = ?, DiaChiNV = ?, SDTNV = ?, ChucVu = ? WHERE MaNV = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ten);
            ps.setString(2, dc);
            ps.setString(3, sdt);
            ps.setString(4, cv);
            ps.setString(5, ma);

            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!");
                loadNhanVien();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên để sửa!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi sửa nhân viên!");
        }
    }

    private void xoaNhanVien() {
        String ma = txtMaNV.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập mã nhân viên cần xóa!");
            return;
        }

        int chon = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa nhân viên " + ma + " ?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (chon != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM NHANVIEN WHERE MaNV = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ma);

            int n = ps.executeUpdate();
            if (n > 0) {
                JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công!");
                loadNhanVien();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên để xóa!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xóa nhân viên!");
        }
    }

    private void timNhanVien() {
        String key = JOptionPane.showInputDialog(this,
                "Nhập mã hoặc tên nhân viên cần tìm:");

        if (key == null || key.trim().isEmpty()) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblNhanVien.getModel();
        model.setRowCount(0);

        String sql = "SELECT MaNV, TenNV, DiaChiNV, SDTNV, ChucVu "
                + "FROM NHANVIEN WHERE MaNV LIKE ? OR TenNV LIKE ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + key.trim() + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("DiaChiNV"),
                        rs.getString("SDTNV"),
                        rs.getString("ChucVu")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm nhân viên!");
        }
    }

    private void lamMoi() {
        txtMaNV.setText("");
        txtTenNV.setText("");
        txtDiaChi.setText("");
        txtSDT.setText("");
        txtChucVu.setText("");
        loadNhanVien();
    }
}
