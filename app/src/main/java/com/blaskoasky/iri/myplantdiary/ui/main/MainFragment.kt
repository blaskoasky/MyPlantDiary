package com.blaskoasky.iri.myplantdiary.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blaskoasky.iri.myplantdiary.R
import com.blaskoasky.iri.myplantdiary.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var mainFragmentBinding: MainFragmentBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainFragmentBinding = MainFragmentBinding.inflate(inflater, container, false)
        return mainFragmentBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // LiveData Observer
        viewModel.plants.observe(viewLifecycleOwner, { plants ->
            // adding autocomplete
            mainFragmentBinding.actPlantName.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    plants
                )
            )
        })
    }

}