package com.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class CoinController {

    @Autowired
    UserStore store;

    // Add a coin for the logged-in user
    @PostMapping("/addCoin")
    public User addCoin(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return null;

        User user = store.users.get(userId);
        if (user != null) {
            user.coins++;
        }
        return user;
    }

    // Return all users (cached avatars only, no Discord API calls)
    @GetMapping("/users")
    public Collection<User> getUsers() {
        return store.users.values();
    }
}