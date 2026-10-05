package com.example.game3d.opengl

import android.opengl.GLES20
import android.util.Log

/**
 * Dedicated Skybox GLSL Shader supporting:
 * - Dynamic 24-hour celestial gradient (Zenith -> Horizon -> Ground)
 * - Procedural drifting Fractal Brownian Motion (fBm) cloud generator
 * - Sun/Moon rim-lighting & cloud shading modulated by 24h cycle
 * - Solar optical disc bloom & corona
 * - Procedural twinkling night starfield
 */
class GLSkyboxShader {

    var programId: Int = 0
        private set

    // Attributes
    var aPositionLoc: Int = -1
        private set

    // Uniforms
    var uMVPMatrixLoc: Int = -1
        private set
    var uZenithColorLoc: Int = -1
        private set
    var uHorizonColorLoc: Int = -1
        private set
    var uGroundColorLoc: Int = -1
        private set
    var uSunDirLoc: Int = -1
        private set
    var uSunColorLoc: Int = -1
        private set
    var uSunGlowLoc: Int = -1
        private set
    var uStarIntensityLoc: Int = -1
        private set
    var uTimeLoc: Int = -1
        private set
    var uCloudCoverageLoc: Int = -1
        private set
    var uCloudDensityLoc: Int = -1
        private set
    var uWindOffsetLoc: Int = -1
        private set
    var uCloudColorTopLoc: Int = -1
        private set
    var uCloudColorBottomLoc: Int = -1
        private set

