package customer.controller.cart;

import model.MonAn;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    public static class CartItem {
        private final MonAn food;
        private int quantity;
        private String note;

        public CartItem(MonAn food, int quantity, String note) {
            this.food = food;
            this.quantity = quantity;
            this.note = note;
        }

        public MonAn getFood() {
            return food;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public BigDecimal getSubtotal() {
            if (food.getDonGia() == null) return BigDecimal.ZERO;
            return food.getDonGia().multiply(BigDecimal.valueOf(quantity));
        }
    }

    private static final List<CartItem> cartItems = new ArrayList<>();
    private static final List<Runnable> listeners = new ArrayList<>();

    public static synchronized void addListener(Runnable listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static synchronized void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public static synchronized void addToCart(MonAn food, int quantity, String note) {
        for (CartItem item : cartItems) {
            if (item.getFood().getMaMon().equals(food.getMaMon())) {
                item.setQuantity(item.getQuantity() + quantity);
                if (note != null && !note.isEmpty()) {
                    String existingNote = item.getNote();
                    if (existingNote == null || existingNote.isEmpty()) {
                        item.setNote(note);
                    } else {
                        item.setNote(existingNote + "; " + note);
                    }
                }
                notifyListeners();
                return;
            }
        }
        cartItems.add(new CartItem(food, quantity, note));
        notifyListeners();
    }

    public static synchronized void updateQuantity(String maMon, int quantity) {
        if (quantity <= 0) {
            removeFromCart(maMon);
            return;
        }
        for (CartItem item : cartItems) {
            if (item.getFood().getMaMon().equals(maMon)) {
                item.setQuantity(quantity);
                notifyListeners();
                return;
            }
        }
    }

    public static synchronized void updateNote(String maMon, String note) {
        for (CartItem item : cartItems) {
            if (item.getFood().getMaMon().equals(maMon)) {
                item.setNote(note);
                notifyListeners();
                return;
            }
        }
    }

    public static synchronized void removeFromCart(String maMon) {
        cartItems.removeIf(item -> item.getFood().getMaMon().equals(maMon));
        notifyListeners();
    }

    public static synchronized void clearCart() {
        cartItems.clear();
        notifyListeners();
    }

    public static synchronized BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public static synchronized int getTotalCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
}
