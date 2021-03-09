package com.gianlucaparadise.memoloco.ui.main

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.gianlucaparadise.memoloco.R
import com.gianlucaparadise.memoloco.databinding.MainFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.main_fragment.*

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by navGraphViewModels(R.id.nav_graph) {
        defaultViewModelProviderFactory
    }

    private lateinit var locationPermissionsDialog: AlertDialog

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

        locationPermissionsDialog = with(AlertDialog.Builder(context)) {
            setTitle(R.string.dialog_location_permissions_title)
            setMessage(R.string.dialog_location_permissions_message)
            setPositiveButton(R.string.dialog_location_permissions_button_positive) { _, _ -> viewModel.onLocationPermissionsDialogConfirm() }
            setNegativeButton(R.string.dialog_location_permissions_button_negative) { _, _ -> viewModel.onLocationPermissionsDialogCancel() }
            create()
        }

        with(viewModel) {
            message.observe(viewLifecycleOwner, Observer { errorDescriptor ->
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
                    MainViewModel.MessageType.BackgroundPermissionsNotGranted -> view.context.getString(
                        R.string.error_background_permissions_not_granted
                    )
                    MainViewModel.MessageType.TwoStepsPermissionRequestNeeded -> view.context.getString(
                        R.string.error_two_steps__permission_request_needed
                    )
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

                btn_requestPermissions.isVisible = when (errorDescriptor.type) {
                    MainViewModel.MessageType.PermissionsNotGranted, MainViewModel.MessageType.BackgroundPermissionsNotGranted, MainViewModel.MessageType.TwoStepsPermissionRequestNeeded -> true
                    else -> false
                }

                btn_requestLocation.isVisible = when (errorDescriptor.type) {
                    MainViewModel.MessageType.MissingHome, MainViewModel.MessageType.InvalidLocationError -> true
                    else -> false
                }

                group_choose_app.isVisible =
                    errorDescriptor.type == MainViewModel.MessageType.MissingAppToOpen
                viewModel.preselectApp(application_list.installedApps)

                val isOk = errorDescriptor.type == MainViewModel.MessageType.Ok
                btn_checkHome.isVisible = isOk
                btn_updateWithCurrentLocation.isVisible = isOk
                btn_removeReminder.isVisible = isOk
            })

            selectedApp.observe(viewLifecycleOwner, Observer {
                Log.d("fragment", "onViewCreated: Selected app: $it")
            })

            isLocationPermissionsDialogVisible.observe(viewLifecycleOwner, Observer { isVisible ->
                Log.d("fragment", "isLocationPermissionsDialogVisibleChanged: $isVisible")
                if (isVisible) locationPermissionsDialog.show()
                else locationPermissionsDialog.dismiss()
            })

            addGeofence() // This will also ask for permissions
        }
    }
}