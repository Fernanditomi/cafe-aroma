'use strict';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[a-zA-Z]{2,}$/;

function normalizeEmail(email) {
  return String(email || '').trim().toLowerCase();
}

function validateEmail(email) {
  const value = normalizeEmail(email);
  if (!value) return 'El correo electronico es obligatorio.';
  if (value.length > 160) return 'El correo electronico es demasiado largo.';
  if (!EMAIL_REGEX.test(value)) return 'El formato del correo electronico no es valido.';
  return null;
}

function validatePassword(password) {
  if (password === undefined || password === null || password === '') {
    return 'La contrasena es obligatoria.';
  }
  if (typeof password !== 'string') return 'La contrasena no es valida.';
  if (password.length < 8) return 'La contrasena debe tener al menos 8 caracteres.';
  if (password.length > 128) return 'La contrasena es demasiado larga.';
  if (!/[a-zA-Z]/.test(password) || !/[0-9]/.test(password)) {
    return 'La contrasena debe incluir letras y numeros.';
  }
  return null;
}

function validateName(name) {
  const value = String(name || '').trim();
  if (!value) return 'El nombre es obligatorio.';
  if (value.length < 3) return 'El nombre debe tener al menos 3 caracteres.';
  if (value.length > 120) return 'El nombre es demasiado largo.';
  return null;
}

function validatePhone(phone) {
  const value = String(phone || '').trim();
  if (value === '') return null;
  if (!/^[+0-9()\s-]{5,40}$/.test(value)) return 'El formato del telefono no es valido.';
  return null;
}

function validateRegistration({ name, email, phone, password }) {
  return (
    validateName(name) ||
    validateEmail(email) ||
    validatePhone(phone) ||
    validatePassword(password)
  );
}

module.exports = {
  normalizeEmail,
  validateEmail,
  validatePassword,
  validateName,
  validatePhone,
  validateRegistration
};
