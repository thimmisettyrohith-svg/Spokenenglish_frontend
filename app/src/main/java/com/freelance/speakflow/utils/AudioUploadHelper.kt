package com.freelance.speakflow.ui.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

fun audioFileToMultipart(file: File): MultipartBody.Part {
    val requestBody = RequestBody.create(
        "audio/m4a".toMediaType(),
        file
    )

    return MultipartBody.Part.createFormData(
        name = "file",
        filename = file.name,
        body = requestBody
    )
}

fun textPart(value: String): RequestBody =
    RequestBody.create("text/plain".toMediaType(), value)
