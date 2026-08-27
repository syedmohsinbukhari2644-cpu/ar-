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
    init { setEGLContextClientVersion(3); setZOrderOnTop(true); holder.setFormat(PixelFormat.TRANSLUCENT); setRenderer(Renderer()); renderMode = RENDERMODE_CONTINUOUSLY }
    override fun onDraw(canvas: android.graphics.Canvas) { points.set(landmarks); super.onDraw(canvas) }
    private inner class Renderer : GLSurfaceView.Renderer {
        private var program = 0; private var frames = 0; private var start = System.nanoTime()
        override fun onSurfaceCreated(gl: javax.microedition.khronos.opengles.GL10?, config: javax.microedition.khronos.egl.EGLConfig?) { val v = "#version 300 es\nin vec2 p;void main(){gl_Position=vec4(p,0.,1.);}"; val f = "#version 300 es\nprecision mediump float;uniform float b,g,w;uniform vec4 face;out vec4 c;void main(){vec2 q=gl_FragCoord.xy/vec2(${width.coerceAtLeast(1)}.,${height.coerceAtLeast(1)}.);float m=smoothstep(1.,.68,distance((q-face.xy)/face.zw,vec2(0.)));vec3 t=vec3(1.+w*.18,.94+w*.06,.90);c=vec4((t-1.)*(b*.22+g)*m,b*.18*m);}"; program = GLES30.glCreateProgram(); val vs = compile(GLES30.GL_VERTEX_SHADER, v); val fs = compile(GLES30.GL_FRAGMENT_SHADER, f); GLES30.glAttachShader(program, vs); GLES30.glAttachShader(program, fs); GLES30.glBindAttribLocation(program, 0, "p"); GLES30.glLinkProgram(program) }
        private fun compile(type: Int, source: String): Int { val shader = GLES30.glCreateShader(type); GLES30.glShaderSource(shader, source); GLES30.glCompileShader(shader); return shader }
        override fun onSurfaceChanged(gl: javax.microedition.khronos.opengles.GL10?, width: Int, height: Int) { GLES30.glViewport(0, 0, width, height) }
        override fun onDrawFrame(gl: javax.microedition.khronos.opengles.GL10?) { GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT); if (!filterEnabled) return; GLES30.glUseProgram(program); val face = points.get(); val bounds = if (face.isEmpty()) floatArrayOf(.5f,.5f,.01f,.01f) else floatArrayOf(face.map { it[0] }.average().toFloat(), 1f-face.map { it[1] }.average().toFloat(), max(.12f,(face.maxOf { it[0] }-face.minOf { it[0] })*.72f), max(.16f,(face.maxOf { it[1] }-face.minOf { it[1] })*.82f)); GLES30.glUniform1f(GLES30.glGetUniformLocation(program,"b"),beauty); GLES30.glUniform1f(GLES30.glGetUniformLocation(program,"g"),brightness); GLES30.glUniform1f(GLES30.glGetUniformLocation(program,"w"),warmth); GLES30.glUniform4fv(GLES30.glGetUniformLocation(program,"face"),1,bounds,0); val buffer=ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer(); buffer.put(floatArrayOf(-1f,-1f,1f,-1f,-1f,1f,1f,1f)).position(0); GLES30.glVertexAttribPointer(0,2,GLES30.GL_FLOAT,false,0,buffer); GLES30.glEnableVertexAttribArray(0); GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,4); frames++; val seconds=(System.nanoTime()-start)/1_000_000_000; if(seconds>0) onMetrics?.invoke(if(face.isEmpty()) 0 else 1,frames/seconds.toInt(),0) }
    }
}