    fun init() {
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec3 aPosition;
            varying vec3 vWorldDir;
            
            void main() {
                vWorldDir = aPosition;
                // Center skybox projection at maximum depth (z = w)
                vec4 pos = uMVPMatrix * vec4(aPosition, 1.0);
                gl_Position = pos.xyww;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            
            uniform vec3 uZenithColor;
            uniform vec3 uHorizonColor;
            uniform vec3 uGroundColor;
            uniform vec3 uSunDir;
            uniform vec3 uSunColor;
            uniform float uSunGlow;
            uniform float uStarIntensity;
            uniform float uTime;
            
            // Procedural Cloud Uniforms
            uniform float uCloudCoverage;
            uniform float uCloudDensity;
            uniform vec2 uWindOffset;
            uniform vec3 uCloudColorTop;
            uniform vec3 uCloudColorBottom;
            
            varying vec3 vWorldDir;
            
            // Fast 2D Pseudo-Noise
            float hash21(vec2 p) {
                p = fract(p * vec2(123.34, 456.21));
                p += dot(p, p + 45.32);
                return fract(p.x * p.y);
            }
            
            float noise2d(vec2 p) {
                vec2 i = floor(p);
                vec2 f = fract(p);
                f = f * f * (3.0 - 2.0 * f);
                float a = hash21(i);
                float b = hash21(i + vec2(1.0, 0.0));
                float c = hash21(i + vec2(0.0, 1.0));
                float d = hash21(i + vec2(1.0, 1.0));
                return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
            }
            
            // 4-Octave Fractal Brownian Motion for Volumetric Clouds
            float fbm(vec2 p) {
                float v = 0.0;
                float a = 0.5;
                for (int i = 0; i < 4; i++) {
                    v += a * noise2d(p);
                    p = p * 2.05 + vec2(1.7, 9.2);
                    a *= 0.5;
                }
                return v;
            }
            
            // Hash for Starfield
            float hash3(vec3 p) {
                p = fract(p * 0.3183099 + 0.1);
                p *= 17.0;
                return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
            }
            
            void main() {
                vec3 dir = normalize(vWorldDir);
                float h = dir.y;
                
                // 1. Smooth 3-Stop Sky Atmospheric Gradient
                vec3 skyColor;
                if (h >= 0.0) {
                    float t = smoothstep(0.0, 0.75, h);
                    skyColor = mix(uHorizonColor, uZenithColor, t);
                } else {
                    float t = smoothstep(0.0, -0.4, h);
                    skyColor = mix(uHorizonColor, uGroundColor, t);
                }
                
                // 2. Solar Bloom & Corona
                float sunDot = max(dot(dir, normalize(uSunDir)), 0.0);
                float sunDisc = pow(sunDot, 120.0) * 1.8;
                float sunCorona = pow(sunDot, 12.0) * uSunGlow;
                vec3 sunGlow = uSunColor * (sunDisc + sunCorona);
                
                // 3. Procedural Starfield (Nighttime)
                vec3 stars = vec3(0.0);
                if (uStarIntensity > 0.01 && h > 0.05) {
                    vec3 starCoord = floor(dir * 180.0);
                    float starVal = hash3(starCoord);
                    if (starVal > 0.988) {
                        float twinkle = sin(uTime * 4.0 + starVal * 60.0) * 0.5 + 0.5;
                        float brightness = (starVal - 0.988) / 0.012;
                        stars = vec3(0.9, 0.95, 1.0) * brightness * twinkle * uStarIntensity * smoothstep(0.05, 0.25, h);
                    }
                }
                
                vec3 baseSky = skyColor + sunGlow + stars;
                
                // 4. Procedural Drifting Clouds Layer
                if (uCloudCoverage > 0.02 && h > 0.02) {
                    // Planar mapping to sky dome
                    vec2 skyUV = dir.xz / (h + 0.20);
                    vec2 driftUV = skyUV * 1.25 + uWindOffset;
                    
                    float n = fbm(driftUV);
                    n += 0.20 * noise2d(driftUV * 3.2 - uWindOffset * 0.5);
                    
                    // Cloud density curve
                    float lowThreshold = 1.0 - uCloudCoverage;
                    float highThreshold = lowThreshold + 0.35;
                    float cloudDensity = smoothstep(lowThreshold, highThreshold, n);
                    
                    // Fade clouds near horizon for clean perspective
                    cloudDensity *= smoothstep(0.02, 0.25, h);
                    
                    if (cloudDensity > 0.01) {
                        // Light scattering through clouds
                        float sunFacing = clamp(dot(dir, normalize(uSunDir)) * 0.5 + 0.5, 0.0, 1.0);
                        vec3 cloudShading = mix(uCloudColorBottom, uCloudColorTop, pow(sunFacing, 1.6) * 0.7 + n * 0.3);
                        
                        float finalAlpha = clamp(cloudDensity * uCloudDensity, 0.0, 1.0);
                        baseSky = mix(baseSky, cloudShading, finalAlpha);
                    }
                }
                
                gl_FragColor = vec4(clamp(baseSky, 0.0, 1.0), 1.0);
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
            Log.e("GLSkyboxShader", "Could not link skybox program: " + GLES20.glGetProgramInfoLog(programId))
            GLES20.glDeleteProgram(programId)
            programId = 0
            return
        }

        aPositionLoc = GLES20.glGetAttribLocation(programId, "aPosition")
        uMVPMatrixLoc = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        uZenithColorLoc = GLES20.glGetUniformLocation(programId, "uZenithColor")
        uHorizonColorLoc = GLES20.glGetUniformLocation(programId, "uHorizonColor")
        uGroundColorLoc = GLES20.glGetUniformLocation(programId, "uGroundColor")
        uSunDirLoc = GLES20.glGetUniformLocation(programId, "uSunDir")
        uSunColorLoc = GLES20.glGetUniformLocation(programId, "uSunColor")
        uSunGlowLoc = GLES20.glGetUniformLocation(programId, "uSunGlow")
        uStarIntensityLoc = GLES20.glGetUniformLocation(programId, "uStarIntensity")
        uTimeLoc = GLES20.glGetUniformLocation(programId, "uTime")

        // Cloud Uniform Locations
        uCloudCoverageLoc = GLES20.glGetUniformLocation(programId, "uCloudCoverage")
        uCloudDensityLoc = GLES20.glGetUniformLocation(programId, "uCloudDensity")
        uWindOffsetLoc = GLES20.glGetUniformLocation(programId, "uWindOffset")
        uCloudColorTopLoc = GLES20.glGetUniformLocation(programId, "uCloudColorTop")
        uCloudColorBottomLoc = GLES20.glGetUniformLocation(programId, "uCloudColorBottom")
    }

    private fun compileShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.e("GLSkyboxShader", "Compilation failed for $type: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    fun use() {
        GLES20.glUseProgram(programId)
    }
}
