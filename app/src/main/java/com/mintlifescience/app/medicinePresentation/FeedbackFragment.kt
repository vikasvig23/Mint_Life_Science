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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FeedbackFragment : Fragment() {

    private lateinit var binding: FragmentFeedbackBinding
    private val viewModel: FeedbackViewModel by viewModels()
    private val activityViewModel: MedicineScreenViewModel by activityViewModels()

    private var doctorFeedback: String? = null
    private var scheduleMeetDate: String? = null

    // Formatter for storing/parsing dates (firebase format)
    private val storageFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    // Formatter for displaying dates to the user
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    companion object {
        fun newInstance(feedback: String, date: String): FeedbackFragment {
            val fragment = FeedbackFragment()
            fragment.arguments = Bundle().apply {
                putString("doctorFeedback", feedback)
                putString("scheduleMeetDate", date)
            }
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

        // Pre-fill existing feedback text
        if (!doctorFeedback.isNullOrBlank()) {
            binding.response.setText(doctorFeedback)
        }

        // Pre-fill existing scheduled date (display formatted)
        scheduleMeetDate?.takeIf { it.isNotBlank() }?.let { storedDate ->
            binding.date.setText(toDisplayDate(storedDate))
        }

        // Open date picker when tapping the field or the end icon
        val openPicker = View.OnClickListener { openDatePicker() }
        binding.date.setOnClickListener(openPicker)
        binding.dateInputLayout.setEndIconOnClickListener(openPicker)

        // Close / Cancel — both just dismiss without saving
        binding.closeButton.setOnClickListener { dismiss() }
        binding.cancelButton.setOnClickListener { dismiss() }

        // Save Notes
        binding.submitButton.setOnClickListener { onSubmitClicked() }

        // Reflect date updates from the ViewModel (after user picks in DatePickerDialog)
        viewModel.selectedDate.observe(viewLifecycleOwner) { storedDate ->
            binding.date.setText(toDisplayDate(storedDate))
        }

        return binding.root
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()

        // If there is already a date set, open the picker pre-selected on that date
        val existingDate = viewModel.selectedDate.value ?: scheduleMeetDate
        if (!existingDate.isNullOrBlank()) {
            storageFormat.parse(existingDate)?.let { parsed ->
                calendar.time = parsed
            }
        }

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formatted = String.format(
                    Locale.getDefault(),
                    "%02d-%02d-%04d",
                    selectedDay, selectedMonth + 1, selectedYear
                )
                viewModel.setSelectedDate(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Only allow today or future dates — no point scheduling a past meeting
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    private fun onSubmitClicked() {
        val feedbackText = binding.response.text?.toString()?.trim() ?: ""

        if (feedbackText.isBlank()) {
            binding.responseLayout.error = "Please add some notes before saving"
            return
        } else {
            binding.responseLayout.error = null
        }

        val selectedDate = viewModel.selectedDate.value ?: scheduleMeetDate
        if (selectedDate.isNullOrBlank()) {
            binding.dateInputLayout.error = "Please select the next meeting date"
            return
        } else {
            binding.dateInputLayout.error = null
        }

        val selectedDateParsed = parseDate(selectedDate)
        if (selectedDateParsed == null) {
            binding.dateInputLayout.error = "Invalid date — please select again"
            return
        }

        // Allow today or future dates only
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        if (selectedDateParsed.before(startOfToday)) {
            binding.dateInputLayout.error = "Next meeting must be today or a future date"
            return
        }

        activityViewModel.updateDoctorData(selectedDate, feedbackText)
        // Activity observes downloadStatus and calls finish() — fragment will be removed automatically
    }

    /** "dd-MM-yyyy"  →  "15 Jan 2025" */
    private fun toDisplayDate(stored: String): String {
        return try {
            storageFormat.parse(stored)?.let { displayFormat.format(it) } ?: stored
        } catch (e: Exception) {
            stored
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            storageFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    private fun dismiss() {
        requireActivity().supportFragmentManager.popBackStack()
    }
}
