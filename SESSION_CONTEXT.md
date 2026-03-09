# Orbital Forest — 세션 컨텍스트

## 프로젝트 개요
- **앱 이름**: 궤도의 숲 (Orbital Forest)
- **패키지**: `space.byeolvit.of`
- **목적**: Markdown(`.md`) 파일에 체크리스트를 저장/관리하는 Android 앱
- **작성자**: Luminon Canoness
- **기술 스택**: Jetpack Compose + Material 3, SAF, MVVM, DataStore, minSdk 26

---

## 빌드 설정

- **Gradle**: 8.13 (`gradle/wrapper/gradle-wrapper.properties`)
- **AGP**: 9.0.1, **Kotlin**: 2.2.10, **compileSdk**: 35, **minSdk**: 26
- **Compose BOM**: 2025.02.00
- **주요 의존성**: navigation-compose 2.8.7, datastore-preferences 1.1.2, documentfile 1.1.0, material-icons-extended

---

## 디렉토리 구조

```
app/src/main/java/space/byeolvit/of/
├── OrbitalForestApp.kt          # Application + DataStore 싱글톤
├── MainActivity.kt
├── data/
│   ├── model/
│   │   ├── ChecklistItem.kt     # id(UUID), rawText, isChecked, indentLevel, children
│   │   └── ParsedDocument.kt    # sealed DocumentBlock: ChecklistBlock | MemoBlock
│   ├── parser/
│   │   ├── MarkdownParser.kt    # GFM 체크리스트 파싱 (탭/2공백 들여쓰기 지원)
│   │   └── MarkdownSerializer.kt  # 탭 들여쓰기로 직렬화 (Obsidian 호환)
│   ├── source/
│   │   ├── DocumentDataSource.kt  # SAF ContentResolver 래퍼
│   │   └── SettingsDataSource.kt  # DataStore Preferences 키 정의
│   └── repository/
│       ├── DocumentRepository.kt (interface)
│       ├── DocumentRepositoryImpl.kt
│       ├── SettingsRepository.kt (interface)
│       └── SettingsRepositoryImpl.kt
├── ui/
│   ├── theme/ Color.kt, Type.kt(PretendardVariable), Shape.kt, Theme.kt
│   ├── components/
│   │   ├── ChecklistItemRow.kt   # @OptIn(ExperimentalFoundationApi::class) 필수
│   │   ├── ContextMenuSheet.kt, DocumentMenuSheet.kt, DocumentNameChip.kt
│   │   ├── MemoBlock.kt, FadingEdge.kt
│   ├── screen/
│   │   ├── launch/  LaunchScreen.kt + LaunchViewModel.kt  ← LNC_01/02 완료
│   │   ├── home/    HomeScreen.kt + HomeViewModel.kt + newdoc/
│   │   ├── select/  SelectDocumentScreen.kt + SelectDocumentViewModel.kt
│   │   └── settings/ SettingsScreen.kt + SettingsViewModel.kt
│   ├── licenses/ LicensesScreen.kt  ← 오픈소스 라이선스 화면
│   └── navigation/
│       ├── Screen.kt               # Launch, Home, SelectDocument, Settings, Licenses
│       └── NavGraph.kt
└── util/
    ├── AnnotatedStringBuilder.kt  # **bold** *italic* ~~strike~~
    ├── SafUtils.kt
    └── UiText.kt                  # sealed class: StringResource(@StringRes) | DynamicString

app/src/main/res/
├── drawable/
│   ├── ic_launcher_background.xml, ic_launcher_foreground.xml  ← 아이콘 72% 스케일(group 태그)
│   ├── ic_symbol.xml  ← (Sources/Brand Assets/img_symbol.svg → VectorDrawable, 320dp)
│   ├── img_wordmark.xml ← (Sources/Brand Assets/img_wordmark.svg → VectorDrawable, 114×28dp)
│   ├── ic_logo_of.xml ← 앱바 문서 메뉴 버튼 아이콘 (Sources/Icons/ic_logo_of.svg)
│   ├── ic_folder.xml  ← (Sources/Icons/ic_folder.svg → VectorDrawable, fillType=evenOdd)
│   └── ic_add.xml, ic_plus.xml, ic_file_add.xml, ic_doc_lists.xml
│       ic_settings.xml, ic_documents.xml, ic_bin.xml, ic_copy.xml
│       ic_pencil.xml, ic_external.xml, ic_check.xml   ← 커스텀 아이콘
├── font/ pretendard_variable.ttf   ← SIL OFL 1.1 라이선스
├── mipmap-anydpi-v26/ ic_launcher.xml, ic_launcher_round.xml (Adaptive Icon)
├── mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ ic_launcher.png, ic_launcher_round.png
└── values/
    ├── strings.xml        ← 한국어 (기본값, ~58개 항목)
    ├── themes.xml
    ├── values-en/strings.xml  ← 영어
    └── values-ja/strings.xml  ← 일본어

Sources/  (소스 에셋 — 앱에 직접 포함되지 않음)
├── Icons/    ← SVG 원본 (ic_add, ic_plus, ic_file_add, ic_doc_lists,
│              ic_settings, ic_documents, ic_bin, ic_copy, ic_pencil,
│              ic_external, ic_check, ic_folder, ic_logo_of)
├── Brand Assets/ img_symbol.svg, img_wordmark.svg
└── UI/ ic_empty.svg
```

