package com.ammar.diwan.ui.dashboard

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.ui.theme.DiwanTypography
import com.ammar.diwan.ui.theme.FoilGold
import com.ammar.diwan.ui.theme.InkMuted
import com.ammar.diwan.ui.theme.LapisNavy
import com.ammar.diwan.ui.theme.ParchmentBorder
import kotlin.math.absoluteValue

@Composable
fun PoemDeck(
    poems: List<Poem>,
    showRomanUrdu: Boolean,
    modifier: Modifier = Modifier,
    onCopyPoem: (Poem) -> Unit = {},
    onSharePoem: (Poem) -> Unit = {}
) {
    if (poems.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No poetry found in this collection.",
                style = DiwanTypography.bodyMedium,
                color = InkMuted
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { poems.size })

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Carousel Pager
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val scale = lerp(
                start = 0.94f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )

            PoemCard(
                poem = poems[page],
                showRomanUrdu = showRomanUrdu,
                onCopy = { onCopyPoem(poems[page]) },
                onShare = { onSharePoem(poems[page]) },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page Indicator (Capsule Dots & Tabular Counter)
        Row(
            modifier = Modifier
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            poems.forEachIndexed { index, _ ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 20.dp else 6.dp, 6.dp)
                        .background(
                            color = if (isSelected) LapisNavy else ParchmentBorder,
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        Text(
            text = "${pagerState.currentPage + 1} of ${poems.size}",
            style = DiwanTypography.labelSmall,
            color = InkMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
