'use strict';

const db = require('./db');
const config = require('./config');
const { hashPassword, toPublicUser } = require('./utils/security');
const { normalizeEmail } = require('./utils/validators');

async function ensureUser({ email, password, name, role }) {
  const normalized = normalizeEmail(email);
  const existing = await db.query('SELECT * FROM users WHERE LOWER(email) = $1 LIMIT 1', [normalized]);
  if (existing.rows.length > 0) return null;

  const passwordHash = await hashPassword(password);
  const result = await db.query(
    `INSERT INTO users (name, email, password_hash, is_admin, role)
     VALUES ($1, $2, $3, $4, $5)
     RETURNING *`,
    [name, normalized, passwordHash, role === 'ADMIN', role]
  );
  return toPublicUser(result.rows[0]);
}

async function seed() {
  const created = [];

  const admin = await ensureUser({
    email: config.seed.adminEmail,
    password: config.seed.adminPassword,
    name: 'Administrador',
    role: 'ADMIN'
  });
  if (admin) created.push(admin.email);

  const client = await ensureUser({
    email: config.seed.clientEmail,
    password: config.seed.clientPassword,
    name: 'Cliente Demo',
    role: 'CLIENTE'
  });
  if (client) created.push(client.email);

  if (created.length > 0) {
    console.log(`[seed] usuarios creados: ${created.join(', ')}`);
  }
}

if (require.main === module) {
  (async () => {
    await db.ping();
    await db.initSchema();
    await seed();
    await db.close();
    console.log('[seed] listo.');
  })().catch((err) => {
    console.error('[seed] error:', err.message);
    process.exit(1);
  });
}

module.exports = { seed };
