package com.familiemunshi.service;

import com.familiemunshi.common.utils.Util;
import com.familiemunshi.http.dtos.requests.ActivateBranchAdminRequest;
import com.familiemunshi.http.validations.ValidationUtil;
import com.familiemunshi.jpa.daos.UserDao;
import com.familiemunshi.jpa.repositories.UserBranchRoleRepository;
import com.familiemunshi.jpa.repositories.UserRepository;
import com.familiemunshi.mapper.UserMapper;
import com.familiemunshi.service.entities.User;
import com.familiemunshi.service.enums.RoleScope;
import com.familiemunshi.service.exceptions.InvalidCredentialsException;
import com.familiemunshi.service.exceptions.UserEmailAlreadyExistsException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String BRANCH_ADMIN_ROLE =
            "BRANCH_ADMIN";
    private final UserRepository userRepository;
    private final UserBranchRoleRepository userBranchRoleRepository;
    private final UserService userService;

    // SIGNUP
    public User signup(User user) {

        String normalizedEmail = normalizeEmail(user.email());

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserEmailAlreadyExistsException(normalizedEmail);
        }

        // Hash the password with a salt
        String salt = Util.generateSalt();
        String hashedPassword = Util.hashPassword(user.password(), salt);
        // Create a new User object with the hashed password
        User hashedUser = user
                .withEmail(normalizedEmail)
                .withPassword(hashedPassword);

        UserDao userDao = UserMapper.toDao(hashedUser, salt);

        // Save the user to the database
        UserDao savedUser = userRepository.save(userDao);

        // Return the saved user as a User entity
        return UserMapper.toEntity(savedUser);
    }

    // LOGIN
    public User login(User user) {
        String normalizedEmail = normalizeEmail(user.email());

        UserDao userDao = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        String hashedPassword = Util.hashPassword(user.password(), userDao.getSalt());

        UserDao verifiedUser = userRepository
                .findByEmailAndPassword(userDao.getEmail(), hashedPassword)
                .orElseThrow(InvalidCredentialsException::new);

        return userService.getUser(verifiedUser.getId());
    }
    // LOGOUT
    public void logout() {
        // Implementation for logout
    }
    @Transactional
    public User activateBranchAdmin(
            ActivateBranchAdminRequest request
    ) {

        // 3.1 NORMALIZE EMAIL

        String normalizedEmail =
                normalizeEmail(request.email());

        // 3.2 FIND USER

        UserDao admin = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        // 3.3 ACCOUNT MUST BE INACTIVE

        if (admin.isActive()) {
            throw new InvalidCredentialsException();
        }

        // 3.4 VERIFY BRANCH ADMIN ROLE

        boolean isBranchAdmin =
                userBranchRoleRepository
                        .hasPendingBranchAdminAssignment(
                                admin.getId(),
                                BRANCH_ADMIN_ROLE,
                                RoleScope.BRANCH
                        );

        if (!isBranchAdmin) {
            throw new InvalidCredentialsException();
        }

        // 3.5 VERIFY OLD PASSWORD

        boolean validOldPassword =
                Util.verifyPassword(
                        request.oldPassword(),
                        admin.getPassword()
                );

        if (!validOldPassword) {
            throw new InvalidCredentialsException();
        }

        // 3.6 VALIDATE NEW PASSWORD

        ValidationUtil.validateNewBranchAdminPassword(
                request.newPassword(),
                admin.getPassword()
        );

        // 3.7 GENERATE NEW SALT AND HASH

        String newSalt = Util.generateSalt();

        String newHashedPassword = Util.hashPassword(
                request.newPassword(),
                newSalt
        );

        // 3.8 UPDATE CREDENTIALS

        admin.setSalt(newSalt);
        admin.setPassword(newHashedPassword);

        // 3.9 ACTIVATE USER ACCOUNT

        admin.setActive(true);

        // 3.10 SAVE ALL CHANGES

        UserDao savedAdmin =
                userRepository.saveAndFlush(admin);

        log.info(
                "Branch Admin activated: userId={}",
                savedAdmin.getId()
        );

        return UserMapper.toEntity(savedAdmin);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
