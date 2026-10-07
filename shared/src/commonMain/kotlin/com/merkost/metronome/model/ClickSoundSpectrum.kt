package com.merkost.metronome.model

internal data class ClickSoundSpectrum(
    val bars: List<Float>,
    val resourceName: String,
    val sourceSha256: String,
) {
    companion object {
        const val barCount = 32
        const val minFrequencyHz = 80
        const val maxFrequencyHz = 12000
    }
}

internal val ClickSound.spectrum: ClickSoundSpectrum
    get() = when (this) {
        ClickSound.WOOD -> woodSpectrum
        ClickSound.CLICK -> clickSpectrum
        ClickSound.CLASSIC -> classicSpectrum
        ClickSound.SOFT -> softSpectrum
        ClickSound.RIM -> rimSpectrum
        ClickSound.CLAVE -> claveSpectrum
        ClickSound.STUDIO -> studioSpectrum
    }

private val woodSpectrum = ClickSoundSpectrum(
    bars = listOf(
        0.0012f, 0.0008f, 0.0007f, 0.0009f, 0.0007f, 0.0009f, 0.0018f, 0.0019f,
        0.0022f, 0.0028f, 0.0023f, 0.0017f, 0.0024f, 0.0044f, 0.0050f, 0.0273f,
        0.2031f, 1.0000f, 0.1131f, 0.0485f, 0.0244f, 0.0368f, 0.0685f, 0.0728f,
        0.0100f, 0.0149f, 0.0112f, 0.0214f, 0.0078f, 0.0073f, 0.0034f, 0.0013f,
    ),
    resourceName = "wood.mp3",
    sourceSha256 = "81d2403b05734a97c5dce9fba90095df6e7be67d13c9d61b70ef7c2ba4c454a6",
)

private val clickSpectrum = ClickSoundSpectrum(
    bars = listOf(
        0.0099f, 0.0122f, 0.0146f, 0.0091f, 0.0304f, 0.0474f, 0.0651f, 0.0621f,
        0.0381f, 0.0602f, 0.1851f, 1.0000f, 0.6734f, 0.3345f, 0.1682f, 0.0822f,
        0.3607f, 0.8870f, 0.4350f, 0.1542f, 0.5663f, 0.3081f, 0.2171f, 0.1622f,
        0.0934f, 0.1034f, 0.1184f, 0.0795f, 0.0519f, 0.0413f, 0.0524f, 0.0412f,
    ),
    resourceName = "click.mp3",
    sourceSha256 = "5e41f07e4a3cf000c90b6abeb89a1cac1ce2b27835ed4556ac7d9f064f1f7b50",
)

private val classicSpectrum = ClickSoundSpectrum(
    bars = listOf(
        0.0022f, 0.0017f, 0.0020f, 0.0020f, 0.0020f, 0.0018f, 0.0019f, 0.0024f,
        0.0032f, 0.0113f, 0.0796f, 0.5064f, 1.0000f, 0.7700f, 0.1596f, 0.1130f,
        0.1218f, 0.1147f, 0.0880f, 0.0898f, 0.0793f, 0.1430f, 0.0921f, 0.1597f,
        0.1340f, 0.1075f, 0.0542f, 0.0223f, 0.0091f, 0.0043f, 0.0025f, 0.0023f,
    ),
    resourceName = "metronome.wav",
    sourceSha256 = "a1904415fd138f3aa4f796a4b6bd48a961cb34b705a674e2ad257278fb5ae955",
)

private val softSpectrum = ClickSoundSpectrum(
    bars = listOf(
        0.0133f, 0.0149f, 0.0169f, 0.0194f, 0.0223f, 0.0259f, 0.0305f, 0.0364f,
        0.0441f, 0.0549f, 0.0709f, 0.0972f, 0.1538f, 0.4588f, 1.0000f, 0.1673f,
        0.0750f, 0.0449f, 0.0920f, 0.0232f, 0.0070f, 0.0035f, 0.0021f, 0.0007f,
        0.0007f, 0.0004f, 0.0003f, 0.0001f, 0.0001f, 0.0001f, 0.0001f, 0.0000f,
    ),
    resourceName = "soft.wav",
    sourceSha256 = "c03921061d0b8492ccf2895f4354e4a36588c16f8cd08229b332eb9b80beb2d2",
)

private val rimSpectrum = ClickSoundSpectrum(
    bars = listOf(
        0.0192f, 0.0186f, 0.0168f, 0.0149f, 0.0157f, 0.0204f, 0.0258f, 0.0263f,
        0.0213f, 0.0162f, 0.0181f, 0.0564f, 0.0694f, 0.0547f, 0.0339f, 0.0321f,
        0.0656f, 0.1037f, 0.1743f, 1.0000f, 0.3472f, 0.1374f, 0.1351f, 0.4551f,
        0.0951f, 0.0472f, 0.0376f, 0.0423f, 0.0364f, 0.0541f, 0.0409f, 0.0477f,
    ),
    resourceName = "rim.wav",
    sourceSha256 = "8beafbaceeb07c067b6d042bf08b0c0e2a214d6e48594b2b3b55bba896ce1d66",
)

private val claveSpectrum = ClickSoundSpectrum(
    bars = listOf(
        0.0009f, 0.0010f, 0.0012f, 0.0014f, 0.0016f, 0.0018f, 0.0020f, 0.0022f,
        0.0024f, 0.0025f, 0.0024f, 0.0021f, 0.0013f, 0.0013f, 0.0044f, 0.0104f,
        0.0209f, 0.0397f, 0.0803f, 0.2715f, 1.0000f, 0.1066f, 0.2068f, 0.0460f,
        0.0102f, 0.0019f, 0.0018f, 0.0008f, 0.0004f, 0.0003f, 0.0002f, 0.0001f,
    ),
    resourceName = "clave.wav",
    sourceSha256 = "d4f9940252e067497f2db8994148ab38c32619d0e00be9d31edd72bd20391e0e",
)

private val studioSpectrum = ClickSoundSpectrum(
    bars = listOf(
        0.0095f, 0.0093f, 0.0090f, 0.0086f, 0.0081f, 0.0074f, 0.0065f, 0.0054f,
        0.0046f, 0.0054f, 0.0086f, 0.0144f, 0.0229f, 0.0357f, 0.0546f, 0.0857f,
        0.1450f, 0.3249f, 1.0000f, 0.2785f, 0.0971f, 0.0475f, 0.1194f, 0.0834f,
        0.0132f, 0.0033f, 0.0021f, 0.0014f, 0.0007f, 0.0005f, 0.0003f, 0.0002f,
    ),
    resourceName = "studio.wav",
    sourceSha256 = "e4e0cce96bbd4d34b0e916bb87dcfe619448330f3f86beb5ebbfb858eab6a45d",
)
