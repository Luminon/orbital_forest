package space.byeolvit.of.ui.screen.launch

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import space.byeolvit.of.R

// AI Footprint URL — replace with actual report URL when available
private const val AI_FOOTPRINT_URL = "https://github.com/Luminon/orbital_forest/blob/primary/ai_footprint.md"

@Composable
fun LaunchScreen(
    viewModel: LaunchViewModel,
    onSetupComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFolderSelected(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.setupComplete.collect { onSetupComplete() }
    }

    AnimatedContent(
        targetState = uiState.step,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "LaunchStepTransition"
    ) { step ->
        when (step) {
            LaunchStep.BRAND -> BrandScreen(
                onStartClicked = { viewModel.onStartClicked() }
            )
            LaunchStep.FOLDER -> FolderSelectScreen(
                isLoading = uiState.isLoading,
                error = uiState.error,
                onSelectFolder = { folderPickerLauncher.launch(null) }
            )
        }
    }
}

@Composable
private fun BrandScreen(
    onStartClicked: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_symbol),
            contentDescription = null,
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.Center),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onStartClicked,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "시작하기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            AiFootprintNotice(onLinkClick = { uriHandler.openUri(AI_FOOTPRINT_URL) })
        }
    }
}

@Composable
private fun AiFootprintNotice(onLinkClick: () -> Unit) {
    val primaryHalfAlpha = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val fullText = "본 어플리케이션은 개발 일부에 AI를 활용했습니다. 사용 이전에 AI Footprint 보고서로 내용을 검토할 수 있습니다."
    val linkText = "AI Footprint 보고서"
    val startIndex = fullText.indexOf(linkText)
    val endIndex = startIndex + linkText.length

    val annotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = primaryHalfAlpha)) {
            append(fullText.substring(0, startIndex))
        }
        pushStringAnnotation(tag = "LINK", annotation = AI_FOOTPRINT_URL)
        withStyle(
            SpanStyle(
                color = primaryHalfAlpha,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(linkText)
        }
        pop()
        withStyle(SpanStyle(color = primaryHalfAlpha)) {
            append(fullText.substring(endIndex))
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "LINK", start = offset, end = offset)
                .firstOrNull()?.let { onLinkClick() }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FolderSelectScreen(
    isLoading: Boolean,
    error: String?,
    onSelectFolder: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_symbol),
            contentDescription = null,
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.Center),
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Surface(
                onClick = onSelectFolder,
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "저장 위치 설정",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = ".md 문서를 저장할 위치를 설정합니다. 선택한 폴더 하위에 \"Orbital Forest\" 폴더를 만듭니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB2B2D6)
                        )
                    }

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
