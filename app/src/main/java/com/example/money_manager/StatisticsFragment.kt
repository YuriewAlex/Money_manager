package com.example.money_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.money_manager.data.DBOpenHelper
import com.example.money_manager.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.math.abs



class StatisticsFragment : PageFragment() {

    private var balance: Float = 0f
    private var allIncomes: Float = 0f
    private var allExpenses: Float = 0f
    private var expenseMap = HashMap<String, Float>()
    private var incomeMap = HashMap<String, Float>()
    private lateinit var binding: FragmentStatisticsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchData()
        updateUI()
    }

    fun fetchData() {
        val db = DBOpenHelper(requireActivity().applicationContext)
        balance = db.getBalance()
        expenseMap = db.getValueByCategory(true)
        incomeMap = db.getValueByCategory(false)
        allIncomes = db.getOperations(false)
        allExpenses = abs(db.getOperations(true))

    }

    fun updateUI() {
        binding.tvBalance.text = balance.toString()
        binding.tvExpenses.text = allExpenses.toString()
        binding.tvIncomes.text = allIncomes.toString()
        setupChart(expenseMap, binding.expensesPiechart, resources.getString(R.string.expenses_text))
        setupChart(incomeMap, binding.incomesPiechart, resources.getString(R.string.incomes_text))
    }

    override fun notifyDataUpdate() {
        fetchData()
        updateUI()
    }

    private fun setupChart(map: HashMap<String, Float>, chart: PieChart,
                           description: String) {


        val list = ArrayList<PieEntry>()
        if(map.size > 0) {
            for((key, value) in map){
                list.add(PieEntry(value, key))
            }

            val desc = Description()
            desc.text = description
            desc.textSize = 20f
            chart.description = desc

            val dataSet = PieDataSet(list, "")

            val data = PieData(dataSet)
            chart.data = data
            dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
            chart.notifyDataSetChanged()
        }
    }
}
