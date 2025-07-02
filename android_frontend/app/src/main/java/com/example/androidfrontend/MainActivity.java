package com.example.androidfrontend;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * PUBLIC_INTERFACE
 * MainActivity
 *
 * Minimalistic calculator supporting +, -, ×, ÷, decimal, clear, backspace, history, and orientation support.
 */
public class MainActivity extends AppCompatActivity {

    private TextView displayText;
    private TextView historyText;

    // State
    private StringBuilder currentInput = new StringBuilder();
    private double lastResult = 0.0;
    private String pendingOperator = "";
    private boolean isNewInput = false;
    private ArrayList<String> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme); // Explicit light theme
        setContentView(R.layout.activity_main);

        displayText = findViewById(R.id.text_display);
        historyText = findViewById(R.id.text_history);

        // Digit buttons
        int[] digitIds = { R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5,
                R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9 };
        for (int i = 0; i < digitIds.length; ++i) {
            int digit = i == 0 ? 0 : i; // btn_0 first, then 1-9
            Button btn = findViewById(digitIds[i]);
            btn.setOnClickListener(v -> onDigitClick(((Button) v).getText().toString()));
        }

        // Operation buttons
        findViewById(R.id.btn_plus).setOnClickListener(v -> onOperatorClick("+"));
        findViewById(R.id.btn_minus).setOnClickListener(v -> onOperatorClick("-"));
        findViewById(R.id.btn_multiply).setOnClickListener(v -> onOperatorClick("×"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> onOperatorClick("÷"));

        // Dot/decimal
        findViewById(R.id.btn_dot).setOnClickListener(v -> onDotClick());

        // Equals
        findViewById(R.id.btn_equals).setOnClickListener(v -> onEqualsClick());

        // Clear and backspace
        findViewById(R.id.btn_clear).setOnClickListener(v -> onClearClick());
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackspaceClick());
    }

    // PUBLIC_INTERFACE
    /** Called for any digit button pressed */
    private void onDigitClick(String digit) {
        if (isNewInput) {
            currentInput.setLength(0);
            isNewInput = false;
        }
        // Prevent multiple leading zeros
        if (currentInput.length() == 1 && currentInput.charAt(0) == '0' && !digit.equals(".")) {
            currentInput.setLength(0);
        }
        currentInput.append(digit);
        updateDisplay();
    }

    // PUBLIC_INTERFACE
    /** Called for "." (decimal point) button */
    private void onDotClick() {
        if (isNewInput) {
            currentInput.setLength(0);
            isNewInput = false;
        }
        if (currentInput.indexOf(".") == -1) {
            if (currentInput.length() == 0) currentInput.append("0");
            currentInput.append(".");
        }
        updateDisplay();
    }

    // PUBLIC_INTERFACE
    /** Called when an operation is clicked */
    private void onOperatorClick(String operator) {
        if (currentInput.length() > 0) {
            if (!TextUtils.isEmpty(pendingOperator)) {
                // Chain calculations
                onEqualsClick();
            } else {
                lastResult = parseInput();
            }
            currentInput.setLength(0);
        }
        pendingOperator = operator;
        isNewInput = false;
        updateDisplay();
    }

    // PUBLIC_INTERFACE
    /** Called when "=" is clicked */
    private void onEqualsClick() {
        if (TextUtils.isEmpty(pendingOperator)) return;
        double input = parseInput();
        double result = lastResult;
        String expression = formatNumber(lastResult) + " " + pendingOperator + " " + formatNumber(input);
        switch (pendingOperator) {
            case "+": result += input; break;
            case "-": result -= input; break;
            case "×": result *= input; break;
            case "÷":
                if (input == 0) {
                    displayText.setText("Error");
                    return;
                }
                result /= input; break;
        }
        String resultStr = formatNumber(result);

        // Optional: Add to history
        history.add(expression + " = " + resultStr);
        if (history.size() > 3) history.remove(0); // Keep latest 3

        // Update UI state
        lastResult = result;
        displayText.setText(resultStr);
        showHistory();
        pendingOperator = "";
        currentInput.setLength(0);
        isNewInput = true;
    }

    // PUBLIC_INTERFACE
    /** Called when "C" (clear) is pressed */
    private void onClearClick() {
        currentInput.setLength(0);
        lastResult = 0.0;
        pendingOperator = "";
        displayText.setText("0");
        isNewInput = false;
    }

    // PUBLIC_INTERFACE
    /** Called when backspace is pressed */
    private void onBackspaceClick() {
        if (isNewInput || currentInput.length() == 0) return;
        currentInput.deleteCharAt(currentInput.length() - 1);
        updateDisplay();
    }

    /** Update the calculator number display */
    private void updateDisplay() {
        if (currentInput.length() > 0) {
            displayText.setText(currentInput.toString());
        } else if (!TextUtils.isEmpty(pendingOperator)) {
            displayText.setText(formatNumber(lastResult) + " " + pendingOperator);
        } else {
            displayText.setText(formatNumber(lastResult));
        }
    }

    /** Show the last N calculation history lines */
    private void showHistory() {
        StringBuilder hist = new StringBuilder();
        for (String h : history) {
            hist.append(h).append("\n");
        }
        historyText.setText(hist.toString().trim());
    }

    /** Parse input value from currentInput, with default fallback 0. */
    private double parseInput() {
        try {
            if (currentInput.length() == 0) return lastResult;
            return Double.parseDouble(currentInput.toString());
        } catch (Exception ex) {
            return lastResult;
        }
    }

    /** Format number for display (cleans up decimals). */
    private String formatNumber(double num) {
        if ((long) num == num) {
            return String.format("%d", (long) num);
        } else {
            return String.format("%s", num);
        }
    }
}
