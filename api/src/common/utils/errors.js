class CartError extends Error {
    constructor(message, originalError = null, status = 400) {
        super(message);
        this.name = 'CartError';
        this.status = status;
        this.originalError = originalError;
    }
}

class AuthError extends Error {
    constructor(message) {
        super(message);
        this.name = 'AuthError';
    }
}

module.exports = {
    CartError,
    AuthError
};
