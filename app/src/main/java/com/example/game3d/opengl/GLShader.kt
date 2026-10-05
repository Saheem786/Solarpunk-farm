package com.example.game3d.opengl

import android.opengl.GLES20
import android.util.Log

class GLShader {

    var programId: Int = 0
        private set

    // Attribute Locations
    var aPositionLoc: Int = -1
        private set
    var aNormalLoc: Int = -1
        private set
    var aColorLoc: Int = -1
        private set

    // Uniform Locations
    var uMVPMatrixLoc: Int = -1
        private set
    var uMVMatrixLoc: Int = -1
        private set
    var uNormalMatrixLoc: Int = -1
        private set
    var uLightDirLoc: Int = -1
        private set
    var uLightColorLoc: Int = -1
        private set
    var uAmbientColorLoc: Int = -1
        private set
    var uObjectColorLoc: Int = -1
        private set
    var uUseVertexColorLoc: Int = -1
        private set
    var uEmissionColorLoc: Int = -1
        private set
    var uShininessLoc: Int = -1
        private set

    fun init() {
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uMVMatrix;
            uniform mat4 uNormalMatrix;
            
            attribute vec3 aPosition;
            attribute vec3 aNormal;
            attribute vec4 aColor;
            
            varying vec3 vNormal;
            varying vec3 vPositionEye;
            varying vec4 vVertexColor;
            
            void main() {
                vVertexColor = aColor;
                vNormal = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0)));
                vec4 eyePos = uMVMatrix * vec4(aPosition, 1.0);
                vPositionEye = eyePos.xyz;
                gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            
            uniform vec3 uLightDir;
            uniform vec3 uLightColor;
            uniform vec3 uAmbientColor;
            uniform vec4 uObjectColor;
            uniform int uUseVertexColor;
            uniform vec3 uEmissionColor;
            uniform float uShininess;
            
            varying vec3 vNormal;
            varying vec3 vPositionEye;
            varying vec4 vVertexColor;
            
            void main() {
                vec4 baseColor = (uUseVertexColor == 1) ? vVertexColor : uObjectColor;
                
                // Normal & Light Vector
                vec3 N = normalize(vNormal);
                vec3 L = normalize(uLightDir);
                
                // Diffuse Lighting (Lambertian)
                float diffuseFactor = max(dot(N, L), 0.0);
                vec3 diffuse = uLightColor * diffuseFactor;
                
                // Specular Lighting (Blinn-Phong)
                vec3 V = normalize(-vPositionEye);
                vec3 H = normalize(L + V);
                float specFactor = pow(max(dot(N, H), 0.0), max(uShininess, 1.0)) * 0.25;
                vec3 specular = uLightColor * specFactor;
                
                // Final Composed Color
                vec3 finalRgb = baseColor.rgb * (uAmbientColor + diffuse) + specular + uEmissionColor;
                gl_FragColor = vec4(clamp(finalRgb, 0.0, 1.0), baseColor.a);
            }
        """.trimIndent()

        val vShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram()
        GLES20.glAttachShader(programId, vShader)
        GLES20.glAttachShader(programId, fShader)
        GLES20.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e("GLShader", "Could not link program: " + GLES20.glGetProgramInfoLog(programId))
            GLES20.glDeleteProgram(programId)
            programId = 0
            return
        }

        // Query Attributes
        aPositionLoc = GLES20.glGetAttribLocation(programId, "aPosition")
        aNormalLoc = GLES20.glGetAttribLocation(programId, "aNormal")
        aColorLoc = GLES20.glGetAttribLocation(programId, "aColor")

        // Query Uniforms
        uMVPMatrixLoc = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        uMVMatrixLoc = GLES20.glGetUniformLocation(programId, "uMVMatrix")
        uNormalMatrixLoc = GLES20.glGetUniformLocation(programId, "uNormalMatrix")
        uLightDirLoc = GLES20.glGetUniformLocation(programId, "uLightDir")
        uLightColorLoc = GLES20.glGetUniformLocation(programId, "uLightColor")
        uAmbientColorLoc = GLES20.glGetUniformLocation(programId, "uAmbientColor")
        uObjectColorLoc = GLES20.glGetUniformLocation(programId, "uObjectColor")
        uUseVertexColorLoc = GLES20.glGetUniformLocation(programId, "uUseVertexColor")
        uEmissionColorLoc = GLES20.glGetUniformLocation(programId, "uEmissionColor")
        uShininessLoc = GLES20.glGetUniformLocation(programId, "uShininess")
    }

    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e("GLShader", "Could not compile shader $type: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun use() {
        GLES20.glUseProgram(programId)
    }
}
