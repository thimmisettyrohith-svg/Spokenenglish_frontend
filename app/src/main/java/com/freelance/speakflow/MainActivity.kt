package com.freelance.speakflow

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.freelance.speakflow.data.*
import com.freelance.speakflow.ui.screens.*
import com.freelance.speakflow.ui.screens.vocab.*
import com.freelance.speakflow.ui.screens.speaking.*
import com.freelance.speakflow.ui.screens.grammar.*
import com.freelance.speakflow.ui.screens.situations.*
import com.freelance.speakflow.ui.screens.gamification.*
import com.freelance.speakflow.ui.theme.SpeakFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpeakFlowTheme {
                Surface {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current

    // ================= GLOBAL STATE =================
    var currentScreen by remember { mutableStateOf("splash") }
    var currentUserId by remember { mutableIntStateOf(0) }

    // State to pass email from Forgot -> Reset screen
    var forgotPasswordEmail by remember { mutableStateOf("") }

    // ================= VOCAB STATE =================
    var selectedCategory by remember { mutableStateOf("") }
    var vocabResults by remember { mutableStateOf<List<VocabAnswerResult>>(emptyList()) }
    var vocabProgress by remember { mutableStateOf<ProgressUpdateResponse?>(null) }

    // ================= SPEAKING STATE =================
    var selectedSpeakingLesson by remember { mutableStateOf<SpeakingLesson?>(null) }
    var currentLessonId by remember { mutableIntStateOf(0) }
    var currentDialogueIndex by remember { mutableIntStateOf(0) }
    var analysisResult by remember { mutableStateOf<SpeakingAnalysisData?>(null) }

    // ================= GRAMMAR STATE =================
    var grammarLevelData by remember { mutableStateOf<GrammarLevelData?>(null) }
    var grammarScore by remember { mutableIntStateOf(0) }
    var grammarProgress by remember { mutableStateOf<ProgressUpdateResponse?>(null) }

    // ================= SITUATIONS STATE =================
    var selectedSituationId by remember { mutableStateOf("") }

    when (currentScreen) {

        // ================= INTRO FLOW =================
        "splash" -> SplashScreen(
            onStartClick = { currentScreen = "onboarding" }
        )

        "onboarding" -> OnboardingScreen(
            onFinished = { currentScreen = "login" }
        )

        // ================= AUTH FLOW =================
        "login" -> LoginScreen(
            onLoginSuccess = { userId ->
                currentUserId = userId
                currentScreen = "home"
            },
            onNavigateToRegister = { currentScreen = "register" },
            onNavigateToForgotPassword = { currentScreen = "forgot_password" }
        )

        "register" -> RegisterScreen(
            onRegisterSuccess = { currentScreen = "login" },
            onNavigateToLogin = { currentScreen = "login" }
        )

        "forgot_password" -> ForgotPasswordScreen(
            onBack = { currentScreen = "login" },
            onNavigateToReset = { email ->
                forgotPasswordEmail = email
                currentScreen = "reset_password"
            }
        )

        "reset_password" -> ResetPasswordScreen(
            email = forgotPasswordEmail,
            onBack = { currentScreen = "forgot_password" },
            onResetSuccess = { currentScreen = "login" }
        )

        // ================= HOME DASHBOARD (MAIN HUB) =================
        "home" -> MainHubScreen(
            userId = currentUserId,
            onNavigateToModule = { moduleId ->
                when (moduleId) {
                    // --- TAB 1: LEARN MODULES ---
                    "vocab" -> currentScreen = "vocab_topics"
                    "speaking" -> currentScreen = "speaking_lessons"
                    "grammar" -> currentScreen = "grammar_intro"
                    "situations" -> currentScreen = "situations"

                    // --- TAB 2: GAME MODULES ---
                    "voice_match" -> currentScreen = "voice_match"
                    "echo_game" -> currentScreen = "echo_game"
                    "speed_race" -> currentScreen = "speed_race"

                    else -> Toast.makeText(
                        context,
                        "Coming Soon!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            // ✅ SETTINGS NAVIGATION CALLBACKS
            onNavigateToEditProfile = { currentScreen = "edit_profile" },
            onNavigateToPrivacy = { currentScreen = "privacy_security" },
            onNavigateToSupport = { currentScreen = "help_support" },
            onLogout = {
                currentUserId = 0
                // Reset ALL states
                vocabResults = emptyList()
                vocabProgress = null
                grammarLevelData = null
                grammarProgress = null
                analysisResult = null
                forgotPasswordEmail = ""

                currentScreen = "login"
            }
        )

        // ================= SETTINGS SCREENS =================
        "edit_profile" -> EditProfileScreen(
            userId = currentUserId, // ✅ CRITICAL: Pass User ID for API call
            onBack = { currentScreen = "home" }
        )

        "privacy_security" -> PrivacySecurityScreen(
            userId = currentUserId, // ✅ PASS USER ID
            onBack = { currentScreen = "home" },
            onNavigateToPrivacyPolicy = { currentScreen = "privacy_policy" },
            onNavigateToTerms = { currentScreen = "terms_conditions" },
            onNavigateToDataDownload = { currentScreen = "data_download" },
            onNavigateToChangePassword = { currentScreen = "change_password" }, // ✅ LINKED
            onAccountDeleted = { // ✅ LOGOUT ON DELETE
                currentUserId = 0
                currentScreen = "login"
            }
        )

        "change_password" -> ChangePasswordScreen(
            userId = currentUserId,
            onBack = { currentScreen = "privacy_security" }
        )

        "data_download" -> DataDownloadScreen(
            onBack = { currentScreen = "privacy_security" }
        )

        "help_support" -> HelpSupportScreen(
            onBack = { currentScreen = "home" },
            onContactSupport = { currentScreen = "contact_support" },
            onNavigateToFaqs = { currentScreen = "faqs" },
            onNavigateToFeedback = { currentScreen = "give_feedback" }
        )

        "give_feedback" -> GiveFeedbackScreen(
            onBack = { currentScreen = "help_support" }
        )

        "contact_support" -> ContactSupportScreen(
            onBack = { currentScreen = "help_support" }
        )

        "faqs" -> FAQsScreen(
            onBack = { currentScreen = "help_support" }
        )

        "privacy_policy" -> LegalScreen(
            title = "Privacy Policy",
            content = LegalContent.PRIVACY_POLICY,
            onBack = { currentScreen = "privacy_security" }
        )

        "terms_conditions" -> LegalScreen(
            title = "Terms & Conditions",
            content = LegalContent.TERMS_CONDITIONS,
            onBack = { currentScreen = "privacy_security" }
        )

        // ================= VOCAB FLOW =================
        "vocab_topics" -> VocabTopicSelectionScreen(
            onTopicChosen = {
                selectedCategory = it
                currentScreen = "vocab_preview"
            },
            onBack = { currentScreen = "home" }
        )

        "vocab_preview" -> VocabPreviewScreen(
            category = selectedCategory,
            onStartGame = { currentScreen = "vocab_game" },
            onBack = { currentScreen = "vocab_topics" }
        )

        "vocab_game" -> VocabGameScreen(
            userId = currentUserId,
            category = selectedCategory,
            onGameComplete = { results, progress ->
                vocabResults = results
                vocabProgress = progress
                currentScreen = "vocab_result"
            }
        )

        "vocab_result" -> VocabResultScreen(
            results = vocabResults,
            progress = vocabProgress,
            onBackHome = {
                vocabResults = emptyList()
                vocabProgress = null
                currentScreen = "home"
            }
        )

        // ================= SPEAKING FLOW =================
        "speaking_lessons" -> SpeakingLessonsScreen(
            userId = currentUserId,
            onLessonClick = { lesson ->
                selectedSpeakingLesson = lesson
                currentLessonId = lesson.id
                currentDialogueIndex = 0
                analysisResult = null
                currentScreen = "speaking_dialogue"
            },
            onBack = { currentScreen = "home" }
        )

        "speaking_dialogue" -> {
            val totalDialogues = selectedSpeakingLesson?.totalDialogues ?: 0
            val dialogueId = (currentLessonId * 100) + (currentDialogueIndex + 1)

            if (analysisResult != null) {
                // 🔵 ANALYSIS
                SpeakingAnalysisScreen(
                    userId = currentUserId,
                    lessonId = currentLessonId,
                    dialogueId = dialogueId,
                    result = analysisResult!!,
                    onBackToLessons = {
                        analysisResult = null
                        currentDialogueIndex++

                        if (currentDialogueIndex >= totalDialogues) {
                            currentDialogueIndex = 0
                            currentScreen = "speaking_lessons"
                        } else {
                            currentScreen = "speaking_dialogue"
                        }
                    }
                )
            } else {
                // 🟢 DIALOGUE
                SpeakingDialogueScreen(
                    lessonId = currentLessonId,
                    dialogueIndex = currentDialogueIndex,
                    onFinishDialogue = { result ->
                        analysisResult = result
                    }
                )
            }
        }

        // ================= GRAMMAR FLOW =================
        "grammar_intro" -> GrammarIntroScreen(
            levelId = 1,
            onStartGame = { data ->
                grammarLevelData = data
                currentScreen = "grammar_game"
            },
            onBack = { currentScreen = "home" }
        )

        "grammar_game" -> {
            grammarLevelData?.let { data ->
                GrammarGameScreen(
                    userId = currentUserId,
                    levelData = data,
                    onLevelComplete = { score, progress ->
                        grammarScore = score
                        grammarProgress = progress
                        currentScreen = "grammar_result"
                    }
                )
            }
        }

        "grammar_result" -> GrammarResultScreen(
            score = grammarScore,
            progress = grammarProgress,
            onHome = {
                grammarLevelData = null
                grammarScore = 0
                grammarProgress = null
                currentScreen = "home"
            }
        )

        // ================= SITUATIONS FLOW =================
        "situations" -> SituationsListScreen(
            onBack = { currentScreen = "home" },
            onSituationClick = { id ->
                selectedSituationId = id
                currentScreen = "situation_game"
            }
        )

        "situation_game" -> SituationGameScreen(
            userId = currentUserId,
            situationId = selectedSituationId,
            onBack = { currentScreen = "situations" }
        )

        // ================= GAMIFICATION (Games) =================
        "voice_match" -> VoiceMatchGameScreen(
            userId = currentUserId,
            onBack = { currentScreen = "home" }
        )

        "echo_game" -> EchoGameScreen(
            userId = currentUserId,
            onBack = { currentScreen = "home" }
        )

        "speed_race" -> SpeedRaceGameScreen(
            userId = currentUserId,
            onBack = { currentScreen = "home" }
        )
    }
}