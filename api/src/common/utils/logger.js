const winston = require('winston');
const config = require('../../../config/default');

const logger = winston.createLogger({
    level: config.logging.level,
    format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.json()
    ),
    transports: [
        new winston.transports.Console(),
        new winston.transports.File({ 
            filename: `${config.logging.directory}/error.log`, 
            level: 'error' 
        }),
        new winston.transports.File({ 
            filename: `${config.logging.directory}/combined.log` 
        })
    ]
});

module.exports = logger;