---

## 다크 테마 색상 토큰 (Figma HME_02 기준)

| 토큰 | 값 | 용도 |
|---|---|---|
| `Background` | `#13131A` | 화면 배경, 툴바 배경, 그룹 카드 배경 |
| `Surface` | `#1C1C26` | 체크리스트 미체크 아이템, 메모블록, ContextMenuSheet 배경 |
| `SurfaceVariant` | `#424259` | 체크된 아이템 강조, ContextMenuSheet 핸들/구분선 |
| `Primary` | `#BDC2FF` | 제목, FAB, `.md` 배지 텍스트/배경, 입력창 배경, 아이콘 tint |
| `OnPrimary` | `#13131A` | FAB 아이콘, 입력창 추가버튼 배경 |
| `OnSurface` | `#FFFFFF` | 리스트 텍스트 |
| `OnSurfaceVariant` | `#E6E0E9` | 서브 텍스트 |
| `PrimaryBadge` | `#3DBDC2FF` (24% alpha) | `.md` 배지 배경 |
| `InverseSurface` | `#424259` | **Snackbar 배경** |
| `InverseOnSurface` | `#FFFFFF` | **Snackbar 텍스트** |
| `InversePrimary` | `#BDC2FF` | **Snackbar 액션 버튼** |

> Snackbar는 M3 기본 동작이 `inverseSurface`/`inverseOnSurface`/`inversePrimary`를 사용하므로,
> 위 세 값을 Color.kt에서 재정의하면 앱 전체 Snackbar에 일괄 적용됨.

---

## LaunchScreen 구조 (Figma LNC_01 / LNC_02 — 완료)

- `AnimatedContent(Fade)` 로 `LaunchStep.BRAND` ↔ `LaunchStep.FOLDER` 전환 (Navigation 미사용)
- 두 화면 모두 `Box(fillMaxSize, background=Background)` 구조
  - 중앙: `Icon(ic_symbol, 320.dp, tint=primary)` — 브랜드 심볼
  - 하단: `Column(align=BottomCenter, navigationBarsPadding, padding(bottom=14~16dp))`

### BrandScreen (LNC_01)
- `Button(RoundedCornerShape(8.dp), containerColor=primary)` — "시작하기"
- `AiFootprintNotice` — `primary.copy(alpha=0.5f)` 텍스트, 링크 underline
  - URL placeholder: `https://example.com/ai-footprint`

### FolderSelectScreen (LNC_02)
- `Surface(onClick, RoundedCornerShape(16.dp), color=surface)` 클릭 가능 리스트 아이템
  - `ic_folder.xml` (tint=primary) + 타이틀/설명 Column + `NavigateNext` 아이콘 or `CircularProgressIndicator`
  - 설명 텍스트 색상: `Color(0xFFB2B2D6)` (Figma 커스텀 토큰)

---

## HomeScreen UI 구조 (Figma HME_02)

### 레이아웃 원칙
- **Scaffold**: `topBar`/`bottomBar`/`floatingActionButton` 슬롯 **미사용**
  - 앱바, 툴바, FAB, Snackbar 모두 content Box 내부 **오버레이** 방식으로 배치
  - `contentWindowInsets = WindowInsets(0, 0, 0, 0)` — Scaffold inset 비활성화
- `appBarHeight = statusBarTop + 60.dp` → LazyColumn `contentPadding.top`에 사용
- PullToRefreshBox로 전체 콘텐츠 감쌈 (ExperimentalMaterial3Api)

