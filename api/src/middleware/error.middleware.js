const logger = require('../common/utils/logger');

module.exports = (err, req, res, next) => {
    const status = err.status || 
                  (err.name === 'AuthError' ? 401 : 
                   err.name === 'CartError' ? (err.status || 400) : 500);
                  
    const message = err.message || 'Internal server error';
    
    logger.error(message, { error: err });
    
    res.status(status).json({
        status: 'error',
        message: message
    });
};