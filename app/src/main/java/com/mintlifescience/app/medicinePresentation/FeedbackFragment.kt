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

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear
                )
                viewModel.setSelectedDate(formattedDate)
            },
            year,
            month,
            day
        ).show()
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

        val currentDate = Calendar.getInstance().time
        val selectedDateParsed = parseDate(selectedDate)

        if (selectedDateParsed != null) {
            if (selectedDateParsed >= currentDate) {
                updateDoctorData(selectedDate, feedbackText)
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





















//import android.app.DatePickerDialog
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.MotionEvent
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.activityViewModels
//import androidx.fragment.app.viewModels
//import com.example.mintlifesciences.databinding.FragmentFeedbackBinding
//import com.google.firebase.database.DatabaseReference
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//
//class FeedbackFragment : androidx.fragment.app.DialogFragment() {
//
//    private lateinit var binding: FragmentFeedbackBinding
//    private val viewModel: FeedbackViewModel by viewModels()
//    private lateinit var doctorRef: DatabaseReference
//
//    // Variables to hold fragment arguments
//    private var doctorFeedback: String? = null
//    private var scheduleMeetDate: String? = null
//    private val activityViewModel: MedicineScreenViewModel by activityViewModels()
//
//    companion object {
//        // Factory method to create a new instance of this fragment with arguments
//        fun newInstance(feedback: String, date: String): FeedbackFragment {
//            val fragment = FeedbackFragment()
//            val args = Bundle().apply {
//                putString("doctorFeedback", feedback)
//                putString("scheduleMeetDate", date)
//            }
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            doctorFeedback = it.getString("doctorFeedback")
//            scheduleMeetDate = it.getString("scheduleMeetDate")
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentFeedbackBinding.inflate(inflater, container, false)
//        binding.lifecycleOwner = viewLifecycleOwner
//        binding.viewModel = viewModel
//
//        // Pre-fill the feedback and date fields
//        binding.response.setText(doctorFeedback)
//        scheduleMeetDate?.let { date ->
//            val parts = date.split("-")
//            binding.dd.text = parts[0]
//            binding.mm.text = parts[1]
//            binding.yy.text = parts[2]
//        }
//
//        // Set up date picker
//        binding.date.setOnClickListener {
//            openDatePicker()
//        }
//
//        // Set up submit button click listener
//        binding.submitButton.setOnClickListener {
//            onSubmitClicked()
//        }
//
//        // Observe the selected date
//        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
//            val parts = date.split("-")
//            binding.dd.text = parts[0]
//            binding.mm.text = parts[1]
//            binding.yy.text = parts[2]
//        }
//
//        return binding.root
//    }
//
//    private fun openDatePicker() {
//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        DatePickerDialog(
//            requireContext(),
//            { _, selectedYear, selectedMonth, selectedDay ->
//                val formattedDate = String.format(
//                    "%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear
//                )
//                viewModel.setSelectedDate(formattedDate)
//            },
//            year,
//            month,
//            day
//        ).show()
//    }
//
//    private fun onSubmitClicked() {
//        val feedbackText = binding.response.text.toString()
//
//        // Check if feedback is empty
//        if (feedbackText.isBlank()) {
//            Toast.makeText(requireContext(), "Feedback cannot be empty", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Use selectedDate from ViewModel if set, otherwise fallback to scheduleMeetDate
//        val selectedDate = viewModel.selectedDate.value ?: scheduleMeetDate
//
//        // If no date is selected or provided, show an error
//        if (selectedDate == null) {
//            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Parse the date
//        val currentDate = Calendar.getInstance().time
//        val selectedDateParsed = parseDate(selectedDate)
//
//        if (selectedDateParsed != null) {
//            // Validate that the selected date is today or in the future
//            if (selectedDateParsed >= currentDate) {
//                updateDoctorData(selectedDate, feedbackText)
//            } else {
//                Toast.makeText(requireContext(), "Selected date must be today or in the future", Toast.LENGTH_SHORT).show()
//            }
//        } else {
//            Toast.makeText(requireContext(), "Invalid selected date", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun parseDate(dateString: String): Date? {
//        return try {
//            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateString)
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    private fun updateDoctorData(selectedDate: String, feedbackText: String) {
//        activityViewModel.updateDoctorData(selectedDate, feedbackText)
//        Toast.makeText(requireContext(), "Feedback and schedule updated successfully", Toast.LENGTH_SHORT).show()
//
//        dismiss() // Close the DialogFragment
//    }
//
//    // Optional: To disable the dismiss on outside click (if you want to keep it)
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_OUTSIDE) {
//            // Optionally you can add custom behavior here if needed
//            return true // To prevent dismiss, or return false to allow dismiss
//        }
//        return super.onTouchEvent(event)
//    }
//}
//

