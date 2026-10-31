package android.aizzak.com

import android.aizzak.com.data.auth.FirebaseAuthRepository
import android.aizzak.com.data.session.HttpAccountContextRepository
import android.aizzak.com.feature.auth.AccountContextRepository
import android.aizzak.com.feature.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth

/** Shared with Firebase's process-wide instance, including pending credential cleanup. */
internal object AppContainer {
    private val firebaseRepository by lazy {
        FirebaseAuthRepository(FirebaseAuth.getInstance())
    }
    val authRepository: AuthRepository get() = firebaseRepository
    val accountContextRepository: AccountContextRepository by lazy {
        HttpAccountContextRepository.create(BuildConfig.API_BASE_URL, firebaseRepository)
    }
}
