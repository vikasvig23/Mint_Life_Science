package com.example.mintlifesciences.medicinePresentation

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.FragmentFeedbackBinding
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FeedbackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FeedbackFragment : Fragment() {
    private lateinit var binding: FragmentFeedbackBinding
    private lateinit var viewModel: FeedbackViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(FeedbackViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner


        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val parts = date.split("-")
            binding.dd.text = parts[0]
            binding.mm.text = parts[1]
            binding.yy.text = parts[2]
        }

        binding.date.setOnClickListener {
            openDatePicker()
        }

        return binding.root
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                binding.dd.text = selectedDay.toString().padStart(2, '0')
                binding.mm.text = (selectedMonth + 1).toString().padStart(2, '0')
                binding.yy.text = selectedYear.toString()
            },
            year,
            month,
            day
        )
        datePicker.show()
    }
}