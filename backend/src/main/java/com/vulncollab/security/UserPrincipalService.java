package com.vulncollab.security;

import com.vulncollab.user.User;
import com.vulncollab.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserPrincipalService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserPrincipalService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPrincipal loadUserByUsername(String publicId) {
        return loadByPublicId(publicId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public Optional<UserPrincipal> loadByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId)
                .filter(User::isEnabled)
                .map(UserPrincipal::from);
    }
}
