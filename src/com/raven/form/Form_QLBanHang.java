package com.raven.form;

import com.raven.database.DBConnection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

public class Form_QLBanHang extends JPanel {

    private JComboBox<String> cbKhach;
    private JComboBox<String> cbNhanVien;
    private JComboBox<String> cbMaHang;
    private JTextField txtTenHang;
    private JTextField txtDonGia;
    private JTextField txtTonKho;
    private JTextField txtSoLuong;
    private JTable tblGioHang;
    private DefaultTableModel modelGioHang;
    private JLabel lblTongTien;
    private JButton btnThem;
    private JButton btnTaoHoaDon;
    private JButton btnXoaGio;

    // Formatter tiền dùng chung
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    public Form_QLBanHang() {
        initComponents();
        loadKhach();
        loadNhanVien();
        loadMaHang();
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));

        // ================== PANEL TRÊN: KHÁCH + NHÂN VIÊN + HÀNG ==================
        JPanel panelTop = new JPanel();
        panelTop.setLayout(new BorderLayout(10, 10));
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelTop.setOpaque(false);

        // ---- Thông tin khách & nhân viên ----
        JPanel panelInfo = new JPanel(new GridLayout(2, 2, 10, 10));
        panelInfo.setBorder(new TitledBorder("Thông tin hóa đơn"));
        panelInfo.setOpaque(false);

        panelInfo.add(new JLabel("Khách hàng:"));
        cbKhach = new JComboBox<>();
        panelInfo.add(cbKhach);

        panelInfo.add(new JLabel("Nhân viên lập hóa đơn:"));
        cbNhanVien = new JComboBox<>();
        panelInfo.add(cbNhanVien);

        // ---- Thông tin hàng bán ----
        JPanel panelHang = new JPanel(new GridLayout(5, 2, 10, 10));
        panelHang.setBorder(new TitledBorder("Thêm hàng vào giỏ"));
        panelHang.setOpaque(false);

        panelHang.add(new JLabel("Mã hàng:"));
        cbMaHang = new JComboBox<>();
        cbMaHang.addActionListener(e -> loadThongTinHang());
        panelHang.add(cbMaHang);

        panelHang.add(new JLabel("Tên hàng:"));
        txtTenHang = new JTextField();
        txtTenHang.setEditable(false);
        panelHang.add(txtTenHang);

        panelHang.add(new JLabel("Đơn giá:"));
        txtDonGia = new JTextField();
        txtDonGia.setEditable(false);
        panelHang.add(txtDonGia);

        panelHang.add(new JLabel("Số lượng tồn:"));
        txtTonKho = new JTextField();
        txtTonKho.setEditable(false);
        panelHang.add(txtTonKho);

        panelHang.add(new JLabel("Số lượng mua:"));
        txtSoLuong = new JTextField();
        panelHang.add(txtSoLuong);

        // ---- Nút thêm vào giỏ ----
        btnThem = new JButton("Thêm vào giỏ");
        btnThem.addActionListener(e -> themVaoGio());

        JPanel panelHangBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelHangBottom.setOpaque(false);
        panelHangBottom.add(btnThem);

        JPanel panelRightTop = new JPanel();
        panelRightTop.setLayout(new BorderLayout());
        panelRightTop.setOpaque(false);
        panelRightTop.add(panelHang, BorderLayout.CENTER);
        panelRightTop.add(panelHangBottom, BorderLayout.SOUTH);

        panelTop.add(panelInfo, BorderLayout.WEST);
        panelTop.add(panelRightTop, BorderLayout.CENTER);

        add(panelTop, BorderLayout.NORTH);

        // ================== BẢNG GIỎ HÀNG ==================
        modelGioHang = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Hàng", "Tên Hàng", "Đơn Giá", "Số Lượng", "Thành tiền"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblGioHang = new JTable(modelGioHang);
        tblGioHang.setRowHeight(24);

        // Format hiển thị tiền cho cột Đơn Giá (2) và Thành tiền (4)
        formatMoneyColumns();

        JScrollPane scrollGio = new JScrollPane(tblGioHang);
        scrollGio.setBorder(new TitledBorder("Giỏ hàng"));
        add(scrollGio, BorderLayout.CENTER);

        // ================== PANEL DƯỚI: TỔNG TIỀN + TẠO HÓA ĐƠN ==================
        JPanel panelBottom = new JPanel(new BorderLayout());
        panelBottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelBottom.setOpaque(false);

        JPanel panelTong = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTong.setOpaque(false);
        panelTong.add(new JLabel("Tổng tiền: "));
        lblTongTien = new JLabel("0");
        lblTongTien.setFont(lblTongTien.getFont().deriveFont(Font.BOLD, 16f));
        panelTong.add(lblTongTien);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelButtons.setOpaque(false);

        btnXoaGio = new JButton("Xóa giỏ hàng");
        btnXoaGio.addActionListener(e -> xoaGioHang());

        btnTaoHoaDon = new JButton("Tạo hóa đơn");
        btnTaoHoaDon.addActionListener(e -> taoHoaDon());

        JButton btnLoad = new JButton("Làm Mới");
        btnLoad.addActionListener(e -> reloadData());

        panelButtons.add(btnLoad);
        panelButtons.add(btnXoaGio);
        panelButtons.add(btnTaoHoaDon);

        panelBottom.add(panelTong, BorderLayout.WEST);
        panelBottom.add(panelButtons, BorderLayout.EAST);

        add(panelBottom, BorderLayout.SOUTH);
    }

    // Renderer định dạng tiền cho 2 cột Đơn giá & Thành tiền
    private void formatMoneyColumns() {
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number) {
                    setHorizontalAlignment(RIGHT);
                    setText(moneyFormat.format(((Number) value).doubleValue()));
                } else if (value != null) {
                    setText(value.toString());
                } else {
                    setText("");
                }
            }
        };

        // Cột 2: Đơn Giá
        TableColumn colDonGia = tblGioHang.getColumnModel().getColumn(2);
        colDonGia.setCellRenderer(moneyRenderer);

        // Cột 4: Thành tiền
        TableColumn colThanhTien = tblGioHang.getColumnModel().getColumn(4);
        colThanhTien.setCellRenderer(moneyRenderer);
    }

    // ================== LOAD DỮ LIỆU COMBOBOX ==================
    private void loadKhach() {
        cbKhach.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT MaKhach, TenKhach FROM KHACH");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String item = rs.getString("MaKhach") + " - " + rs.getString("TenKhach");
                cbKhach.addItem(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách khách hàng!");
        }
    }

    private void loadNhanVien() {
        cbNhanVien.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT MaNV, TenNV FROM NHANVIEN");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String item = rs.getString("MaNV") + " - " + rs.getString("TenNV");
                cbNhanVien.addItem(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách nhân viên!");
        }
    }

    private void loadMaHang() {
        cbMaHang.removeAllItems();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT MaHang FROM MENU");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cbMaHang.addItem(rs.getString("MaHang"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách mã hàng từ MENU!");
        }
    }

    public void reloadData() {
        loadKhach();
        loadNhanVien();
        loadMaHang();
    }

    // ================== LOAD THÔNG TIN HÀNG KHI CHỌN MÃ ==================
    private void loadThongTinHang() {
        String maHang = (String) cbMaHang.getSelectedItem();
        if (maHang == null) {
            return;
        }

        String sql =
                "SELECT m.TenHangBan, m.DonGia, t.SoLuongTon " +
                        "FROM MENU m " +
                        "JOIN TONKHO t ON m.MaHang = t.MaHang " +
                        "WHERE m.MaHang = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtTenHang.setText(rs.getString("TenHangBan"));
                    // DonGia: để dạng số thô, không cần format cũng được
                    txtDonGia.setText(rs.getString("DonGia"));
                    txtTonKho.setText(String.valueOf(rs.getInt("SoLuongTon")));
                } else {
                    txtTenHang.setText("");
                    txtDonGia.setText("");
                    txtTonKho.setText("");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ============ TẠO MÃ HÓA ĐƠN =============
    private String taoMaHDMoi() throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(MaHD, 3) AS UNSIGNED)) AS maxNum FROM HOADON";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int max = 0;
            if (rs.next()) {
                max = rs.getInt("maxNum");
            }

            int next = max + 1;
            return String.format("HD%03d", next);
        }
    }

    // ================== THÊM VÀO GIỎ HÀNG ==================
    private void themVaoGio() {
        String maHang = (String) cbMaHang.getSelectedItem();
        if (maHang == null || maHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chọn mã hàng trước!");
            return;
        }

        String tenHang = txtTenHang.getText().trim();
        if (tenHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không lấy được tên hàng!");
            return;
        }

        int ton;
        try {
            ton = Integer.parseInt(txtTonKho.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng tồn không hợp lệ!");
            return;
        }

        int soLuong;
        try {
            soLuong = Integer.parseInt(txtSoLuong.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Nhập số lượng mua là số nguyên!");
            return;
        }

        if (soLuong <= 0) {
            JOptionPane.showMessageDialog(this, "Số lượng mua phải > 0!");
            return;
        }

        if (soLuong > ton) {
            JOptionPane.showMessageDialog(this, "Không đủ số lượng tồn trong kho!");
            return;
        }

        double donGia;
        try {
            donGia = Double.parseDouble(txtDonGia.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Đơn giá không hợp lệ!");
            return;
        }

        double thanhTien = donGia * soLuong;

        // Thêm dòng vào giỏ hàng (giữ DonGia, ThanhTien là số, renderer sẽ format)
        modelGioHang.addRow(new Object[]{
                maHang,
                tenHang,
                donGia,
                soLuong,
                thanhTien
        });

        // Cập nhật tổng tiền
        capNhatTongTien();

        // Xóa số lượng mua cho lần nhập tiếp
        txtSoLuong.setText("");
    }

    private void capNhatTongTien() {
        double tong = 0;
        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            Object val = modelGioHang.getValueAt(i, 4);   // cột Thành tiền
            if (val instanceof Number) {
                tong += ((Number) val).doubleValue();
            } else if (val != null) {
                // nếu vì lý do nào đó là String có dấu phẩy
                String s = val.toString().replace(",", "");
                try {
                    tong += Double.parseDouble(s);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        lblTongTien.setText(moneyFormat.format(tong));  // VD: 12,600,000
    }

    private void xoaGioHang() {
        modelGioHang.setRowCount(0);
        capNhatTongTien();
    }

    // ================== TẠO HÓA ĐƠN ==================
    private void taoHoaDon() {
        if (modelGioHang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!");
            return;
        }

        if (cbKhach.getSelectedItem() == null || cbNhanVien.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Chọn khách hàng và nhân viên!");
            return;
        }

        String khachItem = cbKhach.getSelectedItem().toString();
        String nhanVienItem = cbNhanVien.getSelectedItem().toString();

        String maKhach = khachItem.split(" - ")[0].trim();
        String maNV = nhanVienItem.split(" - ")[0].trim();

        double tongTien;
        try {
            // lblTongTien đang dạng "1,260,000" -> bỏ dấu phẩy trước khi parse
            String raw = lblTongTien.getText().replace(",", "").trim();
            tongTien = Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tổng tiền không hợp lệ!");
            return;
        }

        Connection conn = null;
        PreparedStatement psInsertHD = null;
        PreparedStatement psUpdateMenu = null;
        PreparedStatement psUpdateTonKho = null;
        PreparedStatement psInsertTT = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // transaction

            // 1. Tạo mã hóa đơn mới (HD001, HD002, ...)
            String maHD = taoMaHDMoi();

            // 2. INSERT vào HOADON (có cả MaHD)
            String sqlHD =
                    "INSERT INTO HOADON (MaHD, NgayTao, TongTien, MaNV, MaKhach) " +
                            "VALUES (?, ?, ?, ?, ?)";

            psInsertHD = conn.prepareStatement(sqlHD);
            psInsertHD.setString(1, maHD);
            psInsertHD.setDate(2, Date.valueOf(java.time.LocalDate.now()));
            psInsertHD.setDouble(3, tongTien);
            psInsertHD.setString(4, maNV);
            psInsertHD.setString(5, maKhach);

            int affected = psInsertHD.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Không tạo được hóa đơn!");
            }

            // 3. Chuẩn bị UPDATE MENU & TONKHO
            String sqlUpdateMenu =
                    "UPDATE MENU SET SoLuongBan = SoLuongBan + ? WHERE MaHang = ?";
            String sqlUpdateTonKho =
                    "UPDATE TONKHO SET SoLuongTon = SoLuongTon - ? WHERE MaHang = ?";

            psUpdateMenu = conn.prepareStatement(sqlUpdateMenu);
            psUpdateTonKho = conn.prepareStatement(sqlUpdateTonKho);

            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                String maHang = modelGioHang.getValueAt(i, 0).toString();
                int soLuong = Integer.parseInt(modelGioHang.getValueAt(i, 3).toString());

                // MENU
                psUpdateMenu.setInt(1, soLuong);
                psUpdateMenu.setString(2, maHang);
                psUpdateMenu.addBatch();

                // TONKHO
                psUpdateTonKho.setInt(1, soLuong);
                psUpdateTonKho.setString(2, maHang);
                psUpdateTonKho.addBatch();
            }

            psUpdateMenu.executeBatch();
            psUpdateTonKho.executeBatch();

            // 4. Ghi bảng THANHTOAN
            String sqlTT =
                    "INSERT INTO THANHTOAN (MaKhach, MaHD, MaNV) VALUES (?, ?, ?)";
            psInsertTT = conn.prepareStatement(sqlTT);
            psInsertTT.setString(1, maKhach);
            psInsertTT.setString(2, maHD);
            psInsertTT.setString(3, maNV);
            psInsertTT.executeUpdate();

            // 5. Commit
            conn.commit();

            JOptionPane.showMessageDialog(this,
                    "Tạo hóa đơn thành công! Mã hóa đơn: " + maHD);

            xoaGioHang();

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo hóa đơn: " + e.getMessage());
        } finally {
            try {
                if (psInsertHD != null) psInsertHD.close();
                if (psUpdateMenu != null) psUpdateMenu.close();
                if (psUpdateTonKho != null) psUpdateTonKho.close();
                if (psInsertTT != null) psInsertTT.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

}
