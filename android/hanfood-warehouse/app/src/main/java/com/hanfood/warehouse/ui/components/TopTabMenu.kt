package com.hanfood.warehouse.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

data class TopTab(val route: String, val label: String, val icon: ImageVector)

/**
 * Primary navigation, deliberately placed at the top of the app (below
 * [BrandHeader]) rather than as a bottom bar. The active tab is bold and
 * colored in the brand green; inactive tabs are muted — tapping a tab
 * visibly darkens/highlights it.
 */
@Composable
fun TopTabMenu(
    tabs: List<TopTab>,
    currentRoute: String?,
    onSelect: (String) -> Unit
) {
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Tab(
                selected = selected,
                onClick = { onSelect(tab.route) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    androidx.compose.material3.Text(
                        tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) }
            )
        }
    }
}
