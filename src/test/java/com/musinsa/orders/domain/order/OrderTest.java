package com.musinsa.orders.domain.order;

import static com.musinsa.orders.Fixtures.createOrder;
import static com.musinsa.orders.Fixtures.createOrderLineItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("주문 객체는")
class OrderTest {

  private ShippingFeePolicy shippingFeePolicy;

  @BeforeEach
  void setUp() {
    shippingFeePolicy = new ShippingFeePolicy();
  }

  @Test
  @DisplayName("1개 이상의 주문 상품으로 주문을 생성할 수 있다")
  void placeOrder() {
    Order actual = createOrder(1L, List.of(
        createOrderLineItem(1L, "신발A", 15_000L),
        createOrderLineItem(2L, "신발B", 16_000L),
        createOrderLineItem(3L, "신발C", 17_000L)
    ), shippingFeePolicy);

    assertThat(actual).isNotNull();
    assertThat(actual.getOrderLineItems()).hasSize(3);
    assertThat(actual.shippingFee()).isEqualTo(Money.from(2_500L));
    assertThat(actual.getOrderLineItems()).extracting("productId", "name", "price")
        .contains(
            tuple(1L, ProductName.from("신발A"), Money.from(15_000L)),
            tuple(2L, ProductName.from("신발B"), Money.from(16_000L)),
            tuple(3L, ProductName.from("신발C"), Money.from(17_000L))
        );
  }

}