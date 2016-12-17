package course.examples.Graphics.CanvasBubbleSurfaceView;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/* @author A. Porter
 * Revised by S. Anderson
 */
public class BubbleActivity extends Activity {

	BubbleView mBubbleView;
	int x = 0;
	double mFPS = 0;
	int mScore = 0;
	private double currentTime = 0;
	private double startTime = 0;
	private static final String TAG = "FPS";


	/** Simply create layout and view. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		startTime = System.currentTimeMillis();
        TextView mTextView = (TextView) findViewById(R.id.FPS);
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.frame);
		// decode resource into a bitmap
		final BubbleView bubbleView = new BubbleView(getApplicationContext(),
				BitmapFactory.decodeResource(getResources(), R.drawable.b256));

		relativeLayout.addView(bubbleView);
	}

	/*
	  SurfaceView is dedicated drawing surface in the view hierarchy.
      SurfaceHolder.Callback determines changes to SurfaceHolder via surfaceXXX
      callbacks.
	 */
	private class BubbleView extends SurfaceView implements

			SurfaceHolder.Callback {

		private final Bitmap mBitmap;
		private final int mBitmapHeightAndWidth, mBitmapHeightAndWidthAdj;
		private final DisplayMetrics mDisplay;
		private final int mDisplayWidth, mDisplayHeight;
		private float mX, mY, mDx, mDy, mRotation;
		private final SurfaceHolder mSurfaceHolder;
		private final Paint mPainter = new Paint(); // control style and color
		private Thread mDrawingThread;
		private boolean intersects = false;

		private static final int MOVE_STEP = 1;
		private static final float ROT_STEP = 0.5f;

		public BubbleView(Context context, Bitmap bitmap) {
			super(context);

			mBitmapHeightAndWidth = (int) getResources().getDimension(
					R.dimen.image_height);
			this.mBitmap = Bitmap.createScaledBitmap(bitmap,
					mBitmapHeightAndWidth/5, mBitmapHeightAndWidth/5, false);

			mBitmapHeightAndWidthAdj = mBitmapHeightAndWidth / 2;

			mDisplay = new DisplayMetrics();
			// get display width/height
			BubbleActivity.this.getWindowManager().getDefaultDisplay()
					.getMetrics(mDisplay);
			mDisplayWidth = mDisplay.widthPixels;
			mDisplayHeight = mDisplay.heightPixels;

			// Give bubble random coords and speed at creation
			Random r = new Random();
			mX = (float) r.nextInt(mDisplayHeight);
			mY = (float) r.nextInt(mDisplayWidth);
			mDx = (float) r.nextInt(mDisplayHeight) / mDisplayHeight;
			mDx *= r.nextInt(2) == 1 ? MOVE_STEP : -1 * MOVE_STEP;
			mDy = (float) r.nextInt(mDisplayWidth) / mDisplayWidth;
			mDy *= r.nextInt(2) == 1 ? MOVE_STEP : -1 * MOVE_STEP;
			mDx *= 0.1;
			mDy *= 0.1;
			mRotation = 1.0f;

			mPainter.setAntiAlias(true); // smooth edges of bitmap
			// This will take care of changes to the bitmap
			mSurfaceHolder = getHolder();
			mSurfaceHolder.addCallback(this);
		}

		/** drawing and rotation */
		private void drawBubble(Canvas canvas) {
			canvas.drawColor(Color.DKGRAY);
			mRotation += ROT_STEP;
			canvas.rotate(mRotation, mY + mBitmapHeightAndWidthAdj, mX
					+ mBitmapHeightAndWidthAdj);
			canvas.drawBitmap(mBitmap, mY, mX, mPainter);
            x++;
            currentTime = System.currentTimeMillis();
            if (currentTime/1000 >= startTime/1000 + 1){
                mFPS = calcFPS(x);
                startTime = System.currentTimeMillis();
                x = 0;
            }

        }
        public void UpdateUI(){
            //mTextView.setText("FPS: "+mFPS+" "+"SCORE:"+mScore);
        }
		/** True iff bubble can move. */
		private boolean move() {
			mX += mDx;
			mY += mDy;
			if (mX < 0 - mBitmapHeightAndWidth
					|| mX > mDisplayHeight + mBitmapHeightAndWidth
					|| mY < 0 - mBitmapHeightAndWidth
					|| mY > mDisplayWidth + mBitmapHeightAndWidth) {
				return false;
			} else {
				return true;
			}
		}

		/** Does nothing for surface change */
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}


		public double calcFPS(int frameCount){
			double fps = 0;
			fps = (frameCount/((currentTime - startTime) / 1000));
			Log.d(TAG, " frame "+frameCount +"fps "+ fps+ "formula"+ ((currentTime - startTime) / 1000));
			return fps;
		}

		/** When surface created, this creates its thread AND starts it running. */
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// Run as separate thread.
			mDrawingThread = new Thread(new Runnable() {
				public void run() {
					Canvas canvas = null;
					// While bubble within view, lock and draw.
					while (!Thread.currentThread().isInterrupted() && move()) {
						canvas = mSurfaceHolder.lockCanvas();
						if (null != canvas) { // Lock canvas while updating bitmap
							drawBubble(canvas);
							mSurfaceHolder.unlockCanvasAndPost(canvas);
						}
					}
				}
			});
			mDrawingThread.start();
		}
		/** Surface destroyed; stop thread. */
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (null != mDrawingThread)
				mDrawingThread.interrupt();
		}

	}
}