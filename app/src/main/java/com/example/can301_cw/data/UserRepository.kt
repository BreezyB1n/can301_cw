package com.example.can301_cw.data

import com.example.can301_cw.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.Date

/**
 * Repository for User operations, including registration and authentication logic.
 */
class UserRepository(private val userDao: UserDao) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /**
     * Registration result sealed class
     */
    sealed class RegisterResult {
        data class Success(val user: User) : RegisterResult()
        data class Error(val message: String) : RegisterResult()
    }

    /**
     * Login result sealed class
     */
    sealed class LoginResult {
        data class Success(val user: User) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    /**
     * Register a new user
     * @param username Username (must be unique)
     * @param email Email address (must be unique)
     * @param password Plain text password (will be hashed before storing)
     * @return RegisterResult indicating success or failure
     */
    suspend fun register(
        username: String,
        email: String,
        password: String
    ): RegisterResult {
        // Validate input
        val validationError = validateRegistrationInput(username, email, password)
        if (validationError != null) {
            return RegisterResult.Error(validationError)
        }

        // Check if username already exists
        if (userDao.isUsernameExists(username)) {
            return RegisterResult.Error("Username already exists")
        }

        // Check if email already exists
        if (userDao.isEmailExists(email)) {
            return RegisterResult.Error("Email already registered")
        }

        // Create user with hashed password
        val user = User(
            username = username.trim(),
            email = email.trim().lowercase(),
            password = hashPassword(password),
            createdAt = Date(),
            updatedAt = Date()
        )

        return try {
            userDao.insertUser(user)
            _currentUser.value = user
            RegisterResult.Success(user)
        } catch (e: Exception) {
            RegisterResult.Error("Registration failed: ${e.message}")
        }
    }

    /**
     * Login with username or email
     * @param account Username or email address
     * @param password Plain text password
     * @return LoginResult indicating success or failure
     */
    suspend fun login(
        account: String,
        password: String
    ): LoginResult {
        // Validate input
        if (account.isBlank()) {
            return LoginResult.Error("Username or email cannot be empty")
        }
        if (password.isBlank()) {
            return LoginResult.Error("Password cannot be empty")
        }

        // Find user by username or email
        val user = if (account.contains("@")) {
            userDao.getUserByEmail(account.trim().lowercase())
        } else {
            userDao.getUserByUsername(account.trim())
        }

        if (user == null) {
            return LoginResult.Error("User not found")
        }

        // Verify password
        if (!verifyPassword(password, user.password)) {
            return LoginResult.Error("Incorrect password")
        }

        _currentUser.value = user
        return LoginResult.Success(user)
    }

    fun logout() {
        _currentUser.value = null
    }

    /**
     * Validate registration input
     * @return Error message if validation fails, null if valid
     */
    private fun validateRegistrationInput(
        username: String,
        email: String,
        password: String
    ): String? {
        // Username validation
        if (username.isBlank()) {
            return "Username cannot be empty"
        }
        if (username.length < 3) {
            return "Username must be at least 3 characters"
        }
        if (username.length > 20) {
            return "Username cannot exceed 20 characters"
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            return "Username can only contain letters, numbers and underscores"
        }

        // Email validation
        if (email.isBlank()) {
            return "Email cannot be empty"
        }
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            return "Please enter a valid email address"
        }

        // Password validation
        if (password.isBlank()) {
            return "Password cannot be empty"
        }
        if (password.length < 6) {
            return "Password must be at least 6 characters"
        }
        if (password.length > 50) {
            return "Password cannot exceed 50 characters"
        }

        return null
    }

    /**
     * Hash password using SHA-256
     */
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verify password against stored hash
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }

    suspend fun getUserById(id: String): User? {
        return userDao.getUserById(id)
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun updateUser(user: User) {
        val updatedUser = user.copy(updatedAt = Date())
        userDao.updateUser(updatedUser)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun deleteUserById(id: String) {
        userDao.deleteUserById(id)
    }
}

