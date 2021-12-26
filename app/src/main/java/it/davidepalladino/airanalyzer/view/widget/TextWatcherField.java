package it.davidepalladino.airanalyzer.view.widget;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class TextWatcherField implements TextWatcher {
    public interface AuthTextWatcherCallback {
        public boolean checkAuthEditText(EditText editText);
    }
    private AuthTextWatcherCallback callback;
    private EditText editText;

    public TextWatcherField(AuthTextWatcherCallback callback, EditText editText) {
        this.callback = callback;
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (callback != null) {
            callback.checkAuthEditText(editText);
        }
    }
}
