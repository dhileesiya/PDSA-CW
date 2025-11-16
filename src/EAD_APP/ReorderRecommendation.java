package EAD_APP;

public class ReorderRecommendation implements Comparable<ReorderRecommendation> {
    private int productId;
    private String productName;
    private int suggestedQuantity;
    private double priorityScore;
    private double daysToStockout;
    private double currentStock;
    private double avgDailyDemand;
    
    public ReorderRecommendation(int productId, String productName, int suggestedQuantity, 
                               double priorityScore, double daysToStockout, double currentStock, double avgDailyDemand) {
        this.productId = productId;
        this.productName = productName;
        this.suggestedQuantity = suggestedQuantity;
        this.priorityScore = priorityScore;
        this.daysToStockout = daysToStockout;
        this.currentStock = currentStock;
        this.avgDailyDemand = avgDailyDemand;
    }
    
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getSuggestedQuantity() { return suggestedQuantity; }
    public double getPriorityScore() { return priorityScore; }
    public double getDaysToStockout() { return daysToStockout; }
    public double getCurrentStock() { return currentStock; }
    public double getAvgDailyDemand() { return avgDailyDemand; }
    
    @Override
    public int compareTo(ReorderRecommendation other) {
        return Double.compare(other.priorityScore, this.priorityScore);
    }
    
    @Override
    public String toString() {
        return String.format("Product: %s (ID: %d), Current Stock: %.0f, Avg Daily Demand: %.2f, Days to Stockout: %.1f, Suggested Qty: %d", 
                           productName, productId, currentStock, avgDailyDemand, daysToStockout, suggestedQuantity);
    }
}