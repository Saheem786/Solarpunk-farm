package com.example.game3d.opengl

import android.opengl.GLES20
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GLModelBuilder {

    private val vertices = mutableListOf<Float>()
    private val normals = mutableListOf<Float>()
    private val colors = mutableListOf<Float>()
    private val indices = mutableListOf<Short>()

    fun reset() {
        vertices.clear()
        normals.clear()
        colors.clear()
        indices.clear()
    }

    fun addBox(
        centerX: Float,
        centerY: Float,
        centerZ: Float,
        sizeX: Float,
        sizeY: Float,
        sizeZ: Float,
        r: Float,
        g: Float,
        b: Float,
        a: Float = 1.0f
    ) {
        val hx = sizeX / 2.0f
        val hy = sizeY / 2.0f
        val hz = sizeZ / 2.0f

        val x0 = centerX - hx
        val x1 = centerX + hx
        val y0 = centerY - hy
        val y1 = centerY + hy
        val z0 = centerZ - hz
        val z1 = centerZ + hz

        // 6 Faces: Front, Back, Top, Bottom, Left, Right
        // Front (+Z)
        addQuad(
            x0, y0, z1,
            x1, y0, z1,
            x1, y1, z1,
            x0, y1, z1,
            0f, 0f, 1f,
            r, g, b, a
        )
        // Back (-Z)
        addQuad(
            x1, y0, z0,
            x0, y0, z0,
            x0, y1, z0,
            x1, y1, z0,
            0f, 0f, -1f,
            r, g, b, a
        )
        // Top (+Y)
        addQuad(
            x0, y1, z1,
            x1, y1, z1,
            x1, y1, z0,
            x0, y1, z0,
            0f, 1f, 0f,
            r * 1.15f, g * 1.15f, b * 1.15f, a
        )
        // Bottom (-Y)
        addQuad(
            x0, y0, z0,
            x1, y0, z0,
            x1, y0, z1,
            x0, y0, z1,
            0f, -1f, 0f,
            r * 0.7f, g * 0.7f, b * 0.7f, a
        )
        // Right (+X)
        addQuad(
            x1, y0, z1,
            x1, y0, z0,
            x1, y1, z0,
            x1, y1, z1,
            1f, 0f, 0f,
            r * 0.9f, g * 0.9f, b * 0.9f, a
        )
        // Left (-X)
        addQuad(
            x0, y0, z0,
            x0, y0, z1,
            x0, y1, z1,
            x0, y1, z0,
            -1f, 0f, 0f,
            r * 0.85f, g * 0.85f, b * 0.85f, a
        )
    }

    fun addQuad(
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float,
        x4: Float, y4: Float, z4: Float,
        nx: Float, ny: Float, nz: Float,
        r: Float, g: Float, b: Float, a: Float = 1.0f
    ) {
        val baseIndex = (vertices.size / 3).toShort()

        // 4 Vertices
        val clrR = r.coerceIn(0f, 1f)
        val clrG = g.coerceIn(0f, 1f)
        val clrB = b.coerceIn(0f, 1f)

        addVertex(x1, y1, z1, nx, ny, nz, clrR, clrG, clrB, a)
        addVertex(x2, y2, z2, nx, ny, nz, clrR, clrG, clrB, a)
        addVertex(x3, y3, z3, nx, ny, nz, clrR, clrG, clrB, a)
        addVertex(x4, y4, z4, nx, ny, nz, clrR, clrG, clrB, a)

        // 2 Triangles: (0, 1, 2) and (0, 2, 3)
        indices.add(baseIndex)
        indices.add((baseIndex + 1).toShort())
        indices.add((baseIndex + 2).toShort())

        indices.add(baseIndex)
        indices.add((baseIndex + 2).toShort())
        indices.add((baseIndex + 3).toShort())
    }

    fun addTriangle(
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        x3: Float, y3: Float, z3: Float,
        nx: Float, ny: Float, nz: Float,
        r: Float, g: Float, b: Float, a: Float = 1.0f
    ) {
        val baseIndex = (vertices.size / 3).toShort()
        val clrR = r.coerceIn(0f, 1f)
        val clrG = g.coerceIn(0f, 1f)
        val clrB = b.coerceIn(0f, 1f)

        addVertex(x1, y1, z1, nx, ny, nz, clrR, clrG, clrB, a)
        addVertex(x2, y2, z2, nx, ny, nz, clrR, clrG, clrB, a)
        addVertex(x3, y3, z3, nx, ny, nz, clrR, clrG, clrB, a)

        indices.add(baseIndex)
        indices.add((baseIndex + 1).toShort())
        indices.add((baseIndex + 2).toShort())
    }

    fun addRoofPrism(
        centerX: Float,
        bottomY: Float,
        centerZ: Float,
        width: Float,
        height: Float,
        depth: Float,
        r: Float,
        g: Float,
        b: Float,
        a: Float = 1.0f
    ) {
        val hw = width / 2.0f
        val hd = depth / 2.0f
        val topY = bottomY + height

        // Front Triangle (+Z)
        addTriangle(
            centerX - hw, bottomY, centerZ + hd,
            centerX + hw, bottomY, centerZ + hd,
            centerX, topY, centerZ + hd,
            0f, 0.4f, 0.9f,
            r * 0.95f, g * 0.95f, b * 0.95f, a
        )
        // Back Triangle (-Z)
        addTriangle(
            centerX + hw, bottomY, centerZ - hd,
            centerX - hw, bottomY, centerZ - hd,
            centerX, topY, centerZ - hd,
            0f, 0.4f, -0.9f,
            r * 0.85f, g * 0.85f, b * 0.85f, a
        )
        // Left Slanted Roof Face (-X)
        val leftNormalX = -height
        val leftNormalY = hw
        val leftLen = sqrt(leftNormalX * leftNormalX + leftNormalY * leftNormalY)
        addQuad(
            centerX - hw, bottomY, centerZ - hd,
            centerX - hw, bottomY, centerZ + hd,
            centerX, topY, centerZ + hd,
            centerX, topY, centerZ - hd,
            leftNormalX / leftLen, leftNormalY / leftLen, 0f,
            r * 1.05f, g * 1.05f, b * 1.05f, a
        )
        // Right Slanted Roof Face (+X)
        val rightNormalX = height
        val rightNormalY = hw
        val rightLen = sqrt(rightNormalX * rightNormalX + rightNormalY * rightNormalY)
        addQuad(
            centerX, topY, centerZ - hd,
            centerX, topY, centerZ + hd,
            centerX + hw, bottomY, centerZ + hd,
            centerX + hw, bottomY, centerZ - hd,
            rightNormalX / rightLen, rightNormalY / rightLen, 0f,
            r * 1.15f, g * 1.15f, b * 1.15f, a
        )
    }

    fun addCylinder(
        centerX: Float,
        bottomY: Float,
        centerZ: Float,
        radius: Float,
        height: Float,
        segments: Int = 8,
        r: Float,
        g: Float,
        b: Float,
        a: Float = 1.0f
    ) {
        val topY = bottomY + height
        val step = (2.0 * Math.PI / segments).toFloat()

        for (i in 0 until segments) {
            val a1 = i * step
            val a2 = (i + 1) * step

            val cos1 = cos(a1)
            val sin1 = sin(a1)
            val cos2 = cos(a2)
            val sin2 = sin(a2)

            val x1 = centerX + radius * cos1
            val z1 = centerZ + radius * sin1
            val x2 = centerX + radius * cos2
            val z2 = centerZ + radius * sin2

            val midCos = cos((a1 + a2) / 2f)
            val midSin = sin((a1 + a2) / 2f)

            // Side Quad
            addQuad(
                x1, bottomY, z1,
                x2, bottomY, z2,
                x2, topY, z2,
                x1, topY, z1,
                midCos, 0f, midSin,
                r, g, b, a
            )

            // Top Cap Triangle
            addTriangle(
                centerX, topY, centerZ,
                x1, topY, z1,
                x2, topY, z2,
                0f, 1f, 0f,
                r * 1.1f, g * 1.1f, b * 1.1f, a
            )

            // Bottom Cap Triangle
            addTriangle(
                centerX, bottomY, centerZ,
                x2, bottomY, z2,
                x1, bottomY, z1,
                0f, -1f, 0f,
                r * 0.7f, g * 0.7f, b * 0.7f, a
            )
        }
    }

    fun addCone(
        centerX: Float,
        bottomY: Float,
        centerZ: Float,
        radius: Float,
        height: Float,
        segments: Int = 7,
        r: Float,
        g: Float,
        b: Float,
        a: Float = 1.0f
    ) {
        val topY = bottomY + height
        val step = (2.0 * Math.PI / segments).toFloat()

        for (i in 0 until segments) {
            val a1 = i * step
            val a2 = (i + 1) * step

            val x1 = centerX + radius * cos(a1)
            val z1 = centerZ + radius * sin(a1)
            val x2 = centerX + radius * cos(a2)
            val z2 = centerZ + radius * sin(a2)

            val midCos = cos((a1 + a2) / 2f)
            val midSin = sin((a1 + a2) / 2f)
            val nx = midCos * 0.8f
            val ny = 0.5f
            val nz = midSin * 0.8f
            val len = sqrt(nx * nx + ny * ny + nz * nz)

            // Slanted Side Triangle
            addTriangle(
                x1, bottomY, z1,
                x2, bottomY, z2,
                centerX, topY, centerZ,
                nx / len, ny / len, nz / len,
                r, g, b, a
            )

            // Bottom Cap Triangle
            addTriangle(
                centerX, bottomY, centerZ,
                x2, bottomY, z2,
                x1, bottomY, z1,
                0f, -1f, 0f,
                r * 0.7f, g * 0.7f, b * 0.7f, a
            )
        }
    }

    fun addSphere(
        centerX: Float,
        centerY: Float,
        centerZ: Float,
        radius: Float,
        rings: Int = 6,
        sectors: Int = 8,
        r: Float,
        g: Float,
        b: Float,
        a: Float = 1.0f
    ) {
        val R = 1f / (rings - 1).toFloat()
        val S = 1f / (sectors - 1).toFloat()

        for (rIdx in 0 until rings - 1) {
            val phi1 = Math.PI.toFloat() * rIdx * R
            val phi2 = Math.PI.toFloat() * (rIdx + 1) * R

            val y1 = cos(phi1)
            val y2 = cos(phi2)
            val r1 = sin(phi1)
            val r2 = sin(phi2)

            for (sIdx in 0 until sectors - 1) {
                val theta1 = 2f * Math.PI.toFloat() * sIdx * S
                val theta2 = 2f * Math.PI.toFloat() * (sIdx + 1) * S

                val x1 = r1 * cos(theta1)
                val z1 = r1 * sin(theta1)
                val x2 = r1 * cos(theta2)
                val z2 = r1 * sin(theta2)

                val x3 = r2 * cos(theta2)
                val z3 = r2 * sin(theta2)
                val x4 = r2 * cos(theta1)
                val z4 = r2 * sin(theta1)

                addQuad(
                    centerX + x1 * radius, centerY + y1 * radius, centerZ + z1 * radius,
                    centerX + x2 * radius, centerY + y1 * radius, centerZ + z2 * radius,
                    centerX + x3 * radius, centerY + y2 * radius, centerZ + z3 * radius,
                    centerX + x4 * radius, centerY + y2 * radius, centerZ + z4 * radius,
                    (x1 + x2 + x3 + x4) / 4f, (y1 + y2) / 2f, (z1 + z2 + z3 + z4) / 4f,
                    r, g, b, a
                )
            }
        }
    }

    private fun addVertex(
        x: Float, y: Float, z: Float,
        nx: Float, ny: Float, nz: Float,
        r: Float, g: Float, b: Float, a: Float
    ) {
        vertices.add(x)
        vertices.add(y)
        vertices.add(z)

        normals.add(nx)
        normals.add(ny)
        normals.add(nz)

        colors.add(r)
        colors.add(g)
        colors.add(b)
        colors.add(a)
    }

    fun build(drawMode: Int = GLES20.GL_TRIANGLES): GLMesh {
        val vertexCoords = vertices.toFloatArray()
        val normalCoords = normals.toFloatArray()
        val colorCoords = colors.toFloatArray()
        val indexArray = indices.toShortArray()

        val vb = GLMesh.createFloatBuffer(vertexCoords)
        val nb = GLMesh.createFloatBuffer(normalCoords)
        val cb = GLMesh.createFloatBuffer(colorCoords)
        val ib = GLMesh.createShortBuffer(indexArray)

        return GLMesh(
            vertexBuffer = vb,
            normalBuffer = nb,
            colorBuffer = cb,
            indexBuffer = ib,
            vertexCount = vertexCoords.size / 3,
            indexCount = indexArray.size,
            drawMode = drawMode
        )
    }
}
