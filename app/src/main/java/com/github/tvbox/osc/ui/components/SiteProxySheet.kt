package com.github.tvbox.osc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.tvbox.osc.R
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.proxy.SiteProxy

/**
 * 站点代理面板：总闸 / 全开 / 全关 / 单站开关 / 单站地址。
 * 开关状态存在 SiteProxy（KV），面板只负责读写与展示。
 */
@Composable
fun SiteProxySheet(onDismiss: () -> Unit) {
    val sources = remember {
        ApiConfig.get().getSourceBeanList()
            .filter { !it.key.isNullOrEmpty() }
            .sortedBy { it.name ?: it.key.orEmpty() }
    }
    var master by remember { mutableStateOf(SiteProxy.masterOn()) }
    val switches = remember {
        mutableStateMapOf<String, Boolean>().apply {
            sources.forEach { put(it.key.orEmpty(), SiteProxy.isOn(it.key.orEmpty())) }
        }
    }
    val addrs = remember {
        mutableStateMapOf<String, String>().apply {
            sources.forEach { put(it.key.orEmpty(), SiteProxy.addr(it.key.orEmpty())) }
        }
    }
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * 0.8f).dp

    AVBoxBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.site_proxy_title),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.heightIn(max = maxSheetHeight),
    ) {
        SettingsGroup(title = null, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
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
                    },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.site_proxy_list),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {
                SiteProxy.setAll(true)
                switches.clear()
                sources.forEach { switches[it.key.orEmpty()] = true }
            }) { Text(stringResource(R.string.site_proxy_all_on)) }
            TextButton(onClick = {
                SiteProxy.setAll(false)
                switches.clear()
                sources.forEach { switches[it.key.orEmpty()] = false }
            }) { Text(stringResource(R.string.site_proxy_all_off)) }
            TextButton(onClick = {
                SiteProxy.resetAll()
                switches.clear()
                sources.forEach { switches[it.key.orEmpty()] = SiteProxy.defaultOn(it.key.orEmpty()) }
            }) { Text(stringResource(R.string.site_proxy_follow_config)) }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            itemsIndexed(sources) { index, bean ->
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
                    val checked = switches[key] == true
                    SettingsSwitchRow(
                        title = bean.name ?: key,
                        subtitle = stringResource(R.string.site_proxy_row_hint, SiteProxy.addr(key).ifEmpty { stringResource(R.string.site_proxy_addr_none) }),
                        subtitleMaxLines = 1,
                        checked = checked,
                        onCheckedChange = { next ->
                            switches[key] = next
                            SiteProxy.setOn(key, next)
                        },
                        valueText = if (SiteProxy.isOverridden(key)) stringResource(R.string.site_proxy_manual) else null,
                    )
                    if (checked) {
                        OutlinedTextField(
                            value = addrs[key] ?: "",
                            onValueChange = { text ->
                                addrs[key] = text
                                SiteProxy.setAddr(key, text)
                            },
                            label = { Text(stringResource(R.string.site_proxy_addr_hint)) },
                            singleLine = true,
                            maxLines = 1,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        )
                    }
                }
            }
        }
    }
}
