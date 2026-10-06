'use strict';

const ApiError = require('../ApiError');

function notFound(req, res) {
  res.status(404).json({ error: 'Recurso no encontrado.', code: 'NOT_FOUND' });
}

// eslint-disable-next-line no-unused-vars
function errorHandler(err, req, res, next) {
  if (err instanceof ApiError) {
    return res.status(err.status).json({ error: err.message, code: err.code });
  }
  if (err && err.type === 'entity.parse.failed') {
    return res.status(400).json({ error: 'JSON invalido en la peticion.', code: 'BAD_JSON' });
  }
  console.error('[api] error inesperado:', err);
  return res.status(500).json({ error: 'Error interno del servidor.', code: 'INTERNAL' });
}

module.exports = { notFound, errorHandler };
