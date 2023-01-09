package com.example.money_manager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.money_manager.adapters.SectionsPagerAdapter
import com.example.money_manager.adapters.TransactionsAdapter
import com.example.money_manager.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

val DEFAULT_CAT_LIST = listOf("General", "Food", "Entertainment", "Sports", "Public Transport",
    "Home", "Work", "Health", "Clothing", "Family", "Services", "Other", "Vacation")

class MainActivity : AppCompatActivity(), TransactionsAdapter.OnItemClickListener {

    companion object {
        const val REQUEST_ADD = 20
        const val REQUEST_EDIT = 21
        const val REQ_ADD_OK = 21
        const val REQ_EDIT_OK = 22
        const val EXTRA_TRANSACTION = "extra_id"
        const val EXTRA_CODE = "request_code"
    }



    private lateinit var sectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        sectionsPagerAdapter =
            SectionsPagerAdapter(this, supportFragmentManager)

        binding.viewPager.adapter = sectionsPagerAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)


       binding.fab.setOnClickListener { view ->

            startAddActivity()

        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_ADD) {

            if(resultCode == REQ_ADD_OK) {
                notifyFragmentsToUpdate()
            }
        } else if (requestCode == REQUEST_EDIT) {
            if(resultCode == REQ_EDIT_OK) {
                notifyFragmentsToUpdate()
            }
        }
    }

    override fun onClick(id: Int) {
        val intent = Intent(this, TransactionActivity::class.java)
        intent.putExtra(EXTRA_CODE, REQUEST_EDIT)
        intent.putExtra(EXTRA_TRANSACTION, id)
        startActivityForResult(intent, REQUEST_EDIT)
    }

    private fun startAddActivity() {
        Log.e("!@#", "Some text, var, position")
        val intent = Intent(this, TransactionActivity::class.java)
        intent.putExtra(EXTRA_CODE, REQUEST_ADD)
        startActivityForResult(intent, REQUEST_ADD)
    }


    private fun getActiveFragments(): SparseArray<Fragment> {
        return sectionsPagerAdapter.getRegisteredFragments()
    }

    fun notifyFragmentsToUpdate() {
        val fragments = getActiveFragments()
        for(i in 0 until fragments.size()) {
            (fragments.get(i) as PageFragment).notifyDataUpdate()
        }
    }
}