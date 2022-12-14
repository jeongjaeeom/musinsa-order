# 무신사 과제

## 요구사항

- 자바 17버전 필요

## 퀵 스타트

### 데이터베이스 생성

```shell
cd docker
docker compose up -d
```

### 웹 어플리케이션 실행

```shell
./gradlew bootRun
```

### Http Client 실행

#### local 환경으로 변경

![](docs/http-local.png)

#### 주문 시나리오 1

http/problem-1.http 파일 클릭

아래 내용 차례대로 실행

```http request
### 주문번호 1번 주문 등록
POST {{host}}/api/v1/orders
Content-Type: application/json

{
  "orderLineItems": [
    {
      "productId": 1,
      "name": "신발A",
      "price": 15000
    },
    {
      "productId": 2,
      "name": "신발B",
      "price": 16000
    },
    {
      "productId": 3,
      "name": "신발C",
      "price": 17000
    }
  ]
}

### 주문번호 1번 - 신발A 교환 반품비 예상금액 조회
POST {{host}}/api/v1/orders/1/exchanges/calculate
Content-Type: application/json

{
  "returnLineItems": [
    {
      "lineItemId": 1
    }
  ]
}

### 주문번호 1번 - 신발A 교환 접수
POST {{host}}/api/v1/exchanges
Content-Type: application/json

{
  "orderId": 1,
  "reason": {
    "reason": "CHANGE_OF_MIND",
    "detailedReason": "상품 색상이 마음에 안들어요"
  },
  "returnShippingFee": 5000,
  "exchangeLineItems": [
    {
      "orderLineItemId": 1
    }
  ]
}

### 주문번호 1번 - 신발B 환불 반품비 예상금액 조회
POST {{host}}/api/v1/orders/1/refunds/calculate
Content-Type: application/json

{
  "returnLineItems": [
    {
      "lineItemId": 2
    }
  ]
}

### 주문번호 1번 - 신발B 환불 접수
POST {{host}}/api/v1/refunds
Content-Type: application/json

{
  "orderId": 2,
  "reason": {
    "reason": "CHANGE_OF_MIND",
    "detailedReason": "상품 색상이 마음에 안들어요"
  },
  "returnShippingFee": 2500,
  "refundLineItems": [
    {
      "orderLineItemId": 2
    }
  ]
}

### 주문번호 1번 - 신발C와 신발A' 한번에 환불 반품비 예상금액 조회
POST {{host}}/api/v1/orders/1/refunds/calculate
Content-Type: application/json

{
  "returnLineItems": [
    {
      "lineItemId": 3
    },
    {
      "lineItemId": 1
    }
  ]
}

### 주문번호 1번 - 신발C와 신발A' 한번에 환불 접수
POST {{host}}/api/v1/refunds
Content-Type: application/json

{
  "orderId": 2,
  "reason": {
    "reason": "CHANGE_OF_MIND",
    "detailedReason": "상품 색상이 마음에 안들어요"
  },
  "returnShippingFee": 2500,
  "refundLineItems": [
    {
      "orderLineItemId": 3
    },
    {
      "orderLineItemId": 1
    }
  ]
}
```

#### 주문 시나리오 2

http/problem-2.http 파일 클릭

아래 내용 차례대로 실행

```http request
### 주문번호 2번 주문 등록
POST {{host}}/api/v1/orders
Content-Type: application/json

{
  "orderLineItems": [
    {
      "productId": 4,
      "name": "셔츠A",
      "price": 40000
    },
    {
      "productId": 5,
      "name": "셔츠B",
      "price": 50000
    },
    {
      "productId": 6,
      "name": "셔츠C",
      "price": 60000
    }
  ]
}

### 주문번호 2번 - 셔츠A 환불 반품비 예상금액 조회
POST {{host}}/api/v1/orders/2/refunds/calculate
Content-Type: application/json

{
  "returnLineItems": [
    {
      "lineItemId": 4
    }
  ]
}

### 주문번호 2번 - 셔츠A 환불 접수
POST {{host}}/api/v1/refunds
Content-Type: application/json

{
  "orderId": 2,
  "reason": {
    "reason": "CHANGE_OF_MIND",
    "detailedReason": "상품 색상이 마음에 안들어요"
  },
  "returnShippingFee": 2500,
  "refundLineItems": [
    {
      "orderLineItemId": 4
    }
  ]
}

### 주문번호 2번 - 셔츠B와 C를 한번에 환불 반품비 예상금액 조회
POST {{host}}/api/v1/orders/2/refunds/calculate
Content-Type: application/json

{
  "returnLineItems": [
    {
      "lineItemId": 5
    },
    {
      "lineItemId": 6
    }
  ]
}

### 주문번호 2번 - 셔츠B와 C를 한번에 환불 접수
POST {{host}}/api/v1/refunds
Content-Type: application/json

{
  "orderId": 2,
  "reason": {
    "reason": "CHANGE_OF_MIND",
    "detailedReason": "상품 색상이 마음에 안들어요"
  },
  "returnShippingFee": 5000,
  "refundLineItems": [
    {
      "orderLineItemId": 5
    },
    {
      "orderLineItemId": 6
    }
  ]
}
```