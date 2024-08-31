package com.example.mintlifesciences.recentDoctors

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.AddDoctorViewModel
import com.example.mintlifesciences.databinding.ActivityRecentDoctorsBinding
import com.example.mintlifesciences.databinding.FragmentRecentDoctorBinding


class RecentDoctorFragment : Fragment() {
    private lateinit var _binding: FragmentRecentDoctorBinding
    val binding get() = _binding!!
    private lateinit var selectedItem: String
    private lateinit var viewModel: AddDoctorViewModel
    private lateinit var adapter:RecentDoctorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recent_doctor, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecentDoctorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecentDoctorFragment().apply {
                arguments = Bundle().apply {


                }
            }
    }
}