package io.wenyou.textquest

import android.content.Context
import io.wenyou.textquest.data.ai.AiDirector
import io.wenyou.textquest.data.llm.ChatClient
import io.wenyou.textquest.data.llm.UsageTracker
import io.wenyou.textquest.data.model.AppBundle
import io.wenyou.textquest.data.model.AppJson
import io.wenyou.textquest.data.model.CharacterData
import io.wenyou.textquest.data.repo.DevMode
import io.wenyou.textquest.data.repo.LocalLibrary
import io.wenyou.textquest.data.repo.SettingsStore
import io.wenyou.textquest.data.repo.ShareInbox
import java.io.File
import java.io.InputStream

/** Same shape as the Android app's WenYouApp, so screens that use WenYouApp.AppContainer are shared unchanged. */
object WenYouApp {
/** 进程级手动依赖注入容器（避免引入 Hilt，保持工程轻量）。 */
class AppContainer(context: Context, suppliedClient: ChatClient? = null) {
    val library = LocalLibrary(context)
    val settings = SettingsStore(context)
    val chatClient = suppliedClient ?: ChatClient(usage = UsageTracker(File(context.filesDir, "usage.json")))
    val director = AiDirector(chatClient)

    init {
        // Every AI request built by this client carries the player's baseline.
        chatClient.baseline = library::currentBaseline
        chatClient.outputLanguage = { io.wenyou.textquest.ui.common.resolveLanguage(settings.state.value.appearance.language) }
    }
    val shareInbox = ShareInbox(context)
    val devMode = DevMode(context)

    /**
     * Preset packs merged at startup, as in the Android app's WenYouApp; [openAsset] reads a bundled asset.
     * The Android app's one-off repair of adult flags from early builds is not needed: there is no such data here.
     */
    suspend fun seedLibrary(openAsset: (String) -> InputStream) {
        applyPresetAssets(openAsset, listOf(
            "presets/wenyou-bare-presets.json",
            "presets/wenyou-bare2-presets.json",
            "presets/wenyou-genres-presets.json",
            "presets/wenyou-genres2-presets.json"
        ))
        applyPresetAssets(openAsset, listOf("presets/wenyou-adult-straight-presets.json"), markAdult = true)
        enrichBuiltinInitials(openAsset)
    }

    /**
     * 一次性为「已存在且未配置」的内置角色补齐 [CharacterData.initial]。
     * 非破坏性：只填充初始状态为空的角色，不覆盖用户自定，
     * 也不会把用户删除的内置内容重新写回（仅针对当前仍存在的 id）。
     */
    private suspend fun enrichBuiltinInitials(openAsset: (String) -> InputStream) {
        if (settings.presetEnrichDone) return
        try {
            val names = listOf(
                "presets/wenyou-bare-presets.json",
                "presets/wenyou-bare2-presets.json",
                "presets/wenyou-adult-straight-presets.json"
            )
            val existing = library.characters.value.associateBy { it.id }
            var changedInitial = false
            val updates = mutableListOf<CharacterData>()
            for (name in names) {
                val text = openAsset(name).bufferedReader(Charsets.UTF_8).use { it.readText() }
                val bundle = AppJson.decodeFromString(AppBundle.serializer(), text)
                for (c in bundle.characters) {
                    val cur = existing[c.id] ?: continue
                    if (cur.initial.metrics.isEmpty() && c.initial.metrics.isNotEmpty()) {
                        updates += cur.copy(initial = c.initial)
                        changedInitial = true
                    }
                }
            }
            for (cc in updates) library.upsertCharacter(cc)
            if (changedInitial) settings.presetEnrichDone = true
        } catch (_: Throwable) {
            // 补齐失败不阻塞主流程，下次启动重试
        }
    }

    /** 逐个资源去重合并（按 id 只补不覆盖）。 */
    private suspend fun applyPresetAssets(openAsset: (String) -> InputStream, presetFiles: List<String>, markAdult: Boolean = false) {
        // 每个资源文件只成功合并一次并记录状态；否则每次启动都会全量重扫，
        // 既重复解析，也会把用户已删除的内置内容重新写回
        val already = settings.appliedPresetFiles()
        for (name in presetFiles) {
            if (name in already) continue
            try {
                applyPresetAsset(openAsset, name, markAdult)
                settings.markPresetFileApplied(name)
            } catch (_: Throwable) {
                // 单个资源失败不影响其它资源与主流程，下次启动重试
            }
        }
    }

    private suspend fun applyPresetAsset(openAsset: (String) -> InputStream, name: String, markAdult: Boolean) {
        val text = openAsset(name).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val bundle = AppJson.decodeFromString(AppBundle.serializer(), text)
        val charIds = library.characters.value.mapTo(mutableSetOf()) { it.id }
        for (c in bundle.characters) {
            if (charIds.add(c.id)) library.upsertCharacter(if (markAdult) c.copy(adult = true) else c)
        }
        val storyIds = library.stories.value.mapTo(mutableSetOf()) { it.id }
        for (s in bundle.stories) {
            if (storyIds.add(s.id)) library.upsertStory(if (markAdult) s.copy(adult = true) else s)
        }
    }
}
}
