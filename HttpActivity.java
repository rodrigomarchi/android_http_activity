
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import br.com.ge.amigoreal.R;

public abstract class HttpActivity extends Activity implements HttpViewHandler {

    private List<EditText> editTextArrayList = new ArrayList<>();
    private View.OnFocusChangeListener hideKeyboardOnFocusChanged = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(!hasFocus)
                hideSoftKeyboard();
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ViewGroup root = (ViewGroup) findViewById(R.id.form);
        findEditText(root);
    }

    private void findEditText(ViewGroup root){
        if(root!=null) {
            EditText editTextTmp;
            for (int i = 0; i < root.getChildCount(); i++)
                if (root.getChildAt(i) instanceof EditText) {
                    editTextTmp = (EditText) root.getChildAt(i);
                    editTextArrayList.add(editTextTmp);
                } else if (root.getChildAt(i) instanceof ViewGroup) {
                    findEditText((ViewGroup) root.getChildAt(i));
                }
        }
    }

    private boolean isTouchInsideView(final MotionEvent ev, final View currentFocus) {
        final int[] loc = new int[2];
        currentFocus.getLocationOnScreen(loc);
        return ev.getRawX() > loc[0] && ev.getRawY() > loc[1] && ev.getRawX() < (loc[0] + currentFocus.getWidth())
                && ev.getRawY() < (loc[1] + currentFocus.getHeight());
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        // all touch events close the keyboard before they are processed except EditText instances.
        // if focus is an EditText we need to check, if the touchevent was inside the focus editTexts
        final View currentFocus = getCurrentFocus();
        if (!(currentFocus instanceof EditText) || !isTouchInsideView(ev, currentFocus)) {
            ((InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return super.dispatchTouchEvent(ev);
    }

    public List<EditText> getEditTextArrayList(){
        return editTextArrayList;
    }

    @Override
    public void showProgress(Boolean show){
        if(findViewById(R.id.form) == null || findViewById(R.id.progress) == null)
            return;

        if(show){
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            findViewById(R.id.form).setVisibility(View.GONE);
        } else {
            findViewById(R.id.progress).setVisibility(View.GONE);
            findViewById(R.id.form).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Activity getCurrentActivity() {
        return this;
    }

    public Boolean areFieldsValidates(){
        hideSoftKeyboard();
        Boolean validate = true;
        for(EditText editText:getEditTextArrayList()){
            String msg = validateField(editText, editText.getText().toString());
            if(msg != null){
                editText.setError(msg);
                if(validate)
                    editText.requestFocus();
                validate = false;
            }
        }
        return validate;
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }


    public String validateField(EditText editText, String text) {
        return null;
    }

    public void doPost(String url, Hashtable<String, String> params) {
        PostTask postTask = new PostTask(url, params, this);
        postTask.execute();
    }
}
