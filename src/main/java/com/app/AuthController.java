package com.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import jakarta.servlet.http.HttpSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class AuthController {

    private final String CLIENT_ID = System.getenv("CLIENT_ID");
    private final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private final String REDIRECT_URI = System.getenv("REDIRECT_URI");

    @Autowired
    UserStore store;

    // Generate Discord login URL
    @GetMapping("/login")
    public String login() {
        String url = "https://discord.com/api/oauth2/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=identify";

        return url;
    }

    // OAuth callback
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code, HttpSession session) {

        try {

            RestTemplate rest = new RestTemplate();

            // Request access token from Discord
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", CLIENT_ID);
            params.add("client_secret", CLIENT_SECRET);
            params.add("grant_type", "authorization_code");
            params.add("code", code);
            params.add("redirect_uri", REDIRECT_URI);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(params, headers);

            ResponseEntity<Map> tokenResp = rest.postForEntity(
                    "https://discord.com/api/oauth2/token",
                    request,
                    Map.class
            );

            String accessToken = (String) tokenResp.getBody().get("access_token");

            // Request user info
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> userReq = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResp = rest.exchange(
                    "https://discord.com/api/users/@me",
                    HttpMethod.GET,
                    userReq,
                    Map.class
            );

            Map userData = userResp.getBody();

            String id = (String) userData.get("id");
            String username = (String) userData.get("username");
            String avatar = (String) userData.get("avatar");

            // Save user in memory
            store.users.putIfAbsent(id, new User(id, username, avatar));

            // Save login session
            session.setAttribute("userId", id);

            // Redirect to homepage
            HttpHeaders redirectHeaders = new HttpHeaders();
            redirectHeaders.setLocation(java.net.URI.create("/"));

            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OAuth Error: " + e.getMessage());
        }
    }

}