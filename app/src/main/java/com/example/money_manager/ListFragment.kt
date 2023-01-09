package com.example.money_manager


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.money_manager.adapters.TransactionsAdapter
import com.example.money_manager.data.DBOpenHelper
import com.example.money_manager.databinding.FragmentListBinding

//import kotlinx.android.synthetic.main.fragment_list.*

/**
 * A simple [Fragment] subclass.
 */
class ListFragment : PageFragment() {

    private lateinit var binding: FragmentListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = DBOpenHelper(requireActivity().applicationContext)
        val cursor = db.getAllTransactions(true)

        if(cursor != null) {
            binding.transactionList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = TransactionsAdapter(cursor, activity as TransactionsAdapter.OnItemClickListener)
            }
        }
    }

    override fun notifyDataUpdate() {
        val db = DBOpenHelper(requireActivity().applicationContext)
        val cursor = db.getAllTransactions(true)

        if(cursor != null) {
            val recyclerViewAdapter = binding.transactionList.adapter as TransactionsAdapter
            recyclerViewAdapter.swapCursor(cursor)
        }
        binding.transactionList.adapter?.notifyDataSetChanged()
    }

}
