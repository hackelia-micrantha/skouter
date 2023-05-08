package com.micrantha.bluebell

import com.micrantha.bluebell.domain.bluebellDomain
import com.micrantha.bluebell.domain.i18n.LocalizedRepository
import com.micrantha.bluebell.ui.bluebellUi
import org.koin.dsl.module

fun bluebellModules() = module {
    includes(bluebellDomain(), bluebellUi())

    single<LocalizedRepository> { get<Platform>() }
}
