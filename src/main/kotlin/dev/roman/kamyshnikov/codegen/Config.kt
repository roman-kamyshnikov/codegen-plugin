package dev.roman.kamyshnikov.codegen

object Config {
    object Input {
        var apiFileSuffix = "Api.kt"
        var apiPackagePrefix = "dev.roman.kamyshnikov.codegen.samples.core.network.apis."
        var unwrapRetrofitResponse = true
    }

    object Output {
        const val DOMAIN_PACKAGE = "domain"
        const val MODEL_PACKAGE = "model"
    }

    object Service {
        const val NOTIFICATION_GROUP_ID = "dev.roman.kamyshnikov.codegen"
    }
}