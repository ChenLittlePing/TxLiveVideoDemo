package com.chenlittleping.txlivevideodemo.filter

import android.opengl.GLES11
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * 灰色滤镜
 *
 */
class GreyFilter {
    /**FBO顶点矩阵*/
    private val mVertexCoors = floatArrayOf(
        -1f, 1f,
        1f, 1f,
        -1f, -1f,
        1f, -1f
    )

    // 纹理坐标
    private val mTextureCoors = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0

    private var mTextureId: Int = -1

    //OpenGL程序ID
    private var mProgram: Int = -1
    // 顶点坐标接收者
    private var mVertexPosHandler: Int = -1
    // 纹理坐标接收者
    private var mTexturePosHandler: Int = -1
    // 纹理接收者
    private var mTextureHandler: Int = -1

//-------------FBO相关的变量--------------
    // FBO 帧缓冲
    private var mFboFrameBuffer: Int = -1

    // FBO 纹理ID
    private var mFboTextureId: Int = -1
//--------------------------------------

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    private var waterMarkDrawer: WaterMarkDrawer =
        WaterMarkDrawer()

    init {
        initPos()
    }

    private fun initPos() {
        val bb = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        bb.order(ByteOrder.nativeOrder())
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        mVertexBuffer = bb.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        val cc = ByteBuffer.allocateDirect(mTextureCoors.size * 4)
        cc.order(ByteOrder.nativeOrder())
        mTextureBuffer = cc.asFloatBuffer()
        mTextureBuffer.put(mTextureCoors)
        mTextureBuffer.position(0)
    }

    fun setVideoSize(videoW: Int, videoH: Int) {
        mVideoWidth = videoW
        mVideoHeight = videoH
    }

    fun setTextureID(id: Int) {
        mTextureId = id
    }

    fun getFboTextureID() : Int {
        return mFboTextureId
    }

    fun draw() {
        if (mTextureId != -1) {
            //【创建、编译并启动OpenGL着色器】
            createGLPrg()
            //【绘制到FBO】
            drawFBO()
        }
    }

    private fun createGLPrg() {
        if (mProgram == -1) {
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader())
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader())

            //创建OpenGL ES程序，注意：需要在OpenGL渲染线程中创建，否则无法渲染
            mProgram = GLES20.glCreateProgram()
            //将顶点着色器加入到程序
            GLES20.glAttachShader(mProgram, vertexShader)
            //将片元着色器加入到程序中
            GLES20.glAttachShader(mProgram, fragmentShader)
            //连接到着色器程序
            GLES20.glLinkProgram(mProgram)

            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mTextureHandler = GLES20.glGetUniformLocation(mProgram, "uTexture")
            mTexturePosHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
        }
        //使用OpenGL程序
        GLES20.glUseProgram(mProgram)
    }

    private fun drawFBO() {
        if (mFboTextureId == -1) {
            // 创建FBO纹理
            mFboTextureId = createFBOTexture(mVideoWidth, mVideoHeight)
        }
        if (mFboFrameBuffer == -1) {
            mFboFrameBuffer = createFrameBuffer()
        }
        // 绑定FBO
        bindFBO(mFboFrameBuffer, mFboTextureId)
        // 配置FBO窗口
        configFboViewport()
        // 激活原视频的纹理
        activateTexture(GLES11.GL_TEXTURE_2D, mTextureId, 0, mTextureHandler)
        // 绘制到FBO
        doDraw()
        waterMarkDrawer.draw()
        // 解绑FBO
        unbindFBO()
    }

    /**
     * 配置FBO窗口
     */
    private fun configFboViewport() {
        // 设置 FBO 窗口大小
        GLES20.glViewport(0, 0, mVideoWidth, mVideoHeight)
        // 设置一个颜色状态
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        //开启混合，即半透明
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 使能颜色状态的值来清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    private fun activateTexture(type: Int, textureId: Int, index: Int, textureHandler: Int) {
        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + index)
        //绑定纹理ID到纹理单元
        GLES20.glBindTexture(type, textureId)
        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(textureHandler, index)
        //配置边缘过渡参数
        GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(type, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(type, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }

    private fun doDraw() {
        //启用顶点的句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)
        //设置着色器参数， 第二个参数表示一个顶点包含的数据数量，这里为xy，所以为2
        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glVertexAttribPointer(mTexturePosHandler, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)
        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    fun release() {
        releaseFBO()
        GLES20.glDisableVertexAttribArray(mVertexPosHandler)
        GLES20.glDisableVertexAttribArray(mTexturePosHandler)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    private fun releaseFBO() {
        val fbs = IntArray(1)
        fbs[0] = mFboFrameBuffer
        val texts = IntArray(1)
        texts[0] = mFboTextureId

        deleteFBO(fbs, texts)
    }

    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "attribute vec2 aCoordinate;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "  vCoordinate = aCoordinate;" +
                "}"
    }

    private fun getFragmentShader(): String {
        return "precision mediump float;" +
                "uniform sampler2D uTexture;" +
                "varying vec2 vCoordinate;" +
                "void main() {" +
                "  vec4 color = texture2D(uTexture, vCoordinate);" +
                "  float grey = (color.r + color.g + color.b) / 3.0;" +
                "  gl_FragColor = vec4(grey, grey, grey, 1.0);" +
                "}"
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        //根据type创建顶点着色器或者片元着色器
        val shader = GLES20.glCreateShader(type)
        //将资源加入到着色器中，并编译
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        return shader
    }

    private fun createFBOTexture(width: Int, height: Int): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
            0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0)
        return textures[0]
    }

    private fun createFrameBuffer(): Int {
        val fbs = IntArray(1)
        GLES20.glGenFramebuffers(1, fbs, 0)
        return fbs[0]
    }

    private fun bindFBO(fb: Int, textureId: Int) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb)
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, textureId, 0)
    }

    private fun unbindFBO() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, GLES20.GL_NONE)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }


    private fun deleteFBO(frame: IntArray, texture:IntArray) {
        //删除Render Buffer
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, GLES20.GL_NONE)
        //删除Frame Buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glDeleteFramebuffers(1, frame, 0)
        //删除纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, texture, 0)
    }
}