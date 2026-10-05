package com.example.game3d.opengl

import android.opengl.GLES20
import android.util.Log

/**
 * High-performance Screen-Space Lens Flare GLSL Shader supporting:
 * - Exponential radial sun corona & burst
 * - Multi-blade diffraction starburst rays
 * - Cinematic anamorphic horizontal lens streaks
 * - Chromatic aberration diffraction rings
 * - Hexagonal & circular multi-stage iris ghosts
 */
class GLLensFlareShader {

    var programId: Int = 0
        private set

    // Attribute Locations
    var aPositionLoc: Int = -1
        private set

    // Uniform Locations
    var uTransformLoc: Int = -1
        private set
    var uElementTypeLoc: Int = -1
        private set
    var uColorLoc: Int = -1
        private set
    var uParamLoc: Int = -1
        private set
    var uTimeLoc: Int = -1
        private set

    fun init() {
        val vertexShaderCode = """
            uniform mat4 uTransform;
            attribute vec2 aPosition;
            varying vec2 vUV;
            
            void main() {
                vUV = aPosition;
                gl_Position = uTransform * vec4(aPosition, 0.0, 1.0);
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            
            uniform int uElementType;
            uniform vec4 uColor;
            uniform float uParam;
            uniform float uTime;
            varying vec2 vUV;
            
            void main() {
                float dist = length(vUV);
                if (dist > 1.0) {
                    discard;
                }
                
                float alpha = 0.0;
                vec3 finalColor = uColor.rgb;
                
                if (uElementType == 0) {
                    // 0: Soft Exponential Radial Corona & Central Sun Burst
                    float falloffPower = max(0.5, uParam);
                    alpha = pow(max(0.0, 1.0 - dist), falloffPower);
                    // Slight chromatic edge separation
                    if (dist > 0.4) {
                        float edgeT = (dist - 0.4) / 0.6;
                        finalColor = mix(uColor.rgb, uColor.rgb * vec3(1.1, 0.9, 0.7), edgeT);
                    }
                } else if (uElementType == 1) {
                    // 1: Multi-Point Diffraction Starburst Rays
                    float angle = atan(vUV.y, vUV.x) + uTime * 0.06;
                    float rayCount = uParam > 0.0 ? uParam : 8.0;
                    float primaryRays = pow(abs(cos(angle * rayCount * 0.5)), 7.0) * 0.7;
                    float secondaryRays = pow(abs(sin(angle * rayCount)), 12.0) * 0.4;
                    float rays = primaryRays + secondaryRays;
                    float radialFalloff = pow(max(0.0, 1.0 - dist), 2.2);
                    alpha = rays * radialFalloff;
                } else if (uElementType == 2) {
                    // 2: Anamorphic Horizontal Flare Streak
                    float distX = abs(vUV.x);
                    float distY = abs(vUV.y);
                    float streakX = pow(max(0.0, 1.0 - distX), 1.6);
                    float streakY = pow(max(0.0, 1.0 - min(1.0, distY * 10.0)), 3.5);
                    alpha = streakX * streakY;
                } else if (uElementType == 3) {
                    // 3: Chromatic Diffraction Halo Ring
                    float ringRadius = 0.68;
                    float ringWidth = uParam > 0.0 ? uParam : 0.16;
                    float ringDist = abs(dist - ringRadius);
                    if (ringDist < ringWidth) {
                        float ringAlpha = (1.0 - (ringDist / ringWidth));
                        alpha = pow(ringAlpha, 1.8);
                        // Chromatic dispersion along ring radius
                        float spectralT = (dist - (ringRadius - ringWidth)) / (ringWidth * 2.0);
                        vec3 rainbow = vec3(
                            smoothstep(0.4, 0.9, spectralT),
                            smoothstep(0.1, 0.7, spectralT) * (1.0 - smoothstep(0.7, 1.0, spectralT)),
                            1.0 - smoothstep(0.1, 0.6, spectralT)
                        );
                        finalColor = mix(uColor.rgb, rainbow, 0.45);
                    }
                } else if (uElementType == 4) {
                    // 4: Hexagonal / Multi-Blade Iris Ghost
                    float angle = atan(vUV.y, vUV.x);
                    float segment = 3.14159265 / 3.0; // 60 degrees for hexagon
                    float hexDist = cos(mod(angle + segment * 0.5, segment) - segment * 0.5) * dist;
                    if (hexDist < 0.85) {
                        float edgeDist = 0.85 - hexDist;
                        alpha = smoothstep(0.0, 0.15, edgeDist) * pow(max(0.0, 1.0 - dist * 0.8), 0.8);
                        if (edgeDist < 0.08) {
                            // Bright edge rim
                            finalColor = mix(uColor.rgb, vec3(1.0), 0.5);
                        }
                    }
                } else if (uElementType == 5) {
                    // 5: Soft Spherical Secondary Ghost Disc
                    alpha = pow(max(0.0, 1.0 - dist * dist), max(0.5, uParam));
                }
                
                float effectiveAlpha = alpha * uColor.a;
                if (effectiveAlpha <= 0.001) {
                    discard;
                }
                
                gl_FragColor = vec4(finalColor * effectiveAlpha, effectiveAlpha);
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram()
        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)
        GLES20.glLinkProgram(programId)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(programId)
            Log.e("GLLensFlareShader", "Error linking Lens Flare shader: $error")
            GLES20.glDeleteProgram(programId)
            programId = 0
            return
        }

        aPositionLoc = GLES20.glGetAttribLocation(programId, "aPosition")
        uTransformLoc = GLES20.glGetUniformLocation(programId, "uTransform")
        uElementTypeLoc = GLES20.glGetUniformLocation(programId, "uElementType")
        uColorLoc = GLES20.glGetUniformLocation(programId, "uColor")
        uParamLoc = GLES20.glGetUniformLocation(programId, "uParam")
        uTimeLoc = GLES20.glGetUniformLocation(programId, "uTime")
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val info = GLES20.glGetShaderInfoLog(shader)
            Log.e("GLLensFlareShader", "Could not compile shader $type: $info")
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun use() {
        if (programId != 0) {
            GLES20.glUseProgram(programId)
        }
    }

    fun dispose() {
        if (programId != 0) {
            GLES20.glDeleteProgram(programId)
            programId = 0
        }
    }
}
