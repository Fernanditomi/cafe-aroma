'use strict';

const config = {
  port: parseInt(process.env.PORT || '8080', 10),
  databaseUrl: process.env.DATABASE_URL || 'postgres://cafe:cafe_secret@localhost:5432/cafe_db',
  jwtSecret: process.env.JWT_SECRET || 'cafe-aroma-dev-secret-change-me',
  jwtExpiresIn: process.env.JWT_EXPIRES_IN || '7d',
  nodeEnv: process.env.NODE_ENV || 'development',
  seed: {
    adminEmail: (process.env.SEED_ADMIN_EMAIL || 'admin@cafearoma.com').toLowerCase(),
    adminPassword: process.env.SEED_ADMIN_PASSWORD || 'admin123',
    clientEmail: (process.env.SEED_CLIENT_EMAIL || 'cliente@cafearoma.com').toLowerCase(),
    clientPassword: process.env.SEED_CLIENT_PASSWORD || 'cliente123'
  },
  rateLimit: {
    windowMs: 15 * 60 * 1000,
    maxAttempts: parseInt(process.env.AUTH_MAX_ATTEMPTS || '10', 10)
  }
};

module.exports = config;
