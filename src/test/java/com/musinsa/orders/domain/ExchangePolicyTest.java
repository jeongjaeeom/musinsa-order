package com.musinsa.orders.domain;

import static com.musinsa.orders.Fixtures.createOrder;
import static com.musinsa.orders.Fixtures.createOrderLineItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExchangePolicyTest {

  private ExchangePolicy exchangePolicy;

  @BeforeEach
  void setUp() {
    exchangePolicy = new ExchangePolicy();
  }

  @Test
  @DisplayName("주문 상품 교환 반품비 계산 요청에 왕복 반품비 5000원을 반환한다")
  void calculateReturnShippingFee() {
    Order order = createOrder(1L, List.of(
        createOrderLineItem(1L, 1L, "신발A", 15_000L),
        createOrderLineItem(2L, 2L, "신발B", 16_000L),
        createOrderLineItem(3L, 3L, "신발C", 17_000L)
    ));

    Money actual = exchangePolicy.calculateReturnShippingFee(order, List.of(1L));
    assertThat(actual).isEqualTo(Money.from(5_000L));
  }

}