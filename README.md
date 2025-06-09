# Shopping Cart System

## Problem Statement
Given a list of shopping items, calculate the total cost of those items.

## Overview
Create a shopping cart system with the following components:
1. A Java backend service for core business logic and pricing rules
2. A JavaScript API layer to expose the shopping cart functionality
3. Real-time Synchronization Feature

## Requirements

### Part 1: Java Core
1. Implement the core shopping cart logic in Java:
- Calculate price of shopping basket
- Items presented one at a time (e.g., `Apple`, `Banana`)
- Multiple items can repeat (e.g., [`Apple`,`Apple`, `Banana`])

2. Pricing Rules:
- Apples: 35p each
- Bananas: 20p each
- Melons: 50p each (buy one get one free)
- Limes: 15p each (three for the price of two)

### Part 2: JavaScript API Layer
- Expose cart operation endpoints
- Communicate with Java backend
- Error handling and response formatting
- Basic authentication

### Part 3: Real-time Synchronization
- Maintain sync between frontend and backend
- Handle network outages
- Automatic conflict resolution

## System Architecture

```mermaid
graph TB
    subgraph "Frontend"
        Client[Browser]
        Store[(Local Storage)]
    end

    subgraph "JavaScript API Layer"
        API[API Gateway]
        Queue[Operation Queue]
        WS[WebSocket Client]
        Sync[Sync Manager]
    end

    subgraph "Java Backend"
        Controller[Cart Controller]
        Service[Cart Service]
        Pricing[Pricing Engine]
        WSHandler[WebSocket Handler]
        Repo[(Cart Repository)]
    end

    Client -->|User Actions| API
    API -->|Store Offline| Queue
    API -->|HTTP| Controller
    Controller -->|Process| Service
    Service -->|Calculate| Pricing
    Service -->|Store| Repo
    Service -->|Events| WSHandler
    WSHandler -->|Updates| WS
    Queue -->|Sync| Sync
    Sync -->|Batch Process| Controller
```

## Component Interactions

### 1. Basic Cart Operation Flow
```mermaid
sequenceDiagram
    participant Client
    participant JS as JS API Layer
    participant Controller
    participant Service
    participant Pricing
    
    Client->>JS: Add Item
    JS->>Controller: POST /cart/{id}/items
    Controller->>Service: addItem(cartId, item)
    Service->>Pricing: calculatePrice(item)
    Pricing-->>Service: price
    Service-->>Controller: updated cart
    Controller-->>JS: response
    JS-->>Client: updated UI
```

### 2. Offline Operation Flow
```mermaid
sequenceDiagram
    participant Client
    participant JS as JS API
    participant Queue
    participant Backend

    Note over Client,Backend: Network Lost
    Client->>JS: Add Item
    JS->>Queue: Store Operation
    JS-->>Client: Status: PENDING

    Note over Client,Backend: Network Restored
    Queue->>JS: Get Pending Ops
    JS->>Backend: Sync Operations
    Backend-->>JS: Success
    JS->>Queue: Clear Operations
```

### 3. Version Conflict Resolution
```mermaid
sequenceDiagram
    participant C as CartController
    participant S as CartService
    participant R as CartRepository

    C->>S: addItem(cartId, item, clientVersion)
    S->>R: findById(cartId)
    R-->>S: serverCart
    
    alt Version Conflict
        S-->>C: throw VersionConflictException
    else Valid Version
        S->>S: processUpdate()
        S->>R: save(updatedCart)
        S-->>C: success
    end
```

## Implementation Details

### JavaScript API Layer
- API endpoints for cart operations
- Offline operation queue
- WebSocket client for real-time updates
- Version tracking
- Error handling

### Java Backend
Key components interaction:
1. CartController: Entry point for HTTP requests
2. CartService: Business logic and pricing
3. PricingStrategy: Price calculation rules
4. CartRepository: Storage operations
5. WebSocketHandler: Real-time updates
6. ErrorHandler: Centralized error management

Pricing Strategies:
- Regular pricing
- Buy One Get One Free
- Three for Two
- Multiple strategy support
  
## Design Choices & Trade-offs

### Current Design
1. Single client per cart assumption
2. Simple version tracking
3. Hybrid WebSocket + HTTP approach
4. Sequential sync for offline operations
5. In-memory storage
6. Basic authentication
7. Queue-based offline handling

### Future Improvements
- Advanced conflict resolution
- Persistent storage
- Advanced retry strategies

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | /api/v1/cart | Create cart |
| GET | /api/v1/cart/{id} | Get cart |
| POST | /api/v1/cart/{id}/items | Add item |
| DELETE | /api/v1/cart/{id}/items/{item} | Remove item |
| DELETE | /api/v1/cart/{id} | Clear cart |
| POST | /api/v1/cart/{id}/sync | Sync offline operations |

WebSocket: ws://host/cart-ws/{cartId}

