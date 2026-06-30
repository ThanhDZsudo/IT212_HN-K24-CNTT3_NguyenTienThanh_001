package refactoring;

import java.util.List;

// --- Models ---
class User {
    private int status;
    private String email;

    public User(int status, String email) {
        this.status = status;
        this.email = email;
    }

    public int getStatus() { return status; }
    public String getEmail() { return email; }
}

class Item {
    private double price;
    private int quantity;

    public Item(double price, int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}

class Cart {
    private List<Item> items;
    public Cart(List<Item> items) { this.items = items; }
    public List<Item> getItems() { return items; }
}

class Order {
    private User user;
    private double total;
    private String status;

    public Order(User user, double total, String status) {
        this.user = user;
        this.total = total;
        this.status = status;
    }

    public User getUser() { return user; }
    public double getTotal() { return total; }
    public String getStatus() { return status; }
}

// --- Interfaces ---
interface VoucherStrategy {
    boolean isApplicable(String voucherCode);
    double apply(double total, String voucherCode);
}

interface PaymentProcessor {
    boolean supports(String paymentMethod);
    void process(double amount);
}

interface NotificationService {
    void sendNotification(User user, String message);
}

// --- Implementations ---
class VipVoucherStrategy implements VoucherStrategy {
    @Override
    public boolean isApplicable(String voucherCode) {
        return voucherCode != null && voucherCode.startsWith("VIP");
    }
    @Override
    public double apply(double total, String voucherCode) {
        return total * 0.8;
    }
}

class FreeshipVoucherStrategy implements VoucherStrategy {
    @Override
    public boolean isApplicable(String voucherCode) {
        return voucherCode != null && voucherCode.startsWith("FREESHIP");
    }
    @Override
    public double apply(double total, String voucherCode) {
        return total - 30000;
    }
}

class MomoPaymentProcessor implements PaymentProcessor {
    @Override
    public boolean supports(String paymentMethod) {
        return "MOMO".equalsIgnoreCase(paymentMethod);
    }
    @Override
    public void process(double amount) {
        System.out.println("Connecting to Momo API...");
    }
}

class VnPayPaymentProcessor implements PaymentProcessor {
    @Override
    public boolean supports(String paymentMethod) {
        return "VNPAY".equalsIgnoreCase(paymentMethod);
    }
    @Override
    public void process(double amount) {
        System.out.println("Connecting to VNPay API...");
    }
}

class EmailNotificationService implements NotificationService {
    @Override
    public void sendNotification(User user, String message) {
        System.out.println("Sending email to " + user.getEmail() + " about " + message);
    }
}

class SmsNotificationService implements NotificationService {
    @Override
    public void sendNotification(User user, String message) {
        System.out.println("Sending SMS to " + user.getEmail() + " (used as phone identifier) about " + message);
    }
}

// --- Core OrderService ---
public class OrderService {
    private final List<VoucherStrategy> voucherStrategies;
    private final List<PaymentProcessor> paymentProcessors;
    private final NotificationService notificationService;

    public OrderService(List<VoucherStrategy> voucherStrategies,
                        List<PaymentProcessor> paymentProcessors,
                        NotificationService notificationService) {
        this.voucherStrategies = voucherStrategies;
        this.paymentProcessors = paymentProcessors;
        this.notificationService = notificationService;
    }

    public Order checkout(Cart cart, User user, String paymentMethod, String voucherCode) {
        // 1. Kiểm tra trạng thái User
        if (user.getStatus() != 1) {
            throw new RuntimeException("User locked");
        }

        // 2. Tính toán tiền hàng gốc
        double total = 0;
        for (Item i : cart.getItems()) {
            total += i.getPrice() * i.getQuantity();
        }

        // 3. Áp dụng Voucher (nếu có) thông qua VoucherStrategy
        if (voucherCode != null && voucherStrategies != null) {
            for (VoucherStrategy strategy : voucherStrategies) {
                if (strategy.isApplicable(voucherCode)) {
                    total = strategy.apply(total, voucherCode);
                    break;
                }
            }
        }

        // 4. Thực hiện thanh toán qua PaymentProcessor tương ứng
        boolean paymentProcessed = false;
        if (paymentProcessors != null) {
            for (PaymentProcessor processor : paymentProcessors) {
                if (processor.supports(paymentMethod)) {
                    processor.process(total);
                    paymentProcessed = true;
                    break;
                }
            }
        }

        if (!paymentProcessed) {
            throw new RuntimeException("Payment not supported");
        }

        // 5. Gửi thông báo thông qua NotificationService
        if (notificationService != null) {
            notificationService.sendNotification(user, "order details...");
        }

        return new Order(user, total, "SUCCESS");
    }
}
