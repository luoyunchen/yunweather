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
	// 屏幕闲置时 
	private static final int TOUCH_STATE_REST = 0; 
	// 屏幕滚动时 
	private static final int TOUCH_STATE_SCROLLING = 1; 
	private int mTouchState = TOUCH_STATE_REST; 
	
	public CustomScrollView(Context context, AttributeSet attrs, int defStyle) { 
		super(context, attrs, defStyle); 
		mScroller = new Scroller(context); 
		// 当前屏幕即为默认的屏幕，即为第一屏 
		mCurrentScreen = mDefaultScreen; 
		// 是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件 
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop(); 
	} 
  
	public CustomScrollView(Context context, AttributeSet attrs) { 
	// 0表示没有风格 
		this(context, attrs, 0); 
	}

	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub
		int childTop = 0;
		int childCount = getChildCount();  //返回的是现实层面上所包含的子View个数。
		for (int i = 0; i < childCount; i++) { 
			final View childView = getChildAt(i);  //getChildAt(0)只能获得当前能看到的item的第i个,并不完全是所有在List中的第一个！
			if (childView.getVisibility() != View.GONE) {  //不等于隐藏
				final int childHeight = childView.getMeasuredHeight(); //是实际View的高度，与屏幕无关
				childView.layout(0, childTop, childView.getMeasuredHeight(), childTop + childHeight); 
				childTop +=childHeight;
			} 
		} 
	} 
	
	public void computeScroll() { 
		//是用来判断动画是否完成，如果没有完成返回true继续执行界面刷新的操作，各种位置信息将被重新计算用以重新绘制最新状态的界面。 
		/** 
		* 如果动画没有完成(mScroller.computeScrollOffset() == true)那么就使用scrollTo方法对mScrollX、mScrollY的值进行重新计算刷新界面， 
		  
		调用postInvalidate()方法重新绘制界面， 
		  
		postInvalidate()方法会调用invalidate()方法， 
		  
		invalidate()方法又会调用computeScroll方法， 
		  
		就这样周而复始的相互调用，直到mScroller.computeScrollOffset() 返回false才会停止界面的重绘动作 
		*/
		if (mScroller.computeScrollOffset()) { 
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY()); 
			postInvalidate(); 
		} 
	} 
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
		super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
		final int height = MeasureSpec.getSize(heightMeasureSpec); //根据提供的测量值(格式)提取大小值(这个大小也就是我们通常所说的大小)
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec); 
		if (widthMode != MeasureSpec.EXACTLY) { 
			throw new IllegalStateException("ScrollLayout only canmCurScreen run at EXACTLY mode!"); 
		} 
	  
		/** 
		* wrap_content 传进去的是AT_MOST 代表的是最大尺寸 
		* 固定数值或fill_parent 传入的模式是EXACTLY 代表的是精确的尺寸 
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
		//在当前视图内容偏移至(x , y)坐标处，即显示(可视)区域位于(x , y)坐标处。 
		//将View的Content的位置移动到(x,y)，而View的大小和位置不发生改变。如果Content超出了View的范围，则超出的部分会被挡住。 
		////scrollTo(mCurrentScreen * width, 0); 
		scrollTo(0,mCurrentScreen*height); 
	} 
	
	public void snapToDestination() { 
		////final int screenWidth = getWidth(); 
		final int screenHeight = getHeight();
		////final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth; 
		final int destScreen = (getScrollY() + screenHeight / 2) / screenHeight; 
		//根据当前x坐标位置确定切换到第几屏  
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
		//用来追踪触摸事件（flinging事件和其他手势事件）的速率。用obtain()函数来获得类的实例 
		mVelocityTracker = VelocityTracker.obtain(); 
		} 
		//用addMovement(MotionEvent)函数将motion event加入到VelocityTracker类实例中 
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
				//在当前视图内容继续偏移(x , y)个单位，显示(可视)区域也跟着偏移(x,y)个单位 
				scrollBy(0, deltaY); 
			break; 
			case MotionEvent.ACTION_UP: 
				final VelocityTracker velocityTracker = mVelocityTracker; 
				//当你使用到速率时，使用computeCurrentVelocity(int)初始化速率的单位，并获得当前的事件的速率， 
				//然后使用getXVelocity() 或getXVelocity()获得横向和竖向的速率。  
				//1000:你使用的速率单位.1的意思是，以一毫秒运动了多少个像素的速率， 1000表示 一秒时间内运动了多少个像素。  
				velocityTracker.computeCurrentVelocity(1000); 
				int velocityY = (int) velocityTracker.getYVelocity();
				//如果手指滑动速率>600并且当前屏幕切换，因为当前屏幕初始化的时候是第一屏，为0 
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
		if(Configure.isMove) return false;//拦截分发给子控件 
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
