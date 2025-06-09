const express = require('express');
const router = express.Router();
const cartService = require('./cart.service');
const { CartError } = require('../../../common/utils/errors');

router.post('/', async (req, res, next) => {
    try {
        const cart = await cartService.createCart();
        res.json(cart);
    } catch (error) {
        next(error);
    }
});

router.post('/:cartId/items', async (req, res, next) => {
    try {
        if (!req.body.itemName) {
            throw new CartError('Item name is required');
        }
        const cart = await cartService.getCart(req.params.cartId);
        const updatedCart = await cartService.addItem(cart.id, req.body.itemName);
        res.json(updatedCart);
    } catch (error) {
        next(error);
    }
});

router.get('/:cartId', async (req, res, next) => {
    try {
        const cart = await cartService.getCart(req.params.cartId);
        res.json(cart);
    } catch (error) {
        next(error);
    }
});

router.delete('/:cartId', async (req, res, next) => {
    try {
        const cart = await cartService.getCart(req.params.cartId);
        await cartService.clearCart(cart.id);
        res.json({ status: 'success' });
    } catch (error) {
        next(error);
    }
});

router.delete('/:cartId/items/:itemName', async (req, res, next) => {
    try {
        const cart = await cartService.getCart(req.params.cartId);
        const updatedCart = await cartService.removeItem(cart.id, req.params.itemName);
        res.json(updatedCart);
    } catch (error) {
        next(error);
    }
});

router.post('/:cartId/sync', async (req, res, next) => {
    try {
        const result = await cartService.syncPendingOperations(req.params.cartId);
        res.json(result);
    } catch (error) {
        next(error);
    }
});

module.exports = router;
