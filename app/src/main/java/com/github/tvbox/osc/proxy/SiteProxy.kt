package com.github.tvbox.osc.proxy

import android.text.TextUtils
import com.github.tvbox.osc.api.ApiConfig
import com.github.tvbox.osc.bean.SourceBean
import com.github.tvbox.osc.util.HawkConfig
import com.github.tvbox.osc.util.KV
import com.github.tvbox.osc.util.LOG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.io.ByteArrayInputStream
import java.net.URLDecoder

/**
 * 站点级代理：每个站点独立开关、独立地址，互不影响。
 *
 * 开关取值优先级：
 *   1. 总闸关闭        -> 全部站点不代理
 *   2. 用户显式关过    -> 关
 *   3. 用户显式开过    -> 开
 *   4. 未设置过        -> 站点配置里带 proxy 字段则开，否则关（跟随配置，不擅自改写）
 *
 * 地址取值优先级：用户填的地址 > 站点配置里的 proxy 字段 > 空（走站点自身脚本，即 AVBox 原有 proxyLocal 路由）
 *
 * 持久化一律用 KV 的 String 通道，不新增 KV 类型，避免踩 KVKeySpec 类型注册。
 */
object SiteProxy {

    private val GSON = Gson()
    private val ADDR_TYPE = object : TypeToken<HashMap<String, String>>() {}.type

    // ---------- 读写 ----------

    private fun readKeys(raw: String?): LinkedHashSet<String> {
        val set = LinkedHashSet<String>()
        if (raw.isNullOrEmpty()) return set
        for (line in raw.split("\n")) {
            val k = line.trim { it <= ' ' }
            if (k.isNotEmpty()) set.add(k)
        }
        return set
    }

    private fun writeKeys(storeKey: String, set: Set<String>) {
        KV.put(storeKey, set.joinToString("\n"))
    }

    private fun onKeys(): LinkedHashSet<String> = readKeys(KV.get(HawkConfig.SITE_PROXY_ON, ""))

    private fun offKeys(): LinkedHashSet<String> = readKeys(KV.get(HawkConfig.SITE_PROXY_OFF, ""))

    private fun addrMap(): HashMap<String, String> {
        val raw = KV.get(HawkConfig.SITE_PROXY_ADDR, "")
        if (raw.isNullOrEmpty()) return HashMap()
        return try {
            GSON.fromJson<HashMap<String, String>>(raw, ADDR_TYPE) ?: HashMap()
        } catch (th: Throwable) {
            LOG.e("site-proxy addr decode failed: " + th.message)
            HashMap()
        }
    }

    private fun writeAddr(map: HashMap<String, String>) {
        KV.put(HawkConfig.SITE_PROXY_ADDR, GSON.toJson(map))
    }

    // ---------- 总闸 ----------

    @JvmStatic
    fun masterOn(): Boolean = KV.get(HawkConfig.SITE_PROXY_MASTER, true)

    @JvmStatic
    fun setMasterOn(on: Boolean) {
        KV.put(HawkConfig.SITE_PROXY_MASTER, on)
    }

    // ---------- 单站开关 ----------

    @JvmStatic
    fun bean(key: String): SourceBean? {
        if (TextUtils.isEmpty(key)) return null
        return try {
            ApiConfig.get().getSource(key)
        } catch (th: Throwable) {
            null
        }
    }

    @JvmStatic
    fun isOverridden(key: String): Boolean = onKeys().contains(key) || offKeys().contains(key)

    /** 未手动设置过时的默认值：站点配置自带 proxy 则开 */
    @JvmStatic
    fun defaultOn(key: String): Boolean {
        val proxy = bean(key)?.proxy
        return !TextUtils.isEmpty(proxy)
    }

    @JvmStatic
    fun isOn(key: String): Boolean {
        if (TextUtils.isEmpty(key)) return false
        if (!masterOn()) return false
        if (offKeys().contains(key)) return false
        if (onKeys().contains(key)) return true
        return defaultOn(key)
    }

    @JvmStatic
    fun setOn(key: String, on: Boolean) {
        if (TextUtils.isEmpty(key)) return
        val onSet = onKeys()
        val offSet = offKeys()
        onSet.remove(key)
        offSet.remove(key)
        if (on) onSet.add(key) else offSet.add(key)
        writeKeys(HawkConfig.SITE_PROXY_ON, onSet)
        writeKeys(HawkConfig.SITE_PROXY_OFF, offSet)
    }

    /** 恢复到"跟随站点配置"的默认状态 */
    @JvmStatic
    fun reset(key: String) {
        val onSet = onKeys()
        val offSet = offKeys()
        onSet.remove(key)
        offSet.remove(key)
        writeKeys(HawkConfig.SITE_PROXY_ON, onSet)
        writeKeys(HawkConfig.SITE_PROXY_OFF, offSet)
    }

    @JvmStatic
    fun setAll(on: Boolean) {
        val keys = allKeys()
        val onSet = LinkedHashSet<String>()
        val offSet = LinkedHashSet<String>()
        for (k in keys) {
            if (on) onSet.add(k) else offSet.add(k)
        }
        writeKeys(HawkConfig.SITE_PROXY_ON, onSet)
        writeKeys(HawkConfig.SITE_PROXY_OFF, offSet)
    }

    @JvmStatic
    fun resetAll() {
        writeKeys(HawkConfig.SITE_PROXY_ON, emptySet())
        writeKeys(HawkConfig.SITE_PROXY_OFF, emptySet())
    }

    @JvmStatic
    fun allKeys(): List<String> {
        return try {
            ApiConfig.get().getSourceBeanList()
                .map { it.key.orEmpty() }
                .filter { it.isNotEmpty() }
        } catch (th: Throwable) {
            emptyList()
        }
    }

    @JvmStatic
    fun enabledKeys(): List<String> = allKeys().filter { isOn(it) }

    // ---------- 地址 ----------

    @JvmStatic
    fun addr(key: String): String {
        val custom = addrMap()[key]
        if (!custom.isNullOrEmpty()) return custom
        return bean(key)?.proxy ?: ""
    }

    @JvmStatic
    fun setAddr(key: String, value: String) {
        val map = addrMap()
        val trimmed = value.trim { it <= ' ' }
        if (trimmed.isEmpty()) map.remove(key) else map[key] = trimmed
        writeAddr(map)
    }

    @JvmStatic
    fun isAddrCustom(key: String): Boolean = addrMap().containsKey(key)

    // ---------- 代理放行 ----------

    /**
     * 站点代理关闭时的处理：不改写、不转发，直接 302 回源，让播放器直连原始地址。
     *
     * 注意不能返回 null —— RemoteServer.getProxy 对 null 会回 500，
     * 那会变成"关掉代理就播不了"，而不是"关掉代理就直连"。
     */
    @JvmStatic
    fun bypass(rawUrl: String?): Array<Any?>? {
        var url = rawUrl
        if (url == null) return null
        url = try {
            URLDecoder.decode(url, "UTF-8")
        } catch (th: Throwable) {
            url
        }
        if (TextUtils.isEmpty(url)) return null
        if (!url!!.startsWith("http://") && !url.startsWith("https://")) return null
        val headers = HashMap<String, String>()
        headers["Location"] = url
        return arrayOf(302, "text/plain", ByteArrayInputStream(ByteArray(0)), headers)
    }
}