### 앱바 (오버레이, `Alignment.TopCenter`)
- `AnimatedVisibility(visible = !isScrolled, slideInVertically + fadeIn)`
- 커스텀 Row: `windowInsetsPadding(statusBars) + padding(horizontal=24.dp, vertical=10.dp)`
- 좌: `DocumentNameChip` (maxLines=1, overflow=Ellipsis, weight=1f) — 긴 제목 줄임 표시
- 우: `Surface(onClick, RoundedCornerShape(999.dp), 72×40dp, color=surface)` — `ic_logo_of` 아이콘

### 하단 컨트롤 visibility 규칙
| 컴포넌트 | 스크롤 시 | 입력 필드 표시 시 |
|---|---|---|
| 툴바 필 | 숨김 | 숨김 |
| FAB | **유지** | 숨김 |
| 입력 필드 | — | 표시 |

### Snackbar (오버레이, `Alignment.BottomCenter`)
- FAB 위에 표시: `navigationBarsPadding() + padding(start=34.dp, end=34.dp, bottom=112.dp)`
  - 112dp = FAB 80dp + 상하 패딩 16dp+16dp
- 색상은 테마 inverse 색상 자동 적용 (Color.kt 재정의)
- `SnackbarDuration.Short` 사용

### 주요 컴포넌트 스펙
- **ToolbarPill**: `Surface(RoundedCornerShape(32.dp), color=background, shadowElevation=4.dp)` — 3개 `IconButton(48dp)`
- **LargeAddFab**: `FloatingActionButton(size=80.dp, shape=RoundedCornerShape(20.dp), containerColor=primary)`
- **ChecklistGroupCard**: `Surface(RoundedCornerShape(16.dp), color=background)` + `Column` + `Spacer(2.dp)` 아이템 사이
- **MemoBlock**: `Surface(RoundedCornerShape(16.dp), color=surface)` + `ic_documents` 아이콘(18dp) + 텍스트
- **DocumentNameChip**: `headlineLarge` Bold 32sp, `primary` 색상, `.md` 배지(PrimaryBadge bg), **maxLines=1 + Ellipsis**

### AddItemField (HME_10 / HME_09 / HME_09A)

- 컨테이너: `Surface(RoundedCornerShape(topStart=36.dp, topEnd=36.dp), color=primary, shadowElevation=2.dp)`
- Column modifier: `padding(start=16.dp, end=16.dp, top=12.dp, bottom=12.dp)`, `verticalArrangement=spacedBy(16.dp)`
- Row modifier: `padding(start=16.dp)` → 텍스트 시작 위치 32dp (컨테이너 16 + Row 16)
- `BasicTextField(textStyle=onPrimary)` + placeholder (`onPrimary.copy(alpha=0.5f)`)
- 추가/확인 버튼: `Surface(RoundedCornerShape(999.dp), color=background)` + `Icon(tint=primary, padding(h=32.dp, v=12.dp))`
  - 일반 모드: `ic_plus`
  - 수정 모드 (`editingItemText != null`): `ic_check`
- 상태 바 (모드별 조건부 표시):
  - `Surface(RoundedCornerShape(99.dp), color=surface)`
  - Row: `spacedBy(12.dp)`, `h=16.dp, v=4.dp`
  - 좌: "하위 항목 작성중" 또는 "수정중" (`primary`)
  - 우: parentName 또는 editingItemText (`onSurface`, maxLines=1, Ellipsis, weight=1f)

### ContextMenuSheet (HME_06)

- `ModalBottomSheet(containerColor=surface, dragHandle=DragHandle(color=surfaceVariant))`
- **미리보기 카드**: `Surface(border=1dp surfaceVariant, rounded=8dp, color=background)`
  - `Row(padding h=16dp v=8dp)`: 텍스트(weight=1f, v=12dp) + `ic_copy`(24dp, tint=primary, clickable → 복사+닫기)
- **액션 툴바**: `Row(IntrinsicSize.Max)` — 3버튼 + 1dp surfaceVariant 구분선
  - `ic_bin` "삭제" / `ic_pencil` "수정" / `ic_add` "하위 항목"
  - 하위 항목: `item.indentLevel >= 1`이면 비활성화 (alpha=0.38f)
  - 수정 버튼: `onEdit()` 호출 → ContextMenuSheet 닫히고 AddItemField 표시 (수정 모드)

