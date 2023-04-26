package com.micrantha.bluebell

import com.micrantha.bluebell.domain.i18n.LocalizedRepository
import com.micrantha.bluebell.domain.i18n.LocalizedString
import platform.Foundation.NSLocalizedString
import platform.UIKit.UIDevice

actual class Platform : LocalizedRepository {
    actual val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    actual override fun resource(str: LocalizedString): String {
        return NSLocalizedString(str.iosKey ?: str.key)
    }
}