package com.gianlucaparadise.memorandaloco.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.gianlucaparadise.memorandaloco.R
import com.gianlucaparadise.memorandaloco.databinding.MainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.main_fragment.*

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

        viewModel.message.observe(viewLifecycleOwner, Observer { errorDescriptor ->
            if (errorDescriptor == null) return@Observer

            txt_message.text = when (errorDescriptor.type) {
                MainViewModel.MessageType.Idle -> "Idle"
                MainViewModel.MessageType.Ok -> view.context.getString(R.string.geofence_ok)
                MainViewModel.MessageType.GeofenceNotAvailable -> view.context.getString(R.string.error_geofence_not_available)
                MainViewModel.MessageType.GeofenceTooManyGeofences -> view.context.getString(R.string.error_too_many_geofences)
                MainViewModel.MessageType.GenericApiError -> view.context.getString(
                    R.string.error_geofences_generic_with_code,
                    errorDescriptor.code
                )
                MainViewModel.MessageType.PermissionsNotGranted -> view.context.getString(R.string.error_permissions_not_granted)
                MainViewModel.MessageType.MissingHome -> view.context.getString(R.string.error_missing_home)
                MainViewModel.MessageType.GenericAddGeofenceError -> view.context.getString(R.string.error_add_geofences_generic)
                MainViewModel.MessageType.GenericRemoveGeofenceError -> view.context.getString(R.string.error_remove_geofences_generic)
                MainViewModel.MessageType.InvalidLocationError -> view.context.getString(R.string.error_location_invalid)
                MainViewModel.MessageType.GenericLocationError -> view.context.getString(R.string.error_location_generic)
                MainViewModel.MessageType.MissingAppToOpen -> view.context.getString(R.string.error_missing_app_to_open)
            }

            txt_message.textAlignment = when (errorDescriptor.type) {
                MainViewModel.MessageType.GeofenceNotAvailable, MainViewModel.MessageType.InvalidLocationError -> View.TEXT_ALIGNMENT_TEXT_START
                else -> View.TEXT_ALIGNMENT_CENTER
            }

            btn_requestPermissions.isVisible =
                errorDescriptor.type == MainViewModel.MessageType.PermissionsNotGranted

            btn_requestLocation.isVisible = when (errorDescriptor.type) {
                MainViewModel.MessageType.MissingHome, MainViewModel.MessageType.InvalidLocationError -> true
                else -> false
            }

            group_choose_app.isVisible =
                errorDescriptor.type == MainViewModel.MessageType.MissingAppToOpen

            val isOk = errorDescriptor.type == MainViewModel.MessageType.Ok
            btn_checkHome.isVisible = isOk
            btn_updateWithCurrentLocation.isVisible = isOk
            btn_removeReminder.isVisible = isOk
        })

        viewModel.selectedApp.observe(viewLifecycleOwner, Observer {
            Log.d("fragment", "onViewCreated: Selected app: $it")
        })

        viewModel.addGeofence() // This will also ask for permissions
    }
}