### 체크리스트 수정 기능 (HME_09A)

- `HomeUiState.pendingEditItem: ChecklistItem?` — 수정 대상 항목
- `onStartEditItem(item)`: 메뉴 닫기 + `showAddField=true` + `addFieldText=item.rawText` + `pendingEditItem=item`
- `onEditItem(text)`: 트리에서 해당 ID 항목 텍스트 교체 → 저장 → "수정되었습니다." Snackbar
- `updateItemTextInTree()`: 재귀 트리 탐색으로 대상 항목 `rawText` 교체
- 완료/취소 시 `pendingEditItem = null` 클리어 (`onAddFieldToggle/Dismiss/AddItem` 모두)

### 아이콘 매핑 (res/drawable 커스텀 아이콘)
| 위치 | 아이콘 파일 |
|---|---|
| 툴바 — 새 문서 | `ic_add.xml` |
| 툴바 — 문서 선택 | `ic_doc_lists.xml` |
| 툴바 — 설정 | `ic_settings.xml` |
| FAB | `ic_plus.xml` (28dp) |
| 앱바 문서 메뉴 버튼 | `ic_logo_of.xml` (24dp, tint=primary) |
| 메모블록 | `ic_documents.xml` (18dp) |
| 입력창 — 일반 추가 | `ic_plus.xml` (tint=primary) |
| 입력창 — 수정 확인 | `ic_check.xml` (tint=primary) |
| 컨텍스트 메뉴 — 삭제 | `ic_bin.xml` |
| 컨텍스트 메뉴 — 수정 | `ic_pencil.xml` |
| 컨텍스트 메뉴 — 하위항목 | `ic_add.xml` |
| 미리보기 카드 — 복사 | `ic_copy.xml` |

> 벡터 드로어블 `fillColor="#FFFFFF"`, Compose `Icon(tint=...)` 으로 색상 제어

---

## 핵심 설계 결정

### SAF 권한 구조
- `takePersistableUriPermission`은 사용자가 선택한 **루트 URI**에만 부여됨
- 앱 폴더(`OrbitalForest/`) URI는 별도 권한 없음
- **DataStore 키**: `SAF_ROOT_URI`(루트), `SAF_APP_FOLDER_URI`(앱폴더), `HIDE_COMPLETED`, `HIDE_NON_CHECKLIST`, `CHILD_INTERACTION`, `CURRENT_DOCUMENT_NAME`

### SAF 파일 생성 MIME 타입 처리
- `DocumentFile.createFile(mimeType, displayName)` 호출 시, SAF 프로바이더는 mimeType과 displayName의 확장자가 불일치하면 mimeType 기준 확장자로 덮어씀
- `"text/markdown"` → 기기에 미등록 시 확장자 없이 생성 (구버전 Android)
- `"text/plain"` → `.txt`로 덮어씀
- **해결**: `MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)` 동적 조회
  - 기기에서 `.md`에 매핑된 MIME 타입을 그대로 사용 → 확장자 일치 보장
  - `DocumentDataSource.mimeTypeFor(fileName)` 헬퍼로 추출 (companion object)
  - Android 10+: `getMimeTypeFromExtension("md")` = `"text/markdown"` → 정상 생성

### NavGraph 시작화면 결정 로직 (`NavGraph.kt`)
```kotlin
LaunchedEffect(Unit) {
    settingsRepo.safRootUri.collect { uri ->
        isUriValid = if (uri != null) isUriPermissionValid(context, uri) else false
        return@collect
    }
}
// isUriValid == null → CircularProgressIndicator
// isUriValid == true → HomeScreen
// isUriValid == false → LaunchScreen
```

### HomeViewModel 문서 로딩 구조
- `init` 블록에서 두 coroutine을 분리 운영:
  - `combine(hideCompleted, hideNonChecklist, childInteraction)` → Triple — 설정값 전용
  - `settingsRepository.currentDocumentName.collect { name -> loadDocument(name) }` — 문서 이름 전용
- `loadDocumentJob: Job?` 로 이전 로딩 취소 후 새 로딩 시작 (stale 문서 방지)
- `loadDocument()`는 DataStore 수정 안 함
- `onNewDocumentCreated()`: `loadDocument()` 직접 호출 + `setCurrentDocumentName` → "새 문서를 생성했어요." Snackbar
- `onDeleteDocument()`: 다음 문서 이름을 `setCurrentDocumentName`으로 설정 → currentDocumentName collector가 loadDocument 실행

