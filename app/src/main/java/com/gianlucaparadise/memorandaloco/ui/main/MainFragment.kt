package com.gianlucaparadise.memorandaloco.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.gianlucaparadise.memorandaloco.R
import com.gianlucaparadise.memorandaloco.databinding.MainFragmentBinding
import com.gianlucaparadise.memorandaloco.exception.PermissionsNotGrantedException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.main_fragment.*
import java.lang.Exception

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.geofenceError.observe(viewLifecycleOwner, Observer { errorDescriptor ->
            if (errorDescriptor == null) return@Observer

            txt_message.text = when (errorDescriptor.type) {
                MainViewModel.ErrorType.None -> view.context.getString(R.string.geofence_ok)
                MainViewModel.ErrorType.GeofenceNotAvailable -> view.context.getString(R.string.error_geofence_not_available)
                MainViewModel.ErrorType.GeofenceTooManyGeofences -> view.context.getString(R.string.error_too_many_geofences)
                MainViewModel.ErrorType.GenericApiError -> view.context.getString(
                    R.string.error_geofences_generic_with_code,
                    errorDescriptor.code
                )
                MainViewModel.ErrorType.PermissionsNotGranted -> view.context.getString(R.string.error_permissions_not_granted)
                MainViewModel.ErrorType.MissingHome -> view.context.getString(R.string.error_missing_home)
                MainViewModel.ErrorType.GenericError -> view.context.getString(R.string.error_geofences_generic)
                MainViewModel.ErrorType.InvalidLocationError -> view.context.getString(R.string.error_location_invalid)
                MainViewModel.ErrorType.GenericLocationError -> view.context.getString(R.string.error_location_generic)
            }

            btn_requestLocation.isVisible =
                errorDescriptor.type == MainViewModel.ErrorType.MissingHome
        })

        viewModel.addGeofence() // This will also ask for permissions
    }
}