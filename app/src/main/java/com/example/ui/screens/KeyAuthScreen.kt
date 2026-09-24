package com.example.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.KeyTier
import com.example.data.auth.KeyValidationResult
import com.example.ui.components.AbhishekAnimatedBanner
import com.example.ui.components.AbhishekSignature
import com.example.ui.theme.CardBorderGradient
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCardGradient
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonPink
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.T1GoldGradient
import com.example.ui.theme.T1Yellow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.GoogleSignInHelper
import com.example.util.NetworkState
import kotlinx.coroutines.launch

@Composable
fun KeyAuthScreen(
    networkState: NetworkState,
    errorMessage: String? = null,
    validatedKeyInfo: KeyValidationResult.KeyVerified? = null,
    onSubmitKey: (key: String) -> Unit,
    onDirectGoogleLogin: (email: String) -> Unit,
    onRegisterWithGoogle: (email: String, username: String, key: String, tier: KeyTier) -> Unit,
    onLoginWithGoogle: (key: String, userName: String, tier: KeyTier) -> Unit,
    onLoginAsGuest: (key: String, guestName: String, tier: KeyTier) -> Unit,
    onClearKey: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var keyInput by remember { mutableStateOf("") }
    var isSigningInGoogle by remember { mutableStateOf(false) }

    // Mode flag: whether the Google Account Picker was clicked for direct login or key registration
    var isDirectGoogleLoginMode by remember { mutableStateOf(false) }

    // Dialog to choose username after picking Google account during registration
    var showChooseUsernameDialog by remember { mutableStateOf(false) }
    var pendingGoogleEmail by remember { mutableStateOf("") }
    var chosenUsernameInput by remember { mutableStateOf("") }

    // Native Android System Google Account Picker Launcher
    // Displays ALL Google accounts logged into this Android phone in an official Android dialog
    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                if (isDirectGoogleLoginMode) {
                    onDirectGoogleLogin(accountName)
                } else {
                    val defaultName = accountName.substringBefore("@")
                        .replace(".", " ")
                        .replace("_", " ")
                        .split(" ")
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { word -> word.replaceFirstChar { c -> c.uppercase() } }
                        .ifBlank { "Pro Player" }
                    pendingGoogleEmail = accountName
                    chosenUsernameInput = defaultName
                    showChooseUsernameDialog = true
                }
            }
        }
    }

    // Fallback dialog for Google sign in if Google Play Services / credentials aren't linked
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var manualGoogleName by remember { mutableStateOf("") }
    var manualGoogleEmail by remember { mutableStateOf("") }

    // Guest name customization dialog
    var showGuestNameDialog by remember { mutableStateOf(false) }
    var guestCustomName by remember { mutableStateOf("") }

    // Fallback Google Account dialog (Ensures user is never blocked if Google Play Services lacks web client id)
    if (showGoogleAccountDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountDialog = false },
            containerColor = Color(0xFF10131E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleGLogo(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDirectGoogleLoginMode) "GOOGLE SIGN IN" else "GOOGLE ACCOUNT DETAILS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isDirectGoogleLoginMode) 
                            "Enter your registered Gmail address to sign in directly:"
                        else 
                            "Enter your Google account details to link your license to this account:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = manualGoogleEmail,
                        onValueChange = { manualGoogleEmail = it },
                        label = { Text("GMAIL ADDRESS", color = T1Yellow, fontSize = 11.sp) },
                        placeholder = { Text("e.g. player@gmail.com", color = TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = T1Yellow,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isDirectGoogleLoginMode) {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = manualGoogleName,
                            onValueChange = { manualGoogleName = it },
                            label = { Text("GOOGLE USERNAME / NAME", color = CyberCyan, fontSize = 11.sp) },
                            placeholder = { Text("e.g. Abhishek Pro", color = TextSecondary.copy(alpha = 0.5f)) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val emailToUse = manualGoogleEmail.trim().lowercase(java.util.Locale.ROOT)
                        val nameToUse = manualGoogleName.trim().ifBlank {
                            emailToUse.substringBefore("@").ifBlank { "Google Player" }
                        }
                        showGoogleAccountDialog = false
                        if (isDirectGoogleLoginMode) {
                            onDirectGoogleLogin(emailToUse)
                        } else if (validatedKeyInfo != null) {
                            onRegisterWithGoogle(emailToUse, nameToUse, validatedKeyInfo.key, validatedKeyInfo.tier)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SIGN IN AS GOOGLE USER", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAccountDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    // Username prompt dialog after selecting Google account during registration
    if (showChooseUsernameDialog && validatedKeyInfo != null) {
        AlertDialog(
            onDismissRequest = { showChooseUsernameDialog = false },
            containerColor = Color(0xFF10131E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleGLogo(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SET YOUR USERNAME",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Linked Google Account:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = pendingGoogleEmail,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = chosenUsernameInput,
                        onValueChange = { chosenUsernameInput = it },
                        label = { Text("CHOOSE YOUR USERNAME", color = T1Yellow, fontSize = 11.sp) },
                        placeholder = { Text("e.g. Abhishek OP, Viper, Ghost", color = TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = T1Yellow,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your profile will be permanently saved to this Google account. Next time, simply tap 'Sign in with Google' to login directly without entering a key!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalUser = chosenUsernameInput.trim().ifBlank {
                            pendingGoogleEmail.substringBefore("@").ifBlank { "Pro Player" }
                        }
                        showChooseUsernameDialog = false
                        onRegisterWithGoogle(
                            pendingGoogleEmail,
                            finalUser,
                            validatedKeyInfo.key,
                            validatedKeyInfo.tier
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = T1Yellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SAVE & ENTER APP", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChooseUsernameDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    // Guest Name Dialog
    if (showGuestNameDialog && validatedKeyInfo != null) {
        AlertDialog(
            onDismissRequest = { showGuestNameDialog = false },
            containerColor = Color(0xFF10131E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY AS GUEST",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CyberCyan,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter a nickname for your guest profile, or leave blank for a generated name:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = guestCustomName,
                        onValueChange = { guestCustomName = it },
                        label = { Text("GUEST NICKNAME (OPTIONAL)", color = CyberCyan, fontSize = 11.sp) },
                        placeholder = { Text("e.g. Guest Pro, Shadow, Viper", color = TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = guestCustomName.trim().ifBlank { "Guest_${(100..999).random()}" }
                        showGuestNameDialog = false
                        onLoginAsGuest(validatedKeyInfo.key, finalName, validatedKeyInfo.tier)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("START PLAYING", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestNameDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("key_auth_screen"),
        containerColor = CyberBlack,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==================== TOP HERO SECTION ====================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Cyber Animated Shield Logo
                CyberAnimatedShieldLogo()

                Spacer(modifier = Modifier.height(14.dp))

                // Title Banner
                Text(
                    text = "T1 ESPORTS HUB",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        brush = T1GoldGradient,
                        letterSpacing = 2.sp
                    )
                )

                Text(
                    text = "V1.0 PRO SENSITIVITY & SQUAD CALLS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyberCyan,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                AbhishekAnimatedBanner()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==================== CENTER AUTH CARD ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardGradient)
                    .border(1.5.dp, CardBorderGradient, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    if (validatedKeyInfo == null) {
                        // ========================================================
                        // STEP 1: ENTER KEY (OR ADMIN PASSCODE)
                        // ========================================================
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = T1Yellow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENTER ACCESS KEY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp
                                )
                            )
                        }

                        Text(
                            text = "Enter your Activation Key or Administrator Passcode to unlock the app.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        // Key Input Field with Paste Icon
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = {
                                Text(
                                    "ACCESS KEY / PASSCODE",
                                    color = T1Yellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            placeholder = {
                                Text(
                                    "e.g. T1-PRO-555 or Passcode",
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = T1Yellow
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            val pasted = clip.getItemAt(0).text?.toString() ?: ""
                                            if (pasted.isNotBlank()) {
                                                keyInput = pasted.trim()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste Key",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("key_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (keyInput.isNotBlank()) {
                                        onSubmitKey(keyInput.trim())
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = T1Yellow,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = T1Yellow,
                                focusedContainerColor = Color(0xFF0F121C),
                                unfocusedContainerColor = Color(0xFF0F121C)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                keyboardController?.hide()
                                onSubmitKey(keyInput.trim())
                            },
                            enabled = keyInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_key_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = T1Yellow,
                                contentColor = Color.Black,
                                disabledContainerColor = DarkBorder,
                                disabledContentColor = TextSecondary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VERIFY KEY & PROCEED",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // OR SIGN IN WITH GOOGLE DIVIDER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(DarkBorder))
                            Text(
                                text = "  OR SIGN IN WITH GOOGLE  ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(DarkBorder))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Direct Google Sign In Button
                        Button(
                            onClick = {
                                isDirectGoogleLoginMode = true
                                try {
                                    val chooseAccountIntent = AccountManager.newChooseAccountIntent(
                                        null,
                                        null,
                                        arrayOf("com.google"),
                                        null,
                                        null,
                                        null,
                                        null
                                    )
                                    googleAccountPickerLauncher.launch(chooseAccountIntent)
                                } catch (e: ActivityNotFoundException) {
                                    isSigningInGoogle = true
                                    coroutineScope.launch {
                                        val res = GoogleSignInHelper.signInWithGoogle(context)
                                        isSigningInGoogle = false
                                        res.onSuccess { googleUser ->
                                            val email = googleUser.email.ifBlank { googleUser.displayName }
                                            onDirectGoogleLogin(email)
                                        }.onFailure {
                                            showGoogleAccountDialog = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    showGoogleAccountDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("direct_google_login_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                GoogleGLogo(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign in with Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1F1F1F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Already registered with Google? Tap above to login directly without entering key.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Admin Note: Admin passcode '111' unlocks Admin Panel directly.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        )

                    } else {
                        // ========================================================
                        // STEP 2: KEY VERIFIED -> GOOGLE LOGIN & GUEST LOGIN
                        // ========================================================
                        val tierColor = when (validatedKeyInfo.tier) {
                            KeyTier.BASIC -> CyberCyan
                            KeyTier.PRO -> T1Yellow
                            KeyTier.ADMIN -> NeonPink
                        }

                        // Verified Badge Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0A1B16))
                                .border(1.5.dp, StatusGreen, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "KEY VERIFIED SUCCESSFULLY",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = StatusGreen,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = validatedKeyInfo.key,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(tierColor.copy(alpha = 0.25f))
                                            .border(1.dp, tierColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "[${validatedKeyInfo.tier.name} TIER]",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = tierColor,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Choose your login method to start playing:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // ==================== 1. GOOGLE LOGIN BUTTON ====================
                        Button(
                            onClick = {
                                if (isSigningInGoogle) return@Button
                                isDirectGoogleLoginMode = false
                                try {
                                    // Launch Android's official system Google Account Picker
                                    // Shows all Google accounts currently logged into this device!
                                    val chooseAccountIntent = AccountManager.newChooseAccountIntent(
                                        null,
                                        null,
                                        arrayOf("com.google"),
                                        null,
                                        null,
                                        null,
                                        null
                                    )
                                    googleAccountPickerLauncher.launch(chooseAccountIntent)
                                } catch (e: ActivityNotFoundException) {
                                    // Fallback to Credential Manager if account picker intent isn't handled
                                    isSigningInGoogle = true
                                    coroutineScope.launch {
                                        val res = GoogleSignInHelper.signInWithGoogle(context)
                                        isSigningInGoogle = false
                                        res.onSuccess { googleUser ->
                                            pendingGoogleEmail = googleUser.email.ifBlank { "google_user@gmail.com" }
                                            chosenUsernameInput = googleUser.displayName.ifBlank { "Pro Player" }
                                            showChooseUsernameDialog = true
                                        }.onFailure {
                                            showGoogleAccountDialog = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    showGoogleAccountDialog = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("google_login_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F1F1F)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            if (isSigningInGoogle) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Connecting to Google...", color = Color.Black, fontWeight = FontWeight.Bold)
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GoogleGLogo(modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ==================== 2. CONTINUE AS GUEST BUTTON ====================
                        // Directly beneath the Google Login button as requested
                        Button(
                            onClick = {
                                showGuestNameDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("continue_as_guest_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF162132),
                                contentColor = CyberCyan
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsEsports,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🎮 Continue as Guest",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Back / Change Key Button
                        TextButton(
                            onClick = onClearKey,
                            modifier = Modifier.testTag("change_key_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enter a different key", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    // Error Message Banner
                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusRed.copy(alpha = 0.15f))
                                    .border(1.dp, StatusRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = StatusRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = StatusRed,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Offline Warning Banner
                    AnimatedVisibility(visible = !networkState.isConnected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2A1C08))
                                .border(1.dp, T1Yellow.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = "Offline",
                                    tint = T1Yellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Internet required to activate and authenticate licenses.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = T1Yellow,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ==================== BOTTOM BRANDING & DEVELOPER ====================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                AbhishekSignature(isProminent = true)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Need a Key? Contact Abhishek on Instagram or Telegram",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * High-definition Vector Canvas Google "G" Logo
 */
@Composable
private fun GoogleGLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.22f
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)
        val blue = Color(0xFF4285F4)

        // Arc Blue (top right)
        drawArc(
            color = blue,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )

        // Arc Green (bottom right to bottom)
        drawArc(
            color = green,
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )

        // Arc Yellow (bottom left)
        drawArc(
            color = yellow,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )

        // Arc Red (top left)
        drawArc(
            color = red,
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Square)
        )

        // Horizontal bar of the "G"
        drawLine(
            color = blue,
            start = Offset(center.x, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}

@Composable
private fun CyberAnimatedShieldLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_effects")
    val logoGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    val rotateRings by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_rings"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(110.dp)
    ) {
        // Outer rotating decorative ring
        Box(
            modifier = Modifier
                .size(108.dp)
                .graphicsLayer { rotationZ = rotateRings }
                .border(
                    width = 1.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(T1Yellow, CyberCyan, NeonPink, T1Yellow)
                    ),
                    shape = CircleShape
                )
        )

        // Middle pulsing glow
        Box(
            modifier = Modifier
                .size(92.dp)
                .graphicsLayer {
                    scaleX = logoGlowScale
                    scaleY = logoGlowScale
                    alpha = pulseAlpha
                }
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            T1Yellow.copy(alpha = 0.35f),
                            CyberCyan.copy(alpha = 0.15f),
                            Color.Black
                        )
                    )
                )
                .border(2.dp, T1Yellow, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.t1_logo),
                contentDescription = "T1 Esports Logo",
                modifier = Modifier.size(68.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
