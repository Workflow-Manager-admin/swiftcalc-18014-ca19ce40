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
        // Map digits 0-9
        for (int i = 0; i < digitIds.length; ++i) {
            Button btn = findViewById(digitIds[i]);
            btn.setOnClickListener(v -> onDigitClick(((Button) v).getText().toString()));
        }

        // Operation buttons (ensure the sign matches XML layout)
        findViewById(R.id.btn_plus).setOnClickListener(v -> onOperatorClick("+"));
        findViewById(R.id.btn_minus).setOnClickListener(v -> onOperatorClick("−")); // Use unicode minus to match XML; careful on comparison later!
        findViewById(R.id.btn_multiply).setOnClickListener(v -> onOperatorClick("×"));
        findViewById(R.id.btn_divide).setOnClickListener(v -> onOperatorClick("÷"));

        // Dot/decimal
        findViewById(R.id.btn_dot).setOnClickListener(v -> onDotClick());

        // Equals
        findViewById(R.id.btn_equals).setOnClickListener(v -> onEqualsClick());

        // Clear and backspace
        findViewById(R.id.btn_clear).setOnClickListener(v -> onClearClick());
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackspaceClick());

        // Always show display properly for first launch
        updateDisplay();
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
                onEqualsClick();
            } else {
                lastResult = parseInput();
            }
            currentInput.setLength(0);
        }
        // Always store operator in normalized form for logic ("-")
        if (operator.equals("−")) {
            pendingOperator = "-";
        } else {
            pendingOperator = operator;
        }
        isNewInput = false;
        updateDisplay();
    }

    // PUBLIC_INTERFACE
    /** Called when "=" is clicked */
    private void onEqualsClick() {
        if (TextUtils.isEmpty(pendingOperator)) return;
        double input = parseInput();
        double result = lastResult;
        // Human-friendly display matches UI, but logic must match actual sign
        String opDisplay = pendingOperator.equals("-") ? "−" : pendingOperator;
        String expression = formatNumber(lastResult) + " " + opDisplay + " " + formatNumber(input);
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
        if (history.size() > 3) history.remove(0);

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
        isNewInput = false;
        updateDisplay(); // Always call updateDisplay, which handles proper display logic
    }

    // PUBLIC_INTERFACE
    /** Called when backspace is pressed */
    private void onBackspaceClick() {
        if (isNewInput || currentInput.length() == 0) return;
        currentInput.deleteCharAt(currentInput.length() - 1);
        // If deletion leaves empty, display should be "0"
        if (currentInput.length() == 0) {
            updateDisplay();
        } else {
            displayText.setText(currentInput.toString());
        }
    }

    /** Update the calculator number display */
    private void updateDisplay() {
        if (currentInput.length() > 0) {
            displayText.setText(currentInput.toString());
        } else if (!TextUtils.isEmpty(pendingOperator)) {
            // Provide the appropriate sign for display ("−" for minus)
            String opDisp = pendingOperator.equals("-") ? "−" : pendingOperator;
            displayText.setText(formatNumber(lastResult) + " " + opDisp);
        } else {
            if (lastResult == 0.0 && currentInput.length() == 0) {
                displayText.setText("0");
            } else {
                displayText.setText(formatNumber(lastResult));
            }
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
