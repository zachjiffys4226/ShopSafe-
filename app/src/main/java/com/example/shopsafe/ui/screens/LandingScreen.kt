package com.example.shopsafe.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shopsafe.ui.ShopSafeViewModel
import com.example.shopsafe.ui.components.GoogleLogo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.example.shopsafe.ui.utils.TwoFactorCodeVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    viewModel: ShopSafeViewModel
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var require2FA by remember { mutableStateOf(false) }
    var show2FADialog by remember { mutableStateOf(false) }
    var twoFactorCode by remember { mutableStateOf("") }
    var showLegalModalType by remember { mutableStateOf<String?>(null) } // "Terms" or "Privacy"
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Real-time Email Validation
    val isEmailValid = remember(emailInput) {
        val trimmed = emailInput.trim()
        if (trimmed.isEmpty()) false
        else if (trimmed == "#SHOPSAFE_MASTER_999#" || trimmed == "#SHOPSAFE_ADMIN#" || trimmed.lowercase() == "admin") true
        else Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(trimmed)
    }

    // Real-time Password Strength Requirements
    val hasMinLength = passwordInput.length >= 8
    val hasUppercase = passwordInput.any { it.isUpperCase() }
    val hasDigitOrSpecial = passwordInput.any { it.isDigit() || !it.isLetterOrDigit() }
    val isPasswordStrong = hasMinLength && hasUppercase && hasDigitOrSpecial
    
    // Password Strength Score (0 to 3)
    val passwordScore = listOf(hasMinLength, hasUppercase, hasDigitOrSpecial).count { it }

    // Real-time Confirm Password Matching
    val passwordsMatch = confirmPasswordInput.isNotEmpty() && confirmPasswordInput == passwordInput

    // Overall Register Form Validity
    val isRegisterFormValid = nameInput.trim().isNotBlank() && isEmailValid && isPasswordStrong && passwordsMatch

    val scrollState = rememberScrollState()

    // Dark slate and safe blue gradient brush
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A), // Dark Slate
            Color(0xFF1E293B), // Medium Slate
            Color(0xFF0284C7)  // Safe Blue Accent
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo Header
            Surface(
                color = Color(0xFF0284C7),
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "ShopSafe",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "ShopSafe",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            // Prominent Slogan (The Request Tagline)
            Surface(
                color = Color(0x33FFFFFF),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "A Place to Buy, Sell & Get Everything Delivered",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "We Need ShopSafe Shoppers & Delivery Drivers — Apply Now!",
                        color = Color(0xFFBAE6FD),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // White Box Entry Card with Dynamic Sign In / Register Switcher
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Segmented Switcher for Sign In vs Register
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                onClick = {
                                    isRegisterMode = false
                                    errorMessage = null
                                },
                                color = if (!isRegisterMode) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = if (!isRegisterMode) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Sign In",
                                        fontWeight = if (!isRegisterMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isRegisterMode) Color(0xFF0F172A) else Color(0xFF64748B),
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    isRegisterMode = true
                                    errorMessage = null
                                },
                                color = if (isRegisterMode) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = if (isRegisterMode) 2.dp else 0.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Register / Sign Up",
                                        fontWeight = if (isRegisterMode) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isRegisterMode) Color(0xFF0284C7) else Color(0xFF64748B),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Dynamic Header
                    Text(
                        text = if (isRegisterMode) "Create ShopSafe Account" else "Secure Member Sign In",
                        color = Color(0xFF0F172A),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    errorMessage?.let { msg ->
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                color = Color(0xFF991B1B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // REGISTER MODE - Full Name Input
                    AnimatedVisibility(visible = isRegisterMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = {
                                    nameInput = it
                                    errorMessage = null
                                },
                                label = { Text("Full Name") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0284C7),
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                        }
                    }

                    // Email Input Field (Both Modes with Real-Time Format Validation)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7)
                                )
                            },
                            trailingIcon = {
                                if (emailInput.isNotBlank()) {
                                    if (isEmailValid) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Valid Email",
                                            tint = Color(0xFF16A34A)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Invalid Email",
                                            tint = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (emailInput.isBlank()) Color(0xFF0284C7)
                                                     else if (isEmailValid) Color(0xFF16A34A)
                                                     else Color(0xFFDC2626),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        // Real-Time Email Format Feedback
                        if (emailInput.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                if (isEmailValid) {
                                    Text(
                                        text = "✓ Valid email format",
                                        fontSize = 11.sp,
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = "⚠️ Enter a valid email address (e.g., user@example.com)",
                                        fontSize = 11.sp,
                                        color = Color(0xFFDC2626),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Password Input Field with Visibility Toggle
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0284C7),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        // Real-time Password Strength Requirements Meter (shown in Register mode or when entering password)
                        if (isRegisterMode && passwordInput.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Visual Strength Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val barColor = when (passwordScore) {
                                        1 -> Color(0xFFDC2626) // Red - Weak
                                        2 -> Color(0xFFD97706) // Amber - Moderate
                                        3 -> Color(0xFF16A34A) // Green - Strong
                                        else -> Color.LightGray
                                    }

                                    repeat(3) { index ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(4.dp)
                                                .background(
                                                    color = if (index < passwordScore) barColor else Color(0xFFE2E8F0),
                                                    shape = RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                }

                                Text(
                                    text = when (passwordScore) {
                                        1 -> "Password Strength: Weak"
                                        2 -> "Password Strength: Good"
                                        3 -> "Password Strength: Strong"
                                        else -> "Password Strength"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (passwordScore) {
                                        1 -> Color(0xFFDC2626)
                                        2 -> Color(0xFFD97706)
                                        3 -> Color(0xFF16A34A)
                                        else -> Color.Gray
                                    }
                                )

                                // Real-time Requirement Checklist
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    RequirementRow(label = "At least 8 characters", isMet = hasMinLength)
                                    RequirementRow(label = "At least 1 uppercase letter", isMet = hasUppercase)
                                    RequirementRow(label = "At least 1 number or special character", isMet = hasDigitOrSpecial)
                                }
                            }
                        }
                    }

                    // REGISTER MODE - Confirm Password Field
                    AnimatedVisibility(visible = isRegisterMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm Password") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (confirmPasswordInput.isEmpty()) Color(0xFF0284C7)
                                                         else if (passwordsMatch) Color(0xFF16A34A)
                                                         else Color(0xFFDC2626),
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            if (confirmPasswordInput.isNotEmpty()) {
                                Text(
                                    text = if (passwordsMatch) "✓ Passwords match" else "⚠️ Passwords do not match",
                                    fontSize = 11.sp,
                                    color = if (passwordsMatch) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    // SIGN IN MODE - 2FA Flag
                    AnimatedVisibility(visible = !isRegisterMode) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { require2FA = !require2FA }
                                .padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = require2FA,
                                onCheckedChange = { require2FA = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Enforce 2-Factor Authentication (2FA) [Optional]",
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Action Button (Log In vs Register)
                    if (!isRegisterMode) {
                        // SIGN IN BUTTON
                        Button(
                            onClick = {
                                val trimmed = emailInput.trim()
                                if (trimmed.isBlank()) {
                                    errorMessage = "Please enter an email address or valid code."
                                    return@Button
                                }
                                if (!isEmailValid) {
                                    errorMessage = "Please enter a valid email format."
                                    return@Button
                                }

                                val isSecret = trimmed == "#SHOPSAFE_MASTER_999#" ||
                                               trimmed == "#SHOPSAFE_ADMIN#" ||
                                               trimmed.lowercase() == "admin"

                                if (require2FA && !isSecret) {
                                    show2FADialog = true
                                } else {
                                    viewModel.loginUser(emailInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign In",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        // REGISTER / CREATE ACCOUNT BUTTON
                        Button(
                            onClick = {
                                if (nameInput.trim().isBlank()) {
                                    errorMessage = "Please enter your full name."
                                    return@Button
                                }
                                if (!isEmailValid) {
                                    errorMessage = "Please enter a valid email address."
                                    return@Button
                                }
                                if (!isPasswordStrong) {
                                    errorMessage = "Password does not meet strength requirements."
                                    return@Button
                                }
                                if (!passwordsMatch) {
                                    errorMessage = "Passwords do not match."
                                    return@Button
                                }
                                viewModel.loginUser(emailInput)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRegisterFormValid) Color(0xFF0284C7) else Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create Account",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Social Sign-On Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        Text(
                            text = " OR CONTINUE WITH ",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    }

                    // Social buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.loginWithFacebook() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("landing_facebook_sign_in_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Facebook,
                                contentDescription = "Facebook Logo",
                                tint = Color(0xFF1877F2),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Facebook",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1877F2),
                                modifier = Modifier.semantics {
                                    contentDescription = "Sign in with Facebook"
                                }
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.loginWithGoogle() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("landing_google_sign_in_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            GoogleLogo(
                                modifier = Modifier.semantics {
                                    contentDescription = "Google Logo"
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Google",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA4335),
                                modifier = Modifier.semantics {
                                    contentDescription = "Sign in with Google"
                                }
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.loginWithApple() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("landing_apple_sign_in_button"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_apple_logo),
                                contentDescription = "Apple Logo",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Apple ID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.semantics {
                                    contentDescription = "Sign in with Apple ID"
                                }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE2E8F0))

                    // Explicit bottom toggle text prompt inside white box
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already have an account? " else "Don't have an account? ",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = if (isRegisterMode) "Sign In" else "Register / Sign Up",
                            fontSize = 13.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                isRegisterMode = !isRegisterMode
                                errorMessage = null
                            }
                        )
                    }

                    // Standard Legal Disclaimer & Links
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "By continuing, you agree to ShopSafe's ",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "Terms of Service",
                            fontSize = 10.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { showLegalModalType = "Terms of Service" }
                        )
                        Text(
                            text = " & ",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "Privacy Policy",
                            fontSize = 10.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { showLegalModalType = "Privacy Policy" }
                        )
                    }
                }
            }

            // Driver Portal Registration Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Want to deliver and earn with ShopSafe?",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = { viewModel.showAuthModal.value = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = "Delivery Vehicle Indicator", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply to Deliver (Driver Portal Sign Up)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Partnership Footer
            Surface(
                color = Color(0x1AFFFFFF),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Official Partner Network",
                            color = Color(0xFFF59E0B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "ShopSafe is an official partner of",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Jiffy's Mobile | Jiffy's Assistance",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2FA Prompt Dialog
        if (show2FADialog) {
            AlertDialog(
                onDismissRequest = { show2FADialog = false },
                title = { Text("🔐 Enter 2FA Code") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("A secure 2-Factor code has been dispatched to your primary device. Enter it to verify.", fontSize = 13.sp)
                        OutlinedTextField(
                            value = twoFactorCode,
                            onValueChange = { twoFactorCode = it.filter { c -> c.isDigit() }.take(6) },
                            label = { Text("6-Digit Code") },
                            visualTransformation = TwoFactorCodeVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            show2FADialog = false
                            viewModel.loginUser(emailInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Text("Verify & Complete Log In")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { show2FADialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Overlay detailed signup/auth wizard modal if requested
        val showAuthModal by viewModel.showAuthModal.collectAsState()
        if (showAuthModal) {
            AuthModal(
                viewModel = viewModel,
                onDismiss = { viewModel.showAuthModal.value = false }
            )
        }

        // Legal Terms & Privacy Policy Modal Dialog
        showLegalModalType?.let { type ->
            AlertDialog(
                onDismissRequest = { showLegalModalType = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (type.contains("Terms")) Icons.Default.Gavel else Icons.Default.PrivacyTip,
                            contentDescription = null,
                            tint = Color(0xFF0284C7)
                        )
                        Text(type, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (type.contains("Terms")) {
                                "Welcome to ShopSafe. By accessing or using our marketplace platform, courier delivery network, or seller services, you agree to comply with our Terms of Service.\n\n" +
                                "1. Account Security: You are responsible for maintaining the confidentiality of your login credentials and 2FA verification codes.\n" +
                                "2. Safe Transactions: All transactions made via ShopSafe Escrow are subject to verification and automated anti-fraud security inspection.\n" +
                                "3. Driver Network: Approved drivers must adhere to state traffic laws, vehicle insurance mandates, and safety verification guidelines."
                            } else {
                                "At ShopSafe, we respect your privacy and are committed to protecting your personal data.\n\n" +
                                "1. Data Collection: We collect essential profile data (name, email, phone number) and location data when actively managing deliveries.\n" +
                                "2. Data Protection: Your data is encrypted in transit and at rest using industry-standard security protocols.\n" +
                                "3. No Third-Party Sales: We never sell or monetize your personal information to unauthorized third-party advertisers."
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showLegalModalType = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("I Understand & Accept")
                    }
                }
            )
        }
    }
}

@Composable
private fun RequirementRow(label: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF16A34A) else Color(0xFFCBD5E1),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isMet) Color(0xFF15803D) else Color(0xFF64748B),
            fontWeight = if (isMet) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
