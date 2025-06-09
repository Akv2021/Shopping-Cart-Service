const app = require('./app');
const config = require('../config/default');
const logger = require('./common/utils/logger');

const server = app.listen(config.server.port, () => {
    logger.info(`Server running on port ${config.server.port}`);
});

// Handle shutdown gracefully
process.on('SIGTERM', () => {
    logger.info('SIGTERM signal received: closing HTTP server');
    server.close(() => {
        logger.info('HTTP server closed');
    });
});

module.exports = server;