package com.woa.helper.dbkp

import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Dbkp{
    fun hasHeader(kernelBuffer: ByteArray): Boolean{
        if (kernelBuffer.size >= 0x10) {
            val hdr = kernelBuffer.copyOfRange(0, 0x10)
            val hdrStr = hdr.toString(Charsets.US_ASCII)
            if (hdrStr == "UNCOMPRESSED_IMG"){
                return true
            }
        }
        return false
    }
    fun patch(kernel : File,fd : File,shellCode: File,patched : File,config: File):Int{
        //code adapted from https://github.com/Project-Aloha/DualBootKernelPatcher
        if (!kernel.exists() || !fd.exists() || !shellCode.exists() || !config.exists())
            return -1
        if (!kernel.canRead() || !fd.canRead() || !shellCode.canRead() || !config.canRead())
            return -2
        if (patched.exists() && !patched.canWrite())
            return -21

        val kernelBuffer = kernel.readBytes()
        val fdBuffer = fd.readBytes()
        val shellCodeBuffer = shellCode.readBytes()
        val configBuffer = config.readBytes()

        var kernelSize = kernel.length()
        var patchedSize = kernelSize + fd.length()
        val patchedBuffer = ByteArray(patchedSize.toInt()+0x10)

        System.arraycopy(kernelBuffer, 0, patchedBuffer, 0, kernelBuffer.size)
        System.arraycopy(fdBuffer, 0, patchedBuffer, kernelBuffer.size, fdBuffer.size)

        if (shellCode.length() > 0x40) {
            if (shellCodeBuffer.size < 15) return -3
            val magic = "SHLLCOD".toByteArray(Charsets.US_ASCII)
            var ok = true
            var i = 0
            while (i < magic.size) {
                if (shellCodeBuffer[8 + i] != magic[i]) { ok = false; break }
                i++
            }
            if (!ok) return -3
        }

        val header = hasHeader(kernelBuffer)
        if (header) kernelSize -= 0x14
        val base = if (header) 0x14 else 0
        if (kernelSize % 0x10 != 0L) {
            val padding = 0x10 - (kernelSize % 0x10)
            kernelSize += padding
            patchedSize += padding

            System.arraycopy(
                patchedBuffer,
                (kernelSize - padding + base).toInt(),
                patchedBuffer,
                kernelSize.toInt(),
                fdBuffer.size
            )
            System.arraycopy(
                ByteArray(padding.toInt()),
                0,
                patchedBuffer,
                (kernelSize - padding + base).toInt(),
                padding.toInt()
            )
        }


        val b3 = patchedBuffer[base + 3].toInt() and 0xFF
        val b7 = patchedBuffer[base + 7].toInt() and 0xFF

        if (b3 == 0x14 && b7 != 0x14) {
            var instr = (patchedBuffer[base + 0].toInt() and 0xFF) or
                    ((patchedBuffer[base + 1].toInt() and 0xFF) shl 8) or
                    ((patchedBuffer[base + 2].toInt() and 0xFF) shl 16)
            instr -= 1
            patchedBuffer[base + 4] = (instr and 0xFF).toByte()
            patchedBuffer[base + 5] = ((instr ushr 8) and 0xFF).toByte()
            patchedBuffer[base + 6] = ((instr ushr 16) and 0xFF).toByte()
            patchedBuffer[base + 7] = 0x14.toByte()
        } else if (b3 == 0x14 && b7 == 0x14) {
            var oldKernelSize = 0L
            var j = 0
            while (j < 8) {
                oldKernelSize = oldKernelSize or ((patchedBuffer[base + 0x30 + j].toLong() and 0xFFL) shl (8 * j))
                j++
            }
            kernelSize = oldKernelSize
        } else {
            val mz = (patchedBuffer[base + 0].toInt() and 0xFF) == 0x4D &&
                    (patchedBuffer[base + 1].toInt() and 0xFF) == 0x5A
            if (mz && b7 == 0x14) {
                if (b7 != 0x14) return -4
            }
        }

        patchedBuffer[base + 0] = 0x10.toByte()
        patchedBuffer[base + 1] = 0
        patchedBuffer[base + 2] = 0
        patchedBuffer[base + 3] = 0x14.toByte()

        val configText = try { configBuffer.toString(Charsets.UTF_8) } catch (_: Throwable) { "" }

        var stackBase = 0L
        var stackSize = 0L

            val lines = configText.split('\n')
            var idx = 0
            while (idx < lines.size) {
                val line = lines[idx].trim()
                if (line.startsWith("StackBase")) {
                    val v = line.substringAfter('=').trim()
                    stackBase = if (v.startsWith("0x") || v.startsWith("0X")) v.substring(2).toLong(16) else v.toLong()
                } else if (line.startsWith("StackSize")) {
                    val v = line.substringAfter('=').trim()
                    stackSize = if (v.startsWith("0x") || v.startsWith("0X")) v.substring(2).toLong(16) else v.toLong()
                }
                idx++
            }


        var k = 0
        while (k < 8) {
            patchedBuffer[base + 0x20 + k] = ((stackBase ushr (8 * k)) and 0xFF).toByte()
            k++
        }

        k = 0
        while (k < 8) {
            patchedBuffer[base + 0x28 + k] = ((stackSize ushr (8 * k)) and 0xFF).toByte()
            k++
        }

        k = 0
        while (k < 8) {
            patchedBuffer[base + 0x30 + k] = ((kernelSize ushr (8 * k)) and 0xFF).toByte()
            k++
        }


        if (base + 0x40 + shellCodeBuffer.size - 0x40 > patchedBuffer.size) return -5

        System.arraycopy(shellCodeBuffer, 0x40, patchedBuffer, base + 0x40, shellCodeBuffer.size - 0x40)

        if (header) {
            val newKernelSize = (kernelSize + fd.length()).toInt()
            patchedBuffer[0x10] = (newKernelSize and 0xFF).toByte()
            patchedBuffer[0x11] = ((newKernelSize ushr 8) and 0xFF).toByte()
            patchedBuffer[0x12] = ((newKernelSize ushr 16) and 0xFF).toByte()
            patchedBuffer[0x13] = ((newKernelSize ushr 24) and 0xFF).toByte()
        }

        patched.writeBytes(patchedBuffer.copyOf(patchedSize.toInt()))
        return 0
    }

    fun isPatched(kernel:File): Boolean{
        val buffer = kernel.readBytes()
        val base = if (hasHeader(buffer)) 0x14 else 0x0
        val b3 = buffer[base + 3].toInt() and 0xFF
        val b7 = buffer[base + 7].toInt() and 0xFF
        return b3 == 0x14 && b7 == 0x14
    }

    fun removePatch(kernel: File,output: File):Int{
        //code adapted from https://github.com/Project-Aloha/DualBootKernelPatcher
        if (!kernel.exists())
            return -1
        if (!kernel.canRead())
            return -2
        if (output.exists() && !output.canWrite())
            return -21

        val buffer = kernel.readBytes()
        val header = hasHeader(buffer)
        val base = if (header) 0x14 else 0x0
        var originalKernelSize = ByteBuffer.wrap(buffer, 0x30 + base  , 8).order(ByteOrder.LITTLE_ENDIAN).long + base
        Log.d("kernel size","$originalKernelSize")
        val code2 = ByteBuffer.wrap(buffer, base + 4, 4).order(ByteOrder.LITTLE_ENDIAN).int
        val recovered = (((code2 and (0xFF shl 24).inv()) + 1) or (0x14 shl 24))

        ByteBuffer.wrap(buffer, base, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(recovered)

        ByteBuffer.wrap(buffer, base + 4, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(0)

        if (header) {
            originalKernelSize -= 0x14
            buffer[0x10] = (originalKernelSize shr 0).toByte()
            buffer[0x11] = (originalKernelSize shr 8).toByte()
            buffer[0x12] = (originalKernelSize shr 16).toByte()
            buffer[0x13] = (originalKernelSize shr 24).toByte()
        }
        output.writeBytes(buffer.copyOf(originalKernelSize.toInt()))
        return 0
    }

    fun updateFD(kernel: File,fd: File,output: File):Int{
        if (!kernel.exists() || !fd.exists())
            return -1
        if (!kernel.canRead() || !fd.canRead() )
            return -2
        if (output.exists() && !output.canWrite())
            return -21
        val buffer = kernel.readBytes()
        val base = if (hasHeader(buffer)) 0x14 else 0x0
        val originalKernelSize = ByteBuffer.wrap(buffer, 0x30 + base  , 8).order(ByteOrder.LITTLE_ENDIAN).long + base
        System.arraycopy(fd.readBytes(),0,buffer,originalKernelSize.toInt(),fd.length().toInt())
        output.writeBytes(buffer)
        return 0
    }
}