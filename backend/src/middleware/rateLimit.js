'use strict';

const config = require('../config');

const attempts = new Map();

function keyFor(req) {
  const ip = req.ip || req.socket?.remoteAddress || 'unknown';
  const email = String((req.body && req.body.email) || '').toLowerCase();
  return `${ip}|${email}`;
}

function rateLimitAuth(req, res, next) {
  const key = keyFor(req);
  const now = Date.now();
  let entry = attempts.get(key);

  if (!entry || now - entry.start > config.rateLimit.windowMs) {
    entry = { start: now, count: 0 };
    attempts.set(key, entry);
  }

  entry.count += 1;
  if (entry.count > config.rateLimit.maxAttempts) {
    const retryAfter = Math.ceil((config.rateLimit.windowMs - (now - entry.start)) / 1000);
    res.set('Retry-After', String(retryAfter));
    return res.status(429).json({
      error: 'Demasiados intentos, intenta de nuevo mas tarde.',
      code: 'RATE_LIMITED'
    });
  }

  if (attempts.size > 5000) attempts.clear();
  return next();
}

function clearAttempts(req) {
  attempts.delete(keyFor(req));
}

module.exports = { rateLimitAuth, clearAttempts };
