package com.corefit.service.market;

import com.corefit.dto.response.market.CartResponse;
import com.corefit.dto.response.market.CartItemResponse;
import com.corefit.dto.response.GeneralResponse;
import com.corefit.entity.market.Cart;
import com.corefit.entity.market.CartItem;
import com.corefit.entity.market.Product;
import com.corefit.entity.User;
import com.corefit.enums.UserType;
import com.corefit.exceptions.GeneralException;
import com.corefit.repository.market.CartItemRepo;
import com.corefit.repository.market.CartRepo;
import com.corefit.repository.market.ProductRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CartItemRepo cartItemRepo;

    @Transactional
    public GeneralResponse<?> getCart(HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Cart cart = getOrCreateCartForUser(user);
        return new GeneralResponse<>("Success", mapToCartDto(cart));
    }

    @Transactional
    public GeneralResponse<?> addItemToCart(HttpServletRequest httpRequest, long productId, int quantity) {
        User user = authService.extractUserFromRequest(httpRequest);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new GeneralException("Product not found"));

        Cart cart = getOrCreateCartForUser(user);
        long marketId = product.getMarket().getId();

        if (cart.getMarket() == null || cart.getCartItems().isEmpty()) {
            cart.setMarket(product.getMarket());
        } else if (cart.getMarket().getId() != marketId) {
            throw new GeneralException("You can't add items from another market. Remove existing cart items first.");
        }

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst().orElse(null);

        if (existingItem != null) {
            if (quantity == 0) {
                cart.getCartItems().remove(existingItem);
            } else {
                existingItem.setQuantity(quantity);
                existingItem.updateTotal();
            }
        } else if (quantity > 0) {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            newItem.updateTotal();

            cart.addItemToCart(newItem);
        }

        cart.updateTotalPrice();
        cartRepo.save(cart);

        return new GeneralResponse<>("Success", mapToCartDto(cart));
    }

    @Transactional
    public GeneralResponse<?> deleteCart(HttpServletRequest httpRequest) {
        User user = authService.extractUserFromRequest(httpRequest);
        Cart cart = cartRepo.findByUserId(user.getId());

        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new GeneralException("Cart is already empty");
        }

        List<CartItem> cartItems = cart.getCartItems();

        cartItems.forEach(cartItem -> cartItem.setCart(null));
        cartItemRepo.deleteAll(cartItems);
        cart.getCartItems().clear();

        cart.setMarket(null);
        cart.setTotalPrice(0.0);
        cartRepo.save(cart);

        return new GeneralResponse<>("Success", mapToCartDto(cart));
    }

    /// Helper method
    private Cart getOrCreateCartForUser(User user) {
        if (user.getType() == UserType.PROVIDER) {
            throw new GeneralException("User not authorized");
        }
        Cart cart = cartRepo.findByUserId(user.getId());
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepo.save(cart);
        }
        return cart;
    }

    private CartResponse mapToCartDto(Cart cart) {
        List<CartItemResponse> cartItems = cart.getCartItems().stream()
                .map(item -> new CartItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getDescription(),
                        item.getProduct().getPrice(),
                        item.getProduct().getOffer(),
                        item.getProduct().getSubCategory().getName(),
                        item.getProduct().getImages(),
                        item.getQuantity(),
                        item.getTotal()
                )).collect(Collectors.toList());

        return new CartResponse(cart.getId(), cart.getMarket() != null ? cart.getMarket().getId() : null, cartItems, cart.getTotalPrice()); // جلب `totalPrice`
    }

}
