package it.davidepalladino.airanalyzer.controller;

import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckField {
    private static final String expressionUsername = "^[a-z0-9_-]{5,20}$";
    private static final String expressionPassword = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#])[A-Za-z\\d@$!%*?&.#]{16,24}$";
    private static final String expressionEmail = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

    public static boolean checkUsername(EditText editText) {
        return checkSyntax(editText, expressionUsername);
    }

    public static boolean checkPassword(EditText editText) {
        return checkSyntax(editText, expressionPassword);
    }

    public static boolean checkEmail(EditText editText) {
        return checkSyntax(editText, expressionEmail);
    }

    private static boolean checkSyntax(EditText editText, String expression) {
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(editText.getText().toString());

        return matcher.matches();
    }
}
