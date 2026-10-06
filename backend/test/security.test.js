'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');

const { hashPassword, verifyPassword, signToken, verifyToken, toPublicUser } = require('../src/utils/security');

test('hashPassword genera un hash bcrypt distinto del texto plano', async () => {
  const hash = await hashPassword('cliente123');
  assert.notEqual(hash, 'cliente123');
  assert.ok(hash.startsWith('$2'));
  assert.equal(await verifyPassword('cliente123', hash), true);
  assert.equal(await verifyPassword('otraclave', hash), false);
});

test('verifyPassword es tolerante ante hashes invalidos', async () => {
  assert.equal(await verifyPassword('x', ''), false);
  assert.equal(await verifyPassword('x', 'no-es-hash'), false);
});

test('signToken / verifyToken generan sesiones validas', () => {
  const token = signToken({ id: 7, email: 'a@b.com', role: 'ADMIN', is_admin: true });
  const payload = verifyToken(token);
  assert.equal(payload.sub, '7');
  assert.equal(payload.email, 'a@b.com');
  assert.equal(payload.isAdmin, true);
});

test('verifyToken rechaza tokens manipulados', () => {
  const token = signToken({ id: 1, email: 'a@b.com', role: 'CLIENTE', is_admin: false });
  assert.throws(() => verifyToken(token + 'x'));
});

test('toPublicUser nunca expone el password_hash', () => {
  const user = toPublicUser({
    id: 3,
    name: 'Ana',
    email: 'ana@cafe.com',
    password_hash: '$2a$10$secreto',
    phone: '',
    address: '',
    avatar_url: '',
    points: 10,
    registration_date: 1700000000000,
    high_score: 0,
    is_admin: false,
    role: 'CLIENTE'
  });
  assert.equal(user.password_hash, undefined);
  assert.equal(Object.prototype.hasOwnProperty.call(user, 'password'), false);
  assert.equal(user.avatarUrl, '');
  assert.equal(user.registrationDate, 1700000000000);
});
