const errorHandler = require('../../src/middleware/error.middleware');
const { CartError, AuthError } = require('../../src/common/utils/errors');

describe('Error Handler', () => {
    let mockRes;
    let mockReq;

    beforeEach(() => {
        mockRes = {
            status: jest.fn().mockReturnThis(),
            json: jest.fn()
        };
        mockReq = {};
    });

    test('should handle auth errors', () => {
        const error = new AuthError('Invalid credentials');
        errorHandler(error, mockReq, mockRes);
        
        expect(mockRes.status).toHaveBeenCalledWith(401);
        expect(mockRes.json).toHaveBeenCalledWith({
            status: 'error',
            message: 'Invalid credentials'
        });
    });

    test('should handle cart errors', () => {
        const error = new CartError('Cart not found');
        errorHandler(error, mockReq, mockRes);
        
        expect(mockRes.status).toHaveBeenCalledWith(400);
        expect(mockRes.json).toHaveBeenCalledWith({
            status: 'error',
            message: 'Cart not found'
        });
    });

    test('should handle version conflict errors', () => {
        const error = new CartError('Version conflict', null, 409);
        errorHandler(error, mockReq, mockRes);
        
        expect(mockRes.status).toHaveBeenCalledWith(409);
        expect(mockRes.json).toHaveBeenCalledWith({
            status: 'error',
            message: 'Version conflict'
        });
    });
});
