package com.kinemaster

import java.io.BufferedInputStream
import java.io.IOException

fun getSigBytes(inStream: BufferedInputStream, sig : ByteArray) {
    val bytes: Int

    try {
        inStream.mark(sig.size)
        bytes = inStream.read(sig)
        inStream.reset()
    } catch (e: IOException) {
        throw Exception("Error reading project file", e)
    }

    if (bytes < sig.size) {
        throw Exception("Project file is invalid or damaged (too small)")
    }
}