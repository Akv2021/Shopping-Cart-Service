const fetch = require('node-fetch');
const config = require('../../../../config/default');
const logger = require('../../../common/utils/logger');
const { CartError } = require('../../../common/utils/errors');

class CartService {
    constructor() {
        this.baseUrl = config.javaBackend.baseUrl;
        this.wsUrl = this.baseUrl.replace('http', 'ws');
        this.pendingOperations = new Map();
        this.lastKnownVersion = 0;
        this.wsConnections = new Map();
    }

    async createCart() {
        try {
            // Log the full URL being called
            const url = `${this.baseUrl}/cart`;
            logger.debug(`Creating cart at URL: ${url}`);

            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            // Log the response details
            logger.debug('Response received:', {
                status: response.status,
                ok: response.ok
            });

            const data = await response.json();
            
            // Log the parsed data
            logger.debug('Parsed response:', data);

            if (!response.ok) {
                throw new CartError(data.message || 'Request failed');
            }

            return data;
        } catch (error) {
            logger.error('Cart creation failed:', error);
            throw new CartError('Failed to create cart', error);
        }
    }

    async addItem(cartId, item) {
        try {
            const response = await this._makeRequest('POST', `${this.baseUrl}/cart/${cartId}/items`, {
                itemName: item,
                clientVersion: this.lastKnownVersion
            });
            this.lastKnownVersion = response.version;
            return response;
        } catch (error) {
            // Remove the offline check here too
            throw new CartError('Failed to add item to cart', error);
        }
    }

    async getCart(cartId) {
        try {
            const response = await this._makeRequest('GET', `${this.baseUrl}/cart/${cartId}`);
            this.lastKnownVersion = response.version;
            return response;
        } catch (error) {
            throw new CartError('Failed to retrieve cart', error);
        }
    }

    async removeItem(cartId, itemName) {
        if (!navigator.onLine) {
            return this.handleOfflineOperation(cartId, 'REMOVE', itemName);
        }

        try {
            const response = await this._makeRequest('DELETE', 
                `${this.baseUrl}/cart/${cartId}/items/${itemName}`,
                { clientVersion: this.lastKnownVersion }
            );
            this.lastKnownVersion = response.version;
            return response;
        } catch (error) {
            if (!navigator.onLine) {
                return this.handleOfflineOperation(cartId, 'REMOVE', itemName);
            }
            throw new CartError('Failed to remove item from cart', error);
        }
    }

    async clearCart(cartId) {
        if (!navigator.onLine) {
            return this.handleOfflineOperation(cartId, 'CLEAR');
        }

        try {
            const response = await this._makeRequest('DELETE', 
                `${this.baseUrl}/cart/${cartId}`,
                { clientVersion: this.lastKnownVersion }
            );
            this.lastKnownVersion = response.version;
            return response;
        } catch (error) {
            throw new CartError('Failed to clear cart', error);
        }
    }

    setupWebSocket(cartId) {
        if (this.wsConnections.has(cartId)) {
            return;
        }

  
        const ws = new WebSocket(`${this.wsUrl}/cart-ws/${cartId}`);
        
        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                if (data.version > this.lastKnownVersion) {
                    this.lastKnownVersion = data.version;
                    this.handleServerUpdate(data);
                }
            } catch (error) {
                logger.error('WebSocket message handling error:', error);
            }
        };

        ws.onclose = () => {
            this.wsConnections.delete(cartId);
            if (typeof navigator !== 'undefined' && !navigator.onLine) {
                setTimeout(() => this.setupWebSocket(cartId), 1000);
            }
        };

        ws.onerror = (error) => {
            logger.error('WebSocket error:', error);
            ws.close();
        };

        this.wsConnections.set(cartId, ws);
    }

    async handleOfflineOperation(cartId, operation, item) {
        if (!this.pendingOperations.has(cartId)) {
            this.pendingOperations.set(cartId, []);
        }
        
        const operations = this.pendingOperations.get(cartId);
        operations.push({ 
            operation, 
            item, 
            timestamp: new Date().toISOString(),
            clientVersion: this.lastKnownVersion 
        });
        
        logger.info(`Queued offline operation: ${operation} for cart ${cartId}`);
        return {
            status: 'PENDING',
            message: 'Operation queued for sync',
            pendingOperations: operations.length
        };
    }

    async syncPendingOperations(cartId) {
        if (!this.pendingOperations.has(cartId) || !navigator.onLine) {
            return;
        }

        const operations = this.pendingOperations.get(cartId);
        while (operations.length > 0 && navigator.onLine) {
            const op = operations[0];
            try {
                let response;
                switch (op.operation) {
                    case 'ADD':
                        response = await this.addItem(cartId, op.item);
                        break;
                    case 'REMOVE':
                        response = await this.removeItem(cartId, op.item);
                        break;
                    case 'CLEAR':
                        response = await this.clearCart(cartId);
                        break;
                    default:
                        throw new CartError(`Invalid operation type: ${op.operation}`);
                }
                this.lastKnownVersion = response.version;
                operations.shift();
            } catch (error) {
                if (!navigator.onLine) break;
                throw error;
            }
        }

        if (operations.length === 0) {
            this.pendingOperations.delete(cartId);
        }
    }

    async _makeRequest(method, url, body = null) {
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        if (body) {
            options.body = JSON.stringify(body);
        }

        try {
            const response = await fetch(url, options);
            if (!response) {
  
                throw new Error('No response received');
            }

            const data = await response.json();
            if (!response.ok) {
                throw new CartError(data.message || 'Operation failed', null, response.status);
            }

            return data;
        } catch (error) {
            if (error instanceof CartError) throw error;
            throw new CartError('Service unavailable', error);
        }
    }

    close() {
        this.wsConnections.forEach(ws => ws.close());
        this.wsConnections.clear();
    }
}

module.exports = new CartService();
