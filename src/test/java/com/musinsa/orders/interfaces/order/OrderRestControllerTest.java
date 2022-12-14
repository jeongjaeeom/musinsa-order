package com.musinsa.orders.interfaces.order;

import static com.musinsa.orders.Fixtures.createOrder;
import static com.musinsa.orders.Fixtures.createOrderLineItem;
import static com.musinsa.orders.Fixtures.exchange;
import static com.musinsa.orders.Fixtures.exchangeLineItem;
import static com.musinsa.orders.Fixtures.refund;
import static com.musinsa.orders.Fixtures.refundLineItem;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.musinsa.orders.application.order.OrderDtos.OrderLineItemRequest;
import com.musinsa.orders.application.order.OrderDtos.OrderRequest;
import com.musinsa.orders.application.order.OrderDtos.OrderReturnRequest;
import com.musinsa.orders.application.order.OrderDtos.ReturnLineItemRequest;
import com.musinsa.orders.domain.exchange.ExchangeReason;
import com.musinsa.orders.domain.exchange.ExchangeReason.ExchangeReasonType;
import com.musinsa.orders.domain.exchange.ExchangeRepository;
import com.musinsa.orders.domain.order.Money;
import com.musinsa.orders.domain.order.Order;
import com.musinsa.orders.domain.order.OrderLineItem;
import com.musinsa.orders.domain.order.OrderRepository;
import com.musinsa.orders.domain.refund.RefundReason;
import com.musinsa.orders.domain.refund.RefundReason.RefundReasonType;
import com.musinsa.orders.domain.refund.RefundRepository;
import io.restassured.RestAssured;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("?????? API")
class OrderRestControllerTest {

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private RefundRepository refundRepository;

  @Autowired
  private ExchangeRepository exchangeRepository;

  @Value("${local.server.port}")
  int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @AfterEach
  void tearDown() {
    refundRepository.deleteAll();
    exchangeRepository.deleteAll();
    orderRepository.deleteAll();
  }

  @Nested
  @DisplayName("?????? ?????? ?????????")
  class PlaceOrder {

    @Test
    @DisplayName("???????????? 201 Created??? ????????? ????????? ????????????")
    void placeOrder() {
      OrderRequest request = new OrderRequest(
          List.of(
              new OrderLineItemRequest(1L, "??????A", 40_000L),
              new OrderLineItemRequest(2L, "??????B", 50_000L),
              new OrderLineItemRequest(3L, "??????C", 60_000L)
          )
      );

      given().
          header("Content-type", MediaType.APPLICATION_JSON_VALUE).
          body(request).
          log().all().
          when().
          post("/api/v1/orders").
          then().
          statusCode(HttpStatus.CREATED.value()).
          log().all()
          .body("orderLineItems", hasSize(3));
    }
  }

  @Nested
  @DisplayName("?????? ?????? ?????????")
  class GetOrder {

    @Test
    @DisplayName("???????????? 200 OK??? ????????? ????????????")
    void getOrder() {
      Order order = orderRepository.save(
          createOrder(1L, List.of(
              createOrderLineItem(1L, "??????A", 15_000L),
              createOrderLineItem(2L, "??????B", 16_000L),
              createOrderLineItem(3L, "??????C", 17_000L)
          ))
      );

      given().
          header("Content-type", MediaType.APPLICATION_JSON_VALUE).
          log().all().
          when().
          get("/api/v1/orders/{orderId}", order.id()).
          then().
          statusCode(HttpStatus.OK.value()).
          log().all()
          .body("orderLineItems", hasSize(3));
    }
  }

  @Nested
  @DisplayName("?????? ?????? 1???")
  class ProblemOrder1 {

    private final Order order = createOrder(List.of(
        createOrderLineItem(1L, "??????A", 15_000L),
        createOrderLineItem(2L, "??????B", 16_000L),
        createOrderLineItem(3L, "??????C", 17_000L)
    ));

    @Test
    @DisplayName("??????A ?????? ????????? ????????? 5,000?????? ????????????")
    void case1() {
      Order savedOrder = orderRepository.save(order);
      OrderLineItem orderLineItemA = savedOrder.getOrderLineItems().get(0);
      OrderReturnRequest request = new OrderReturnRequest(
          List.of(new ReturnLineItemRequest(orderLineItemA.id())));

      given().
          header("Content-type", MediaType.APPLICATION_JSON_VALUE).
          body(request).
          log().all().
          when().
          post("/api/v1/orders/{orderId}/exchanges/calculate", savedOrder.id()).
          then().
          statusCode(HttpStatus.OK.value()).
          log().all()
          .body("returnShippingFee", equalTo(5_000));
    }

