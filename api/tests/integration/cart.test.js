const request = require('supertest');
const app = require('../../src/app');
const cartService = require('../../src/api/v1/cart/cart.service');
const { CartError } = require('../../src/common/utils/errors');

jest.mock('../../src/api/v1/cart/cart.service', () => ({
    createCart: jest.fn(),
    getCart: jest.fn(),
    addItem: jest.fn(),
    removeItem: jest.fn(),
    clearCart: jest.fn(),
    syncPendingOperations: jest.fn()
}));

describe('Cart API Integration', () => {
    const validAuth = Buffer.from('test:test').toString('base64');
    const authHeader = { Authorization: `Basic ${validAuth}` };
    let cartId;

    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('Basic Operations', () => {
        test('should create cart', async () => {
            const mockCart = { id: '123', items: [], version: 1, total: 0 };
            cartService.createCart.mockResolvedValue(mockCart);

            const response = await request(app)
                .post('/api/v1/cart')
                .set(authHeader);
            
            expect(response.status).toBe(200);
            expect(response.body).toEqual(mockCart);
        });

        test('should add item to cart', async () => {
            const mockCart = { id: '123', items: [], version: 1, total: 0 };
            const mockCartWithItem = { 
                id: '123', 
                items: [{ name: 'APPLE', quantity: 1 }],
                version: 2,
                total: 0.35
            };

            cartService.getCart.mockResolvedValue(mockCart);
            cartService.addItem.mockResolvedValue(mockCartWithItem);

            const response = await request(app)
                .post('/api/v1/cart/123/items')
                .set(authHeader)
                .send({ itemName: 'APPLE' });
            
            expect(response.status).toBe(200);
            expect(response.body).toEqual(mockCartWithItem);
        });

        test('should get cart', async () => {
            const mockCart = { 
                id: '123', 
                items: [{ name: 'APPLE', quantity: 1 }],
                version: 1,
                total: 0.35
            };
            cartService.getCart.mockResolvedValue(mockCart);

            const response = await request(app)
                .get('/api/v1/cart/123')
                .set(authHeader);
            
            expect(response.status).toBe(200);
            expect(response.body).toEqual(mockCart);
        });

        test('should remove item from cart', async () => {
            const mockCart = { 
                id: '123', 
                items: [{ name: 'APPLE', quantity: 1 }],
                version: 1,
                total: 0.35
            };
            const mockUpdatedCart = {
                id: '123',
                items: [],
                version: 2,
                total: 0
            };

            cartService.getCart.mockResolvedValue(mockCart);
            cartService.removeItem.mockResolvedValue(mockUpdatedCart);

            const response = await request(app)
                .delete('/api/v1/cart/123/items/APPLE')
                .set(authHeader);
            
            expect(response.status).toBe(200);
            expect(response.body).toEqual(mockUpdatedCart);
        });

        test('should clear cart', async () => {
            const mockCart = { 
                id: '123', 
                items: [{ name: 'APPLE', quantity: 1 }],
                version: 1,
                total: 0.35
            };
            cartService.getCart.mockResolvedValue(mockCart);
            cartService.clearCart.mockResolvedValue({ status: 'success' });

            const response = await request(app)
                .delete('/api/v1/cart/123')
                .set(authHeader);
            
            expect(response.status).toBe(200);
            expect(response.body).toEqual({ status: 'success' });
        });
    });

    describe('Error Scenarios', () => {
        test('should handle invalid cart ID', async () => {
            const error = new CartError('Cart not found');
            cartService.getCart.mockRejectedValue(error);

            const response = await request(app)
                .get('/api/v1/cart/invalid-id')
                .set(authHeader);
            
            expect(response.status).toBe(400);
            expect(response.body.status).toBe('error');
            expect(response.body.message).toBe('Cart not found');
        });

        test('should handle missing item name', async () => {
            const response = await request(app)
                .post('/api/v1/cart/123/items')
                .set(authHeader)
                .send({});
            
            expect(response.status).toBe(400);
            expect(response.body.message).toBe('Item name is required');
        });

        test('should handle backend unavailable', async () => {
            cartService.getCart.mockRejectedValue(new Error('Service unavailable'));

            const response = await request(app)
                .get('/api/v1/cart/123')
                .set(authHeader);
            
            expect(response.status).toBe(500);
            expect(response.body.status).toBe('error');
            expect(response.body.message).toBe('Service unavailable');
        });

        test('should handle version conflicts', async () => {
            cartService.getCart.mockResolvedValue({ id: '123', version: 2 });
            const error = new CartError('Version conflict', null, 409);
            cartService.addItem.mockRejectedValue(error);

            const response = await request(app)
                .post('/api/v1/cart/123/items')
                .set(authHeader)
                .send({ 
                    itemName: 'APPLE',
                    clientVersion: 1
                });
            
            expect(response.status).toBe(409);
            expect(response.body.status).toBe('error');
            expect(response.body.message).toBe('Version conflict');
        });
    });

    describe('Offline Operations', () => {
        beforeEach(() => {
            global.navigator.onLine = false;
        });

        afterEach(() => {
            global.navigator.onLine = true;
        });

        test('should queue operations when offline', async () => {
            const mockPendingResponse = {
                status: 'PENDING',
                message: 'Operation queued for sync',
                pendingOperations: 1
            };

            cartService.getCart.mockResolvedValue({ id: '123', version: 1 });
            cartService.addItem.mockResolvedValue(mockPendingResponse);

            const response = await request(app)
                .post('/api/v1/cart/123/items')
                .set(authHeader)
                .send({ itemName: 'APPLE' });
            
            expect(response.status).toBe(200);
            expect(response.body).toEqual(mockPendingResponse);
        });

        test('should sync when back online', async () => {
            const syncResponse = {
                status: 'success',
                version: 2,
                syncedOperations: 1
            };

            cartService.getCart.mockResolvedValue({ id: '123', version: 1 });
            cartService.syncPendingOperations.mockResolvedValue(syncResponse);

            global.navigator.onLine = true;

            const response = await request(app)
                .post('/api/v1/cart/123/sync')
                .set(authHeader)
                .send({
                    pendingOperations: [
                        { type: 'ADD', item: 'APPLE', clientVersion: 1 }
                    ]
                });
            
            expect(response.status).toBe(200);
            expect(response.body).toEqual(syncResponse);
        });
    });
});
