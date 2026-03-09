package com.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

@RestController
public class AuthController {

private final String CLIENT_ID = System.getenv("CLIENT_ID");
private final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
private final String REDIRECT_URI = System.getenv("REDIRECT_URI");

    @Autowired
    UserStore store;

    @GetMapping("/login")
    public String login() {
        String url = "https://discord.com/api/oauth2/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=identify";
        return url;
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code, HttpSession session) {
        try {
            RestTemplate rest = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String body =
                    "client_id=" + CLIENT_ID +
                    "&client_secret=" + CLIENT_SECRET +
                    "&grant_type=authorization_code" +
                    "&code=" + code +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> tokenResp = rest.postForEntity("https://discord.com/api/oauth2/token", entity, Map.class);

            String accessToken = (String) tokenResp.getBody().get("access_token");

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> userEntity = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResp = rest.exchange(
                    "https://discord.com/api/users/@me",
                    HttpMethod.GET,
                    userEntity,
                    Map.class
            );

            Map userData = userResp.getBody();
            String id = (String) userData.get("id");
            String username = (String) userData.get("username");
            String avatar = (String) userData.get("avatar");

            // Save in memory
            store.users.putIfAbsent(id, new User(id, username, avatar));

            // Save userId in session
            session.setAttribute("userId", id);

            // Redirect to main page (no user ID in URL)
            HttpHeaders redirectHeaders = new HttpHeaders();
            redirectHeaders.setLocation(java.net.URI.create("/"));
            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}