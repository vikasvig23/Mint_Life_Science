package com.example.mintlifesciences.medicinePresentation

import android.app.DatePickerDialog
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.FragmentFeedbackBinding
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FeedbackFragment : Fragment() {

    private lateinit var binding: FragmentFeedbackBinding
    private val viewModel: FeedbackViewModel by viewModels()
    private lateinit var doctorRef: DatabaseReference

    // Variables to hold fragment arguments
    private var doctorFeedback: String? = null
    private var scheduleMeetDate: String? = null
    private val activityViewModel: MedicineScreenViewModel by activityViewModels()


    companion object {
        // Factory method to create a new instance of this fragment with arguments
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
    ): View{
        binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val bounds = Rect()
                binding.fragmentContainer.getGlobalVisibleRect(bounds)
                if (!bounds.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    // Dismiss the fragment
                    parentFragmentManager.popBackStack()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Pre-fill the feedback and date fields
        binding.response.setText(doctorFeedback)
        scheduleMeetDate?.let { date ->
            val parts = date.split("-")
            binding.dd.text = parts[0]
            binding.mm.text = parts[1]
            binding.yy.text = parts[2]
        }

        // Set up date picker
        binding.date.setOnClickListener {
            openDatePicker()
        }

        // Set up submit button click listener
        binding.submitButton.setOnClickListener {
            onSubmitClicked()
        }

        // Observe the selected date
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

    private fun onSubmitClicked() {
        val feedbackText = binding.response.text.toString()

        if (feedbackText.isBlank()) {
            Toast.makeText(requireContext(), "Feedback cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Use selectedDate from ViewModel if set, otherwise fallback to scheduleMeetDate
        val selectedDate = viewModel.selectedDate.value ?: scheduleMeetDate

        if (selectedDate == null) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = Calendar.getInstance()
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)

        val selectedDateParsed = parseDate(selectedDate)

        if (selectedDateParsed != null) {
            // Validate that the selected date is today or in the future
            if (selectedDateParsed >= currentDate.time) {
                updateDoctorData(selectedDate, feedbackText)
                Toast.makeText(requireContext(), "Thank you for your feedback", Toast.LENGTH_SHORT).show()
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



    private fun updateDoctorData(selectedDate: String, feedbackText: String) {
        activityViewModel.updateDoctorData(selectedDate, feedbackText)
        //Toast.makeText(requireContext(), "Feedback and schedule updated successfully", Toast.LENGTH_SHORT).show()

        requireActivity().supportFragmentManager.popBackStack()
    }

}
