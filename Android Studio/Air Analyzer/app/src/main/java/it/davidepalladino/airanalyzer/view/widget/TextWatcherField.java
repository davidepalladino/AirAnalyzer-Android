/*
 * This view class provides to check the EditText field, providing a callback method for the purpose,
 *  and that will be utilized into the original methods of TextWatcher superclass.
 * For an example, see the Login activity.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 1.0.1
 * @date 3rd January, 2022
 *
 * This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 */

package it.davidepalladino.airanalyzer.view.widget;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class TextWatcherField implements TextWatcher {
    public interface AuthTextWatcherCallback {
        boolean checkSyntaxEditText(EditText editText);
    }

    private final AuthTextWatcherCallback callback;
    private final EditText editText;

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
            callback.checkSyntaxEditText(editText);
        }
    }
}
