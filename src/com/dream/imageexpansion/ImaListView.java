package com.dream.imageexpansion;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;


public class ImaListView extends ListView {

	private static final int BACK_SCALE = 0;
	private boolean isHaveHead = false;
	private float scaleY = 0;
	private int displayWidth;
	private boolean isBacking = false;
	private Context mContext;
	private Bitmap bmp;
	private View headerView;
	private ImageView imageView;
	private Matrix matrix = new Matrix();
	private Matrix currentMatrix = new Matrix();
	private Matrix defaultMatrix = new Matrix();
	private float imgHeight, imgWidth;
	private int mode = 0;
	private final int MODE_DRAG = 1;
	private PointF startPoint = new PointF();

	private int mImageId;

	private AttributeSet attrs;

	public ImaListView(Context context) {
		super(context);
		this.mContext = context;
		initView();
	}

	public ImaListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		this.attrs = attrs;
		initView();
	}

	public ImaListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		this.attrs = attrs;
		initView();
	}

	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}

	public void addHeaderView(View v) {
		super.addHeaderView(v);
	}

	public void setImageId(int id) {
		this.mImageId = id;
		bmp = BitmapFactory.decodeResource(getResources(), mImageId);
		if (isHaveHead)
			this.removeHeaderView(headerView);
		initHead();
	}

	public void setImageBitmap(Bitmap bit) {
		this.bmp = bit;
		if (isHaveHead)
			this.removeHeaderView(headerView);
		initHead();
	}

	private void initView() {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager mWm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		mWm.getDefaultDisplay().getMetrics(dm);
		displayWidth = dm.widthPixels;

		TypedArray a = mContext.obtainStyledAttributes(attrs,
				R.styleable.ImgListView);
		mImageId = a.getResourceId(R.styleable.ImgListView_headimage, 0);
		a.recycle();
		if (null == bmp && mImageId != 0) {
			bmp = BitmapFactory.decodeResource(getResources(), mImageId);
			initHead();
		}
	}

	private void initHead() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		headerView = inflater.inflate(R.layout.top_img, null);
		imageView = (ImageView) headerView.findViewById(R.id.imageView);
		float scale = (float) displayWidth / (float) bmp.getWidth();// 1080/1800
		matrix.postScale(scale, scale, 0, 0);
		imageView.setImageMatrix(matrix);
		defaultMatrix.set(matrix);
		imgHeight = scale * bmp.getHeight();
		imgWidth = scale * bmp.getWidth();
		ListView.LayoutParams relativeLayout = new ListView.LayoutParams(
				(int) imgWidth, (int) imgHeight);
		imageView.setLayoutParams(relativeLayout);
		this.addHeaderView(headerView);
		isHaveHead = true;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent event) {

		if (!isHaveHead) {
			return super.onTouchEvent(event);
		}
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			if (isBacking) {
				return super.onTouchEvent(event);
			}
			int[] location = new int[2];
			imageView.getLocationInWindow(location);
			if (location[1] >= 0) {
				mode = MODE_DRAG;
				currentMatrix.set(imageView.getImageMatrix());
				startPoint.set(event.getX(), event.getY());
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == MODE_DRAG) {
				float dx = event.getX() - startPoint.x;
				float dy = event.getY() - startPoint.y; 
				if (dy / 2 + imgHeight <= 1.5 * imgHeight) {
					matrix.set(currentMatrix);
					float scale = (dy / 2 + imgHeight) / (imgHeight);
					if (dy > 0) {
						scaleY = dy;
						ListView.LayoutParams relativeLayout = new ListView.LayoutParams(
								(int) (scale * imgWidth),
								(int) (scale * imgHeight));
						imageView.setLayoutParams(relativeLayout);
						matrix.postScale(scale, scale, imgWidth / 2, 0);
						imageView.setImageMatrix(matrix);
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			mHandler.sendEmptyMessage(BACK_SCALE);
		case MotionEvent.ACTION_POINTER_UP:
			mode = 0;
			break;
		}

		return super.onTouchEvent(event);
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BACK_SCALE:
				float scale = (scaleY / 2 + imgHeight) / (imgHeight);
				if (scaleY > 0) {
					isBacking = true;
					matrix.set(currentMatrix);
					ListView.LayoutParams relativeLayout = new ListView.LayoutParams(
							(int) (scale * imgWidth), (int) (scale * imgHeight));
					imageView.setLayoutParams(relativeLayout);
					matrix.postScale(scale, scale, imgWidth / 2, 0);
					imageView.setImageMatrix(matrix);
					scaleY = (float) (scaleY / 2 - 1);
					mHandler.sendEmptyMessageDelayed(BACK_SCALE, 20);
				} else {
					scaleY = 0;
					ListView.LayoutParams relativeLayout = new ListView.LayoutParams(
							(int) imgWidth, (int) imgHeight);
					imageView.setLayoutParams(relativeLayout);
					matrix.set(defaultMatrix);
					imageView.setImageMatrix(matrix);
					isBacking = false;
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
}
