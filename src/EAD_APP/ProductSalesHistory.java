package EAD_APP;

import java.util.*;
import java.sql.Date;

public class ProductSalesHistory {
    private List<Date> saleDates;
    private List<Integer> quantities;
    
    public ProductSalesHistory() {
        this.saleDates = new ArrayList<>();
        this.quantities = new ArrayList<>();
    }
    
    public void addSale(Date saleDate, int quantity) {
        if (saleDate != null) {
            saleDates.add(saleDate);
            quantities.add(quantity);
        }
    }
    
    public int getTotalSales() {
        return quantities.stream().mapToInt(Integer::intValue).sum();
    }
    
    public int getSalesCount() {
        return saleDates.size();
    }
}