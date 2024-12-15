package com.example.mintlifesciences.model

data class Medicine(
    var image: String? = null,
    var name: String? = null,
    var salt: String? = null,
    var description: String? = null,
    var uses: String? = null,
    var mrp: String? = null,
    var videoUrl: String? = null,
    var pdfUrl: String? = null
)