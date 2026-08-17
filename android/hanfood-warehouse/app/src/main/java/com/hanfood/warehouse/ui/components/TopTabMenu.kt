package com.hanfood.warehouse.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class TopTab(val route: String, val label: String, val icon: ImageVector)

/**
 * Primary navigation, deliberately placed at the top of the app (below
 * [BrandHeader]) rather than as a bottom bar. The active tab is bold and
 * colored in the brand green; inactive tabs are muted — tapping a tab
 * visibly darkens/highlights it.
 *
 * Uses [ScrollableTabRow] rather than a fixed-width [androidx.compose.material3.TabRow]:
 * with 5 tabs and longer labels (Russian "Накладные", German compounds,
 * etc.) an evenly-divided fixed row squeezes each tab too narrow and wraps
 * the label onto two lines. A scrollable row instead sizes each tab to its
 * own content, so labels always stay on one line.
 */
@Composable
fun TopTabMenu(
    tabs: List<TopTab>,
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 12.dp
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onSelect(tab.route) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
                text = {
                    Text(
                        tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) }
            )
        }
    }
}
