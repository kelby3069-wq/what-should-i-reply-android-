package com.replysense.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.replysense.app.ui.theme.AppSpacing

/**
 * ReplySense Premium Components
 * - Consistent surfaces + spacing + hierarchy
 * - Stripe/YC-ish feel without being "default Material"
 */

/* -----------------------------
   Layout wrappers
   ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RSScaffold(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RSAppTopBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions
            )
        },
        bottomBar = { bottomBar?.invoke() },
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RSAppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = { navigationIcon?.invoke() ?: Spacer(Modifier.width(12.dp)) },
        actions = actions,
        colors = centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun RSScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.screen)
            .padding(top = AppSpacing.lg, bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        content = content
    )
}

/* -----------------------------
   Cards / Surfaces
   ----------------------------- */

@Composable
fun RSCard(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(AppSpacing.card),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = tonalElevation,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            content = content
        )
    }
}

@Composable
fun RSSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

/* -----------------------------
   Buttons
   ----------------------------- */

@Composable
fun RSPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    ) {
        if (leading != null) {
            Box(Modifier.padding(end = 10.dp)) { leading() }
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun RSSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: @Composable (() -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = MaterialTheme.shapes.medium,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
    ) {
        if (leading != null) {
            Box(Modifier.padding(end = 10.dp)) { leading() }
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun RSTertiaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/* -----------------------------
   Chips / Pills
   ----------------------------- */

@Composable
fun RSChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(
                role = Role.Button,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        color = bg,
        tonalElevation = if (selected) 2.dp else 0.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = fg
        )
    }
}

/* -----------------------------
   Input fields
   ----------------------------- */

@Composable
fun RSInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        label = { Text(label) },
        placeholder = { if (placeholder != null) Text(placeholder) },
        supportingText = { if (supportingText != null) Text(supportingText) },
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

/* -----------------------------
   Premium list row
   ----------------------------- */

@Composable
fun RSListRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null) {
        Modifier.clickable(
            role = Role.Button,
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )
    } else Modifier

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(clickMod),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (leading != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { leading() }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (trailing != null) {
                Box(contentAlignment = Alignment.CenterEnd) { trailing() }
            }
        }
    }
}

/* -----------------------------
   Empty state
   ----------------------------- */

@Composable
fun RSEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    primaryActionText: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.screen),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) { icon() }
            }

            Spacer(Modifier.height(AppSpacing.lg))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(AppSpacing.xl))

        if (primaryActionText != null && onPrimaryAction != null) {
            RSPrimaryButton(
                text = primaryActionText,
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (secondaryActionText != null && onSecondaryAction != null) {
            Spacer(Modifier.height(AppSpacing.sm))
            RSSecondaryButton(
                text = secondaryActionText,
                onClick = onSecondaryAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* -----------------------------
   Small premium utility
   ----------------------------- */

@Composable
fun RSBadge(
    text: String,
    modifier: Modifier = Modifier,
    bg: Color = MaterialTheme.colorScheme.surfaceVariant,
    fg: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = bg,
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = fg
        )
    }
}
