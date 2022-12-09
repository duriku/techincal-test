package com.jurajkucera.technicaltest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.util.stream.Collectors.toCollection;

/**
 * A limit order book stores customer orders on a price time priority basis. The highest bid and lowest oer
 * are considered "best" with all other orders stacked in price levels behind. In this test, the best order is considered to be at level 1.
 */
public class OrderBook {
    private Map<Double, Set<Order>> bidOrderMap;
    private Map<Double, Set<Order>> offerOrderMap;
    private Map<Long, Order> orderIdOrderMap;
    private ConcurrentMap<Double, Object> locks = new ConcurrentHashMap<>();

    public OrderBook() {
        this.bidOrderMap = new TreeMap<>(Collections.reverseOrder());
        this.offerOrderMap = new TreeMap<>();
        this.orderIdOrderMap = new HashMap<>();
    }

    public void add(Order order) {
        this.addToOrdersSynchronizedOnPrice(order, getOrdersBySide(order.getSide()));
    }

    public int getOrderCountBySideAndLevel(char side, int level) {
        return getOrdersForSideAndLevel(side, level).size();
    }

    public double getPriceForSideAndLevel(char side, int level) {
        return getOrdersForSideAndLevel(side, level).stream().findAny().get().getPrice();
    }

    public Set<Order> getAllOrderBySide(char side) {
        return getOrdersBySide(side).values().stream()
                .flatMap(Collection::stream)
                .collect(toCollection(LinkedHashSet::new));
    }

    public void remove(long orderId) {
        var order = orderIdOrderMap.get(orderId);
        synchronized (getCacheSyncObject(order)) {
            var ordersMap = getOrdersBySide(order.getSide());
            removeInternal(order, ordersMap);
        }
    }

    public void changeSize(long orderId, long size) {
        var order = orderIdOrderMap.get(orderId);
        synchronized (getCacheSyncObject(order)) {
            var ordersMap = getOrdersBySide(order.getSide());
            removeInternal(order, ordersMap);

            var newOrder = new Order(orderId, order.getPrice(), order.getSide(), size);
            addToOrder(newOrder, ordersMap);
        }
    }

    private void addToOrdersSynchronizedOnPrice(Order order, Map<Double, Set<Order>> orderMap) {
        synchronized (getCacheSyncObject(order)) {
            addToOrder(order, orderMap);
        }
    }

    private void addToOrder(Order order, Map<Double, Set<Order>> orderMap) {
        Set<Order> ordersAtLevel = orderMap.containsKey(order.getPrice())
                ? orderMap.get(order.getPrice())
                : new HashSet<>();

        ordersAtLevel.add(order);
        orderMap.put(order.getPrice(), ordersAtLevel);
        this.orderIdOrderMap.put(order.getId(), order);
    }

    private void removeInternal(Order order, Map<Double, Set<Order>> ordersMap) {
        var orders = ordersMap.get(order.getPrice());
        orders.remove(order);
        if (orders.isEmpty()) {
            ordersMap.remove(order.getPrice());
        }
        orderIdOrderMap.remove(order.getId());
    }

    private Map<Double, Set<Order>> getOrdersBySide(char side) {
        if (side == 'B') {
            return bidOrderMap;
        }

        if (side == 'O') {
            return offerOrderMap;
        }
        throw new RuntimeException(format("Invalid side %s", side));
    }

    private Object getCacheSyncObject(final Order order) {
        var lockValue = order.getSide() == 'B' ? order.getPrice() * -1 : order.getPrice();
        locks.putIfAbsent(lockValue, lockValue);
        return locks.get(lockValue);
    }

    private Set<Order> getOrdersForSideAndLevel(char side, int level) {
        var iter = getOrdersBySide(side).entrySet().iterator();
        var levelIter = 1;
        while (iter.hasNext() && levelIter++ < level) {
            iter.next();
        }

        if (level >= levelIter) {
            return Collections.emptySet();
        }

        return iter.next().getValue();
    }
}
