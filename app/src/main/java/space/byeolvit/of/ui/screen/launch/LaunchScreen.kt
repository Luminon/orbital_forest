package space.byeolvit.of.ui.screen.launch

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

// AI Footprint URL — replace with actual report URL when available
private const val AI_FOOTPRINT_URL = "https://example.com/ai-footprint"

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // 브랜드 일러스트 영역 (에셋 제공 시 교체)
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "궤도의 숲",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onStartClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("시작하기")
            }

            AiFootprintNotice(onLinkClick = { uriHandler.openUri(AI_FOOTPRINT_URL) })
        }
    }
}

@Composable
private fun AiFootprintNotice(onLinkClick: () -> Unit) {
    val fullText = "본 어플리케이션은 개발 일부에 AI를 활용했습니다. 사용 이전에 AI Footprint 보고서로 내용을 검토할 수 있습니다."
    val linkText = "AI Footprint 보고서"
    val startIndex = fullText.indexOf(linkText)
    val endIndex = startIndex + linkText.length

    val annotatedString = buildAnnotatedString {
        append(fullText.substring(0, startIndex))
        pushStringAnnotation(tag = "LINK", annotation = AI_FOOTPRINT_URL)
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(linkText)
        }
        pop()
        append(fullText.substring(endIndex))
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "LINK", start = offset, end = offset)
                .firstOrNull()?.let { onLinkClick() }
        }
    )
}

@Composable
private fun FolderSelectScreen(
    isLoading: Boolean,
    error: String?,
    onSelectFolder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // 브랜드 일러스트 영역 (에셋 제공 시 교체)
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "궤도의 숲",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "저장 위치 설정",
                style = MaterialTheme.typography.titleMedium
            )

            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = onSelectFolder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("폴더 선택")
                }
            }
        }
    }
}
