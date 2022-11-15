package com.musinsa.orders.domain.refund;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class RefundLineItems {

  @ElementCollection
  @CollectionTable(name = "refund_line_item", joinColumns = @JoinColumn(name = "refund_id"))
  private List<RefundLineItem> refundLineItems = List.of();

  public RefundLineItems(List<RefundLineItem> refundLineItems) {
    if (refundLineItems == null || refundLineItems.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.refundLineItems = refundLineItems;
  }

  public static RefundLineItems from(List<Long> refundRequestLineItemIds) {
    return new RefundLineItems(refundRequestLineItemIds.stream()
        .map(RefundLineItem::new)
        .collect(Collectors.toList()));
  }

  public List<RefundLineItem> refundLineItems() {
    return Collections.unmodifiableList(refundLineItems);
  }

}