const cartService = require('../../src/api/v1/cart/cart.service');
const { CartError } = require('../../src/common/utils/errors');
const fetch = require('node-fetch');

jest.mock('node-fetch');

describe('CartService', () => {
    let mockWs;

    beforeEach(() => {
        fetch.mockClear();
        cartService.pendingOperations.clear();
        cartService.wsConnections.clear();
        cartService.lastKnownVersion = 0;
        global.navigator = { onLine: true };
        
        // Mock WebSocket
        mockWs = {
            close: jest.fn(),
            send: jest.fn()
        };
        global.WebSocket = jest.fn(() => mockWs);
    });

    describe('Online Operations', () => {
        test('should create cart and setup WebSocket', async () => {
            const mockCart = { cartId: '123', items: [], version: 1 };
            const mockResponse = {
                ok: true,
                status: 200,
                json: () => Promise.resolve(mockCart)
            };
            
            fetch.mockResolvedValueOnce(mockResponse);

            const result = await cartService.createCart();
            expect(result).toEqual(mockCart);
            expect(global.WebSocket).toHaveBeenCalled();
            expect(cartService.wsConnections.has('123')).toBeTruthy();
        });

        test('should add item with version', async () => {
            const mockResult = { 
                cartId: '123', 
                items: [{ name: 'APPLE' }],
                version: 2
            };
            const mockResponse = {
                ok: true,
                status: 200,
                json: () => Promise.resolve(mockResult)
            };
            
            fetch.mockResolvedValueOnce(mockResponse);

            const result = await cartService.addItem('123', 'APPLE');
            expect(result).toEqual(mockResult);
            expect(cartService.lastKnownVersion).toBe(2);
        });
    });

    describe('Offline Operations', () => {
        beforeEach(() => {
            global.navigator.onLine = false;
        });

        test('should queue operations when offline', async () => {
            const result = await cartService.addItem('123', 'APPLE');
            
            expect(result.status).toBe('PENDING');
            expect(cartService.pendingOperations.get('123')).toHaveLength(1);
            const operation = cartService.pendingOperations.get('123')[0];
            expect(operation.clientVersion).toBe(cartService.lastKnownVersion);
        });

        test('should sync pending operations when online', async () => {
            await cartService.handleOfflineOperation('123', 'ADD', 'APPLE');
            
            global.navigator.onLine = true;
            const mockResponse = {
                ok: true,
                status: 200,
                json: () => Promise.resolve({ 
                    cartId: '123', 
                    items: [{ name: 'APPLE' }],
                    version: 1
                })
            };
            
            fetch.mockResolvedValueOnce(mockResponse);

            await cartService.syncPendingOperations('123');
            const remainingOps = cartService.pendingOperations.get('123') || [];
            expect(remainingOps).toHaveLength(0);
        });
    });

    describe('WebSocket Handling', () => {
        test('should handle server updates', async () => {
            const mockCart = { cartId: '123', version: 1 };
            fetch.mockResolvedValueOnce({
                ok: true,
                status: 200,
                json: () => Promise.resolve(mockCart)
            });

            await cartService.createCart();
            
            // Simulate WebSocket message
            const message = {
                type: 'ITEM_ADDED',
                cartId: '123',
                version: 2
            };
            mockWs.onmessage({ data: JSON.stringify(message) });
            
            expect(cartService.lastKnownVersion).toBe(2);
        });

        test('should reconnect WebSocket when closed', () => {
            jest.useFakeTimers();
            cartService.setupWebSocket('123');
            mockWs.onclose();
            
            jest.runAllTimers();
            
            expect(global.WebSocket).toHaveBeenCalledTimes(2);
            jest.useRealTimers();
        });

        test('should cleanup WebSocket connections', () => {
            cartService.setupWebSocket('123');
            cartService.close();
            
            expect(mockWs.close).toHaveBeenCalled();
            expect(cartService.wsConnections.size).toBe(0);
        });
    });

    describe('Error Handling', () => {
        test('should handle server errors', async () => {
            const mockResponse = {
                ok: false,
                status: 400,
                json: () => Promise.resolve({ message: 'Error' })
            };
            
            fetch.mockResolvedValueOnce(mockResponse);

            await expect(cartService.addItem('123', 'INVALID'))
                .rejects
                .toThrow(CartError);
        });

        test('should handle network errors', async () => {
            fetch.mockRejectedValueOnce(new Error('Network error'));

            await expect(cartService.addItem('123', 'APPLE'))
                .rejects
                .toThrow(CartError);
        });

        test('should handle null response', async () => {
            fetch.mockResolvedValueOnce(null);

            await expect(cartService.createCart())
                .rejects
                .toThrow(CartError);
        });

        test('should handle invalid JSON response', async () => {
            const mockResponse = {
                ok: true,
                json: () => Promise.reject(new Error('Invalid JSON'))
            };
            
            fetch.mockResolvedValueOnce(mockResponse);

            await expect(cartService.createCart())
                .rejects
                .toThrow(CartError);
        });

        test('should handle network errors during sync', async () => {
            await cartService.handleOfflineOperation('123', 'ADD', 'APPLE');
            global.navigator.onLine = true;
            fetch.mockRejectedValueOnce(new Error('Network error'));

            await expect(cartService.syncPendingOperations('123'))
                .rejects
                .toThrow(CartError);
        });
    });
});
