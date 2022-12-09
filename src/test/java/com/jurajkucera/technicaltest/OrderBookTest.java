package com.jurajkucera.technicaltest;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderBookTest {

    private OrderBook orderBook = new OrderBook();

    @Test
    public void testAddFirstBidOrderWhenOrdersAreEmpty() {
        // GIVEN
        assertThat(orderBook.getAllOrderBySide('B')).isEmpty();
        assertThat(orderBook.getAllOrderBySide('O')).isEmpty();

        // WHEN
        orderBook.add(new Order(1l, 3.2, 'B', 2));

        // THEN
        assertThat(orderBook.getAllOrderBySide('B')).singleElement().satisfies(order -> {
            assertThat(order.getId()).isEqualTo(1l);
            assertThat(order.getPrice()).isEqualTo(3.2);
            assertThat(order.getSide()).isEqualTo('B');
            assertThat(order.getSize()).isEqualTo(2);
        });
        assertThat(orderBook.getPriceForSideAndLevel('B', 1)).isEqualTo(3.2);
        assertThat(orderBook.getAllOrderBySide('O')).isEmpty();
    }

    @Test
    public void testAddBidOrderWithNewValue() {
        // GIVEN
        orderBook.add(new Order(1l, 3.2, 'B', 2));
        assertThat(orderBook.getAllOrderBySide('B')).hasSize(1);

        // WHEN
        orderBook.add(new Order(2l, 4.2, 'B', 5));

        // THEN
        assertThat(orderBook.getAllOrderBySide('B')).hasSize(2).satisfiesExactly(order -> {
            assertThat(order.getId()).isEqualTo(2l);
            assertThat(order.getPrice()).isEqualTo(4.2);
            assertThat(order.getSide()).isEqualTo('B');
            assertThat(order.getSize()).isEqualTo(5);
        }, order -> {
            assertThat(order.getId()).isEqualTo(1l);
            assertThat(order.getPrice()).isEqualTo(3.2);
            assertThat(order.getSide()).isEqualTo('B');
            assertThat(order.getSize()).isEqualTo(2);
        });
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 1)).isEqualTo(1);
        assertThat(orderBook.getPriceForSideAndLevel('B', 1)).isEqualTo(4.2);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 2)).isEqualTo(1);
        assertThat(orderBook.getPriceForSideAndLevel('B', 2)).isEqualTo(3.2);
    }

    @Test
    public void testAddOfferOrderWithNewValue() {
        // GIVEN
        orderBook.add(new Order(1l, 3.2, 'O', 2));
        assertThat(orderBook.getAllOrderBySide('O')).hasSize(1);

        // WHEN
        orderBook.add(new Order(2l, 4.2, 'O', 5));

        // THEN
        assertThat(orderBook.getAllOrderBySide('O')).hasSize(2).satisfiesExactly(order -> {
            assertThat(order.getId()).isEqualTo(1l);
            assertThat(order.getPrice()).isEqualTo(3.2);
            assertThat(order.getSide()).isEqualTo('O');
            assertThat(order.getSize()).isEqualTo(2);
        }, order -> {
            assertThat(order.getId()).isEqualTo(2l);
            assertThat(order.getPrice()).isEqualTo(4.2);
            assertThat(order.getSide()).isEqualTo('O');
            assertThat(order.getSize()).isEqualTo(5);
        });
        assertThat(orderBook.getOrderCountBySideAndLevel('O', 1)).isEqualTo(1);
        assertThat(orderBook.getPriceForSideAndLevel('O', 1)).isEqualTo(3.2);
        assertThat(orderBook.getOrderCountBySideAndLevel('O', 2)).isEqualTo(1);
        assertThat(orderBook.getPriceForSideAndLevel('O', 2)).isEqualTo(4.2);
    }

    @Test
    public void testAddOfferOrderWhenBidHasTheSameValue() {
        // GIVEN
        orderBook.add(new Order(1l, 3.2, 'B', 2));
        assertThat(orderBook.getAllOrderBySide('B')).hasSize(1);

        // WHEN
        orderBook.add(new Order(2l, 3.2, 'O', 5));

        // THEN
        assertThat(orderBook.getAllOrderBySide('B')).singleElement()
                .satisfies(order -> assertThat(order.getSize()).isEqualTo(2));
        assertThat(orderBook.getAllOrderBySide('O')).singleElement()
                .satisfies(order -> assertThat(order.getSize()).isEqualTo(5));
    }

    @Test
    public void testAddBidOrderWithExistingValue() {
        // GIVEN
        orderBook.add(new Order(1l, 3.2, 'B', 2));
        orderBook.add(new Order(2l, 4.2, 'B', 5));
        assertThat(orderBook.getAllOrderBySide('B')).hasSize(2);

        // WHEN
        orderBook.add(new Order(3l, 4.2, 'B', 3));

        // THEN
        assertThat(orderBook.getAllOrderBySide('B')).hasSize(3)
                .satisfiesExactly(order -> {
                    assertThat(order.getId()).isEqualTo(2l);
                    assertThat(order.getPrice()).isEqualTo(4.2);
                    assertThat(order.getSide()).isEqualTo('B');
                    assertThat(order.getSize()).isEqualTo(5);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(3l);
                    assertThat(order.getPrice()).isEqualTo(4.2);
                    assertThat(order.getSide()).isEqualTo('B');
                    assertThat(order.getSize()).isEqualTo(3);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(1l);
                    assertThat(order.getPrice()).isEqualTo(3.2);
                    assertThat(order.getSide()).isEqualTo('B');
                    assertThat(order.getSize()).isEqualTo(2);
                });
        assertThat(orderBook.getPriceForSideAndLevel('B', 1)).isEqualTo(4.2);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 1)).isEqualTo(2);
        assertThat(orderBook.getPriceForSideAndLevel('B', 2)).isEqualTo(3.2);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 2)).isEqualTo(1);
    }

    @Test
    public void testAddBidOrdersAndRemoveThem() {
        // GIVEN
        orderBook.add(new Order(1l, 3.2, 'B', 1));
        orderBook.add(new Order(2l, 4.2, 'B', 3));
        orderBook.add(new Order(3l, 3.2, 'B', 2));
        orderBook.add(new Order(4l, 5.2, 'B', 9));

        assertThat(orderBook.getOrderCountBySideAndLevel('B', 1)).isEqualTo(1);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 2)).isEqualTo(1);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 3)).isEqualTo(2);


        // WHEN
        orderBook.remove(2);

        // THEN
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 1)).isEqualTo(1);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 2)).isEqualTo(2);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 3)).isEqualTo(0);

        // WHEN
        orderBook.remove(4);

        // THEN
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 1)).isEqualTo(2);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 2)).isEqualTo(0);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 3)).isEqualTo(0);

        // WHEN
        orderBook.remove(1);

        // THEN
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 1)).isEqualTo(1);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 2)).isEqualTo(0);
        assertThat(orderBook.getOrderCountBySideAndLevel('B', 3)).isEqualTo(0);
    }


    @Test
    public void testAddMultipleOrdersAndVerifyOrder() {
        // WHEN
        orderBook.add(new Order(1l, 3.2, 'B', 1));
        orderBook.add(new Order(2l, 4.2, 'B', 3));
        orderBook.add(new Order(3l, 1.2, 'B', 5));
        orderBook.add(new Order(4l, 2.2, 'B', 3));
        orderBook.add(new Order(5l, 3.2, 'B', 1));

        orderBook.add(new Order(6l, 3.2, 'O', 1));
        orderBook.add(new Order(7l, 4.2, 'O', 3));
        orderBook.add(new Order(8l, 1.2, 'O', 2));
        orderBook.add(new Order(9l, 2.2, 'O', 10));
        orderBook.add(new Order(10l, 3.2, 'O', 2));

        // GIVEN
        assertThat(orderBook.getAllOrderBySide('B'))
                .satisfiesExactly(order -> {
                    assertThat(order.getId()).isEqualTo(2L);
                    assertThat(order.getPrice()).isEqualTo(4.2);
                    assertThat(order.getSize()).isEqualTo(3);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(1L);
                    assertThat(order.getPrice()).isEqualTo(3.2);
                    assertThat(order.getSize()).isEqualTo(1);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(5L);
                    assertThat(order.getPrice()).isEqualTo(3.2);
                    assertThat(order.getSize()).isEqualTo(1);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(4L);
                    assertThat(order.getPrice()).isEqualTo(2.2);
                    assertThat(order.getSize()).isEqualTo(3);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(3L);
                    assertThat(order.getPrice()).isEqualTo(1.2);
                    assertThat(order.getSize()).isEqualTo(5);
                });

        assertThat(orderBook.getAllOrderBySide('O'))
                .satisfiesExactly(order -> {
                    assertThat(order.getId()).isEqualTo(8L);
                    assertThat(order.getPrice()).isEqualTo(1.2);
                    assertThat(order.getSize()).isEqualTo(2);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(9L);
                    assertThat(order.getPrice()).isEqualTo(2.2);
                    assertThat(order.getSize()).isEqualTo(10);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(6L);
                    assertThat(order.getPrice()).isEqualTo(3.2);
                    assertThat(order.getSize()).isEqualTo(1);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(10L);
                    assertThat(order.getPrice()).isEqualTo(3.2);
                    assertThat(order.getSize()).isEqualTo(2);
                }, order -> {
                    assertThat(order.getId()).isEqualTo(7L);
                    assertThat(order.getPrice()).isEqualTo(4.2);
                    assertThat(order.getSize()).isEqualTo(3);
                });
    }

    @Test
    public void testChangeSize() {
        // GIVEN
        orderBook.add(new Order(1l, 3.2, 'B', 1));
        orderBook.add(new Order(2l, 5.2, 'B', 9));

        assertThat(orderBook.getAllOrderBySide('B')).hasSize(2).anySatisfy(order -> {
            assertThat(order.getSize()).isEqualTo(9);
        });

        // WHEN
        orderBook.changeSize(2, 5);

        // THEN
        assertThat(orderBook.getAllOrderBySide('B')).hasSize(2).anySatisfy(order -> {
            assertThat(order.getSize()).isEqualTo(5);
        });
    }

}