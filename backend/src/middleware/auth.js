'use strict';

const ApiError = require('../ApiError');
const { verifyToken } = require('../utils/security');

function authRequired(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');
  if (scheme !== 'Bearer' || !token) {
    return next(new ApiError(401, 'Token de sesion no proporcionado.', 'NO_TOKEN'));
  }
  try {
    const payload = verifyToken(token);
    req.user = { id: parseInt(payload.sub, 10), email: payload.email, role: payload.role, isAdmin: payload.isAdmin };
    return next();
  } catch {
    return next(new ApiError(401, 'La sesion ha expirado, inicia sesion nuevamente.', 'TOKEN_EXPIRED'));
  }
}

function adminRequired(req, res, next) {
  if (!req.user || !req.user.isAdmin) {
    return next(new ApiError(403, 'Se requieren permisos de administrador.', 'FORBIDDEN'));
  }
  return next();
}

module.exports = { authRequired, adminRequired };
