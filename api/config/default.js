module.exports = {
    server: {
        port: process.env.PORT || 3000
    },
    javaBackend: {
        baseUrl: process.env.JAVA_SERVICE_URL || 'http://localhost:8080/api/v1',
        timeout: 5000
    },
    auth: {
        user: process.env.API_USER,
        password: process.env.API_PASSWORD
    },
    logging: {
        level: process.env.LOG_LEVEL || 'info',
        directory: 'logs'
    }
};
