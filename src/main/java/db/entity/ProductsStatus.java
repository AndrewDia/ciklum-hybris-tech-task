package db.entity;

public enum ProductsStatus {
    OUT_OF_STOCK("out_of_stock"),
    IN_STOCK("in_stock"),
    RUNNING_LOW("running_low");

    private final String productStatus;

    ProductsStatus(String status) {
        this.productStatus = status;
    }

    public String getProductStatus() {
        return this.productStatus;
    }

    public static ProductsStatus fromString(String status) {
        for (ProductsStatus productsStatus : ProductsStatus.values())
            if (productsStatus.productStatus.equalsIgnoreCase(status))
                return productsStatus;
        return null;
    }
}
