package io.wenyou.textquest.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Material You 动态配色关闭时的回退主题种子与基础色。
 * 星蓝、冷白与蓝灰；打开「动态配色」后由系统壁纸取色覆盖。
 */
val BrandPrimary = Color(0xFF365E87)
val BrandOnPrimary = Color(0xFFFFFFFF)
val BrandPrimaryContainer = Color(0xFFD8E9FC)
val BrandOnPrimaryContainer = Color(0xFF102B45)

val BrandSecondary = Color(0xFF566477)
val BrandOnSecondary = Color(0xFFFFFFFF)
val BrandSecondaryContainer = Color(0xFFDEE7F1)
val BrandOnSecondaryContainer = Color(0xFF1A2635)

val BrandTertiary = Color(0xFF715D35)
val BrandTertiaryContainer = Color(0xFFF3E4B9)

val BrandBackgroundLight = Color(0xFFF4F7FA)
val BrandSurfaceLight = Color(0xFFFBFCFF)
val BrandBackgroundDark = Color(0xFF101820)
val BrandSurfaceDark = Color(0xFF18222D)

/**
 * 头像 / 封面 / 节点标签的固定取色板（独立于主题，保证不同存档间辨识度一致）。
 * 12 色，圆角形态下做 tonal 头像与封面渐变。
 */
val AvatarPalette: List<Color> = listOf(
    Color(0xFF6750A4), // 紫
    Color(0xFF006A6A), // 青
    Color(0xFF7D5260), // 玫红
    Color(0xFF386A20), // 绿
    Color(0xFF825500), // 琥珀
    Color(0xFF0842A0), // 蓝
    Color(0xFFB3261E), // 红
    Color(0xFF006C4C), // 翠
    Color(0xFF65558F), // 蓝紫
    Color(0xFFB05712), // 橙
    Color(0xFF455A64), // 蓝灰
    Color(0xFF6A3C00)  // 棕
)

fun avatarColor(index: Int): Color =
    AvatarPalette[((index % AvatarPalette.size) + AvatarPalette.size) % AvatarPalette.size]