    @Test
    @DisplayName("??????A ?????? ?????? ?????? ??????B ????????? ????????? 2,500?????? ????????????")
    void case2() {
      Order savedOrder = orderRepository.save(order);
      OrderLineItem orderLineItemA = savedOrder.getOrderLineItems().get(0);
      OrderLineItem orderLineItemB = savedOrder.getOrderLineItems().get(1);
      OrderReturnRequest request = new OrderReturnRequest(
          List.of(new ReturnLineItemRequest(orderLineItemB.id())));

      exchangeRepository.save(
          exchange(
              1L,
              savedOrder.id(),
              new ExchangeReason(ExchangeReasonType.CHANGE_OF_MIND, "?????? ????????? ????????? ????????????"),
              Money.from(5_000L),
              List.of(exchangeLineItem(orderLineItemA.id()))
          )
      );

      given().
          header("Content-type", MediaType.APPLICATION_JSON_VALUE).
          body(request).
          log().all().
          when().
          post("/api/v1/orders/{orderId}/refunds/calculate", savedOrder.id()).
          then().
          statusCode(HttpStatus.OK.value()).
          log().all()
          .body("returnShippingFee", equalTo(2_500));
    }

    @Test
    @DisplayName("??????B ?????? ?????? ?????? ??????C??? ??????A??? ????????? ????????? ????????? 2,500?????? ????????????")
    void case3() {
      Order savedOrder = orderRepository.save(order);
      OrderLineItem orderLineItemA = savedOrder.getOrderLineItems().get(0);
      OrderLineItem orderLineItemB = savedOrder.getOrderLineItems().get(1);
      OrderLineItem orderLineItemC = savedOrder.getOrderLineItems().get(2);
      OrderReturnRequest request = new OrderReturnRequest(
          List.of(
              new ReturnLineItemRequest(orderLineItemA.id()),
              new ReturnLineItemRequest(orderLineItemC.id())
          )
      );

      exchangeRepository.save(
          exchange(
              1L,
              savedOrder.id(),
              new ExchangeReason(ExchangeReasonType.CHANGE_OF_MIND, "?????? ????????? ????????? ????????????"),
              Money.from(5_000L),
              List.of(exchangeLineItem(orderLineItemA.id()))
          )
      );

      refundRepository.save(
          refund(
              1L,
              savedOrder.id(),
              new RefundReason(RefundReasonType.CHANGE_OF_MIND, "?????? ????????? ????????? ????????????"),
              Money.from(2_500L),
              List.of(refundLineItem(orderLineItemB.id()))
          )
      );

      given().
          header("Content-type", MediaType.APPLICATION_JSON_VALUE).
          body(request).
          log().all().
          when().
          post("/api/v1/orders/{orderId}/refunds/calculate", savedOrder.id()).
          then().
          statusCode(HttpStatus.OK.value()).
          log().all()
          .body("returnShippingFee", equalTo(2_500));
    }

  }

  @Nested
  @DisplayName("?????? ?????? 2???")
  class ProblemOrder2 {

    private final Order order = createOrder(List.of(
        createOrderLineItem(1L, "??????A", 40_000L),
        createOrderLineItem(2L, "??????B", 50_000L),
        createOrderLineItem(3L, "??????C", 60_000L)
    ));

    @Test
    @DisplayName("??????A ?????? ????????? ????????? 2,500?????? ????????????")
    void case1() {
      Order savedOrder = orderRepository.save(order);
      OrderLineItem orderLineItemA = savedOrder.getOrderLineItems().get(0);
      OrderReturnRequest request = new OrderReturnRequest(
          List.of(new ReturnLineItemRequest(orderLineItemA.id())));

      given().
          header("Content-type", MediaType.APPLICATION_JSON_VALUE).
          body(request).
          log().all().
          when().
          post("/api/v1/orders/{orderId}/refunds/calculate", savedOrder.id()).
          then().
          statusCode(HttpStatus.OK.value()).
          log().all()
          .body("returnShippingFee", equalTo(2_500));
    }

    @Test
    @DisplayName("??????A ?????? ?????? ?????? ??????B??? C??? ????????? ????????? ????????? 5,000?????? ????????????")
    void case2() {
      Order savedOrder = orderRepository.save(order);
      OrderLineItem orderLineItemA = savedOrder.getOrderLineItems().get(0);
      OrderLineItem orderLineItemB = savedOrder.getOrderLineItems().get(1);
      OrderLineItem orderLineItemC = savedOrder.getOrderLineItems().get(2);
      OrderReturnRequest request = new OrderReturnRequest(
          List.of(
              new ReturnLineItemRequest(orderLineItemB.id()),
              new ReturnLineItemRequest(orderLineItemC.id())
          )
      );

      refundRepository.save(
          refund(
              1L,
              savedOrder.id(),
              new RefundReason(RefundReasonType.CHANGE_OF_MIND, "?????? ????????? ????????? ????????????"),
              Money.from(2_500L),
              List.of(refundLineItem(orderLineItemA.id()))
          )
      );

      given().
          header("Content-type", MediaType.APPLICATION_JSON_VALUE).
          body(request).
          log().all().
          when().
          post("/api/v1/orders/{orderId}/refunds/calculate", savedOrder.id()).
          then().
          statusCode(HttpStatus.OK.value()).
          log().all()
          .body("returnShippingFee", equalTo(5_000));
    }

  }


}