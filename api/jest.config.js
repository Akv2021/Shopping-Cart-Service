module.exports = {
    testEnvironment: 'node',
    setupFiles: ['<rootDir>/tests/setup.js'],
    collectCoverageFrom: [
        'src/**/*.js',
        '!src/server.js'
    ],
    coverageThreshold: {
        global: {
            branches: 75,
            functions: 75,
            lines: 75,
            statements: 75
        }
    },
    testTimeout: 10000,
    forceExit: true,
    moduleDirectories: ['node_modules', 'src'],
    testPathIgnorePatterns: ['/node_modules/'],
    coveragePathIgnorePatterns: [
        'node_modules',
        'tests/setup.js'
    ]
};
