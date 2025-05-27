package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.User;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private Firestore firestore;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Try to find user by email first
            Query query = firestore.collection("users")
                    .whereEqualTo("email", username)
                    .limit(1);
            
            QuerySnapshot snapshot = query.get().get();
            if (!snapshot.isEmpty()) {
                User user = snapshot.getDocuments().get(0).toObject(User.class);
                return createUserDetails(user);
            }

            // If not found by email, try username
            query = firestore.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1);
            
            snapshot = query.get().get();
            if (!snapshot.isEmpty()) {
                User user = snapshot.getDocuments().get(0).toObject(User.class);
                return createUserDetails(user);
            }

            throw new UsernameNotFoundException("User not found with username/email: " + username);
        } catch (ExecutionException | InterruptedException e) {
            throw new UsernameNotFoundException("Error loading user", e);
        }
    }

    private UserDetails createUserDetails(User user) {
        String role = user.getRole();
        // If role already starts with ROLE_, use it as is, otherwise add the prefix
        String authorityRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(), // Using email as the principal
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(authorityRole.toUpperCase()))
        );
    }
} 