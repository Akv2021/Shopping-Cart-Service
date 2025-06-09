const express = require('express');
const cors = require('cors');
const config = require('../config/default');
const authMiddleware = require('./middleware/auth.middleware');
const errorMiddleware = require('./middleware/error.middleware');
const cartRoutes = require('./api/v1/cart/cart.routes');

const app = express();

app.use(cors());
app.use(express.json());
app.use(authMiddleware);

app.use('/api/v1/cart', cartRoutes);
app.use(errorMiddleware);

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'UP' });
});

// Error handling for unhandled routes
app.use((req, res) => {
    res.status(404).json({ 
        status: 'error',
        message: 'Route not found' 
    });
});

module.exports = app;