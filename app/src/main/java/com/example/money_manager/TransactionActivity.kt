package com.example.money_manager

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.money_manager.data.DBOpenHelper
import com.example.money_manager.data.Transaction
import com.example.money_manager.databinding.ActivityTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : AppCompatActivity() {

    private lateinit var selectedDate: Date
    private var requestCode = 0
    private var calendar = Calendar.getInstance()
    private var selectedCategoryId = 0
    private var transactionId = 0
    private lateinit var binding: ActivityTransactionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        selectedDate = Calendar.getInstance().time

        val requestIntent = intent
        requestCode = requestIntent.getIntExtra(MainActivity.EXTRA_CODE, MainActivity.REQUEST_ADD)

        binding.dateSpinner.text = convertDate(selectedDate)

        val dateListener = DatePickerDialog.OnDateSetListener { view, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            updateDate()
        }

        binding.dateSpinner.setOnClickListener {
            DatePickerDialog(this@TransactionActivity,
                dateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }


        val catSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            DEFAULT_CAT_LIST)
        catSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        with(binding.groupSpinner) {
            adapter = catSpinnerAdapter
        }

        if(requestCode == MainActivity.REQUEST_EDIT) {
            transactionId = intent.getIntExtra(MainActivity.EXTRA_TRANSACTION, MainActivity.REQUEST_EDIT)
            val db = DBOpenHelper(this)
            val transaction = db.getTransaction(transactionId)
            extractData(transaction)
        }

        binding.groupSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryId = 0
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategoryId = position
            }

        }

       binding.buttonOk.setOnClickListener {
            saveAndFinish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        if(requestCode == MainActivity.REQUEST_EDIT) {
            menuInflater.inflate(R.menu.transaction_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)

        return when(item.itemId) {
            R.id.menu_delete_item -> {
                deleteAndFinish()
                true
            } else -> {
                false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun extractData(data: Transaction) {
        binding.valueInput.setText(data.value.toString())
        binding.etTitle.setText(data.title)
        selectedCategoryId = data.category
        binding.groupSpinner.setSelection(data.category, false)
        binding.expenseSwitch.isChecked = data.isExpense
        calendar.time = data.date
        updateDate()
        binding.description.setText(data.desc)
    }

    @SuppressLint("SimpleDateFormat", "WeekBasedYear")
    private fun convertDate(date: Date): String {
        val format = SimpleDateFormat("dd-MM-YYYY")
        return format.format(date)
    }

    private fun updateDate() {
        val date = convertDate(calendar.time)
        binding.dateSpinner.text = (date)
        selectedDate = calendar.time
    }

    private fun saveAndFinish() {
        val value = binding.valueInput.text.toString().toFloat()
        val db = DBOpenHelper(this)
        var resultCode = -1
        if(requestCode == MainActivity.REQUEST_ADD) {
            db.insertTransaction(value, binding.expenseSwitch.isChecked, selectedDate, binding.etTitle.text.toString(),
                selectedCategoryId, binding.description.text.toString())
            resultCode = MainActivity.REQ_ADD_OK
        } else if (requestCode == MainActivity.REQUEST_EDIT) {
            db.updateTransaction(Transaction(transactionId, value, binding.etTitle.text.toString(),
                binding.expenseSwitch.isChecked, selectedDate, binding.description.text.toString(), selectedCategoryId))
            resultCode = MainActivity.REQ_EDIT_OK
        }

        val resultIntent = Intent()
        setResult(resultCode, resultIntent)
        finish()
    }

    private fun deleteAndFinish() {
        if(requestCode == MainActivity.REQUEST_EDIT) {
            val db = DBOpenHelper(this)
            db.deleteTransaction(transactionId)
        }
        val resultIntent = Intent()
        setResult(MainActivity.REQ_EDIT_OK, resultIntent)
        finish()
    }
}
