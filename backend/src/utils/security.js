'use strict';

const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const config = require('../config');

const SALT_ROUNDS = 10;

async function hashPassword(plain) {
  return bcrypt.hash(String(plain), SALT_ROUNDS);
}

async function verifyPassword(plain, hash) {
  if (!hash) return false;
  try {
    return await bcrypt.compare(String(plain), hash);
  } catch {
    return false;
  }
}

function signToken(user) {
  return jwt.sign(
    { sub: String(user.id), email: user.email, role: user.role, isAdmin: user.is_admin },
    config.jwtSecret,
    { expiresIn: config.jwtExpiresIn }
  );
}

function verifyToken(token) {
  return jwt.verify(token, config.jwtSecret);
}

function toPublicUser(row) {
  if (!row) return null;
  return {
    id: row.id,
    name: row.name,
    email: row.email,
    phone: row.phone || '',
    address: row.address || '',
    avatarUrl: row.avatar_url || '',
    points: row.points ?? 0,
    registrationDate: Number(row.registration_date ?? 0),
    highScore: row.high_score ?? 0,
    isAdmin: Boolean(row.is_admin),
    role: row.role || 'CLIENTE'
  };
}

module.exports = { hashPassword, verifyPassword, signToken, verifyToken, toPublicUser };
