package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("T1 ESPORTS", appName)
  }

  @Test
  fun `sensitivity calculation is within valid 0 to 200 bounds`() {
    val profile = com.example.data.model.SensitivityProfile.calculateForDevice(
      refreshRateHz = 120,
      screenDpi = 440,
      ramGb = 8,
      manufacturer = "Samsung"
    )
    assert(profile.general in 0..200)
    assert(profile.redDot in 0..200)
    assert(profile.scope2x in 0..200)
    assert(profile.scope4x in 0..200)
    assert(profile.sniperScope in 0..200)
    assert(profile.freeLook in 0..200)
  }

  @Test
  fun `budget and flagship devices generate noticeably different sensitivities`() {
    val budgetProfile = com.example.data.model.SensitivityProfile.calculateForDevice(
      refreshRateHz = 60,
      screenDpi = 280,
      ramGb = 3,
      manufacturer = "Infinix",
      model = "Hot 10"
    )
    val flagshipProfile = com.example.data.model.SensitivityProfile.calculateForDevice(
      refreshRateHz = 144,
      screenDpi = 480,
      ramGb = 16,
      manufacturer = "Asus",
      model = "ROG Phone 7"
    )

    // Budget phone requires higher sensitivity (due to digitizer friction & 60Hz)
    assert(budgetProfile.general >= 165) { "Budget general was ${budgetProfile.general}" }

    // Flagship requires controlled, lower sensitivity to prevent crosshair overshoot
    assert(flagshipProfile.general <= 165) { "Flagship general was ${flagshipProfile.general}" }

    // Clearly distinct
    assert(budgetProfile.general > flagshipProfile.general) {
      "Expected variance between devices but got ${budgetProfile.general} vs ${flagshipProfile.general}"
    }
  }

  @Test
  fun `character skill database contains essential roles`() {
    val roles = com.example.data.model.CharacterSkillDatabase.combinations
    assert(roles.isNotEmpty())
    assert(roles.any { it.roleId == "rusher" })
    assert(roles.any { it.roleId == "sniper" })
  }

  @Test
  fun `uid inspector repository defaults to redx live checker`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = com.example.data.network.UidInspectorRepository(context)
    assertEquals("", repository.configuredEndpoint)
  }

  @Test
  fun `admin passcode 111 triggers AdminPanel result`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authRepository = com.example.data.auth.AuthRepository(context)
    val result = authRepository.validateKey("111", "AbhishekAdmin")
    assert(result is com.example.data.auth.KeyValidationResult.AdminPanel)
  }

  @Test
  fun `key lifecycle - generates tiered keys and enforces single use with PIN`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authRepository = com.example.data.auth.AuthRepository(context)

    // 1. Generate PRO Key
    val proKey = authRepository.generateNewKey("VIP Player 1", com.example.data.auth.KeyTier.PRO)
    assert(proKey.tier == com.example.data.auth.KeyTier.PRO)
    assert(proKey.key.startsWith("T1-PRO-"))

    // 2. First validation requires PIN setup
    val firstCheck = authRepository.validateKey(proKey.key, "Rahul Gamer")
    assert(firstCheck is com.example.data.auth.KeyValidationResult.RequirePinSetup)
    val pinSetup = firstCheck as com.example.data.auth.KeyValidationResult.RequirePinSetup
    assertEquals(com.example.data.auth.KeyTier.PRO, pinSetup.tier)

    // 3. User sets 4-digit PIN
    val activationSuccess = authRepository.activateKeyWithPin(proKey.key, "Rahul Gamer", pinSetup.tier, "7890")
    assert(activationSuccess)
    assert(authRepository.isUserLoggedIn())
    assertEquals(com.example.data.auth.KeyTier.PRO, authRepository.getUserTier())

    // 4. Single-use enforcement: Second user tries to use the same key -> MUST FAIL
    val secondCheck = authRepository.validateKey(proKey.key, "Hacker User")
    assert(secondCheck is com.example.data.auth.KeyValidationResult.Error)
    assert((secondCheck as com.example.data.auth.KeyValidationResult.Error).message.contains("already used"))

    // 5. Returning user logs in with 4-digit PIN
    val pinLogin = authRepository.loginWithPin("Rahul Gamer", "7890")
    assert(pinLogin is com.example.data.auth.KeyValidationResult.Success)

    // 6. Incorrect PIN fails
    val wrongPinLogin = authRepository.loginWithPin("Rahul Gamer", "0000")
    assert(wrongPinLogin is com.example.data.auth.KeyValidationResult.Error)
  }

  @Test
  fun `scenario skill database contains CS and BR scenarios`() {
    val scenarios = com.example.data.model.ScenarioSkillDatabase.scenarios
    assert(scenarios.isNotEmpty())
    assert(scenarios.any { it.id == "cs_rush" })
    assert(scenarios.any { it.id == "br_survival" })
  }

  @Test
  fun `AdminViewModel generates keys with value, type, and isUsed fields and validates user role`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val adminViewModel = com.example.ui.AdminViewModel(application)

    // 1. Generate basic key
    val basicKey = adminViewModel.generateKey("basic", "Test Basic")
    assertEquals("basic", basicKey.type)
    assert(basicKey.value.startsWith("T1-BASIC-"))
    assertEquals(false, basicKey.isUsed)

    // 2. Generate pro key
    val proKey = adminViewModel.generateKey("pro", "Test Pro")
    assertEquals("pro", proKey.type)
    assert(proKey.value.startsWith("T1-PRO-"))
    assertEquals(false, proKey.isUsed)

    // 3. Generate admin key
    val adminKey = adminViewModel.generateKey("admin", "Test Admin")
    assertEquals("admin", adminKey.type)
    assert(adminKey.value.startsWith("T1-ADMIN-"))
    assertEquals(false, adminKey.isUsed)

    // 4. Validate user role
    adminViewModel.validateUserRole(proKey.value)
    val state = adminViewModel.roleValidationState.value
    assert(state is com.example.ui.RoleValidationState.Valid)
    val validState = state as com.example.ui.RoleValidationState.Valid
    assertEquals(proKey.value, validState.key)
    assertEquals("pro", validState.role)
    assertEquals(false, validState.isUsed)

    // 5. Admin Passcode 111 role validation
    adminViewModel.validateUserRole("111")
    val adminState = adminViewModel.roleValidationState.value
    assert(adminState is com.example.ui.RoleValidationState.Valid)
    assertEquals("admin", (adminState as com.example.ui.RoleValidationState.Valid).role)
  }
}

