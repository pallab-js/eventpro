package com.eventpro.admin.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricAuthManager @Inject constructor(
    private val biometricManager: BiometricManager
) {
    sealed class AuthResult {
        data object Success : AuthResult()
        data class Error(val message: CharSequence) : AuthResult()
        data object NotAvailable : AuthResult()
    }

    val isAvailable: Boolean
        get() = biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS

    fun authenticate(
        activity: FragmentActivity,
        title: String = "App Access",
        subtitle: String = "Authenticate to access EventPro Admin",
        onResult: (AuthResult) -> Unit
    ) {
        if (!isAvailable) {
            onResult(AuthResult.NotAvailable)
            return
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDeviceCredentialAllowed(true)
            .build()

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(AuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        onResult(AuthResult.Error(errString))
                    }
                }

                override fun onAuthenticationFailed() {
                    onResult(AuthResult.Error("Authentication failed"))
                }
            }
        )

        prompt.authenticate(promptInfo)
    }
}
