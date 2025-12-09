package com.example.runningrouteresearcher.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.runningrouteresearcher.R
import com.example.runningrouteresearcher.viewmodels.MapViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SettingsFragment : BottomSheetDialogFragment() {

    private val viewModel: MapViewModel by activityViewModels()
    private var userLocation: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            dismiss()
        }

        val distanceInput = view.findViewById<EditText>(R.id.distance_input)
        val generateButton = view.findViewById<Button>(R.id.generate_route_button)

        generateButton.setOnClickListener {
            val distance = distanceInput.text.toString().toDoubleOrNull()

            if(distance == null || distance <= 0) {
                Toast.makeText(context, "Enter a valid distance",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(userLocation == null) {
                Toast.makeText(context, "Location not available, wait",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.generateRoute(userLocation!!,distance)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                val displayHeight = resources.displayMetrics.heightPixels

                sheet.layoutParams.height = (displayHeight * 0.4).toInt()

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = true
            }
        }
    }
    fun setUserLocation(location: LatLng) {
        userLocation = location
    }
}