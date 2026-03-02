package space.byeolvit.of.ui.screen.licenses

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import space.byeolvit.of.R

// ──────────────────────────────────────────────
// 데이터
// ──────────────────────────────────────────────

private data class LibraryLicense(
    val name: String,
    val copyright: String,
    val license: String
)

private val apacheLicenseLibraries = listOf(
    LibraryLicense(
        name = "AndroidX Core KTX",
        copyright = "Copyright 2018 The Android Open Source Project",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "AndroidX Lifecycle",
        copyright = "Copyright 2018 The Android Open Source Project",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "AndroidX Activity",
        copyright = "Copyright 2018 The Android Open Source Project",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Jetpack Compose",
        copyright = "Copyright 2019 The Android Open Source Project",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "AndroidX Navigation",
        copyright = "Copyright 2018 The Android Open Source Project",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "AndroidX DataStore",
        copyright = "Copyright 2020 The Android Open Source Project",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "AndroidX DocumentFile",
        copyright = "Copyright 2018 The Android Open Source Project",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Kotlin",
        copyright = "Copyright 2010–2024 JetBrains s.r.o. and Kotlin Programming Language contributors",
        license = "Apache License 2.0"
    ),
    LibraryLicense(
        name = "Kotlin Coroutines",
        copyright = "Copyright 2016–2024 JetBrains s.r.o.",
        license = "Apache License 2.0"
    )
)

private val oflLicenseLibraries = listOf(
    LibraryLicense(
        name = "Pretendard Variable",
        copyright = "Copyright (c) 2021, Kil Hyung-jin\n" +
                "Copyright 2014–2021 Adobe Systems Incorporated (Source Han Sans)\n" +
                "Copyright (c) 2016 The Inter Project Authors\n" +
                "Copyright (c) 2021 The M+ FONTS Project Authors",
        license = "SIL Open Font License 1.1"
    )
)

private const val APACHE_LICENSE_TEXT = """Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License."""

private const val OFL_LICENSE_TEXT = """SIL Open Font License
Version 1.1 - 26 February 2007

PREAMBLE
The goals of the Open Font License (OFL) are to stimulate worldwide development of collaborative font projects, to support the font creation efforts of academic and linguistic communities, and to provide a free and open framework in which fonts may be shared and improved in partnership with others.

The OFL allows the licensed fonts to be used, studied, modified and redistributed freely as long as they are not sold by themselves. The fonts, including any derivative works, can be bundled, embedded, redistributed and/or sold with any software provided that any reserved names are not used by derivative works. The fonts and derivatives, however, cannot be released under any other type of license. The requirement for fonts to remain under this license does not apply to any document created using the fonts or their derivatives.

PERMISSION & CONDITIONS
Permission is hereby granted, free of charge, to any person obtaining a copy of the Font Software, to use, study, copy, merge, embed, modify, redistribute, and sell modified and unmodified copies of the Font Software, subject to the following conditions:

1) Neither the Font Software nor any of its individual components, in Original or Modified Versions, may be sold by itself.

2) Original or Modified Versions of the Font Software may be bundled, redistributed and/or sold with any software, provided that each copy contains the above copyright notice and this license. These can be included either as stand-alone text files, human-readable headers or in the appropriate machine-readable metadata fields within text or binary files as long as those fields can be easily viewed by the user.

3) No Modified Version of the Font Software may use the Reserved Font Name(s) unless explicit written permission is granted by the corresponding Copyright Holder. This restriction only applies to the primary font name as presented to the users.

4) The name(s) of the Copyright Holder(s) or the Author(s) of the Font Software shall not be used to promote or endorse products derived from the Font Software without specific prior written permission of the Copyright Holder(s).

5) The Font Software, modified or unmodified, in part or in whole, must be distributed entirely under this license, and must not be distributed under any other license. The requirement for fonts to remain under this license does not apply to any document created using the Font Software or its derivatives.

TERMINATION
This license becomes null and void if any of the above conditions are not met.

DISCLAIMER
THE FONT SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF COPYRIGHT, PATENT, TRADEMARK, OR OTHER RIGHT. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, INCLUDING ANY GENERAL, SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF THE USE OR INABILITY TO USE THE FONT SOFTWARE OR FROM OTHER DEALINGS IN THE FONT SOFTWARE."""

// ──────────────────────────────────────────────
// 화면
// ──────────────────────────────────────────────

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val glowColor = MaterialTheme.colorScheme.primary
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height + 22.dp.toPx()),
                    radius = size.width * 0.74f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // 앱바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(100.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.settings_oss_licenses),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Apache 2.0 라이브러리 목록
            LicenseSection(libraries = apacheLicenseLibraries)

            Spacer(modifier = Modifier.height(2.dp))

            // OFL 1.1 라이브러리 목록
            LicenseSection(libraries = oflLicenseLibraries)

            Spacer(modifier = Modifier.height(16.dp))

            // Apache 2.0 전문
            LicenseTextCard(text = APACHE_LICENSE_TEXT)

            Spacer(modifier = Modifier.height(8.dp))

            // OFL 1.1 전문
            LicenseTextCard(text = OFL_LICENSE_TEXT)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LicenseSection(libraries: List<LibraryLicense>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        libraries.forEach { lib ->
            LibraryItem(lib)
        }
    }
}

@Composable
private fun LibraryItem(lib: LibraryLicense) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = lib.name,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = lib.copyright,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFB2B2D6)
        )
        Text(
            text = lib.license,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun LicenseTextCard(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = Color(0xFFB2B2D6),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
    }
}
