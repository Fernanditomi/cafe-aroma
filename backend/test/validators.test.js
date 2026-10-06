'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');

const {
  validateEmail,
  validatePassword,
  validateName,
  validatePhone,
  validateRegistration,
  normalizeEmail
} = require('../src/utils/validators');

test('normalizeEmail recorta y pasa a minusculas', () => {
  assert.equal(normalizeEmail('  Admin@CafeRoma.com '), 'admin@caferoma.com');
  assert.equal(normalizeEmail(undefined), '');
});

test('validateEmail acepta correos validos', () => {
  assert.equal(validateEmail('admin@cafearoma.com'), null);
  assert.equal(validateEmail(' cliente+tag@dominio.co '), null);
});

test('validateEmail rechaza correos invalidos', () => {
  assert.notEqual(validateEmail(''), null);
  assert.notEqual(validateEmail('sin-arroba'), null);
  assert.notEqual(validateEmail('a@b'), null);
  assert.notEqual(validateEmail('a b@c.com'), null);
});

test('validatePassword exige 8+ caracteres con letras y numeros', () => {
  assert.equal(validatePassword('cliente123'), null);
  assert.notEqual(validatePassword('corta1'), null);
  assert.notEqual(validatePassword('solo letras'), null);
  assert.notEqual(validatePassword('12345678'), null);
  assert.notEqual(validatePassword(''), null);
  assert.notEqual(validatePassword(null), null);
});

test('validateName y validatePhone', () => {
  assert.equal(validateName('Cliente Demo'), null);
  assert.notEqual(validateName('An'), null);
  assert.equal(validatePhone(''), null);
  assert.equal(validatePhone('+503 7000-0000'), null);
  assert.notEqual(validatePhone('abc'), null);
});

test('validateRegistration devuelve el primer error o null', () => {
  const valid = { name: 'Juan Perez', email: 'juan@cafe.com', phone: '70000000', password: 'clave1234' };
  assert.equal(validateRegistration(valid), null);
  assert.notEqual(validateRegistration({ ...valid, email: 'mal@' }), null);
  assert.notEqual(validateRegistration({ ...valid, password: '123' }), null);
});
