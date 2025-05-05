package com.reljicd.controller;

import com.reljicd.exception.NotEnoughProductsInStockException;
import com.reljicd.service.ProductService;
import com.reljicd.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.client.RestTemplate;
import com.reljicd.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;

@Controller
public class ShoppingCartController {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    private final ShoppingCartService shoppingCartService;

    private final ProductService productService;
    private final RestTemplate restTemplate;

    @Autowired
    public ShoppingCartController(ShoppingCartService shoppingCartService, ProductService productService, RestTemplate restTemplate) {
        this.shoppingCartService = shoppingCartService;
        this.productService = productService;
        this.restTemplate = restTemplate;
    }



    @GetMapping("/shoppingCart")
    public ModelAndView shoppingCart() {
        ModelAndView modelAndView = new ModelAndView("/shoppingCart");
        modelAndView.addObject("products", shoppingCartService.getProductsInCart());
        modelAndView.addObject("total", shoppingCartService.getTotal().toString());
        return modelAndView;
    }

    @GetMapping("/shoppingCart/addProduct/{productId}")
    public ModelAndView addProductToCart(@PathVariable("productId") Long productId) {
        productService.findById(productId).ifPresent(shoppingCartService::addProduct);
        return shoppingCart();
    }

    @GetMapping("/shoppingCart/removeProduct/{productId}")
    public ModelAndView removeProductFromCart(@PathVariable("productId") Long productId) {
        productService.findById(productId).ifPresent(shoppingCartService::removeProduct);
        return shoppingCart();
    }

    @GetMapping("/shoppingCart/checkout")
    public ModelAndView checkout() {
        try {
            Double totalAmount = shoppingCartService.getTotal().doubleValue();  
            if (totalAmount == 0) {
                return shoppingCart().addObject("paymentError", "Empty cart.");
            }
            String url = "http://localhost:8000/process_payment";
            String paymentResponse = restTemplate.postForObject(paymentServiceUrl, 
                new PaymentRequest(totalAmount, "USD"), String.class);
            // ObjectMapper objectMapper = new ObjectMapper();
            // JsonNode responseJson = objectMapper.readTree(paymentResponse);
            // if ("success".equals(responseJson.get("status").asText())) {
            //     // Gán giá trị của message cho paymentResponse
            //     paymentResponse = responseJson.get("message").asText();
            // }
            int successIndex = paymentResponse.indexOf("success");
            if (successIndex != -1) {
                // Tìm vị trí của dấu ":" sau "success"
                int messageStartIndex = paymentResponse.indexOf("\"message\":\"") + 10;
                
                // Lấy phần thông điệp từ chuỗi, từ vị trí messageStartIndex đến hết chuỗi
                paymentResponse = paymentResponse.substring(messageStartIndex+1, paymentResponse.length() - 2);
                
                
            }
            shoppingCartService.checkout();
            return shoppingCart().addObject("paymentResponse", paymentResponse);
        } catch (NotEnoughProductsInStockException e) {
            return shoppingCart().addObject("outOfStockMessage", e.getMessage());
        } catch (Exception e) {
            return shoppingCart().addObject("paymentError", e.getMessage());
        }
        
    }
}
