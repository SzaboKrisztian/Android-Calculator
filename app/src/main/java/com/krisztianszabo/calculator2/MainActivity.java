package com.krisztianszabo.calculator2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity {

    // Old-school calculators don't let you modify a result, instead you start typing a new
    // number, or if an operation is pressed, you operate on the result as it is. This flag
    // represents that state.
    private boolean isInResultState = false;
    // Attribute that provides "memory" for a value, for the functions M+, M-, MR, MC
    private String memory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void buttonPress(View view) {
        // Get references for all the needed textViews
        TextView currentValueView = findViewById(R.id.display);
        TextView previousValueView = findViewById(R.id.display2);
        TextView operationView = findViewById(R.id.display3);
        TextView memoryView = findViewById(R.id.display4);

        // Extract the Strings from the textViews
        String currentValue = currentValueView.getText().toString();
        String previousValue = previousValueView.getText().toString();
        String operation = operationView.getText().toString();
        String temporaryValue;

        // Get the button's tag
        Button button = (Button) view;
        String pressedButton = (String)button.getTag();

        // Take action according to which button was pressed
        switch (pressedButton) {
            // Number input functions
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
            case "0":
                // If we're in the result state, and no operation was chosen we should
                // overwrite the result and start inputting a new number
                if (isInResultState && operation.isEmpty()) {
                    currentValueView.setText(pressedButton);
                    isInResultState = false;

                // If an operation was just chosen, we need to start inputting a new
                // number (i.e. the second operand) and save the current one as the
                // old number (i.e. the first operand)
                } else if (!operation.isEmpty() && previousValue.isEmpty()) {
                    previousValueView.setText(currentValue);
                    currentValueView.setText(pressedButton);

                // If we're in the initial state, we simply have to replace "0"
                // with the value of whichever button was pressed
                } else if (currentValue.equals("0") && !pressedButton.equals("0")) {
                    currentValueView.setText(pressedButton);

                // In all other cases, we have to append the pressed button's
                // value to the current number that's being typed
                } else {
                    currentValueView.setText(currentValue.concat(pressedButton));
                }
                break;

            // Delete function
            case "d":
                // In the result state, we don't want to allow editing of the number
                // so we simply reset it to the initial state
                if (isInResultState) {
                    currentValueView.setText("0");

                // If there's a previous number, an operation, and the current number
                // is 0 we want to delete the operation, and bring back the previous
                // number as the current one
                } else if (!previousValue.isEmpty() && !operation.isEmpty()
                        && currentValue.equals("0")) {
                    currentValueView.setText(previousValue);
                    previousValueView.setText("");
                    operationView.setText("");

                // If an operation was just chosen, that's what we need to delete
                } else if (!operation.isEmpty() && previousValue.isEmpty()) {
                    operationView.setText("");

                // Finally, simply remove the last digit, or set the number to 0 if it only has one
                } else if (currentValue.length() > 1) {
                    currentValueView.setText(currentValue.substring(0, currentValue.length() - 1));
                } else {
                    currentValueView.setText("0");
                }
                break;

            // Clear function
            case "c":
                // Resets the calculator to the initial state
                currentValueView.setText("0");
                previousValueView.setText("");
                operationView.setText("");
                isInResultState = false;
                break;

            // Decimal point input function
            case ".":
                // Only add the decimal if we're not in the result
                // state and the number doesn't already contain one
                if (!currentValue.contains(".") && !isInResultState) {

                    // If an operation was just selected, and . was directly pressed
                    // we assume the user wants to input 0.something
                    if (!operation.isEmpty() && previousValue.isEmpty()) {
                        currentValueView.setText("0.");

                    // Otherwise just insert . at the current position
                    } else {
                        currentValueView.setText(currentValue.concat("."));
                    }
                }
                break;

            // Operation functions
            case "+":
            case "-":
            case "*":
            case "/":
                // Unless we're in the error state, set the chosen operation
                if (!currentValue.equals("ERR")) {
                    operationView.setText(pressedButton);
                }
                break;

            // Calculate result function
            case "=":
                // If there's a previous value and a chosen operation, do the math
                if (!previousValue.isEmpty() && !operation.isEmpty()) {

                    // Calculate and clean the result
                    String result = calculate(previousValue, currentValue, operation);
                    result = cleanNumber(result);

                    // Remove the previous value, the chosen operation,
                    // display the result, and set the result state flag
                    previousValueView.setText("");
                    operationView.setText("");
                    currentValueView.setText(result);
                    this.isInResultState = true;
                }
                break;

            // Memory plus function
            case "mp":
                // Add the current value to the memory
                memoryView.setText("M");
                temporaryValue = memory == null ? "0" : memory;
                memory = calculate(temporaryValue, currentValue, "+");
                break;

            // Memory minus function
            case "mm":
                // Subtract the current value from the memory
                memoryView.setText("M");
                temporaryValue = memory == null ? "0" : memory;
                memory = calculate(temporaryValue, currentValue, "-");
                break;

            // Memory clear function
            case "mc":
                memoryView.setText("");
                memory = null;
                break;

            // Memory recall function
            case "mr":
                if (memory != null) {

                    // If an operation was just chosen, put the current value as the previous value
                    if (!operation.isEmpty() && previousValue.isEmpty()) {
                        previousValueView.setText(currentValue);
                    }

                    currentValueView.setText(memory);
                    if (isInResultState) {
                        isInResultState = false;
                    }
                }
                break;

            // Square root function
            case "s":
                currentValueView.setText(sqrt(currentValue));
                isInResultState = true;
                break;
        }
    }

    // Takes string representations of two decimal numbers, an operation, calculates the
    // result through the use of the BigDecimal class, and returns its string representation
    private String calculate(String firstOperand, String secondOperand, String operation) {
        DecimalFormat parser = new DecimalFormat();
        parser.setParseBigDecimal(true);
        String result = null;

        try {
            BigDecimal op1 = (BigDecimal) parser.parse(firstOperand);
            BigDecimal op2 = (BigDecimal) parser.parse(secondOperand);

            switch (operation) {
                case "+":
                    result = op1.add(op2).toString();
                    break;
                case "-":
                    result = op1.subtract(op2).toString();
                    break;
                case "*":
                    result = op1.multiply(op2).toString();
                    break;
                case "/":
                    // Check division by zero
                    if (op2.equals(BigDecimal.ZERO)) {
                        result = "ERR";
                    } else {
                        result = op1.divide(op2, 16, RoundingMode.HALF_DOWN).toString();
                    }
                    break;
            }
        } catch (ParseException e) {
            // should never happen
        }

        return result;
    }

    // Parse a string representation of a decimal number, calculate
    // the square root, and return its string representation
    private String sqrt(String stringValue) {
        DecimalFormat parser = new DecimalFormat();
        parser.setParseBigDecimal(true);
        String result = null;

        try {
            BigDecimal value = (BigDecimal) parser.parse(stringValue);

            // Negative numbers should give an error
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                result = "ERR";
            }

            result = cleanNumber(sqrtAlgorithm(value, 16).toString());
        } catch (ParseException e) {
            // should never happen
        }

        return result;
    }

    // Algorithm to calculate the square root of a BigDecimal
    private BigDecimal sqrtAlgorithm(BigDecimal A, final int SCALE) {
        BigDecimal x0 = new BigDecimal("0");
        BigDecimal x1 = new BigDecimal(Math.sqrt(A.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = A.divide(x0, SCALE, BigDecimal.ROUND_HALF_UP);
            x1 = x1.add(x0);
            x1 = x1.divide(BigDecimal.valueOf(2), SCALE, BigDecimal.ROUND_HALF_UP);

        }
        return x1;
    }

    // Removes trailing decimal zeroes on fractional numbers
    private String cleanNumber(String result) {
        if (result.matches("[0-9]+.[1-9]*0+")) {
            result = result.replaceAll("0+$", "");
            if (result.endsWith(".")) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

}
