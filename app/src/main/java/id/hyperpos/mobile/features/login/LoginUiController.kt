package id.hyperpos.mobile.features.login

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import id.hyperpos.mobile.application.auth.LoginRequest
import id.hyperpos.mobile.application.auth.LoginResult
import id.hyperpos.mobile.application.auth.LoginUseCase
import id.hyperpos.mobile.application.auth.LogoutResult
import id.hyperpos.mobile.application.auth.LogoutUseCase
import id.hyperpos.mobile.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class LoginUiController(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val onAuthenticated: (String) -> Unit,
    private val resetAuthenticatedUi: () -> Unit,
) {
    fun bind() {
        binding.loginButton.setOnClickListener { login() }
        binding.logoutButton.setOnClickListener { logout() }
    }

    fun reset() {
        setLoginFormVisible(true)
        binding.logoutButton.visibility = View.GONE
        binding.logoutButton.isEnabled = true
    }

    fun showAuthenticatedControls() {
        setLoginFormVisible(false)
        binding.logoutButton.visibility = View.VISIBLE
        binding.logoutButton.isEnabled = true
    }

    private fun login() {
        binding.loginButton.isEnabled = false
        binding.statusText.text = "Login berjalan..."
        val request = LoginRequest(
            email = binding.emailInput.text.toString().trim(),
            password = binding.passwordInput.text.toString(),
            deviceName = binding.deviceNameInput.text.toString().trim(),
        )

        thread {
            val result = loginUseCase.execute(request)
            activity.runOnUiThread {
                binding.loginButton.isEnabled = true
                binding.statusText.text = when (result) {
                    is LoginResult.Success -> {
                        onAuthenticated(result.session.actor.role)
                        "Login berhasil: ${result.session.actor.name} (${result.session.actor.role})"
                    }
                    is LoginResult.Failure -> result.message
                }
            }
        }
    }

    private fun logout() {
        binding.logoutButton.isEnabled = false
        binding.statusText.text = "Logout berjalan..."

        thread {
            val result = logoutUseCase.execute()
            activity.runOnUiThread {
                binding.logoutButton.isEnabled = true
                resetAuthenticatedUi()
                binding.statusText.text = when (result) {
                    is LogoutResult.Success -> result.message
                    is LogoutResult.NoSession -> result.message
                    is LogoutResult.Failure -> "${result.message} Sesi lokal dibersihkan."
                }
            }
        }
    }

    private fun setLoginFormVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.emailInput.visibility = visibility
        binding.passwordInput.visibility = visibility
        binding.deviceNameInput.visibility = visibility
        binding.loginButton.visibility = visibility
        binding.loginButton.isEnabled = true
    }
}
