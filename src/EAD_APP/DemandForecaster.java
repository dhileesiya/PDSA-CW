package EAD_APP;

import DB.DB;
import java.util.*;
import java.sql.*;

public class DemandForecaster {
    private HashMap<Integer, ProductSalesHistory> salesData;
    private PriorityQueue<ReorderRecommendation> reorderQueue;
    
    public DemandForecaster() {
        this.salesData = new HashMap<>();
        this.reorderQueue = new PriorityQueue<>();
    }
    
    public void loadSalesData() throws SQLException {
        salesData.clear();
        System.out.println("Loading sales data from database...");
        
        String query = "SELECT product_id, sale_date, quantity FROM sales";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                int pid = rs.getInt("product_id");
                Timestamp timestamp = rs.getTimestamp("sale_date");
                java.sql.Date saleDate = null;
                if (timestamp != null) {
                    saleDate = new java.sql.Date(timestamp.getTime());
                } else {
                    saleDate = new java.sql.Date(System.currentTimeMillis());
                }
                int quantity = rs.getInt("quantity");
                
                if (!salesData.containsKey(pid)) {
                    salesData.put(pid, new ProductSalesHistory());
                }
                salesData.get(pid).addSale(saleDate, quantity);
                count++;
            }
            System.out.println("✓ Loaded " + count + " sales records for " + salesData.size() + " products");
        }
    }
    
    private int getCurrentStock(int productId) throws SQLException {
        String query = "SELECT stock FROM products WHERE product_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("stock") : 0;
        }
    }
    
    private String getProductName(int productId) throws SQLException {
        String query = "SELECT product_name FROM products WHERE product_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("product_name") : "Unknown Product";
        }
    }
    
    private double[] getProductCosts(int productId) throws SQLException {
        String query = "SELECT ordering_cost, holding_cost FROM products WHERE product_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double orderCost = rs.getDouble("ordering_cost");
                double holdCost = rs.getDouble("holding_cost");
                return new double[]{
                    orderCost > 0 ? orderCost : 50.0,
                    holdCost > 0 ? holdCost : 10.0
                };
            }
            return new double[]{50.0, 10.0};
        }
    }
    
    private double computeEOQ(double demand, double orderCost, double holdCost) {
        if (holdCost <= 0) holdCost = 0.1;
        return Math.sqrt((2 * demand * orderCost) / holdCost);
    }
    
    public void calculateReorderSuggestions() throws SQLException {
        reorderQueue.clear();
        loadSalesData();
        
        System.out.println("Calculating reorder suggestions...");
        
        // First, let's get all products and their current stock
        String query = "SELECT p.product_id, p.product_name, p.stock, " +
                      "COALESCE(SUM(s.quantity), 0) as total_sales " +
                      "FROM products p " +
                      "LEFT JOIN sales s ON p.product_id = s.product_id " +
                      "GROUP BY p.product_id, p.product_name, p.stock";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            
            int processed = 0;
            int suggested = 0;
            
            while (rs.next()) {
                int pid = rs.getInt("product_id");
                String productName = rs.getString("product_name");
                int currentStock = rs.getInt("stock");
                int totalSales = rs.getInt("total_sales");
                
                processed++;
                
                // Calculate average daily demand
                double avgDailyDemand;
                ProductSalesHistory history = salesData.get(pid);
                
                if (history != null && history.getSalesCount() > 0) {
                    // Since all sales have same timestamp, use total sales with assumption of 30-day period
                    avgDailyDemand = (double) totalSales / 30.0;
                    System.out.println("Product: " + productName + " (ID: " + pid + ")");
                    System.out.println("  - Total Sales: " + totalSales);
                    System.out.println("  - Current Stock: " + currentStock);
                    System.out.println("  - Avg Daily Demand: " + String.format("%.2f", avgDailyDemand));
                } else {
                    // No sales history - use conservative default
                    avgDailyDemand = 0.5;
                    System.out.println("Product: " + productName + " (ID: " + pid + ") - No sales history");
                }
                
                // Avoid division by zero
                if (avgDailyDemand <= 0) avgDailyDemand = 0.1;
                
                double daysToStockout = currentStock / avgDailyDemand;
                System.out.println("  - Days to Stockout: " + String.format("%.1f", daysToStockout));
                
                // Suggest reorder if: low stock OR high sales velocity with low remaining stock
                boolean needsReorder = (daysToStockout < 45) || (currentStock < 10 && totalSales > 0);
                
                if (needsReorder) {
                    double[] costs = getProductCosts(pid);
                    double monthlyDemand = avgDailyDemand * 30;
                    double eoq = computeEOQ(monthlyDemand, costs[0], costs[1]);
                    
                    // Ensure minimum order quantity
                    int suggestedQty = Math.max((int) Math.ceil(eoq), 10);
                    
                    // Calculate priority (lower daysToStockout = higher priority)
                    double priority = 100.0 / (daysToStockout + 1.0);
                    
                    reorderQueue.offer(new ReorderRecommendation(
                        pid, productName, suggestedQty, priority, 
                        daysToStockout, currentStock, avgDailyDemand
                    ));
                    suggested++;
                    System.out.println("  ✅ SUGGEST REORDER: " + suggestedQty + " units");
                } else {
                    System.out.println("  ✓ Stock sufficient");
                }
                System.out.println();
            }
            System.out.println("✓ Processed " + processed + " products, generated " + suggested + " reorder suggestions");
        }
    }
    
    public List<ReorderRecommendation> getReorderSuggestions() {
        List<ReorderRecommendation> suggestions = new ArrayList<>();
        PriorityQueue<ReorderRecommendation> tempQueue = new PriorityQueue<>(reorderQueue);
        
        while (!tempQueue.isEmpty()) {
            suggestions.add(tempQueue.poll());
        }
        return suggestions;
    }
}