package com.example.game3d.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class GLMesh(
    val vertexBuffer: FloatBuffer,
    val normalBuffer: FloatBuffer,
    val colorBuffer: FloatBuffer?,
    val indexBuffer: ShortBuffer?,
    val vertexCount: Int,
    val indexCount: Int,
    val drawMode: Int = GLES20.GL_TRIANGLES
) {

    fun draw(shader: GLShader) {
        if (vertexCount == 0) return

        // 1. Position Attribute
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(shader.aPositionLoc)
        GLES20.glVertexAttribPointer(
            shader.aPositionLoc,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )

        // 2. Normal Attribute
        normalBuffer.position(0)
        GLES20.glEnableVertexAttribArray(shader.aNormalLoc)
        GLES20.glVertexAttribPointer(
            shader.aNormalLoc,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            normalBuffer
        )

        // 3. Color Attribute
        if (colorBuffer != null && shader.aColorLoc >= 0) {
            colorBuffer.position(0)
            GLES20.glEnableVertexAttribArray(shader.aColorLoc)
            GLES20.glVertexAttribPointer(
                shader.aColorLoc,
                4,
                GLES20.GL_FLOAT,
                false,
                0,
                colorBuffer
            )
            GLES20.glUniform1i(shader.uUseVertexColorLoc, 1)
        } else {
            if (shader.aColorLoc >= 0) {
                GLES20.glDisableVertexAttribArray(shader.aColorLoc)
            }
            GLES20.glUniform1i(shader.uUseVertexColorLoc, 0)
        }

        // 4. Draw Call
        if (indexBuffer != null && indexCount > 0) {
            indexBuffer.position(0)
            GLES20.glDrawElements(
                drawMode,
                indexCount,
                GLES20.GL_UNSIGNED_SHORT,
                indexBuffer
            )
        } else {
            GLES20.glDrawArrays(drawMode, 0, vertexCount)
        }

        // Clean up state
        GLES20.glDisableVertexAttribArray(shader.aPositionLoc)
        GLES20.glDisableVertexAttribArray(shader.aNormalLoc)
        if (colorBuffer != null && shader.aColorLoc >= 0) {
            GLES20.glDisableVertexAttribArray(shader.aColorLoc)
        }
    }

    companion object {
        fun createFloatBuffer(coords: FloatArray): FloatBuffer {
            val bb = ByteBuffer.allocateDirect(coords.size * 4)
            bb.order(ByteOrder.nativeOrder())
            val fb = bb.asFloatBuffer()
            fb.put(coords)
            fb.position(0)
            return fb
        }

        fun createShortBuffer(indices: ShortArray): ShortBuffer {
            val bb = ByteBuffer.allocateDirect(indices.size * 2)
            bb.order(ByteOrder.nativeOrder())
            val sb = bb.asShortBuffer()
            sb.put(indices)
            sb.position(0)
            return sb
        }
    }
}
