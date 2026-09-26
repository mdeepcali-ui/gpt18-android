package com.gptplus18.app.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom
import android.util.Base64

/**
 * Google Sign-In عبر Credential Manager
 * يعرض Bottom Sheet الأصلي بكل حسابات الجهاز (نفس أسلوب DeepSeek/ChatGPT)
 */
object GoogleAuthLauncher {

    private const val TAG = "GoogleAuthLauncher"

    // ⚠️ Web Client ID من Google Cloud Console
    private const val WEB_CLIENT_ID =
        "75349753112-0a5up0b347j08l0voebd1drrj76t3jpk.apps.googleusercontent.com"

    fun launch(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (idToken: String, email: String, displayName: String) -> Unit,
        onError: (String) -> Unit,
    ) {
        scope.launch(Dispatchers.Main) {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)  // كل الحسابات
                    .setAutoSelectEnabled(false)           // إظهار Picker دائماً
                    .setNonce(generateNonce())
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result: GetCredentialResponse =
                    credentialManager.getCredential(
                        request = request,
                        context = context,
                    )

                handleSignIn(result, onSuccess, onError)

            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "ألغى المستخدم")
                onError("__cancel__")
            } catch (e: NoCredentialException) {
                Log.e(TAG, "لا توجد حسابات Google على الجهاز", e)
                onError("لا توجد حسابات Google على هذا الجهاز")
            } catch (e: GetCredentialException) {
                Log.e(TAG, "فشل تسجيل الدخول: ${e.message}", e)
                onError("فشل تسجيل الدخول: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "خطأ غير متوقع: ${e.message}", e)
                onError("حدث خطأ غير متوقع")
            }
        }
    }

    private fun handleSignIn(
        result: GetCredentialResponse,
        onSuccess: (String, String, String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val credential = result.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val g = GoogleIdTokenCredential.createFrom(credential.data)
                onSuccess(g.idToken, g.id, g.displayName ?: "")
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "فشل تحليل Google ID Token", e)
                onError("فشل تحليل بيانات Google")
            }
        } else {
            onError("نوع بيانات الاعتماد غير مدعوم")
        }
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
