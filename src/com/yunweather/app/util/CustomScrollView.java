package com.yunweather.app.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class CustomScrollView extends ViewGroup {
	private Scroller mScroller;
	private int mCurrentScreen;
	private int mDefaultScreen = 0;
	private int mTouchSlop;
	private VelocityTracker mVelocityTracker;
	private float mLastMotionY;
	private static final int SNAP_VELOCITY = 600;
	private PageListener pageListener;
	// ��Ļ����ʱ 
	private static final int TOUCH_STATE_REST = 0; 
	// ��Ļ����ʱ 
	private static final int TOUCH_STATE_SCROLLING = 1; 
	private int mTouchState = TOUCH_STATE_REST; 
	
	public CustomScrollView(Context context, AttributeSet attrs, int defStyle) { 
		super(context, attrs, defStyle); 
		mScroller = new Scroller(context); 
		// ��ǰ��Ļ��ΪĬ�ϵ���Ļ����Ϊ��һ�� 
		mCurrentScreen = mDefaultScreen; 
		// ��һ�����룬��ʾ������ʱ���ֵ��ƶ�Ҫ�����������ſ�ʼ�ƶ��ؼ� 
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop(); 
	} 
  
	public CustomScrollView(Context context, AttributeSet attrs) { 
	// 0��ʾû�з�� 
		this(context, attrs, 0); 
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		int childTop = 0;
		int childCount = getChildCount();  //���ص�����ʵ����������������View������
		for (int i = 0; i < childCount; i++) { 
			final View childView = getChildAt(i);  //getChildAt(0)ֻ�ܻ�õ�ǰ�ܿ�����item�ĵ�i��,������ȫ��������List�еĵ�һ����
			if (childView.getVisibility() != View.GONE) {  //����������
				final int childHeight = childView.getMeasuredHeight(); //��ʵ��View�ĸ߶ȣ�����Ļ�޹�
				childView.layout(0, childTop, childView.getMeasuredHeight(), childTop + childHeight); 
				childTop +=childHeight;
			} 
		} 
	} 
	
	public void computeScroll() { 
		//�������ж϶����Ƿ���ɣ����û����ɷ���true����ִ�н���ˢ�µĲ���������λ����Ϣ�������¼����������»�������״̬�Ľ��档 
		/** 
		* �������û�����(mScroller.computeScrollOffset() == true)��ô��ʹ��scrollTo������mScrollX��mScrollY��ֵ�������¼���ˢ�½��棬 
		  
		����postInvalidate()�������»��ƽ��棬 
		  
		postInvalidate()���������invalidate()������ 
		  
		invalidate()�����ֻ����computeScroll������ 
		  
		�������ܶ���ʼ���໥���ã�ֱ��mScroller.computeScrollOffset() ����false�Ż�ֹͣ������ػ涯�� 
		*/
		if (mScroller.computeScrollOffset()) { 
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY()); 
			postInvalidate(); 
		} 
	} 
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
		super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
		final int height = MeasureSpec.getSize(heightMeasureSpec); //�����ṩ�Ĳ���ֵ(��ʽ)��ȡ��Сֵ(�����СҲ��������ͨ����˵�Ĵ�С)
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec); 
		if (widthMode != MeasureSpec.EXACTLY) { 
			throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!"); 
		} 
	  
		/** 
		* wrap_content ����ȥ����AT_MOST ����������ߴ� 
		* �̶���ֵ��fill_parent �����ģʽ��EXACTLY ������Ǿ�ȷ�ĳߴ� 
		*/
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec); 
		if (heightMode != MeasureSpec.EXACTLY) { 
			throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!"); 
		} 
	  
		// The children are given the same width and height as the scrollLayout 
		final int count = getChildCount(); 
		for (int i = 0; i < count; i++) { 
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec); 
		} 
		//�ڵ�ǰ��ͼ����ƫ����(x , y)���괦������ʾ(����)����λ��(x , y)���괦�� 
		//��View��Content��λ���ƶ���(x,y)����View�Ĵ�С��λ�ò������ı䡣���Content������View�ķ�Χ���򳬳��Ĳ��ֻᱻ��ס�� 
		////scrollTo(mCurrentScreen * width, 0); 
		scrollTo(0,mCurrentScreen*height); 
	} 
	
	public void snapToDestination() { 
		////final int screenWidth = getWidth(); 
		final int screenHeight = getHeight();
		////final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth; 
		final int destScreen = (getScrollY() + screenHeight / 2) / screenHeight; 
		//���ݵ�ǰx����λ��ȷ���л����ڼ���  
		snapToScreen(destScreen); 
	} 
	
	public void snapToScreen(int whichScreen) { 
		// get the valid layout page 
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1)); 
		if (getScrollY() != (whichScreen * getHeight())) { 	  
			final int delta = whichScreen * getHeight() - getScrollY();  
			mScroller.startScroll(0,getScrollY(), 0,delta,  Math.abs(delta) * 2); 
			mCurrentScreen = whichScreen; 
			if(mCurrentScreen>Configure.curentPage){ 
				Configure.curentPage = whichScreen; 
				pageListener.page(Configure.curentPage); 
			}else if(mCurrentScreen<Configure.curentPage){ 
				Configure.curentPage = whichScreen; 
				pageListener.page(Configure.curentPage); 
			} 
			invalidate(); // Redraw the layout 
		} 
	} 
	
	public int getCurScreen() { 
		return mCurrentScreen; 
	} 
	
	@Override
	public boolean onTouchEvent(MotionEvent event) { 
	  
		if (mVelocityTracker == null) { 
		//����׷�ٴ����¼���flinging�¼������������¼��������ʡ���obtain()������������ʵ�� 
		mVelocityTracker = VelocityTracker.obtain(); 
		} 
		//��addMovement(MotionEvent)������motion event���뵽VelocityTracker��ʵ���� 
		mVelocityTracker.addMovement(event); 
		  
		final int action = event.getAction(); 
		final float y = event.getY(); 
	  
		switch (action) { 
			case MotionEvent.ACTION_DOWN: 
				if (!mScroller.isFinished()) { 
					mScroller.abortAnimation(); 
				} 
				mLastMotionY = y; 
			break; 
			case MotionEvent.ACTION_MOVE: 
				int deltaY = (int) (mLastMotionY - y); 
				mLastMotionY = y; 
				//�ڵ�ǰ��ͼ���ݼ���ƫ��(x , y)����λ����ʾ(����)����Ҳ����ƫ��(x,y)����λ 
				scrollBy(0, deltaY); 
			break; 
			case MotionEvent.ACTION_UP: 
				final VelocityTracker velocityTracker = mVelocityTracker; 
				//����ʹ�õ�����ʱ��ʹ��computeCurrentVelocity(int)��ʼ�����ʵĵ�λ������õ�ǰ���¼������ʣ� 
				//Ȼ��ʹ��getXVelocity() ��getXVelocity()��ú������������ʡ�  
				//1000:��ʹ�õ����ʵ�λ.1����˼�ǣ���һ�����˶��˶��ٸ����ص����ʣ� 1000��ʾ һ��ʱ�����˶��˶��ٸ����ء�  
				velocityTracker.computeCurrentVelocity(1000); 
				int velocityY = (int) velocityTracker.getYVelocity();
				//�����ָ��������>600���ҵ�ǰ��Ļ�л�����Ϊ��ǰ��Ļ��ʼ����ʱ���ǵ�һ����Ϊ0 
				if (velocityY > SNAP_VELOCITY && getCurScreen() > 0) { 
					// Fling enough to move left 
					snapToScreen(getCurScreen() - 1); 
					//--Configure.curentPage; 
					pageListener.page(Configure.curentPage); 
				} 
				else if (velocityY < -SNAP_VELOCITY && getCurScreen() < getChildCount() - 1) { 
					// Fling enough to move right 
					snapToScreen(getCurScreen() + 1);  
				} else { 
					snapToDestination(); 
				} 
				if (mVelocityTracker != null) { 
					mVelocityTracker.recycle(); 
					mVelocityTracker = null; 
				} 
				mTouchState = TOUCH_STATE_REST; 
			break; 
			case MotionEvent.ACTION_CANCEL: 
				mTouchState = TOUCH_STATE_REST; 
			break; 
		} 
		return true; 
	} 
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) { 
		if(Configure.isMove) return false;//���طַ����ӿؼ� 
		final int action = ev.getAction(); 
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) { 
			return true; 
		} 
	  
		///final float x = ev.getX(); 
		final float y = ev.getY(); 
		  
		switch (action) { 
		case MotionEvent.ACTION_MOVE: 
			////final int xDiff = (int) Math.abs(mLastMotionX - x); 
			final int yDiff = (int) Math.abs(mLastMotionY - y); 
			////if (xDiff > mTouchSlop) { 
			if (yDiff > mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING; 
			} 
			break; 
		case MotionEvent.ACTION_DOWN: 
			///mLastMotionX = x; 
			mLastMotionY = y; 
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING; 
			break; 
		case MotionEvent.ACTION_CANCEL: 
		case MotionEvent.ACTION_UP: 
			mTouchState = TOUCH_STATE_REST; 
			break; 
		} 
		return mTouchState != TOUCH_STATE_REST; 
	} 
	
	public void setPageListener(PageListener pageListener) { 
		this.pageListener = pageListener; 
	} 
	  
	public interface PageListener { 
		void page(int page); 
	} 
}
