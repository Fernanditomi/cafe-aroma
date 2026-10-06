'use strict';

const { createApp } = require('./app');
const db = require('./db');
const config = require('./config');
const { seed } = require('./seed');

async function main() {
  let ready = false;
  const maxAttempts = 30;

  for (let attempt = 1; attempt <= maxAttempts && !ready; attempt++) {
    try {
      await db.ping();
      ready = true;
    } catch (err) {
      console.log(`[db] esperando postgres (${attempt}/${maxAttempts}): ${err.message}`);
      await new Promise((r) => setTimeout(r, 2000));
    }
  }

  if (!ready) {
    console.error('[db] no se pudo conectar a la base de datos.');
    process.exit(1);
  }

  await db.initSchema();
  await seed();

  const app = createApp();
  app.listen(config.port, '0.0.0.0', () => {
    console.log(`[api] Cafe Aroma API escuchando en http://0.0.0.0:${config.port}/api`);
    console.log(`[api] admin: ${config.seed.adminEmail} | cliente: ${config.seed.clientEmail}`);
  });
}

main().catch((err) => {
  console.error('[api] fallo al iniciar:', err);
  process.exit(1);
});

process.on('SIGTERM', async () => {
  await db.close().catch(() => {});
  process.exit(0);
});
