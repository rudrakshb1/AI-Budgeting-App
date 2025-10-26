package com.example.aibudgetapp.ui.screens.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class AddUserDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setDialogContent(
        onCreateSpy: (String, String, String, String?) -> Unit = { _, _, _, _ -> },
        onDismissSpy: () -> Unit = {}
    ) {
        composeRule.setContent {
            MaterialTheme {
                AddUserDialog(
                    onDismiss = onDismissSpy,
                    onCreate = onCreateSpy
                )
            }
        }
    }

    @Test
    fun noInput() {
        setDialogContent()

        // Check "Create account" Button
        composeRule.onNodeWithText("Create account").assertIsNotEnabled()
    }

    @Test
    fun password_mismatch_and_disables_button() {
        setDialogContent()
        composeRule.onNodeWithText("Email").performTextInput("user@example.com")
        composeRule.onNodeWithText("First name").performTextInput("Test")
        composeRule.onNodeWithText("Password (min 6 chars)").performTextInput("abcdef")
        composeRule.onNodeWithText("Confirm password").performTextInput("abcdeg")

        // Error Message
        composeRule.onNodeWithText("Passwords do not match").assertExists()
        // Check "Create account" Button
        composeRule.onNodeWithText("Create account").assertIsNotEnabled()
    }

    @Test
    fun valid_inputs_enable_button_and_call_onCreate_with_lastName_null_when_blank() {
        var called = false
        var capturedEmail = ""
        var capturedPassword = ""
        var capturedFirst = ""
        var capturedLast: String? = "placeholder"

        setDialogContent(
            onCreateSpy = { email, password, first, last ->
                called = true
                capturedEmail = email
                capturedPassword = password
                capturedFirst = first
                capturedLast = last
            }
        )

        composeRule.onNodeWithText("Email").performTextInput("user2@example.com")
        composeRule.onNodeWithText("First name").performTextInput("Test2")
        composeRule.onNodeWithText("Last name (optional)").performTextInput("") // intentionally blank
        composeRule.onNodeWithText("Password (min 6 chars)").performTextInput("abcdef")
        composeRule.onNodeWithText("Confirm password").performTextInput("abcdef")

        // Check "Create account" Button
        composeRule.onNodeWithText("Create account").assertIsEnabled().performClick()

        // Assert variables
        assert(called)
        assert(capturedEmail == "user2@example.com")
        assert(capturedPassword == "abcdef")
        assert(capturedFirst == "Test2")
        assert(capturedLast == null)
    }

    @Test
    fun valid_inputs_with_lastName_enable_button_and_pass_lastName() {
        var called = false
        var capturedEmail = ""
        var capturedPassword = ""
        var capturedFirst = ""
        var capturedLast: String? = null

        setDialogContent(
            onCreateSpy = { email, password, first, last ->
                called = true
                capturedEmail = email
                capturedPassword = password
                capturedFirst = first
                capturedLast = last
            }
        )

        composeRule.onNodeWithText("Email").performTextInput("ok@example.com")
        composeRule.onNodeWithText("First name").performTextInput("Jane")
        composeRule.onNodeWithText("Last name (optional)").performTextInput("Doe")
        composeRule.onNodeWithText("Password (min 6 chars)").performTextInput("123456")
        composeRule.onNodeWithText("Confirm password").performTextInput("123456")

        // Check "Create account" Button
        composeRule.onNodeWithText("Create account").assertIsEnabled().performClick()

        // Assert variables
        assert(called)
        assert(capturedEmail == "ok@example.com")
        assert(capturedPassword == "123456")
        assert(capturedFirst == "Jane")
        assert(capturedLast == "Doe")
    }

    @Test
    fun cancel_calls_onDismiss() {
        var dismissed = false
        setDialogContent(onDismissSpy = { dismissed = true })

        composeRule.onNodeWithText("Cancel").assertHasClickAction().performClick()

        assert(dismissed)
    }
}
