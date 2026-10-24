package com.github.tvbox.osc.ui.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.tvbox.osc.R
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.bean.SourceBean
import com.github.tvbox.osc.proxy.SiteProxy
import com.github.tvbox.osc.ui.components.LoadState
import com.github.tvbox.osc.ui.components.LoadStateBox
import com.github.tvbox.osc.ui.components.SettingsCard
import com.github.tvbox.osc.ui.components.SettingsCardPosition
import com.github.tvbox.osc.ui.components.SettingsGroup
import com.github.tvbox.osc.ui.components.SettingsSwitchRow
import com.github.tvbox.osc.ui.components.SiteProxySheet

/**
 * 本地脚本子模块：与点播同级的独立 tab。
 * 只负责"脚本站点"这一类（type 3 / api 以 .py、.js 结尾），
 * 列表、切换、代理开关都复用 AVBox 现有的 ApiConfig 与设置组件。
 */
@Composable
fun ScriptPage(contentPadding: PaddingValues) {
    val sources = remember {
        ApiConfig.get().getSourceBeanList().filter(::isScriptSite)
    }
    var showProxySheet by remember { mutableStateOf(false) }
    var master by remember { mutableStateOf(SiteProxy.masterOn()) }
    val switches = remember {
        mutableStateMapOf<String, Boolean>().apply {
            sources.forEach { put(it.key.orEmpty(), SiteProxy.isOn(it.key.orEmpty())) }
        }
    }
    val currentKey = remember { ApiConfig.get().getHomeSourceBean().key.orEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        SettingsGroup(title = stringResource(R.string.script_proxy_group)) {
            SettingsCard(
                position = SettingsCardPosition.SINGLE,
                color = MaterialTheme.colorScheme.surfaceBright,
            ) {
                SettingsSwitchRow(
                    title = stringResource(R.string.site_proxy_master),
                    subtitle = stringResource(R.string.site_proxy_master_hint),
                    checked = master,
                    onCheckedChange = {
                        master = it
                        SiteProxy.setMasterOn(it)
                        switches.clear()
                        sources.forEach { bean -> switches[bean.key.orEmpty()] = SiteProxy.isOn(bean.key.orEmpty()) }
                    },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.script_site_list),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { showProxySheet = true }) {
                Text(stringResource(R.string.site_proxy_title))
            }
        }

        if (sources.isEmpty()) {
            LoadStateBox(
                state = LoadState.Empty,
                emptyText = stringResource(R.string.script_site_empty),
                errorText = "",
                retryText = "",
                emptyIconRes = R.drawable.ic_empty_record,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )
        } else {
            SettingsGroup(title = null) {
                sources.forEachIndexed { index, bean ->
                    val key = bean.key.orEmpty()
                    SettingsCard(
                        position = when {
                            sources.size == 1 -> SettingsCardPosition.SINGLE
                            index == 0 -> SettingsCardPosition.FIRST
                            index == sources.size - 1 -> SettingsCardPosition.LAST
                            else -> SettingsCardPosition.MIDDLE
                        },
                        color = MaterialTheme.colorScheme.surfaceBright,
                    ) {
                        SettingsSwitchRow(
                            title = bean.name ?: key,
                            subtitle = stringResource(
                                R.string.script_row_hint,
                                kindOf(bean),
                                if (key == currentKey) stringResource(R.string.script_row_current) else "",
                            ),
                            subtitleMaxLines = 2,
                            checked = switches[key] == true,
                            onCheckedChange = { next ->
                                switches[key] = next
                                SiteProxy.setOn(key, next)
                            },
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.script_switch_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            )
        }
    }

    if (showProxySheet) {
        SiteProxySheet(onDismiss = { showProxySheet = false })
    }
}

private fun isScriptSite(bean: SourceBean): Boolean {
    if (bean.key.isNullOrEmpty()) return false
    val api = bean.api.orEmpty()
    if (api.endsWith(".py", true) || api.endsWith(".js", true)) return true
    return bean.type == 3
}

private fun kindOf(bean: SourceBean): String {
    val api = bean.api.orEmpty()
    return when {
        api.endsWith(".py", true) -> "py"
        api.endsWith(".js", true) -> "js"
        else -> "type " + bean.type
    }
}
