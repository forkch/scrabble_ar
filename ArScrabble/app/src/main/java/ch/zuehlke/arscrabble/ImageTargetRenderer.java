/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package ch.zuehlke.arscrabble;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Matrix34F;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ch.zuehlke.arscrabble.vuforiautils.CubeShaders;
import ch.zuehlke.arscrabble.vuforiautils.SampleApplication3DModel;
import ch.zuehlke.arscrabble.vuforiautils.SampleUtils;
import ch.zuehlke.arscrabble.vuforiautils.Teapot;
import ch.zuehlke.arscrabble.vuforiautils.Texture;

// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer {
    private static final String LOGTAG = "ImageTargetRenderer";

    private ApplicationSession vuforiaAppSession;
    private ImageTargetsActivity mActivity;

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


    public ImageTargetRenderer(ImageTargetsActivity activity,
                               ApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;
    }


    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }


    // Function for initializing the renderer.
    private void initRendering() {
        mTeapot = new Teapot();

        mRenderer = Renderer.getInstance();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f : 1.0f);

        for (Texture t : mTextures) {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, t.mWidth, t.mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(CubeShaders.CUBE_MESH_VERTEX_SHADER, CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID, "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID, "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID, "texSampler2D");

        try {
            mBuildingsModel = new SampleApplication3DModel();
            mBuildingsModel.loadModel(mActivity.getResources().getAssets(), "ImageTargets/Buildings.txt");
        } catch (IOException e) {
            Log.e(LOGTAG, "Unable to load buildings");
        }
    }

    // The render function.
    private void renderFrame() {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON) {
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        } else {
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera
        }

        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);

            Trackable trackable = result.getTrackable();

            printUserData(trackable);
            final Matrix34F pose = result.getPose();
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(pose);
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
/*

            float targetCenter_X = modelViewMatrix[12];
            float targetCenter_Y = modelViewMatrix[13];
            float targetCenter_Z = modelViewMatrix[14];
            ImageTarget imageTarget = (ImageTarget) trackable;
            ImageTargetResult imageTargetResult = (ImageTargetResult) result;

            final Image image = state.getFrame().getImage(0);

            final int imageWidth = image.getWidth();
            final int imageHeight = image.getHeight();
            Log.i(LOGTAG, "Image Size w=" + imageWidth + " h=" + imageHeight);

            final Vec2F imageTargetSize = imageTarget.getSize();
            float width = imageTargetSize.getData()[0];
            float height = imageTargetSize.getData()[1];
            float halfWidth = width * 0.5f;
            float halfHeight = height * 0.5f;

            Log.i(LOGTAG, "Target Size: w=" + width + ", h=" + height);

            final Vec3F targetVec = new Vec3F(targetCenter_X, targetCenter_Y, targetCenter_Z);
            Log.i(LOGTAG, "Target Center:  " + vecToString(targetVec));

            Vec3F upperLeft = new Vec3F(targetCenter_X - halfWidth, targetCenter_Y + halfHeight, 0.f);
            Vec3F upperRight = new Vec3F(targetCenter_X + halfWidth, targetCenter_Y + halfHeight, 0.f);
            Vec3F lowerLeft = new Vec3F(targetCenter_X - halfWidth, targetCenter_Y - halfHeight, 0.f);
            Vec3F lowerRight = new Vec3F(targetCenter_X + halfWidth, targetCenter_Y - halfHeight, 0.f);

            Log.i(LOGTAG, "3D:  " + vecToString(upperLeft) + " " + vecToString(upperRight) + " " + vecToString(lowerLeft) + " " + vecToString(lowerRight));

            final CameraCalibration cameraCalibration = CameraDevice.getInstance().getCameraCalibration();
            Log.i(LOGTAG, "calibration: size=" + vecToString(cameraCalibration.getSize()) + " distortionParams=" + vecToString(cameraCalibration.getDistortionParameters()) + " focalLength=" + vecToString(cameraCalibration.getFocalLength()) + " principalPoint=" + vecToString(cameraCalibration.getPrincipalPoint()));
            final Vec2F upperLeftScreenSpace = Tool.projectPoint(cameraCalibration, pose, upperLeft);
            final Vec2F upperRightScreenSpace = Tool.projectPoint(cameraCalibration, pose, upperRight);
            final Vec2F lowerLeftScreenSpace = Tool.projectPoint(cameraCalibration, pose, lowerLeft);
            final Vec2F lowerRightScreenSpace = Tool.projectPoint(cameraCalibration, pose, lowerRight);

            Log.i(LOGTAG, "2D: " + vecToString(upperLeftScreenSpace) + " " + vecToString(upperRightScreenSpace) + " " + vecToString(lowerLeftScreenSpace) + " " + vecToString(lowerRightScreenSpace));
            final Vec2F targetInScreenSpace = Tool.projectPoint(cameraCalibration, pose, targetVec);
            Log.i(LOGTAG, "Target screen space: " + vecToString(targetInScreenSpace));

            final Vec2F upperLeft = cameraPointToScreenPoint(upperLeftScreenSpace);
            final Vec2F upperRight = cameraPointToScreenPoint(upperRightScreenSpace);
            final Vec2F lowerLeft = cameraPointToScreenPoint(lowerLeftScreenSpace);
            final Vec2F lowerRight = cameraPointToScreenPoint(lowerRightScreenSpace);


            VideoBackgroundConfig config = Renderer.
                    getInstance().getVideoBackgroundConfig();
            Log.i(LOGTAG, "VBC: " + config.getSize().getData()[0] + "x" + config.getSize().getData()[1]);
            Log.i(LOGTAG, "2D (image): " + vecToString(upperLeft) + " " + vecToString(upperRight) + " " + vecToString(lowerLeft) + " " + vecToString(lowerRight));
*/
            int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0
                    : 1;
            textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 2
                    : textureIndex;

            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];

            if (!mActivity.isExtendedTrackingActive()) {
                Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f,
                        OBJECT_SCALE_FLOAT);
                Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                        OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
            } else {
                Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
                        kBuildingScale, kBuildingScale);
            }

            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                    .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);

            if (!mActivity.isExtendedTrackingActive()) {
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mTeapot.getVertices());
                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mTeapot.getNormals());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());

                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);

                // activate texture 0, bind it, and pass to shader
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(textureIndex).mTextureID[0]);
                GLES20.glUniform1i(texSampler2DHandle, 0);

                // pass the model view matrix to the shader
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);

                // finally draw the teapot
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT, mTeapot.getIndices());

                // disable the enabled arrays
                GLES20.glDisableVertexAttribArray(vertexHandle);
                GLES20.glDisableVertexAttribArray(normalHandle);
                GLES20.glDisableVertexAttribArray(textureCoordHandle);
            } else {
                GLES20.glDisable(GLES20.GL_CULL_FACE);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, mBuildingsModel.getVertices());
                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, mBuildingsModel.getNormals());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mBuildingsModel.getTexCoords());

                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures.get(3).mTextureID[0]);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjection, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mBuildingsModel.getNumObjectVertex());

                SampleUtils.checkGLError("Renderer DrawBuildings");
            }

            SampleUtils.checkGLError("Render Frame");

        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        mRenderer.end();
    }


    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<Texture> textures) {
        mTextures = textures;

    }
}