## Detailed Flow Diagrams

### JavaScript Layer Interactions

1. Online Operations:
```mermaid
sequenceDiagram
    participant UI as Browser UI
    participant API as Cart Service
    participant WS as WebSocket Client
    participant Cache as Local Storage
    participant BE as Backend

    UI->>API: addToCart(item)
    API->>API: checkConnection()
    
    alt Online
        API->>BE: POST /cart/{id}/items
        BE-->>API: Updated Cart
        API->>Cache: updateLocalState()
        API-->>UI: Update Success
    else Offline
        API->>Cache: queueOperation(ADD, item)
        API-->>UI: Operation Pending
    end

    Note over WS,BE: WebSocket Connection
    BE->>WS: Cart Updated Event
    WS->>API: handleUpdate()
    API->>Cache: syncVersion()
    API->>UI: updateUI()
```

2. Offline Sync:
```mermaid
sequenceDiagram
    participant UI as Browser UI
    participant API as Cart Service
    participant Queue as Operation Queue
    participant BE as Backend

    Note over UI,BE: Connection Restored
    API->>Queue: getPendingOperations()
    
    loop For Each Operation
        API->>BE: syncOperation(op)
        alt Success
            BE-->>API: Confirmed
            API->>Queue: removeOperation(op)
        else Conflict
            BE-->>API: Version Conflict
            API->>BE: getCurrentState()
            BE-->>API: Latest State
            API->>Queue: rebaseOperations()
        end
    end
    
    API->>UI: syncComplete()
```

3. Version Management:
```mermaid
sequenceDiagram
    participant API as Cart Service
    participant Cache as Local Storage
    participant WS as WebSocket
    participant BE as Backend

    API->>BE: addItem(version=5)
    BE-->>API: Conflict(server_version=6)
    API->>BE: getLatestState()
    BE-->>API: currentState
    API->>Cache: updateState()
    API->>BE: retryOperation(version=6)
    BE-->>API: Success
    
    Note over WS: Real-time Updates
    BE->>WS: stateChanged
    WS->>API: updateReceived
    API->>Cache: syncVersion()
```

### Java Backend Interactions

1. Cart Operation Processing:
```mermaid
sequenceDiagram
    participant C as CartController
    participant S as CartService
    participant PS as PricingService
    participant ST as PricingStrategy
    participant R as CartRepository
    participant E as EventPublisher
    participant WS as WebSocketHandler

    C->>S: addItem(cartId, item)
    S->>R: findById(cartId)
    R-->>S: Cart
    
    S->>PS: calculatePrice(item, quantity)
    PS->>ST: getStrategy(item)
    PS->>ST: calculatePrice(quantity, basePrice)
    ST-->>PS: finalPrice
    PS-->>S: calculatedPrice
    
    S->>S: updateCartTotal()
    S->>R: save(cart)
    
    S->>E: publishEvent(ITEM_ADDED)
    E->>WS: handleCartEvent()
    WS->>WS: findSession(cartId)
    WS-->>Client: sendUpdate()
```

2. Special Offer Processing:
```mermaid
sequenceDiagram
    participant S as CartService
    participant PS as PricingService
    participant Config as PricingConfig
    participant BOGO as BOGOStrategy
    participant TTT as ThreeForTwoStrategy
    participant Calc as PriceCalculator

    S->>PS: calculatePrice(item, quantity)
    PS->>Config: getStrategies(item)
    Config-->>PS: List<Strategy>
    
    PS->>PS: sortByPriority()
    
    alt Melons
        PS->>BOGO: apply(quantity, price)
        BOGO->>Calc: calculate()
        Calc-->>BOGO: bogoPriced
        BOGO-->>PS: finalPrice
    else Limes
        PS->>TTT: apply(quantity, price)
        TTT->>Calc: calculate()
        Calc-->>TTT: threePriced
        TTT-->>PS: finalPrice
    end
    
    PS-->>S: calculatedPrice
```

3. WebSocket Management:
```mermaid
sequenceDiagram
    participant Client
    participant WS as WebSocketHandler
    participant S as SessionRegistry
    participant E as EventListener
    participant Srv as CartService

    Client->>WS: connect(cartId)
    WS->>S: registerSession(cartId)
    WS-->>Client: connected

    Note over Srv: Cart Updated
    Srv->>E: publishEvent(CartEvent)
    E->>WS: handleCartEvent(event)
    WS->>S: getSession(cartId)
    S-->>WS: session
    WS->>Client: sendMessage(update)

    Note over Client: Connection Lost
    Client->>WS: disconnect
    WS->>S: removeSession(cartId)
```

## Running the Project

```bash
# Start JavaScript API
cd api
npm install
npm start   # Runs on 3000
npm test

# Start Java Backend
cd backend
./mvnw clean install
./mvnw spring-boot:run  # Runs on 8080
```