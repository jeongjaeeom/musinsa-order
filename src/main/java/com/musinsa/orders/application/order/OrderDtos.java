package com.musinsa.orders.application.order;

import com.musinsa.orders.domain.order.Order;
import com.musinsa.orders.domain.order.OrderLineItem;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

public class OrderDtos {

  public record OrderRequest(List<OrderLineItemRequest> orderLineItems) {

    public List<OrderLineItem> toOrderLineItems() {
      return orderLineItems.stream()
          .map(it -> new OrderLineItem(it.productId(), it.name(), it.price()))
          .collect(Collectors.toList());
    }
  }

  public record OrderLineItemRequest(Long productId, String name, Long price) {

  }

  public record OrderReturnRequest(List<ReturnLineItemRequest> returnLineItems) {

    public List<Long> toLineItemIds() {
      return this.returnLineItems.stream()
          .map(it -> it.lineItemId)
          .collect(Collectors.toList());
    }

  }

  public record ReturnLineItemRequest(Long lineItemId) {

  }

  @Data
  public static class OrderResponse {

    private final Long id;

    private final List<OrderLineItemResponse> orderLineItems;

    public OrderResponse(final Order order) {
      this.id = order.id();
      this.orderLineItems = order.getOrderLineItems().stream()
          .map(OrderLineItemResponse::new)
          .collect(Collectors.toList());
    }
  }

  @Data
  public static class OrderLineItemResponse {

    private final Long id;
    private final Long productId;
    private final String name;
    private final Long price;

    public OrderLineItemResponse(final OrderLineItem orderLineItem) {
      this.id = orderLineItem.id();
      this.productId = orderLineItem.productId();
      this.name = orderLineItem.name().toString();
      this.price = orderLineItem.price().amount();
    }
  }

  public record ReturnShippingFeeResponse(Long returnShippingFee) {

  }

}
