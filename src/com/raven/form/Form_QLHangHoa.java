package com.raven.form;

import com.raven.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Form_QLHangHoa extends JPanel {

    private JTable tblLoaiHang;
    private JTextField txtMaHang, txtTenHangNhap, txtSoLuongNhap, txtNgayNhan, txtNgayHetHan, txtMaNCC;
    private JButton btnThem, btnSua, btnXoa, btnTim, btnLamMoi;

    public Form_QLHangHoa() {
        initComponents();
        loadLoaiHang();
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));

        // ====== BẢNG Ở TRÊN ======
        tblLoaiHang = new JTable();

        DefaultTableModel modelLoaiHang = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Hàng", "Tên Hàng Nhập", "Số Lượng Nhập", "Ngày Nhận", "Ngày Hết Hạn", "Mã Nhà Cung Cấp"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLoaiHang.setModel(modelLoaiHang);
        tblLoaiHang.setRowHeight(24);

        // Click vào bảng -> đổ dữ liệu xuống textfield
        tblLoaiHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblLoaiHang.getSelectedRow();
                if (row >= 0) {
                    txtMaHang.setText(tblLoaiHang.getValueAt(row, 0).toString());
                    txtTenHangNhap.setText(tblLoaiHang.getValueAt(row, 1).toString());
                    txtSoLuongNhap.setText(tblLoaiHang.getValueAt(row, 2).toString());
                    txtNgayNhan.setText(String.valueOf(tblLoaiHang.getValueAt(row, 3)));
                    txtNgayHetHan.setText(String.valueOf(tblLoaiHang.getValueAt(row, 4)));
                    txtMaNCC.setText(tblLoaiHang.getValueAt(row, 5).toString());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblLoaiHang);
        scroll.setBorder(BorderFactory.createTitledBorder("Danh sách lô hàng nhập"));
        add(scroll, BorderLayout.CENTER);

        // ====== PANEL DƯỚI: NHẬP LIỆU + NÚT ======
        JPanel panelBottom = new JPanel();
        panelBottom.setOpaque(false);
        panelBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GroupLayout layout = new GroupLayout(panelBottom);
        panelBottom.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel lbMaHang = new JLabel("Mã hàng:");
        JLabel lbTenHangNhap = new JLabel("Tên hàng nhập:");
        JLabel lbSoLuongNhap = new JLabel("Số lượng nhập:");
        JLabel lbNgayNhan = new JLabel("Ngày nhận (yyyy-MM-dd):");
        JLabel lbNgayHetHan = new JLabel("Ngày hết hạn (yyyy-MM-dd):");
        JLabel lbMaNCC = new JLabel("Mã NCC:");

        txtMaHang = new JTextField();
        txtTenHangNhap = new JTextField();
        txtSoLuongNhap = new JTextField();
        txtNgayNhan = new JTextField();
        txtNgayHetHan = new JTextField();
        txtMaNCC = new JTextField();

        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnTim = new JButton("Tìm");
        btnLamMoi = new JButton("Làm mới");

        // Gán sự kiện nút
        btnThem.addActionListener((ActionEvent e) -> themLoaiHang());
        btnSua.addActionListener((ActionEvent e) -> suaLoaiHang());
        btnXoa.addActionListener((ActionEvent e) -> xoaLoaiHang());
        btnTim.addActionListener((ActionEvent e) -> timLoaiHang());
        btnLamMoi.addActionListener((ActionEvent e) -> lamMoi());

        // Layout ngang/dọc
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lbMaHang)
                                                .addComponent(txtMaHang)
                                                .addComponent(lbNgayNhan)
                                                .addComponent(txtNgayNhan))
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lbTenHangNhap)
                                                .addComponent(txtTenHangNhap)
                                                .addComponent(lbNgayHetHan)
                                                .addComponent(txtNgayHetHan))
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(lbSoLuongNhap)
                                                .addComponent(txtSoLuongNhap)
                                                .addComponent(lbMaNCC)
                                                .addComponent(txtMaNCC))
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
                                        .addComponent(lbMaHang)
                                        .addComponent(lbTenHangNhap)
                                        .addComponent(lbSoLuongNhap)
                        )
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtMaHang)
                                        .addComponent(txtTenHangNhap)
                                        .addComponent(txtSoLuongNhap)
                        )
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lbNgayNhan)
                                        .addComponent(lbNgayHetHan)
                                        .addComponent(lbMaNCC)
                        )
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtNgayNhan)
                                        .addComponent(txtNgayHetHan)
                                        .addComponent(txtMaNCC)
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

        add(panelBottom, BorderLayout.SOUTH);
    }

    // ============= HÀM LÀM VIỆC VỚI CSDL =============
    private void loadLoaiHang() {
        DefaultTableModel model = (DefaultTableModel) tblLoaiHang.getModel();
        model.setRowCount(0);

        String sql = "SELECT MaHang, TenHangNhap, SoLuongNhap, NgayNhan, NgayHetHan, MaNCC FROM LOAIHANG";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("MaHang"),
                    rs.getString("TenHangNhap"),
                    rs.getInt("SoLuongNhap"),
                    rs.getDate("NgayNhan"),
                    rs.getDate("NgayHetHan"),
                    rs.getString("MaNCC")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu loại hàng!");
        }
    }

    // ====== THÊM LOẠI HÀNG + CẬP NHẬT TỒN KHO ======
    private void themLoaiHang() {
        String ma = txtMaHang.getText().trim();
        String ten = txtTenHangNhap.getText().trim();
        String slStr = txtSoLuongNhap.getText().trim();
        String ngayNhanStr = txtNgayNhan.getText().trim();
        String ngayHetHanStr = txtNgayHetHan.getText().trim();
        String maNCC = txtMaNCC.getText().trim();

        if (ma.isEmpty() || ten.isEmpty() || slStr.isEmpty()
                || ngayNhanStr.isEmpty() || ngayHetHanStr.isEmpty() || maNCC.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không được để trống các trường!");
            return;
        }

        int soLuongNhap;
        try {
            soLuongNhap = Integer.parseInt(slStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng nhập phải là số nguyên!");
            return;
        }

        Date ngayNhan;
        Date ngayHetHan;
        try {
            ngayNhan = Date.valueOf(ngayNhanStr);      // yyyy-MM-dd
            ngayHetHan = Date.valueOf(ngayHetHanStr);  // yyyy-MM-dd
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Ngày phải đúng định dạng yyyy-MM-dd!");
            return;
        }

        String sqlInsertLoaiHang
                = "INSERT INTO LOAIHANG(MaHang, TenHangNhap, SoLuongNhap, NgayNhan, NgayHetHan, MaNCC) "
                + "VALUES (?,?,?,?,?,?)";
        
        // TONKHO luôn mirror LOAIHANG: SoLuongTon = SoLuongNhap
        String sqlTonKhoUpsert
                = "INSERT INTO TONKHO(MaHang, TenHangTon, SoLuongTon) "
                + "VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE "
                + " TenHangTon = VALUES(TenHangTon), "
                + " SoLuongTon = VALUES(SoLuongTon)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối CSDL!");
                return;
            }

            try {
                conn.setAutoCommit(false);  // bắt đầu transaction

                // 1. Thêm vào LOAIHANG
                try (PreparedStatement ps = conn.prepareStatement(sqlInsertLoaiHang)) {
                    ps.setString(1, ma);
                    ps.setString(2, ten);
                    ps.setInt(3, soLuongNhap);
                    ps.setDate(4, ngayNhan);
                    ps.setDate(5, ngayHetHan);
                    ps.setString(6, maNCC);
                    ps.executeUpdate();
                }

                // 2. Cập nhật TONKHO = mirror
                try (PreparedStatement psTK = conn.prepareStatement(sqlTonKhoUpsert)) {
                    psTK.setString(1, ma);
                    psTK.setString(2, ten);
                    psTK.setInt(3, soLuongNhap);
                    psTK.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this,
                        "Nhập hàng thành công!");
                loadLoaiHang();

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi nhập hàng, đã rollback!");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL!");
        }
    }

    private void suaLoaiHang() {
        String ma = txtMaHang.getText().trim();
        String ten = txtTenHangNhap.getText().trim();
        String slStr = txtSoLuongNhap.getText().trim();
        String ngayNhanStr = txtNgayNhan.getText().trim();
        String ngayHetHanStr = txtNgayHetHan.getText().trim();
        String maNCC = txtMaNCC.getText().trim();

        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập Mã hàng cần sửa!");
            return;
        }

        int soLuongNhap;
        try {
            soLuongNhap = Integer.parseInt(slStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng nhập phải là số nguyên!");
            return;
        }

        Date ngayNhan;
        Date ngayHetHan;
        try {
            ngayNhan = Date.valueOf(ngayNhanStr);
            ngayHetHan = Date.valueOf(ngayHetHanStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Ngày phải đúng định dạng yyyy-MM-dd!");
            return;
        }

        String sqlUpdateLoaiHang
                = "UPDATE LOAIHANG SET TenHangNhap = ?, SoLuongNhap = ?, NgayNhan = ?, NgayHetHan = ?, MaNCC = ? "
                + "WHERE MaHang = ?";

        String sqlUpdateTonKho
                = "UPDATE TONKHO SET TenHangTon = ?, SoLuongTon = ? WHERE MaHang = ?";

        String sqlInsertTonKho
                = "INSERT INTO TONKHO(MaHang, TenHangTon, SoLuongTon) VALUES (?,?,?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối CSDL!");
                return;
            }

            try {
                conn.setAutoCommit(false);

                // 1. Sửa LOAIHANG
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdateLoaiHang)) {
                    ps.setString(1, ten);
                    ps.setInt(2, soLuongNhap);
                    ps.setDate(3, ngayNhan);
                    ps.setDate(4, ngayHetHan);
                    ps.setString(5, maNCC);
                    ps.setString(6, ma);
                    int n = ps.executeUpdate();
                    if (n == 0) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Không tìm thấy Mã hàng để sửa!");
                        return;
                    }
                }

                // 2. Đồng bộ TONKHO với LOAIHANG
                int affected;
                try (PreparedStatement psTK = conn.prepareStatement(sqlUpdateTonKho)) {
                    psTK.setString(1, ten);
                    psTK.setInt(2, soLuongNhap);
                    psTK.setString(3, ma);
                    affected = psTK.executeUpdate();
                }

                // Nếu chưa có thì insert mới
                if (affected == 0) {
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsertTonKho)) {
                        psIns.setString(1, ma);
                        psIns.setString(2, ten);
                        psIns.setInt(3, soLuongNhap);
                        psIns.executeUpdate();
                    }
                }

                conn.commit();
                JOptionPane.showMessageDialog(this,
                        "Cập nhật lô hàng thành công!");
                loadLoaiHang();

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi sửa dữ liệu loại hàng, đã rollback!");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL!");
        }
    }

    private void xoaLoaiHang() {
        String ma = txtMaHang.getText().trim();

        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập Mã hàng cần xóa!");
            return;
        }

        int chon = JOptionPane.showConfirmDialog(this,
                "Xóa lô hàng sẽ xóa luôn mặt hàng này khỏi TỒN KHO.\nBạn có chắc chắn?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (chon != JOptionPane.YES_OPTION) {
            return;
        }

        String sqlDeleteTonKho = "DELETE FROM TONKHO WHERE MaHang = ?";
        String sqlDeleteLoaiHang = "DELETE FROM LOAIHANG WHERE MaHang = ?";

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Không thể kết nối CSDL!");
                return;
            }

            try {
                conn.setAutoCommit(false);

                // 1. Xóa tồn kho trước (do có FK MaHang -> LOAIHANG)
                try (PreparedStatement psTK = conn.prepareStatement(sqlDeleteTonKho)) {
                    psTK.setString(1, ma);
                    psTK.executeUpdate();  // có hay không cũng không sao
                }

                // 2. Xóa lô hàng
                int affected;
                try (PreparedStatement psLH = conn.prepareStatement(sqlDeleteLoaiHang)) {
                    psLH.setString(1, ma);
                    affected = psLH.executeUpdate();
                }

                if (affected == 0) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Không tìm thấy Mã hàng để xóa!");
                    return;
                }

                conn.commit();
                JOptionPane.showMessageDialog(this,
                        "Xóa lô hàng thành công!");
                loadLoaiHang();

            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi xóa dữ liệu loại hàng, đã rollback!");
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL!");
        }
    }

    private void timLoaiHang() {
        String key = JOptionPane.showInputDialog(this,
                "Nhập mã hàng, tên hàng hoặc mã NCC cần tìm:");

        if (key == null || key.trim().isEmpty()) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblLoaiHang.getModel();
        model.setRowCount(0);

        String sql
                = "SELECT MaHang, TenHangNhap, SoLuongNhap, NgayNhan, NgayHetHan, MaNCC "
                + "FROM LOAIHANG "
                + "WHERE MaHang LIKE ? OR TenHangNhap LIKE ? OR MaNCC LIKE ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + key.trim() + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("MaHang"),
                        rs.getString("TenHangNhap"),
                        rs.getInt("SoLuongNhap"),
                        rs.getDate("NgayNhan"),
                        rs.getDate("NgayHetHan"),
                        rs.getString("MaNCC")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm loại hàng!");
        }
    }

    private void lamMoi() {
        txtMaHang.setText("");
        txtTenHangNhap.setText("");
        txtSoLuongNhap.setText("");
        txtNgayNhan.setText("");
        txtNgayHetHan.setText("");
        txtMaNCC.setText("");
        loadLoaiHang();
    }
}
