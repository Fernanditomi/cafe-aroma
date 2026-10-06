'use strict';

const express = require('express');
const authService = require('../services/authService');
const { authRequired, adminRequired } = require('../middleware/auth');
const { rateLimitAuth, clearAttempts } = require('../middleware/rateLimit');

const router = express.Router();

router.post('/auth/register', async (req, res, next) => {
  try {
    const { name, email, phone, password } = req.body || {};
    const result = await authService.register({ name, email, phone, password });
    res.status(201).json(result);
  } catch (err) {
    next(err);
  }
});

router.post('/auth/login', rateLimitAuth, async (req, res, next) => {
  try {
    const { email, password } = req.body || {};
    const result = await authService.login({ email, password });
    clearAttempts(req);
    res.json(result);
  } catch (err) {
    next(err);
  }
});

router.get('/auth/me', authRequired, async (req, res, next) => {
  try {
    res.json({ user: await authService.me(req.user.id) });
  } catch (err) {
    next(err);
  }
});

router.patch('/users/me', authRequired, async (req, res, next) => {
  try {
    res.json({ user: await authService.updateProfile(req.user.id, req.body || {}) });
  } catch (err) {
    next(err);
  }
});

router.delete('/users/me', authRequired, async (req, res, next) => {
  try {
    res.json(await authService.deleteAccount(req.user.id));
  } catch (err) {
    next(err);
  }
});

router.get('/users', authRequired, adminRequired, async (req, res, next) => {
  try {
    res.json({ users: await authService.listUsers() });
  } catch (err) {
    next(err);
  }
});

module.exports = router;
