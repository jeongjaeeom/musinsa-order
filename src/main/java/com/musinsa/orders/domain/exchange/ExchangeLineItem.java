package com.musinsa.orders.domain.exchange;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class ExchangeLineItem {

  @Column(nullable = false)
  private Long orderLineItemId;

  public ExchangeLineItem(Long orderLineItemId) {
    if (Objects.isNull(orderLineItemId)) {
      throw new IllegalArgumentException("OrderLineItem ID does not allow null");
    }
    this.orderLineItemId = orderLineItemId;
  }

  public Long orderLineItemId() {
    return orderLineItemId;
  }
}
