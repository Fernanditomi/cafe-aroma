'use strict';

const db = require('../db');
const ApiError = require('../ApiError');
const { normalizeEmail, validateRegistration, validateEmail } = require('../utils/validators');
const { hashPassword, verifyPassword, signToken, toPublicUser } = require('../utils/security');

async function findUserByEmail(email) {
  const result = await db.query('SELECT * FROM users WHERE LOWER(email) = $1 LIMIT 1', [
    normalizeEmail(email)
  ]);
  return result.rows[0] || null;
}

async function findUserById(id) {
  const result = await db.query('SELECT * FROM users WHERE id = $1 LIMIT 1', [id]);
  return result.rows[0] || null;
}

async function register({ name, email, phone, password }) {
  const validationError = validateRegistration({ name, email, phone, password });
  if (validationError) throw new ApiError(400, validationError, 'VALIDATION');

  const normalizedEmail = normalizeEmail(email);
  const existing = await findUserByEmail(normalizedEmail);
  if (existing) throw new ApiError(409, 'El correo ya esta registrado.', 'EMAIL_TAKEN');

  const role = /\badmin\b/i.test(String(name)) ? 'ADMIN' : 'CLIENTE';
  const passwordHash = await hashPassword(password);

  const result = await db.query(
    `INSERT INTO users (name, email, password_hash, phone, is_admin, role)
     VALUES ($1, $2, $3, $4, $5, $6)
     RETURNING *`,
    [String(name).trim(), normalizedEmail, passwordHash, String(phone || '').trim(), role === 'ADMIN', role]
  );

  const user = result.rows[0];
  return { token: signToken(user), user: toPublicUser(user) };
}

async function login({ email, password }) {
  const emailError = validateEmail(email);
  if (emailError) throw new ApiError(400, emailError, 'VALIDATION');
  if (password === undefined || password === null || password === '') {
    throw new ApiError(400, 'La contrasena es obligatoria.', 'VALIDATION');
  }

  const user = await findUserByEmail(email);
  const ok = user ? await verifyPassword(password, user.password_hash) : false;
  if (!ok) throw new ApiError(401, 'Correo o contrasena incorrectos.', 'INVALID_CREDENTIALS');

  return { token: signToken(user), user: toPublicUser(user) };
}

async function me(userId) {
  const user = await findUserById(userId);
  if (!user) throw new ApiError(401, 'Sesion invalida.', 'INVALID_SESSION');
  return toPublicUser(user);
}

async function updateProfile(userId, patch = {}) {
  const fields = {};
  if (typeof patch.name === 'string' && patch.name.trim()) fields.name = patch.name.trim();
  if (typeof patch.phone === 'string') fields.phone = patch.phone.trim();
  if (typeof patch.address === 'string') fields.address = patch.address.trim();
  if (typeof patch.avatarUrl === 'string') fields.avatarUrl = patch.avatarUrl.trim();
  if (Number.isInteger(patch.points) && patch.points >= 0) fields.points = patch.points;
  if (Number.isInteger(patch.highScore) && patch.highScore >= 0) fields.highScore = patch.highScore;

  const columns = {
    name: 'name',
    phone: 'phone',
    address: 'address',
    avatarUrl: 'avatar_url',
    points: 'points',
    highScore: 'high_score'
  };

  const keys = Object.keys(fields);
  if (keys.length === 0) throw new ApiError(400, 'No hay campos para actualizar.', 'VALIDATION');

  const setClause = keys.map((k, i) => `${columns[k]} = $${i + 2}`).join(', ');
  const values = [userId, ...keys.map((k) => fields[k])];

  const result = await db.query(
    `UPDATE users SET ${setClause}, updated_at = NOW() WHERE id = $1 RETURNING *`,
    values
  );
  if (!result.rows[0]) throw new ApiError(401, 'Sesion invalida.', 'INVALID_SESSION');
  return toPublicUser(result.rows[0]);
}

async function listUsers() {
  const result = await db.query('SELECT * FROM users ORDER BY created_at DESC LIMIT 200');
  return result.rows.map(toPublicUser);
}

async function deleteAccount(userId) {
  const result = await db.query('DELETE FROM users WHERE id = $1 RETURNING id', [userId]);
  if (!result.rows[0]) throw new ApiError(401, 'Sesion invalida.', 'INVALID_SESSION');
  return { deleted: true };
}

module.exports = { register, login, me, updateProfile, listUsers, deleteAccount, findUserByEmail, findUserById };
