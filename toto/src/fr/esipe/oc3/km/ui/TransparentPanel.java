package fr.esipe.oc3.km.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TransparentPanel extends LinearLayout {

	Paint innerPaint, borderPaint;

	public TransparentPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TransparentPanel(Context context) {
		super(context);
		init();
	}

	private void init() {
		innerPaint = new Paint();
//		innerPaint.setARGB(150, 75, 75, 75); //gray
		innerPaint.setColor(android.graphics.Color.GRAY);
		innerPaint.setAlpha(170);
		innerPaint.setAntiAlias(true);

		borderPaint = new Paint();
		borderPaint.setColor(android.graphics.Color.BLACK);
		borderPaint.setAlpha(225);
//		borderPaint.setARGB(255, 0, 0, 0);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(7);
	}

	public void setInnerPaint(Paint innerPaint) {
		this.innerPaint = innerPaint;
	}

	public void setBorderPaint(Paint borderPaint) {
		this.borderPaint = borderPaint;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		RectF drawRect = new RectF();
		drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

		canvas.drawRoundRect(drawRect, 10, 10, innerPaint);
		canvas.drawRoundRect(drawRect, 13, 13, borderPaint);

	}

}
