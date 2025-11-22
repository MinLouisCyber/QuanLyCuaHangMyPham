package com.raven.form;

import com.raven.database.DBConnection;
import com.raven.model.Model_Card;
import com.raven.model.StatusType;
import com.raven.swing.ScrollBar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Form_Home extends javax.swing.JPanel {

    public Form_Home() {
        initComponents();

        // Thiết lập Scroll UI
        spTable.setVerticalScrollBar(new ScrollBar());
        spTable.getVerticalScrollBar().setBackground(Color.WHITE);
        spTable.getViewport().setBackground(Color.WHITE);
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        spTable.setCorner(JScrollPane.UPPER_RIGHT_CORNER, p);

        // Load từ DB vào Dashboard
        loadDashboardFromDB();
    }

    // ======================================================
    // ===============  HÀM LOAD DASHBOARD  =================
    // ======================================================
    public void loadDashboardFromDB() {
        double stockTotal = 0.0;
        double totalRevenue = 0.0;
        int uniqueCustomers = 0;

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Tổng giá trị tồn kho = SUM(SoLuongTon * DonGia)
            String sqlStock =
                    "SELECT SUM(t.SoLuongTon * m.DonGia) AS StockTotal " +
                    "FROM TONKHO t JOIN MENU m ON t.MaHang = m.MaHang";

            try (PreparedStatement ps = conn.prepareStatement(sqlStock);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stockTotal = rs.getDouble("StockTotal");
                }
            }

            // 2. Tổng doanh thu = SUM(TongTien)
            String sqlRevenue = "SELECT SUM(TongTien) AS TotalRevenue FROM HOADON";

            try (PreparedStatement ps = conn.prepareStatement(sqlRevenue);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalRevenue = rs.getDouble("TotalRevenue");
                }
            }

            // 3. Unique Visitors = số khách từng có hóa đơn
            String sqlUnique = "SELECT COUNT(DISTINCT MaKhach) AS UniqueVisitors FROM HOADON";

            try (PreparedStatement ps = conn.prepareStatement(sqlUnique);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    uniqueCustomers = rs.getInt("UniqueVisitors");
                }
            }

            // ========== SET DATA CHO 3 CARD ==========
            card1.setData(new Model_Card(
                    new ImageIcon(getClass().getResource("/com/raven/icon/stock.png")),
                    "Tổng giá trị kho",
                    formatMoney(stockTotal),
                    "Giá trị tồn kho hiện tại"
            ));

            card2.setData(new Model_Card(
                    new ImageIcon(getClass().getResource("/com/raven/icon/profit.png")),
                    "Tổng doanh thu",
                    formatMoney(totalRevenue),
                    "Tổng doanh thu bán hàng"
            ));

            card3.setData(new Model_Card(
                    new ImageIcon(getClass().getResource("/com/raven/icon/flag.png")),
                    "Tổng số khách hàng",
                    String.valueOf(uniqueCustomers),
                    "Số khách từng mua hàng"
            ));

            // Load bảng hóa đơn mới nhất
            loadRecentInvoices(conn);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải Dashboard!");
        }
    }

    // ======================================================
    // ===============  LOAD BẢNG HÓA ĐƠN ===================
    // ======================================================
    private void loadRecentInvoices(Connection conn) throws SQLException {
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    model.setRowCount(0);

    String sql =
            "SELECT k.TenKhach, k.SDTKhach, h.NgayTao, " +
            "       SUM(h.TongTien) AS TongTienNgay " +
            "FROM HOADON h " +
            "JOIN KHACH k ON h.MaKhach = k.MaKhach " +
            "GROUP BY k.MaKhach, k.TenKhach, k.SDTKhach, h.NgayTao " +
            "ORDER BY h.NgayTao DESC " +
            "LIMIT 20";

    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            String tenKhach = rs.getString("TenKhach");
            String sdt      = rs.getString("SDTKhach");
            Date ngay       = rs.getDate("NgayTao");
            double tongTien = rs.getDouble("TongTienNgay");
            
            table.addRow(new Object[]{
                    tenKhach,
                    sdt,
                    String.format("%,.0f VND", tongTien),
                    ngay != null ? ngay.toString() : "",
                    StatusType.APPROVED
            });
        }
    }
}



    // ======================================================
    // ===============  FORMAT TIỀN TỆ ======================
    // ======================================================
    private String formatMoney(double value) {
        return String.format("%,.0f VND", value);
    }

    // ======================================================
    // ===============   GUI AUTO-GENERATED   ===============
    // ======================================================
    @SuppressWarnings("unchecked")
    private void initComponents() {

        panel = new javax.swing.JLayeredPane();
        card1 = new com.raven.component.Card();
        card2 = new com.raven.component.Card();
        card3 = new com.raven.component.Card();
        panelBorder1 = new com.raven.swing.PanelBorder();
        jLabel1 = new javax.swing.JLabel();
        spTable = new javax.swing.JScrollPane();
        table = new com.raven.swing.Table();

        panel.setLayout(new java.awt.GridLayout(1, 0, 10, 0));

        card1.setColor1(new java.awt.Color(142, 142, 250));
        card1.setColor2(new java.awt.Color(123, 123, 245));
        panel.add(card1);

        card2.setColor1(new java.awt.Color(186, 123, 247));
        card2.setColor2(new java.awt.Color(167, 94, 236));
        panel.add(card2);

        card3.setColor1(new java.awt.Color(241, 208, 62));
        card3.setColor2(new java.awt.Color(211, 184, 61));
        panel.add(card3);

        panelBorder1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("sansserif", 1, 18));
        jLabel1.setForeground(new java.awt.Color(127, 127, 127));
        jLabel1.setText("Hóa đơn gần đây");

        spTable.setBorder(null);

        table.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {},
                new String [] {
                        "Tên Khách Hàng", "SĐT", "Tổng số tiền", "Ngày Mua", "Trạng Thái"
                }
        ) {
            boolean[] canEdit = new boolean [] {
                    false, false, false, false, false
            };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        spTable.setViewportView(table);

        javax.swing.GroupLayout panelBorder1Layout = new javax.swing.GroupLayout(panelBorder1);
        panelBorder1.setLayout(panelBorder1Layout);
        panelBorder1Layout.setHorizontalGroup(
                panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelBorder1Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel1)
                                        .addComponent(spTable, javax.swing.GroupLayout.DEFAULT_SIZE, 850, Short.MAX_VALUE))
                                .addContainerGap())
        );
        panelBorder1Layout.setVerticalGroup(
                panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelBorder1Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spTable, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(panelBorder1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, 875, Short.MAX_VALUE))
                                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(panel, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(panelBorder1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(20, 20, 20))
        );
    }

    // Variables declaration
    private com.raven.component.Card card1;
    private com.raven.component.Card card2;
    private com.raven.component.Card card3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLayeredPane panel;
    private com.raven.swing.PanelBorder panelBorder1;
    private javax.swing.JScrollPane spTable;
    private com.raven.swing.Table table;
}
