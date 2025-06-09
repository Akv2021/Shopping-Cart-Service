const request = require('supertest');
const app = require('../../src/app');
const cartService = require('../../src/api/v1/cart/cart.service');

jest.mock('../../src/api/v1/cart/cart.service', () => ({
    createCart: jest.fn(),
    getCart: jest.fn(),
    addItem: jest.fn(),
    removeItem: jest.fn(),
    clearCart: jest.fn(),
    syncPendingOperations: jest.fn()
}));

describe('Cart Controller', () => {
    const validAuth = Buffer.from('test:test').toString('base64');
    const authHeader = { Authorization: `Basic ${validAuth}` };

    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('Authentication', () => {
        test('should reject requests without auth', async () => {
            const response = await request(app).post('/api/v1/cart');
            expect(response.status).toBe(401);
        });
    });

    describe('Success Cases', () => {
        test('should handle basic CRUD operations', async () => {
            // Create
            const mockCart = { id: '123', items: [] };
            cartService.createCart.mockResolvedValue(mockCart);
            let response = await request(app)
                .post('/api/v1/cart')
                .set(authHeader);
            expect(response.status).toBe(200);
            expect(response.body).toEqual(mockCart);

            // Add Item
            const mockCartWithItem = { id: '123', items: [{ name: 'APPLE' }] };
            cartService.getCart.mockResolvedValue({ id: '123' });
            cartService.addItem.mockResolvedValue(mockCartWithItem);
            response = await request(app)
                .post('/api/v1/cart/123/items')
                .set(authHeader)
                .send({ itemName: 'APPLE' });
            expect(response.status).toBe(200);
            expect(response.body).toEqual(mockCartWithItem);

            // Get
            cartService.getCart.mockResolvedValue(mockCartWithItem);
            response = await request(app)
                .get('/api/v1/cart/123')
                .set(authHeader);
            expect(response.status).toBe(200);
            expect(response.body).toEqual(mockCartWithItem);

            // Remove Item
            cartService.removeItem.mockResolvedValue(mockCart);
            response = await request(app)
                .delete('/api/v1/cart/123/items/APPLE')
                .set(authHeader);
            expect(response.status).toBe(200);

            // Clear
            cartService.clearCart.mockResolvedValue({ status: 'success' });
            response = await request(app)
                .delete('/api/v1/cart/123')
                .set(authHeader);
            expect(response.status).toBe(200);
        });
    });

    describe('Error Cases', () => {
        test('should handle validation errors', async () => {
            const response = await request(app)
                .post('/api/v1/cart/123/items')
                .set(authHeader)
                .send({});
            
            expect(response.status).toBe(400);
            expect(response.body.status).toBe('error');
            expect(response.body.message).toBe('Item name is required');
        });

        test('should handle service errors', async () => {
            cartService.getCart.mockRejectedValue(new Error('Service error'));
            
            const response = await request(app)
                .get('/api/v1/cart/123')
                .set(authHeader);
            
            expect(response.status).toBe(500);
            expect(response.body.status).toBe('error');
            expect(response.body.message).toBe('Service error');
        });
    });
});