### SelectDocumentScreen UI 구조 (SED_01)
- Scaffold-free, Box 오버레이 구조 (HomeScreen과 동일)
- 배경: `#13131A` + Canvas radial gradient 글로우
- 앱바: `Row(h=64.dp, px=24, py=8)` — 원형 Surface(40dp, surface) 뒤로가기 + "문서 선택" titleLarge/primary
- 안내 배너: `Surface(RoundedCornerShape(16.dp), color=Color(0xFF172E33))` + `Text(color=Color(0xFF74E8FF), labelMedium)`
- 문서 목록: `Column(clip=RoundedCornerShape(16dp), spacedBy=2dp)` + `verticalScroll`
  - 선택 항목: `surfaceVariant` 배경 + `ic_check` 아이콘
  - 미선택 항목: `surface` 배경
- 길게 누르면 `DocumentMenuSheet` 표시
- `onDocumentSelected: (String) -> Unit` 콜백 — NavGraph에서 `homeVm.loadDocument(fileName)` 호출 후 `popBackStack()`

### NavGraph 문서 선택 흐름 (네비게이션 버그 수정)
- SelectDocument composable 내에서 `navController.getBackStackEntry(Screen.Home.route)`로 HomeViewModel 직접 참조
- `homeVm.loadDocument(fileName)` 호출 후 `popBackStack()` — DataStore Flow 타이밍 문제 우회
- `viewModel(viewModelStoreOwner = homeEntry, factory = ...)` 패턴으로 기존 인스턴스 재사용

### SettingsScreen UI 구조 (STN_01)
- Scaffold-free, Box 오버레이 구조 + `verticalScroll`
- 배경: `#13131A` + Canvas radial gradient 글로우
- 앱바: SelectDocumentScreen과 동일한 커스텀 Row 패턴
- 브랜드 영역:
  - `Icon(ic_symbol, 320dp, primary)` — LaunchScreen과 동일
  - `Icon(img_wordmark, 114×28dp, primary)` — `res/drawable/img_wordmark.xml` (SVG path → VectorDrawable)
  - 배지: `Surface(RoundedCornerShape(4.dp), color=primary.copy(0.24f))` + labelMedium/primary
- 설정 그룹 / 앱 정보 그룹: `Column(clip=RoundedCornerShape(16dp), spacedBy=2dp)` × 2
- 아이템 스펙: `bg=surface`, `px=16, py=8`, 내용 `py=12`, 제목↔설명 간격 `2dp`
  - `SettingsActionItem`: clickable Row (저장 위치 변경)
  - `SettingsToggleItem`: clickable Row + Switch, `horizontalArrangement=spacedBy(8dp)`
  - `SettingsInfoItem`: Row + 오른쪽 value 텍스트 (primary색)
  - `AiFootprintItem`: clickable Row + ic_external 아이콘 + 설명+불렛포인트
  - `SettingsActionItem(settings_oss_licenses)`: 앱 정보 그룹 최하단 → LicensesScreen으로 이동
- 태그라인 텍스트: `textAlign = TextAlign.Center` 적용

### LicensesScreen UI 구조 (`ui/screen/licenses/LicensesScreen.kt`)
- Scaffold-free, Box 오버레이 구조 + `verticalScroll` (SettingsScreen과 동일)
- 배경: `#13131A` + Canvas radial gradient 글로우, 앱바: 뒤로가기 + titleLarge
- `LibraryLicense(name, copyright, license)` data class
- **Apache 2.0 섹션** (9개 라이브러리): AndroidX Core KTX, Lifecycle, Activity, Jetpack Compose, Navigation, DataStore, DocumentFile, Kotlin, Kotlin Coroutines
- **OFL 1.1 섹션** (1개): Pretendard Variable — 저작권자 4곳 (Kil Hyung-jin, Adobe Systems/Source Han Sans, The Inter Project Authors, The M+ FONTS Project Authors)
- 각 섹션 하단에 해당 라이선스 전문을 `LicenseTextCard`(monospace)로 표시
- Screen.Licenses → `Screen("licenses")`, NavGraph에 composable 라우트 추가

### i18n 구조 (다국어 지원)
- **`util/UiText.kt`**: ViewModel에서 string resource ID 보관 → Composable에서 `.asString()` resolve
  ```kotlin
  sealed class UiText {
      data class StringResource(@StringRes val resId: Int) : UiText()
      data class DynamicString(val value: String) : UiText()
      @Composable fun asString(): String = ...
  }
  ```
