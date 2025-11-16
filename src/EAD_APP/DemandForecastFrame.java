
package EAD_APP;

import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import java.util.List;
import java.sql.SQLException;
import javax.swing.JOptionPane;



public class DemandForecastFrame extends javax.swing.JFrame {

    public DemandForecastFrame() {
        initComponents();
         setTitle("Intelligent Demand Forecasting");
    setLocationRelativeTo(null);
    
    // Auto-load data when frame opens
    SwingUtilities.invokeLater(() -> {
        refreshBtnActionPerformed(null);
    });
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        refreshBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        forecastTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Intelligent Demand Forecasting\n");

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("Intelligent Demand Forecasting");
        jPanel1.add(jLabel1, java.awt.BorderLayout.PAGE_START);

        refreshBtn.setBackground(new java.awt.Color(102, 102, 255));
        refreshBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        refreshBtn.setForeground(new java.awt.Color(255, 255, 255));
        refreshBtn.setText("Refresh Forecast");
        refreshBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBtnActionPerformed(evt);
            }
        });
        jPanel1.add(refreshBtn, java.awt.BorderLayout.PAGE_END);

        statusLabel.setText("Click 'Refresh Forecast' to generate recommendations");

        forecastTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Priority", "ProductID", "ProductName", "Current Stock", "Avg Daily Demand", "Days to Stockout", "Suggested Qty"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(forecastTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(statusLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 833, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, java.awt.BorderLayout.LINE_START);

        jButton1.setBackground(new java.awt.Color(102, 102, 255));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("HOME");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, java.awt.BorderLayout.LINE_END);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
        refreshBtn.setEnabled(false);
    refreshBtn.setText("⏳ Generating...");
    statusLabel.setText("Generating demand forecasts...");
    
    // Run in background to avoid freezing GUI
    new Thread(() -> {
        try {
            SwingUtilities.invokeLater(() -> {
                // Clear table - we'll do this programmatically
                DefaultTableModel model = (DefaultTableModel) forecastTable.getModel();
                model.setRowCount(0);
            });
            
            DemandForecaster forecaster = new DemandForecaster();
            forecaster.calculateReorderSuggestions();
            List<ReorderRecommendation> suggestions = forecaster.getReorderSuggestions();
            
            SwingUtilities.invokeLater(() -> {
                // Add data to table
                DefaultTableModel model = (DefaultTableModel) forecastTable.getModel();
                for (int i = 0; i < suggestions.size(); i++) {
                    ReorderRecommendation rec = suggestions.get(i);
                    model.addRow(new Object[]{
                        i + 1, // Priority rank
                        rec.getProductId(),
                        rec.getProductName(),
                        (int) rec.getCurrentStock() + " units",
                        String.format("%.2f units/day", rec.getAvgDailyDemand()),
                        String.format("%.1f days", rec.getDaysToStockout()),
                        rec.getSuggestedQuantity() + " units"
                    });
                }
                
                statusLabel.setText("✓ Generated " + suggestions.size() + " reorder suggestions");
                refreshBtn.setText("🔄 Refresh Forecast");
                refreshBtn.setEnabled(true);
                
                if (suggestions.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "All products have sufficient stock levels!", 
                        "No Reorders Needed", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            });
            
        } catch (SQLException ex) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("❌ Error generating forecasts");
                refreshBtn.setText("🔄 Refresh Forecast");
                refreshBtn.setEnabled(true);
                JOptionPane.showMessageDialog(this, 
                    "Error loading forecast data: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            });
        }
    }).start();
    }//GEN-LAST:event_refreshBtnActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
              Home newPage = new Home();
       newPage.setVisible(true);
       this.dispose(); // go tohhome page
    }//GEN-LAST:event_jButton1ActionPerformed


    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DemandForecastFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DemandForecastFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DemandForecastFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DemandForecastFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new DemandForecastFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable forecastTable;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
}
