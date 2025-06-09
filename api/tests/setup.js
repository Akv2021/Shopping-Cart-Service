// Mock navigator
global.navigator = {
    onLine: true
};

// Mock environment variables
process.env.API_USER = 'test';
process.env.API_PASSWORD = 'test';
process.env.JAVA_SERVICE_URL = 'http://localhost:8080/api';
