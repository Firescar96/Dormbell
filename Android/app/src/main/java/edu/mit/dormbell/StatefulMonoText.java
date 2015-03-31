package edu.mit.dormbell;

import android.content.Context;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;

public class StatefulMonoText extends EditText implements TextWatcher {

	private Context context;

    public static final int TEXT_BAD=0;
    public static final int TEXT_GOOD=1;
    public static final int TEXT_PROGRESS=2;
    public static final int TEXT_UNSET=3;

    private int textState=TEXT_UNSET;

	public StatefulMonoText(Context context) {
		super(context);
		this.context = context;
		addTextChangedListener(this);
	}

	public StatefulMonoText(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		addTextChangedListener(this);
	}

	public StatefulMonoText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		addTextChangedListener(this);
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
        textState = TEXT_UNSET;}

	@Override
	public void afterTextChanged(Editable ed) {
		System.out.println("text chaningdd");
		String result = ed.toString().replaceAll(" ", "");
	    if (!ed.toString().equals(result)) 
	    {
	         setText(result);
	         setSelection(result.length());
	    }
	}

    public int getTextState() {
        return textState;
    }

    public void setTextState(int state) {
        textState = state;
    }
}
