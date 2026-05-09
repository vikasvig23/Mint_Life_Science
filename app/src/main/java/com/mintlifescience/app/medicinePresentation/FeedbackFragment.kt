package com.mintlifescience.app.medicinePresentation

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.mintlifescience.app.databinding.FragmentFeedbackBinding
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FeedbackFragment : Fragment() {
    private lateinit var binding: FragmentFeedbackBinding
    private val viewModel: FeedbackViewModel by viewModels()
    private lateinit var doctorRef: DatabaseReference

    private var doctorFeedback: String? = null
    private var scheduleMeetDate: String? = null
    private val activityViewModel: MedicineScreenViewModel by activityViewModels()

    companion object {
        fun newInstance(feedback: String, date: String): FeedbackFragment {
            val fragment = FeedbackFragment()
            val args = Bundle().apply {
                putString("doctorFeedback", feedback)
                putString("scheduleMeetDate", date)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            doctorFeedback = it.getString("doctorFeedback")
            scheduleMeetDate = it.getString("scheduleMeetDate")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.response.setText(doctorFeedback)
        scheduleMeetDate?.let { date ->
            val parts = date.split("-")
            binding.dd.text = parts[0]
            binding.mm.text = parts[1]
            binding.yy.text = parts[2]
        }

        binding.date.setOnClickListener { openDatePicker() }
        binding.submitButton.setOnClickListener { onSubmitClicked() }

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            val parts = date.split("-")
            binding.dd.text = parts[0]
            binding.mm.text = parts[1]
            binding.yy.text = parts[2]
        }
        return binding.root
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear
                )
                viewModel.setSelectedDate(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun onSubmitClicked() {
        val feedbackText = binding.response.text.toString()

        if (feedbackText.isBlank()) {
            Toast.makeText(requireContext(), "Feedback cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedDate = viewModel.selectedDate.value ?: scheduleMeetDate
        if (selectedDate == null) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = Calendar.getInstance().time
        val selectedDateParsed = parseDate(selectedDate)

        if (selectedDateParsed != null) {
            if (selectedDateParsed >= currentDate) {
                activityViewModel.updateDoctorData(selectedDate, feedbackText)
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Selected date must be today or in the future", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Invalid selected date", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}
