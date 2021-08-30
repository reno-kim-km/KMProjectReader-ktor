package com.kinemaster.decoder.constants

import decoder.protobuffer.FourCC


const val FILE_EXTENSION_KINE = "kine"
const val FILE_EXTENSION_KMPROJECT = "kmproject"
const val DIRECTORY_NAME_CONTENTS = "contents"

@Suppress("SpellCheckingInspection")
internal val FOURCC_KHDR: Int = FourCC.fromChars('K', 'H', 'D', 'R')
@Suppress("SpellCheckingInspection")
internal val FOURCC_TLIN = FourCC.fromChars('T', 'L', 'I', 'N')
@Suppress("SpellCheckingInspection")
internal val FOURCC_FTRX = FourCC.fromChars('F', 'T', 'R', 'X')
