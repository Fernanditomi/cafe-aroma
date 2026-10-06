'use strict';

const { Pool } = require('pg');
const config = require('./config');

const pool = new Pool({
  connectionString: config.databaseUrl,
  max: 10,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 10000
});

pool.on('error', (err) => {
  console.error('[db] conexion inactiva fallida:', err.message);
});

async function query(text, params) {
  return pool.query(text, params);
}

async function ping() {
  const res = await pool.query('SELECT 1 AS ok');
  return res.rows[0].ok === 1;
}

const SCHEMA = `
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL,
  password_hash TEXT NOT NULL,
  phone VARCHAR(40) NOT NULL DEFAULT '',
  address TEXT NOT NULL DEFAULT '',
  avatar_url TEXT NOT NULL DEFAULT '',
  points INTEGER NOT NULL DEFAULT 0,
  registration_date BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
  high_score INTEGER NOT NULL DEFAULT 0,
  is_admin BOOLEAN NOT NULL DEFAULT FALSE,
  role VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique_idx ON users (LOWER(email));
`;

async function initSchema() {
  await query(SCHEMA);
}

async function close() {
  await pool.end();
}

module.exports = { query, ping, initSchema, close };