- **strings.xml 3벌**: `values/`(한국어, ~58개 항목), `values-en/`(영어), `values-ja/`(일본어)
- **UiText 적용 위치**: `HomeUiState.snackbarMessage: UiText?`, `SnackbarAction.label: UiText`, `NewDocumentUiState.error: UiText?`
- **`@Composable` 제약 우회**: `asString()`은 LaunchedEffect 내부 호출 불가 → Composable scope에서 미리 resolve 후 변수로 전달
- **ViewModel의 문자열**: Context 보유 ViewModel(`LaunchViewModel`)은 `context.getString(R.string.xxx)` 직접 사용
- **`NewDocumentViewModel.loadDefaultName(baseName: String)`**: 하드코딩 제거, Dialog에서 `stringResource(R.string.default_doc_name)` resolve 후 전달

### 전체 타이포그래피 한국어 줄바꿈 (word-break: keep-all)
- `Type.kt`의 모든 텍스트 스타일에 `lineBreak = keepAllLineBreak` 적용
- `keepAllLineBreak = LineBreak.Simple.copy(wordBreak = LineBreak.WordBreak.Phrase)`
- `LineBreak.WordBreak.Phrase` = CSS `word-break: keep-all` — CJK 문자 사이 줄바꿈 금지, 어절 단위만 허용
- **주의**: `Strategy.HighQuality`(=`LineBreak.Paragraph`)는 단락 전체 최적화를 수행해 좁은 컨테이너(AlertDialog 등)에서 이상한 여백 발생. `Strategy.Simple`(greedy)이 올바른 기반.

### 앱 아이콘 스케일
- `ic_launcher_foreground.xml`: 기존 path들을 `<group>` 태그로 감싸고 `scaleX=0.72`, `scaleY=0.72`, `pivotX=54`, `pivotY=54` 적용 (108dp 캔버스 중앙 기준)
- 0.72 = 초기 80% → 추가 10% 축소 (0.8 × 0.9)

### M3 화면 전환 애니메이션
- `NavGraph.kt`에 NavHost 기본 전환 적용 (Android M3 Emphasized easing)
  ```kotlin
  private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
  private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
  // enterTransition: fadeIn + scaleIn(0.92f → 1.0f), 300ms, EmphasizedDecelerate
  // exitTransition: fadeOut, 200ms, EmphasizedAccelerate
  // popExitTransition: fadeOut + scaleOut(1.0f → 0.92f), 200ms, EmphasizedAccelerate
  ```
- **주의**: `private val`은 import 블록 이후에 선언해야 함 (중간에 삽입 시 컴파일 오류)

### Markdown 파싱
- 정규식: `^(\s*)- \[( |x)] (.*)$`
- 들여쓰기: 탭 1개 = 1단계, 공백 2개 = 1단계 (`countIndent()` 함수로 구분)
- 직렬화: 탭(`\t`) 사용 (Obsidian 호환)
- 최대 표시 깊이: `MAX_DISPLAY_DEPTH = 1` (2단계 이상은 ErrorChecklistItem 표시)
- 체크리스트 외 줄 = MemoBlock

### 시스템 Inset 처리 원칙
- `Scaffold(contentWindowInsets = WindowInsets(0,0,0,0))` — Scaffold inset 비활성화
- 하단 오버레이 UI는 각자 직접 inset 처리:
  - **ToolbarPill/FAB/Snackbar 박스**: `navigationBarsPadding()`
  - **AddItemField**: `windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))`
- `Theme.kt`의 `OrbitalForestTheme`: `M3Surface(fillMaxSize, color=background)` 래퍼
- `themes.xml`: `android:windowBackground="#13131A"` — 스플래시/전환 시 흰 배경 방지

---

## 버그 수정 이력

