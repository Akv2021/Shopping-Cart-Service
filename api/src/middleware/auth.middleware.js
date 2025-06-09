const auth = require('basic-auth');
const config = require('../../config/default');
const {
    AuthError
} = require('../common/utils/errors');

module.exports = (req, res, next) => {
    try {
        const credentials = auth(req);

        if (!credentials || !isValid(credentials)) {
            throw new AuthError('Invalid credentials');
        }

        next();
    } catch (error) {
        next(error);
    }

};

function isValid(credentials) {
    return credentials.name === config.auth.user &&
        credentials.pass === config.auth.password;
}