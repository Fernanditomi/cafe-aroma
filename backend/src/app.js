'use strict';

const express = require('express');
const cors = require('cors');
const apiRoutes = require('./routes/api');
const { notFound, errorHandler } = require('./middleware/errorHandler');
const db = require('./db');

function createApp() {
  const app = express();

  app.disable('x-powered-by');
  app.set('trust proxy', 1);
  app.use(cors());
  app.use(express.json({ limit: '64kb' }));

  app.use((req, res, next) => {
    console.log(`[req] ${req.method} ${req.originalUrl} <- ${req.ip}`);
    next();
  });

  app.get('/api/health', async (req, res) => {
    let dbUp = false;
    try {
      dbUp = await db.ping();
    } catch {
      dbUp = false;
    }
    res.status(dbUp ? 200 : 503).json({
      status: dbUp ? 'ok' : 'degraded',
      database: dbUp ? 'up' : 'down',
      uptime: Math.round(process.uptime()),
      timestamp: new Date().toISOString()
    });
  });

  app.use('/api', apiRoutes);

  app.use(notFound);
  app.use(errorHandler);

  return app;
}

module.exports = { createApp };