| 버그 | 원인 | 수정 |
|---|---|---|
| 앱 실행 시 매번 Launch 화면 | `safAppFolderUri`로 영구권한 검사 (항상 false) | `safRootUri`로 변경 |
| 새 문서 생성 시 깜빡임 | `loadDocument()`가 `setCurrentDocumentName` 호출 → init flow 이중 트리거 | `loadDocument()`에서 DataStore 수정 제거 |
| `combinedClickable` 빌드 오류 | ExperimentalFoundationApi | `@OptIn(ExperimentalFoundationApi::class)` 추가 |
| LaunchScreen 흰 배경 | `Box`에 background modifier 누락 | `.background(MaterialTheme.colorScheme.background)` 추가 |
| 앱 전체 흰 배경 (스플래시/전환) | Theme에 Surface 래퍼 없음, themes.xml windowBackground 없음 | `M3Surface` 래퍼 추가 + `themes.xml` windowBackground 설정 |
| `Surface` 이름 충돌 | `Color.kt`의 `val Surface = Color(...)` vs `material3.Surface` | `import ... Surface as M3Surface` 별칭 사용 |
| 입력창 하단 여백 과다 | Scaffold 기본 contentWindowInsets + AddItemField 내부 inset 중복 계산 | `contentWindowInsets = WindowInsets(0,0,0,0)` + `windowInsetsPadding(ime.union(navBars))` |
| 뒤로가기로 입력창 안 닫힘 | `BackHandler` 없음 | `BackHandler(enabled=showAddField) { onAddFieldDismiss() }` 추가 |
| 앱바 빈 공간 (스크롤 시) | Scaffold `topBar` 슬롯이 AnimatedVisibility 숨겨도 높이 유지 | 앱바를 `topBar` 슬롯에서 제거 → Box 오버레이로 이동 |
| Snackbar가 FAB 아래 표시 | Scaffold `snackbarHost` 슬롯이 화면 최하단 고정 | Scaffold snackbarHost 제거 → Box 오버레이 + bottom=112dp padding |
| 새 문서가 `.md`로 생성 안 됨 | `"text/markdown"` MIME 타입 미등록 기기에서 확장자 없이 생성, `"text/plain"` 사용 시 `.txt`로 덮어씀 | `MimeTypeMap` 동적 조회로 기기별 MIME 타입 사용 |
| `padding(horizontal, top, bottom)` 빌드 오류 | 유효하지 않은 `Modifier.padding` 오버로드 | `padding(start=, end=, top=, bottom=)`으로 변경 |
| SED_01 문서 전환 안 됨 / 검은 화면 | `popBackStack()` 후 DataStore Flow가 아직 미전달 → HomeViewModel이 구 문서 로딩 | NavGraph에서 `getBackStackEntry(Home)`로 HomeVM 직접 참조, `homeVm.loadDocument()` 먼저 호출 |
| 새 문서 생성 Snackbar 미표시 | `onNewDocumentCreated()`에 Snackbar 설정 없음 | `snackbarMessage = "새 문서를 생성했어요."` 추가 |
| `combine` stale 중간값 문제 | 4개 Flow를 combine 시 `currentDocumentName` 변경 시 stale Triple 방출 | `currentDocumentName`을 combine에서 분리, 독립 collector + `loadDocumentJob` 취소 패턴 |
| AlertDialog 이상한 줄바꿈 | `LineBreak.Paragraph`(HighQuality 전략)이 단락 최적화로 불균형 줄바꿈 발생 | `keepAllLineBreak = LineBreak.Simple.copy(wordBreak = LineBreak.WordBreak.Phrase)`로 변경 |
| NavGraph.kt 컴파일 오류 | `private val` 선언이 import 블록 중간에 삽입됨 (`imports are only allowed in the beginning of file`) | 모든 import 이후로 private val 이동 |
| 전체 문서 삭제 후 빈 화면 미표시 | `HomeViewModel.init`의 `currentDocumentName` collector가 `if (name.isNotEmpty())` 분기만 있고 else 없음 → currentDocument 상태 미초기화 | `else { _uiState.update { it.copy(currentDocument = null) } }` 추가 |
| NavGraph `getBackStackEntry` Compose 경고 | `remember(navController)` — navController는 key로 부적합 | `remember(backStackEntry)` (composable 람다 파라미터)로 변경 |

---

## 완료된 추가 작업

- **Empty View 이미지**: HME_01, HME_15 적용 완료
- **클립보드 복사**: `HomeScreen.kt`에서 `ClipboardManager.setPrimaryClip()` 구현 완료
- **가로화면 차단**: `AndroidManifest.xml` MainActivity에 `android:screenOrientation="portrait"` 추가

## 아직 미완성 항목

현재 미완성 항목 없음.

---

## 개발/테스트 환경

- Android Studio에서 USB 디버깅으로 실기기 테스트
- 단위 테스트: `app/src/test/java/space/byeolvit/of/MarkdownParserTest.kt` (8개 케이스)
