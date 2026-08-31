package com.isoffice.kakushito

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Firebase / `/api/me` から見た現在の認証状態。 */
sealed interface AuthState {
    /** 初期化中・サインイン処理中・アカウント読み込み中。 */
    data object Loading : AuthState

    /** 未ログイン。 */
    data object SignedOut : AuthState

    /** ログイン済み。`/api/me` で取得したユーザーを保持する。 */
    data class SignedIn(val user: User) : AuthState

    /** サインインまたは `/api/me` 取得に失敗。 */
    data class Error(val message: String) : AuthState
}

/**
 * アプリ全体で共有する現在のユーザー / 認証状態のホルダー。
 *
 * `state` は Compose の snapshot state なので、Composable 内で読むと
 * 変化時に再コンポーズされる。
 */
object CurrentUser {
    var state: AuthState by mutableStateOf(AuthState.Loading)
        private set

    val user: User? get() = (state as? AuthState.SignedIn)?.user
    val isSignedIn: Boolean get() = state is AuthState.SignedIn

    fun setLoading() {
        state = AuthState.Loading
    }

    fun setSignedIn(user: User) {
        state = AuthState.SignedIn(user)
    }

    fun setSignedOut() {
        state = AuthState.SignedOut
    }

    fun setError(message: String) {
        state = AuthState.Error(message)
    }
}
