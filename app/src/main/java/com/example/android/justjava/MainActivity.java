package com.example.android.justjava;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * This app displays an order form to order coffee.
 */
public class MainActivity extends ActionBarActivity {
    // Global variables
    // Only one stored preference, so no pref_file name needed
    String customerName = "N/A";
    int coffeeNum = 1;
    int coffeePrice = 5;
    double optionsPrice = 0;
    Map<Integer, Double> optionList = new HashMap(); // Options are listed in onCreate()
    List<CheckBox> optionsOrdered = new ArrayList<CheckBox>();
    double totalPrice = coffeeNum * coffeePrice;

    // Order number
    String orderNum_prefix = "0000";
    int orderNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkDate();

        // Set list of options and prices
        optionList.put(R.id.whippedCream_checkBox, .5);
        optionList.put(R.id.milk_checkBox, .5);
        optionList.put(R.id.sugar_checkBox, .0);
        optionList.put(R.id.chocolate_checkBox, .5);
    }

    /**
     * checkDate() sets the orderNum_prefix to the current year/month and resets the orderNum every month
     */
    private void checkDate() {
        // Set current year/month for order number
        Calendar cal = Calendar.getInstance();
        orderNum_prefix = (cal.get(Calendar.YEAR) % 100) + String.format("%02d", (cal.get(Calendar.MONTH)+1));

        // Restore preference: order number
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        String storedPrefix = settings.getString("orderNum_prefix", "0000");

        // Reset orderNum if new month
        if (!orderNum_prefix.equals(storedPrefix)) {
            editor.putString("orderNum_prefix", orderNum_prefix);
            editor.putInt("orderNum", 1);
            orderNum = 1;
            Log.v("MainActivity", "Order Number Reset #" + orderNum_prefix + "-" + orderNum);
        }
        else {
            orderNum = settings.getInt("orderNum", orderNum);
        }
        editor.apply();
    }

    /**
     * This method is called when the order button is clicked.
     */
    public void submitOrder(View view) {
        // Update customer name
        TextView name = (TextView) findViewById(R.id.name_view);
        customerName = name.getText().toString();

        String summary = createOrderSummary();
        displayMessage(summary);
//        emailOrder(summary);

        Log.i("MainActivity.java", summary);

        orderNum++;
        // Update preference
        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("orderNum", orderNum);
        editor.commit();

        resetOrder();
        updateAll(true);
    }

    /**
     * emailOrder() prepares and email with the order summary
     */
    private void emailOrder(String summary) {
        Log.v("MainActivity", "Email order #" + orderNum + " by " + customerName);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Handled w/ email app
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " | " +
                                        getString(R.string.order_num) + orderNum);
        intent.putExtra(Intent.EXTRA_TEXT, summary + "\n\nThank you " + customerName + "!");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * This method displays the given quantity value on the screen.
     */
    private void display(int number) {
        TextView quantityTextView = (TextView) findViewById(
                R.id.quantity_text_view);
        quantityTextView.setText("" + number);
    }

    /**
     * This method displays the given price on the screen.
     *
     * @param price of order
     */
    private void displayPrice(double price) {
        TextView priceTextView = (TextView) findViewById(R.id.price_text_view);
        priceTextView.setText(NumberFormat.getCurrencyInstance().format(price));
    }

    /**
     * This method displays the given text on the screen.
     *
     * @param message to display
     */
    private void displayMessage(String message) {
        TextView summaryTextView = (TextView) findViewById(R.id.summary_text_view);
        summaryTextView.setText(message);
        summaryTextView.setVisibility(View.VISIBLE);
    }

    /**
     * `increment` increases coffeeNum by 1
     */
    public void increment(View view) {
        if (coffeeNum < 100) {
            coffeeNum++;
            updateAll(false);
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.error_tooMuch, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * `decrement` decreases coffeeNum by 1
     */
    public void decrement(View view) {
        if(coffeeNum > 1) {
            coffeeNum--;
            updateAll(false);
            Log.v("MainActivity", "1 coffee removed to order.");
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.error_tooLittle, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * The updateAll() method updates the quantity and price values
     */
    private void updateAll (boolean submitted) {
        totalPrice = coffeeNum * (coffeePrice + optionsPrice);

        display(coffeeNum);
        displayPrice(totalPrice);

        // Hide order summary if new order is being inputted
        if(submitted==false){
            TextView summaryTextView = (TextView) findViewById(R.id.summary_text_view);
            summaryTextView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * The createOrderSummary() outputs a message with the name, quantity, and price of the order.
     *
     * @return Text summary
     */
    private String createOrderSummary () {
        Toast.makeText(getApplicationContext(), "Thank you!", Toast.LENGTH_SHORT).show();

        String orderName = "Name: " + customerName;
        String orderNumber = "\n#" + orderNum_prefix + "-" + orderNum;
        String orderQuantity = "\nQuantity: " + coffeeNum;
        String orderOptions = "\nOptions: " + listOptionsOrdered();
        String orderPrice = "\nTotal: " + NumberFormat.getCurrencyInstance().format(totalPrice);
        String message = orderName + orderNumber + orderQuantity + orderOptions + orderPrice;
        return message;
    }

    /**
     * checkOptions() adds to optionsPrice depending on which one is clicked
     */
    public void checkOption(View optionView){
        CheckBox option = (CheckBox) findViewById(optionView.getId());
        if (option.isChecked()) {
            optionsOrdered.add(option);
            // Add cost to optionPrice
            optionsPrice += optionList.get(option.getId());

            Log.v("MainActivity", option.getText() + " (" + NumberFormat.getCurrencyInstance().format(optionList.get(option.getId())) +
                                    ") option added. Total option price: " + optionsPrice);
        }
        else {
            optionsOrdered.remove(optionsOrdered.indexOf(option));
            optionsPrice -= optionList.get(option.getId());

            Log.v("MainActivity", option.getText() + " option removed. Total option price: " + optionsPrice);
        }

        updateAll(false);
    }

    /**
     * listOptionsOrdered() creates a String of the options selected and adds up option prices
     */
    private String listOptionsOrdered() {
        StringBuilder sb = new StringBuilder();
        if (optionsOrdered.isEmpty()) {
            return "N/A";
        }
        // Add first option
        sb.append(optionsOrdered.get(0).getText());
        // Add the rest of the options
        if (optionsOrdered.size() > 1) {
            for (CheckBox checkBox : optionsOrdered.subList(1, optionsOrdered.size())) {
                sb.append(", " + checkBox.getText());
            }
        }
        return sb.toString();
    }

    /**
     * resetOrder() resets all order parameters
     */
    private void resetOrder(){
        EditText name = (EditText) findViewById(R.id.name_view);
        name.setText("");
        coffeeNum = 1;

        // Uncheck all options
        CheckBox option;
        for (int key : optionList.keySet()){
            option = (CheckBox) findViewById(key);
            option.setChecked(false);
        }
        optionsOrdered.clear();
        optionsPrice = 0.0;
    }
}