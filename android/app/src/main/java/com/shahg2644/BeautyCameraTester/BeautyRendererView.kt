package com.shahg2644.BeautyCameraTester

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

class BeautyRendererView(context: Context) : GLSurfaceView(context) {
    var beauty = .35f; var brightness = .05f; var warmth = .05f; var filterEnabled = true
    var onMetrics: ((Int, Int, Long) -> Unit)? = null
    var landmarks: List<FloatArray> = emptyList()
    private val points = AtomicReference<List<FloatArray>>(emptyList())
    init { setEGLContextClientVersion(3); setEGLConfigChooser(8, 8, 8, 8, 16, 0); setZOrderOnTop(true); holder.setFormat(PixelFormat.TRANSLUCENT); setRenderer(Renderer()); renderMode = RENDERMODE_CONTINUOUSLY }
    override fun onDraw(canvas: android.graphics.Canvas) { points.set(landmarks); super.onDraw(canvas) }
    private inner class Renderer : GLSurfaceView.Renderer {
        private var program = 0; private var frames = 0; private var start = System.nanoTime(); private var smoothed = emptyList<FloatArray>()
        override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) { val v = "#version 300 es\nin vec2 p;void main(){gl_Position=vec4(p,0.,1.);}"; val f = "#version 300 es\nprecision mediump float;uniform float b,g,w;uniform vec4 face,leftEye,rightEye,mouth,resolution;out vec4 c;float ellipse(vec2 q,vec4 shape){return 1.-smoothstep(.72,1.,length((q-shape.xy)/shape.zw));}void main(){vec2 q=gl_FragCoord.xy/resolution.xy;float faceMask=ellipse(q,face);float eyeMask=max(ellipse(q,leftEye),ellipse(q,rightEye));float mouthMask=ellipse(q,mouth);float skinMask=faceMask*(1.-max(eyeMask,mouthMask));float strength=clamp(b*.32+g*.12,0.,.34)*skinMask;vec3 correction=vec3(.98+w*.10,.93+w*.04,.90);c=vec4(correction*strength,strength);}"; program = GLES30.glCreateProgram(); val vs = compile(GLES30.GL_VERTEX_SHADER, v); val fs = compile(GLES30.GL_FRAGMENT_SHADER, f); GLES30.glAttachShader(program, vs); GLES30.glAttachShader(program, fs); GLES30.glBindAttribLocation(program, 0, "p"); GLES30.glLinkProgram(program); GLES30.glClearColor(0f, 0f, 0f, 0f); GLES30.glEnable(GLES30.GL_BLEND); GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA) }
        private fun compile(type: Int, source: String): Int { val shader = GLES30.glCreateShader(type); GLES30.glShaderSource(shader, source); GLES30.glCompileShader(shader); return shader }
        override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) { GLES30.glViewport(0, 0, width, height) }
        override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT); if (!filterEnabled) return; GLES30.glUseProgram(program); val face = smooth(points.get()); val bounds = bounds(face); val leftEye = featureBounds(face, listOf(33, 133, 159, 145), .045f, .022f); val rightEye = featureBounds(face, listOf(362, 263, 386, 374), .045f, .022f); val mouth = featureBounds(face, listOf(61, 291, 13, 14), .10f, .045f); GLES30.glUniform1f(GLES30.glGetUniformLocation(program,"b"),beauty); GLES30.glUniform1f(GLES30.glGetUniformLocation(program,"g"),brightness); GLES30.glUniform1f(GLES30.glGetUniformLocation(program,"w"),warmth); GLES30.glUniform4fv(GLES30.glGetUniformLocation(program,"face"),1,bounds,0); GLES30.glUniform4fv(GLES30.glGetUniformLocation(program,"leftEye"),1,leftEye,0); GLES30.glUniform4fv(GLES30.glGetUniformLocation(program,"rightEye"),1,rightEye,0); GLES30.glUniform4fv(GLES30.glGetUniformLocation(program,"mouth"),1,mouth,0); GLES30.glUniform4f(GLES30.glGetUniformLocation(program,"resolution"),width.coerceAtLeast(1).toFloat(),height.coerceAtLeast(1).toFloat(),0f,0f); val buffer=ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer(); buffer.put(floatArrayOf(-1f,-1f,1f,-1f,-1f,1f,1f,1f)).position(0); GLES30.glVertexAttribPointer(0,2,GLES30.GL_FLOAT,false,0,buffer); GLES30.glEnableVertexAttribArray(0); GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,4); frames++; val seconds=(System.nanoTime()-start)/1_000_000_000; if(seconds>0) onMetrics?.invoke(if(face.isEmpty()) 0 else 1,frames/seconds.toInt(),0) }
        private fun smooth(current: List<FloatArray>): List<FloatArray> { if (current.isEmpty()) { smoothed = emptyList(); return smoothed }; if (smoothed.size != current.size) { smoothed = current.map { it.copyOf() }; return smoothed }; smoothed = current.mapIndexed { index, point -> floatArrayOf(smoothed[index][0] * .72f + point[0] * .28f, smoothed[index][1] * .72f + point[1] * .28f) }; return smoothed }
        private fun bounds(face: List<FloatArray>): FloatArray { if (face.isEmpty()) return floatArrayOf(.5f,.5f,.001f,.001f); val xs = face.map { it[0] }; val ys = face.map { it[1] }; return floatArrayOf(xs.average().toFloat(),1f-ys.average().toFloat(),max(.12f,(xs.max()-xs.min())*.72f),max(.16f,(ys.max()-ys.min())*.82f)) }
        private fun featureBounds(face: List<FloatArray>, indices: List<Int>, fallbackWidth: Float, fallbackHeight: Float): FloatArray { val selected = indices.mapNotNull { face.getOrNull(it) }; if (selected.isEmpty()) return floatArrayOf(.5f,.5f,.001f,.001f); val xs = selected.map { it[0] }; val ys = selected.map { it[1] }; return floatArrayOf(xs.average().toFloat(),1f-ys.average().toFloat(),max(fallbackWidth,(xs.max()-xs.min())*1.8f),max(fallbackHeight,(ys.max()-ys.min())*2.4f)) }
    }
}
