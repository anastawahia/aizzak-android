package android.aizzak.com

import android.aizzak.com.data.auth.FirebaseAuthRepository
import android.aizzak.com.feature.auth.AuthRepository
import com.google.firebase.auth.FirebaseAuth

/** Shared with Firebase's process-wide instance, including pending credential cleanup. */
internal object AppContainer {
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(FirebaseAuth.getInstance())
    }
}
