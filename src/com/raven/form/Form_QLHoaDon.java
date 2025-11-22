package com.raven.form;

import com.raven.database.DBConnection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*; 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.DecimalFormat;

// iText
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.net.URL;

public class Form_QLHoaDon extends JPanel {

    private JTable tblHoaDon;
    private JTable tblChiTiet;
    private DefaultTableModel modelHoaDon;
    private DefaultTableModel modelChiTiet;
    private JTextField txtSearch;
    private JButton btnReload;
    private JButton btnSearch;
    private JButton btnExportPDF;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    public Form_QLHoaDon() {
        initComponents();
        loadHoaDon();
    }

    private void initComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));

        // ===== PANEL TOP (SEARCH + BUTTONS) =====
        JPanel panelTop = new JPanel(new BorderLayout());
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelTop.setOpaque(false);

        JPanel panelSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSearch.setOpaque(false);

        panelSearch.add(new JLabel("Tìm kiếm (Mã HD / Tên khách / Tên NV): "));
        txtSearch = new JTextField(25);
        panelSearch.add(txtSearch);

        btnSearch = new JButton("Tìm");
        btnSearch.addActionListener(e -> timHoaDon());
        panelSearch.add(btnSearch);

        panelTop.add(panelSearch, BorderLayout.WEST);

        JPanel panelTopRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTopRight.setOpaque(false);

        btnReload = new JButton("Làm Mới ");
        btnReload.addActionListener(e -> loadHoaDon());
        panelTopRight.add(btnReload);

        btnExportPDF = new JButton("Xuất hóa đơn");
        btnExportPDF.addActionListener(e -> xuatHoaDonPDF());
        panelTopRight.add(btnExportPDF);

        panelTop.add(panelTopRight, BorderLayout.EAST);

        add(panelTop, BorderLayout.NORTH);

        // ===== BẢNG HÓA ĐƠN =====
        modelHoaDon = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã HD", "Khách Hàng", "Nhân Viên", "Ngày Tạo", "Tổng Tiền"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Double.class;
                return String.class;
            }
        };

        tblHoaDon = new JTable(modelHoaDon);
        tblHoaDon.setRowHeight(24);

        tblHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblHoaDon.getSelectedRow();
                if (row >= 0) {
                    String maHD = tblHoaDon.getValueAt(row, 0).toString();
                    loadChiTietHoaDon(maHD);
                }
            }
        });

        JScrollPane spHoaDon = new JScrollPane(tblHoaDon);
        spHoaDon.setBorder(new TitledBorder("Danh sách hóa đơn"));

        // ===== BẢNG CHI TIẾT =====
        modelChiTiet = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Hàng", "Tên Hàng", "Số Lượng", "Đơn Giá", "Thành Tiền"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class;
                if (columnIndex == 3 || columnIndex == 4) return Double.class;
                return String.class;
            }
        };

        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(24);

        JScrollPane spChiTiet = new JScrollPane(tblChiTiet);
        spChiTiet.setBorder(new TitledBorder("Chi tiết hóa đơn"));

        // ===== SPLIT PANE =====
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spHoaDon, spChiTiet);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);
    }

    // =========================================================
    //                       LOAD DỮ LIỆU
    // =========================================================

    private void loadHoaDon() {
        modelHoaDon.setRowCount(0);
        modelChiTiet.setRowCount(0);

        String sql =
                "SELECT h.MaHD, k.TenKhach, nv.TenNV, h.NgayTao, h.TongTien " +
                "FROM HOADON h " +
                "JOIN KHACH k ON h.MaKhach = k.MaKhach " +
                "JOIN NHANVIEN nv ON h.MaNV = nv.MaNV " +
                "ORDER BY h.NgayTao DESC, h.MaHD DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String maHD     = rs.getString("MaHD");
                String tenKhach = rs.getString("TenKhach");
                String tenNV    = rs.getString("TenNV");
                Date   ngayTao  = rs.getDate("NgayTao");
                double tongTien = rs.getDouble("TongTien");

                modelHoaDon.addRow(new Object[]{
                        maHD,
                        tenKhach,
                        tenNV,
                        (ngayTao != null ? ngayTao.toString() : ""),
                        tongTien
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách hóa đơn!");
        }
    }

    private void timHoaDon() {
        String key = txtSearch.getText().trim();
        if (key.isEmpty()) {
            loadHoaDon();
            return;
        }

        modelHoaDon.setRowCount(0);
        modelChiTiet.setRowCount(0);

        String sql =
                "SELECT h.MaHD, k.TenKhach, nv.TenNV, h.NgayTao, h.TongTien " +
                "FROM HOADON h " +
                "JOIN KHACH k ON h.MaKhach = k.MaKhach " +
                "JOIN NHANVIEN nv ON h.MaNV = nv.MaNV " +
                "WHERE h.MaHD LIKE ? " +
                "   OR k.TenKhach LIKE ? " +
                "   OR nv.TenNV LIKE ? " +
                "ORDER BY h.NgayTao DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + key + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHD     = rs.getString("MaHD");
                    String tenKhach = rs.getString("TenKhach");
                    String tenNV    = rs.getString("TenNV");
                    Date   ngayTao  = rs.getDate("NgayTao");
                    double tongTien = rs.getDouble("TongTien");

                    modelHoaDon.addRow(new Object[]{
                            maHD,
                            tenKhach,
                            tenNV,
                            (ngayTao != null ? ngayTao.toString() : ""),
                            tongTien
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm hóa đơn!");
        }
    }

    private void loadChiTietHoaDon(String maHD) {
        modelChiTiet.setRowCount(0);

        String sql =
                "SELECT MaHang, TenHang, SoLuong, DonGia, ThanhTien " +
                "FROM CT_HOADON " +
                "WHERE MaHD = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maHang    = rs.getString("MaHang");
                    String tenHang   = rs.getString("TenHang");
                    int    soLuong   = rs.getInt("SoLuong");
                    double donGia    = rs.getDouble("DonGia");
                    double thanhTien = rs.getDouble("ThanhTien");

                    modelChiTiet.addRow(new Object[]{
                            maHang,
                            tenHang,
                            soLuong,
                            donGia,
                            thanhTien
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải chi tiết hóa đơn!");
        }
    }

    // =========================================================
    //                       XUẤT PDF (UNICODE)
    // =========================================================

    private void xuatHoaDonPDF() {
    int row = tblHoaDon.getSelectedRow();
    if (row < 0) {
        JOptionPane.showMessageDialog(this, "Chọn một hóa đơn trước khi xuất hóa đơn!");
        return;
    }

    String maHD = tblHoaDon.getValueAt(row, 0).toString();

    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(new java.io.File("HoaDon_" + maHD + ".pdf"));
    int result = chooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) {
        return;
    }
    java.io.File file = chooser.getSelectedFile();

    String tenKhach = "";
    String sdtKhach = "";
    String tenNV    = "";
    Date   ngayTao  = null;
    double tongTien = 0;

    try (Connection conn = DBConnection.getConnection()) {

        // ----- Lấy thông tin hóa đơn -----
        String sqlHD =
                "SELECT h.MaHD, h.NgayTao, h.TongTien, " +
                        "       k.TenKhach, k.SDTKhach, nv.TenNV " +
                        "FROM HOADON h " +
                        "JOIN KHACH k ON h.MaKhach = k.MaKhach " +
                        "JOIN NHANVIEN nv ON h.MaNV = nv.MaNV " +
                        "WHERE h.MaHD = ?";

        try (PreparedStatement ps = conn.prepareStatement(sqlHD)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenKhach = rs.getString("TenKhach");
                    sdtKhach = rs.getString("SDTKhach");
                    tenNV    = rs.getString("TenNV");
                    ngayTao  = rs.getDate("NgayTao");
                    tongTien = rs.getDouble("TongTien");
                }
            }
        }

        // ----- SQL chi tiết -----
        String sqlCT =
                "SELECT MaHang, TenHang, SoLuong, DonGia, ThanhTien " +
                        "FROM CT_HOADON " +
                        "WHERE MaHD = ?";

        // ----- Tạo document (khổ A5, căn lề nhỏ lại cho rộng không gian) -----
        Document document = new Document(PageSize.A5, 24, 24, 24, 24);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // ===== FONT UNICODE (Arial) =====
        BaseFont bf = BaseFont.createFont(
                "C:\\Windows\\Fonts\\arial.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
        );

        com.itextpdf.text.Font titleFont =
                new com.itextpdf.text.Font(bf, 16, com.itextpdf.text.Font.BOLD, BaseColor.RED);
        com.itextpdf.text.Font shopNameFont =
                new com.itextpdf.text.Font(bf, 13, com.itextpdf.text.Font.BOLD, BaseColor.RED);
        com.itextpdf.text.Font smallRedFont =
                new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.NORMAL, BaseColor.RED);
        com.itextpdf.text.Font normalFont =
                new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
        com.itextpdf.text.Font headerFont =
                new com.itextpdf.text.Font(bf, 9, com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        com.itextpdf.text.Font boldTotalFont =
                new com.itextpdf.text.Font(bf, 11, com.itextpdf.text.Font.BOLD, BaseColor.RED);

        // ===== HEADER: logo + thông tin cửa hàng =====
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 3f});
        headerTable.setSpacingAfter(5f);

        // Cột logo
        PdfPCell logoCell;
        try {
            URL url = getClass().getResource("/com/raven/icon/logo-my-pham.png");
            com.itextpdf.text.Image logo =
                    com.itextpdf.text.Image.getInstance(url);
            logo.scaleToFit(60, 60);
            logoCell = new PdfPCell(logo, true);
        } catch (Exception exLogo) {
            logoCell = new PdfPCell(new Phrase(""));
        }
        logoCell.setBorder(PdfPCell.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingRight(15f); 
        headerTable.addCell(logoCell);

        // Cột thông tin cửa hàng
        PdfPCell infoShopCell = new PdfPCell();
        infoShopCell.setBorder(PdfPCell.NO_BORDER);

        Paragraph shopName = new Paragraph("CỬA HÀNG MỸ PHẨM MQHĐH BEAUTY", shopNameFont);
        shopName.setAlignment(Element.ALIGN_LEFT);
        shopName.setSpacingAfter(2f);

        Paragraph shopLine1 = new Paragraph("Chuyên mỹ phẩm chính hãng", smallRedFont);
        Paragraph shopLine2 = new Paragraph("SĐT: 0939 36 36 36  -  Địa chỉ: 250/60 Phan Trọng Tuệ, Hà Nội", smallRedFont);

        infoShopCell.addElement(shopName);
        infoShopCell.addElement(shopLine1);
        infoShopCell.addElement(shopLine2);

        headerTable.addCell(infoShopCell);

        document.add(headerTable);

        // ===== TIÊU ĐỀ HÓA ĐƠN =====
        Paragraph title = new Paragraph("HÓA ĐƠN THANH TOÁN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(2f);
        title.setSpacingAfter(8f);
        document.add(title);

        // ===== THÔNG TIN KHÁCH HÀNG & HÓA ĐƠN =====
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1.4f, 1.6f});
        infoTable.setSpacingAfter(6f);

        PdfPCell cellLeft = new PdfPCell();
        cellLeft.setBorder(PdfPCell.NO_BORDER);
        cellLeft.setPadding(0f);
        cellLeft.addElement(new Paragraph("Khách hàng : " + tenKhach, normalFont));
        cellLeft.addElement(new Paragraph("Số điện thoại: " + sdtKhach, normalFont));

        PdfPCell cellRight = new PdfPCell();
        cellRight.setBorder(PdfPCell.NO_BORDER);
        cellRight.setPadding(0f);
        cellRight.addElement(new Paragraph("Mã hóa đơn: " + maHD, normalFont));
        cellRight.addElement(new Paragraph(
                "Ngày lập   : " + (ngayTao != null ? ngayTao.toString() : ""),
                normalFont
        ));

        infoTable.addCell(cellLeft);
        infoTable.addCell(cellRight);

        document.add(infoTable);

        // ===== BẢNG CHI TIẾT (STT, Sản phẩm, SL, Đơn giá, Thành tiền) =====
        PdfPTable tableCT = new PdfPTable(5);
        tableCT.setWidthPercentage(100);
        tableCT.setWidths(new float[]{1f, 3.5f, 0.8f, 1.8f, 2f});
        tableCT.setSpacingBefore(2f);
        tableCT.setSpacingAfter(6f);

        // Header hàng đầu – nền đỏ, chữ trắng
        PdfPCell headerCell;

        String[] headers = {"STT", "SẢN PHẨM", "SL", "ĐƠN GIÁ", "THÀNH TIỀN"};
        for (String h : headers) {
            headerCell = new PdfPCell(new Phrase(h, headerFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setBackgroundColor(BaseColor.RED);
            headerCell.setPaddingTop(3f);
            headerCell.setPaddingBottom(3f);
            tableCT.addCell(headerCell);
        }

        // Dòng dữ liệu
        int stt = 1;
        try (PreparedStatement ps = conn.prepareStatement(sqlCT)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenHang  = rs.getString("TenHang");
                    int soLuong     = rs.getInt("SoLuong");
                    double donGia   = rs.getDouble("DonGia");
                    double thanhTien= rs.getDouble("ThanhTien");

                    PdfPCell cStt = new PdfPCell(new Phrase(String.valueOf(stt++), normalFont));
                    cStt.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cStt.setPadding(2f);
                    tableCT.addCell(cStt);

                    PdfPCell cTen = new PdfPCell(new Phrase(tenHang, normalFont));
                    cTen.setPadding(2f);
                    tableCT.addCell(cTen);

                    PdfPCell cSL = new PdfPCell(new Phrase(String.valueOf(soLuong), normalFont));
                    cSL.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cSL.setPadding(2f);
                    tableCT.addCell(cSL);

                    PdfPCell cDG = new PdfPCell(new Phrase(moneyFormat.format(donGia), normalFont));
                    cDG.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cDG.setPadding(2f);
                    tableCT.addCell(cDG);

                    PdfPCell cTT = new PdfPCell(new Phrase(moneyFormat.format(thanhTien), normalFont));
                    cTT.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cTT.setPadding(2f);
                    tableCT.addCell(cTT);
                }
            }
        }

        document.add(tableCT);

        // ===== TỔNG CỘNG =====
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{3f, 2f});
        totalTable.setSpacingBefore(4f);

        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setBorder(PdfPCell.NO_BORDER);
        totalTable.addCell(emptyCell);

        PdfPCell totalCell = new PdfPCell();
        totalCell.setBorder(PdfPCell.NO_BORDER);

        Paragraph total = new Paragraph(
                "TỔNG CỘNG: " + moneyFormat.format(tongTien) + " VND",
                boldTotalFont
        );
        total.setAlignment(Element.ALIGN_RIGHT);
        totalCell.addElement(total);
        totalTable.addCell(totalCell);

        document.add(totalTable);

        // ===== CHỮ KÝ (gọn cho khổ A5) =====
        document.add(new Paragraph("\n"));

        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        signTable.setWidths(new float[]{1f, 1f});

        PdfPCell c1 = new PdfPCell();
        c1.setBorder(PdfPCell.NO_BORDER);
        Paragraph p1 = new Paragraph("KHÁCH HÀNG\n\n\n( Ký và ghi rõ họ tên )", normalFont);
        p1.setAlignment(Element.ALIGN_CENTER);
        c1.addElement(p1);

        PdfPCell c2 = new PdfPCell();
        c2.setBorder(PdfPCell.NO_BORDER);
        Paragraph p2 = new Paragraph("NHÂN VIÊN BÁN HÀNG\n\n\n( " + tenNV + " )", normalFont);
        p2.setAlignment(Element.ALIGN_CENTER);
        c2.addElement(p2);

        signTable.addCell(c1);
        signTable.addCell(c2);

        document.add(signTable);

        document.close();

        JOptionPane.showMessageDialog(this,
                "Xuất hóa đơn thành công");

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + ex.getMessage());
    }
}


    // Hàm thêm ô header, dùng font Unicode truyền vào
    private void addHeaderCell(PdfPTable table, String text, com.itextpdf.text.Font headerFont) {
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }
}
