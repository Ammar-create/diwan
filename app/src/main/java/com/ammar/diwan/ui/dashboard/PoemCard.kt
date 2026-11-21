package com.ammar.diwan.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.ui.theme.FoilGold
import com.ammar.diwan.ui.theme.GarnetRuby
import com.ammar.diwan.ui.theme.InkMuted
import com.ammar.diwan.ui.theme.LocalEnglishFont
import com.ammar.diwan.ui.theme.LocalUrduFont

@Composable
fun PoemCard(
    poem: Poem,
    showRomanUrdu: Boolean,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    var showEnglishTranslation by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }

    val urduFont = LocalUrduFont.current
    val englishFont = LocalEnglishFont.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Poet Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = poem.poetEnglish,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Poetic Title
            Text(
                text = "${poem.titleUrdu} — ${poem.poetUrdu}",
                fontFamily = urduFont,
                fontSize = 30.sp,
                lineHeight = 52.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Verses Block (Cascading Nastaliq + Romanization)
            poem.verses.forEachIndexed { _, verse ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Authentic Urdu Verse
                    Text(
                        text = verse.urdu,
                        fontFamily = urduFont,
                        fontSize = 24.sp,
                        lineHeight = 48.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // On-Device Roman Urdu Transliteration
                    AnimatedVisibility(
                        visible = showRomanUrdu,
                        enter = fadeIn() + expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = verse.roman,
                            fontFamily = englishFont,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    // English Poetic Translation
                    AnimatedVisibility(
                        visible = showEnglishTranslation,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = verse.english,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Traditional Golden Ornament Separator
            Text(
                text = "❖",
                color = FoilGold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation Section (Tashreeh)
            if (poem.explanationUrdu != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExplanation = !showExplanation },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مفہوم و تشریح (Explanation)",
                                fontFamily = englishFont,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (showExplanation) "Hide" else "Read",
                                style = MaterialTheme.typography.labelSmall,
                                color = FoilGold
                            )
                        }

                        AnimatedVisibility(visible = showExplanation) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text(
                                    text = poem.explanationUrdu,
                                    fontFamily = urduFont,
                                    fontSize = 18.sp,
                                    lineHeight = 36.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (poem.explanationEnglish != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = poem.explanationEnglish,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Action Strip (Copy, Toggle Translation, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy verses",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = { showEnglishTranslation = !showEnglishTranslation }) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = "Toggle English translation",
                        tint = if (showEnglishTranslation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Share card",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
