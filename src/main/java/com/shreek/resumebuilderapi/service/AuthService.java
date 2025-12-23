package com.shreek.resumebuilderapi.service;

import com.shreek.resumebuilderapi.document.User;
import com.shreek.resumebuilderapi.dto.AuthResponse;
import com.shreek.resumebuilderapi.dto.LoginRequest;
import com.shreek.resumebuilderapi.dto.RegisterRequest;
import com.shreek.resumebuilderapi.exception.ResourceExistsException;
import com.shreek.resumebuilderapi.repository.UserRepository;
import com.shreek.resumebuilderapi.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;

    public AuthResponse register(RegisterRequest request){
        log.info("Inside AuthService : register() {}", request);

        if(userRepository.existsByEmail(request.getEmail())){
            throw new ResourceExistsException("User aldredy Exists");
        }

        User newUser =toDocument(request);
        userRepository.save(newUser);

        sendVerificationEmail(newUser);

        return toResponse(newUser);

    }

    private void sendVerificationEmail(User user) {

        log.info("Inside AuthService sendVerificationEmail(): {}", user.getEmail());

        String verificationLink =
                appBaseUrl + "/api/auth/verify-email?token=" + user.getVerificationToken();

        String html =
                "<!DOCTYPE html>"
                        + "<html>"
                        + "<body style='margin:0;padding:0;background:#f4f6f8;'>"

                        + "<table width='100%' cellpadding='0' cellspacing='0' style='padding:20px;'>"
                        + "<tr><td align='center'>"

                        + "<table style='max-width:600px;background:#ffffff;border-radius:10px;"
                        + "box-shadow:0 4px 12px rgba(0,0,0,0.08);font-family:Arial;'>"

                        + "<tr><td style='padding:24px;background:#0d6efd;color:#fff;"
                        + "text-align:center;border-radius:10px 10px 0 0;'>"
                        + "<h2>CTRL + CV</h2>"
                        + "</td></tr>"

                        + "<tr><td style='padding:30px;color:#333;font-size:14px;'>"
                        + "<p>Hello <b>" + user.getName() + "</b>,</p>"
                        + "<p>Please verify your email address to activate your account.</p>"

                        + "<div style='text-align:center;margin:30px 0;'>"
                        + "<a href='" + verificationLink + "' "
                        + "style='background:#0d6efd;color:#fff;padding:12px 24px;"
                        + "text-decoration:none;border-radius:6px;'>"
                        + "Verify Email</a>"
                        + "</div>"

                        + "<p style='font-size:12px;color:#777;'>"
                        + "This link expires in 24 hours.</p>"
                        + "</td></tr>"

                        + "<tr><td style='padding:16px;text-align:center;font-size:12px;"
                        + "color:#999;background:#fafafa;border-radius:0 0 10px 10px;'>"
                        + "© 2025 CTRL + CV</td></tr>"

                        + "</table></td></tr></table>"
                        + "</body></html>";

        emailService.sendEmail(
                user.getEmail(),
                "Verify your email",
                html
        );
    }


    private AuthResponse toResponse(User newUser){
        return AuthResponse.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .profileImageUrl(newUser.getProfileImageUrl())
                .emailVerified(newUser.isEmailVerified())
                .subscriptionPlan(newUser.getSubscriptionPlan())
                .createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt())
                .build();
    }

    private User toDocument(RegisterRequest request){
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }

    public void verifyEmail(String token){
        log.info("Inside AuthService: verifyEmail(): {}", token);
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(()-> new RuntimeException("Invalid or Expired Verification token "));

        if(user.getVerificationExpires() !=null && user.getVerificationExpires().isBefore(LocalDateTime.now()) ){
            throw new RuntimeException("Verification token has expired. Please request another one");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);
        userRepository.save(user);

    }
    public AuthResponse login(LoginRequest request){
        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())){
            throw new UsernameNotFoundException("Invalid email or password");
        }

        if(!existingUser.isEmailVerified()){
            throw new RuntimeException("Please verify your email before logging in");
        }
        String token = jwtUtil.generateToken(existingUser.getId());

        AuthResponse response = toResponse(existingUser);
        response.setToken(token);
        return response;
    }

    public void resendVerification(String email) {
        //step 1: Fetch the user account by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //step 2: Check the email is Verified
        if(user.isEmailVerified()){
            throw new RuntimeException("Email is aldredy Verified");
        }

        //step 3: Set the new Verification token and expires time
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationExpires(LocalDateTime.now().plusHours(24));

        //step 4: Update the user
        userRepository.save(user);

        //step 5: resend the verification email
        sendVerificationEmail(user);
    }

    public AuthResponse getProfile(Object principalObject) {
        User existingUser = (User) principalObject;
        return toResponse(existingUser);
    }
}
