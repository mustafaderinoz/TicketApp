package com.mustafaderinoz.core.util

import com.mustafaderinoz.core.domain.error.AppError

enum class ErrorContext {
    GENERIC, LOGIN, REGISTER,HOME, EVENT_DETAIL,
    PURCHASE_CREATE, PAY, TICKET_DETAIL
}

fun AppError.toUserMessage(context: ErrorContext = ErrorContext.GENERIC): String = when (this) {
    is AppError.Network -> "İnternet bağlantısı yok"
    is AppError.Unknown -> message ?: "Bilinmeyen bir hata oluştu."
    is AppError.Api     -> resolveApiMessage(code, context)
}

private fun resolveApiMessage(code: Int, context: ErrorContext): String = when {
    code in 500..599 -> "Sunucu şu anda cevap veremiyor"

    context == ErrorContext.LOGIN            && code == 401 -> "Email veya şifre hatalı"
    context == ErrorContext.REGISTER         && code == 400 -> "Geçersiz email veya şifre formatı"
    context == ErrorContext.REGISTER         && code == 409 -> "Bu email zaten kayıtlı"
    context == ErrorContext.EVENT_DETAIL     && code == 404 -> "Etkinlik bulunamadı"
    context == ErrorContext.PURCHASE_CREATE  && code == 400 -> "Geçersiz bilet adedi veya veri formatı."
    context == ErrorContext.PURCHASE_CREATE  && code == 404 -> "Seçilen bilet türü bulunamadı."
    context == ErrorContext.PURCHASE_CREATE  && code == 409 -> "Kapasite aşıldı, lütfen sayfayı yenileyip stokları kontrol edin."
    context == ErrorContext.PAY              && code == 403 -> "Bu satın alım işlemini onaylama yetkiniz yok."
    context == ErrorContext.PAY              && code == 404 -> "Satın alım kaydı bulunamadı."
    context == ErrorContext.PAY              && code == 409 -> "Bu bilet zaten ödenmiş veya stok tükenmiş."

    else -> "Beklenmeyen bir hata oluştu"
}