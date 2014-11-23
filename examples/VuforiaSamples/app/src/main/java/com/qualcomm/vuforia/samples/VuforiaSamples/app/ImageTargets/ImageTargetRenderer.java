/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Teapot;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;


// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer
{
    private static final String LOGTAG = "ImageTargetRenderer";
    
    private SampleApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;
    
    private Vector<Texture> mTextures;
    
    private int shaderProgramID;
    
    private int vertexHandle;
    
    private int normalHandle;
    
    private int textureCoordHandle;
    
    private int mvpMatrixHandle;
    
    private int texSampler2DHandle;
    
    private Teapot mTeapot;
    
    private float kBuildingScale = 12.0f;
    private SampleApplication3DModel mBuildingsModel;
    
    private Renderer mRenderer;
    
    boolean mIsActive = false;
    
    private static final float OBJECT_SCALE_FLOAT = 3.0f;
    
    
    public ImageTargetRenderer(ImageTargets activity,
        SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();
    }
    
    
    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        initRendering();
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        mTeapot = new Teapot();
        
        mRenderer = Renderer.getInstance();
        
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
            : 1.0f);
        
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.CUBE_MESH_VERTEX_SHADER,
            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");
        
        try
        {
            mBuildingsModel = new SampleApplication3DModel();
            mBuildingsModel.loadModel(mActivity.getResources().getAssets(),
                "ImageTargets/Buildings.txt");
        } catch (IOException e)
        {
            Log.e(LOGTAG, "Unable to load buildings");
        }
        
        // Hide the Loading Dialog
        mActivity.loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        
    }
    
    
    // The render function.
    private void renderFrame()
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        
        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera
            
        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
            
            int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0
                : 1;
            textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 2
                : textureIndex;
            
            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];
            
            if (!mActivity.isExtendedTrackingActive())
            {
                Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                    OBJECT_SCALE_FLOAT);
                Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                    OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
            } else
            {
                Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
                    kBuildingScale, kBuildingScale);
            }
            
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            
            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);
            
            if (!mActivity.isExtendedTrackingActive())
            {
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mTeapot.getVertices());
                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mTeapot.getNormals());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());
                
                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);
                
                // activate texture 0, bind it, and pass to shader
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(textureIndex).mTextureID[0]);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                
                // pass the model view matrix to the shader
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjection, 0);
                
                // finally draw the teapot
                GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                    mTeapot.getIndices());
                
                // disable the enabled arrays
                GLES20.glDisableVertexAttribArray(vertexHandle);
                GLES20.glDisableVertexAttribArray(normalHandle);
                GLES20.glDisableVertexAttribArray(textureCoordHandle);
            } else
            {
                GLES20.glDisable(GLES20.GL_CULL_FACE);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mBuildingsModel.getVertices());
                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mBuildingsModel.getNormals());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, mBuildingsModel.getTexCoords());
                
                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);
                
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(3).mTextureID[0]);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjection, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                    mBuildingsModel.getNumObjectVertex());
                
                SampleUtils.checkGLError("Renderer DrawBuildings");
            }
            
            SampleUtils.checkGLError("Render Frame");
            
        }
        
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        mRenderer.end();
    }
    
    
    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }
    
    
    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        
    }
    
}
