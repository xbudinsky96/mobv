package com.example.zadanie.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.R
import com.example.zadanie.api.apiService
import com.example.zadanie.databinding.FragmentCheckInDetailBinding
import com.example.zadanie.geofence.GeofenceBroadcastReceiver
import com.example.zadanie.model.CompanyViewModel
import com.example.zadanie.model.Element
import com.example.zadanie.model.NearbyCompanyViewModel
import com.example.zadanie.model.loggedInUser
import com.example.zadanie.utilities.getDistanceFromLatLon
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class CheckInDetailFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentCheckInDetailBinding? = null
    val binding get() = _binding!!
    private lateinit var nearbyCompanyViewModel: NearbyCompanyViewModel
    private lateinit var companyViewModel: CompanyViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var nearestCompany: Element
    private val args: CheckInDetailFragmentArgs by navArgs()
    private val SEARCHPREFIX = "https://www.google.com/maps/search/?api=1&query=" //"https://www.google.com/maps/place/@"
    private lateinit var geofencingClient: GeofencingClient
    private var mapView: MapView? = null

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckInDetailBinding.inflate(inflater, container, false)
        nearbyCompanyViewModel = ViewModelProvider(this)[NearbyCompanyViewModel::class.java]
        companyViewModel = ViewModelProvider(this)[CompanyViewModel::class.java]
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        mapView = binding.mapView
        mapView?.isVisible = false

        val specifyButton = binding.specify
        val showOnMap = binding.showonmap
        showOnMap.isEnabled = false

        specifyButton.setOnClickListener {
            val action = CheckInDetailFragmentDirections.actionCheckInDetailFragmentToCheckInFragment()
            findNavController().navigate(action)
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        getCompany()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getCompany() {
        try {
            if (args.id.toInt() != 0) {
                apiService.getCompanyByID(this, args.id)
                cancelAnimation()
            }
        } catch (_: Exception) {
            Log.i("noargs", "No argument found")
        }

        requestLocationPermission()
        val animation = binding.animationView
        animation.setOnClickListener {
            animation.playAnimation()
            Snackbar.make(requireView(), "Finding nearest company", Snackbar.LENGTH_SHORT).show()
            getLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (hasLocationPermission()) {
            fusedLocationProviderClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, object: CancellationToken()  {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Snackbar.make(requireView(), "Couldn't get location", Snackbar.LENGTH_SHORT).show()
                }
                else {
                    setData(location)
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setData(location: Location) {
        loggedInUser.lat = location.latitude
        loggedInUser.lon = location.longitude
        val lat = location.latitude
        val lon = location.longitude
        apiService.fetchNearbyCompanies(lat, lon, this, nearbyCompanyViewModel)
        nearbyCompanyViewModel.readData.observe(viewLifecycleOwner) { elements ->
            if (elements.isEmpty()) {
                pauseAnimation()
            }
            else {
                cancelAnimation()
                nearestCompany = getNearestCompany(elements, lat, lon)
                if (nearestCompany.tags.name != null && nearestCompany.tags.name != "") {
                    setCoordinates(nearestCompany.lat, nearestCompany.lon)
                    apiService.checkInCompany(nearestCompany, this)
                    setConfirmButton()
                    setDetails(nearestCompany, binding)
                }
                else {
                    binding.compName.text = "No company"
                    Snackbar.make(requireView(), "No companies found!", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setPositionOnMap(lat: Double, lon: Double) {
        println("LATLON: " + lat + ", " + lon)
        mapView!!.isVisible = true
        val cameraPosition = CameraOptions.Builder()
            .zoom(15.0)
            .center(Point.fromLngLat(lon, lat - 0.0010000))
            .build()
        mapView?.getMapboxMap()?.setCamera(cameraPosition)
        mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS
        ) { addAnnotationToMap(lat, lon) }
    }

    private fun addAnnotationToMap(lat: Double, lon: Double) {
        bitmapFromDrawableRes(
            this.requireContext(),
            R.drawable.ic_baseline_where_to_vote_24
        )?.let {
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager()
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(lon, lat))
                .withIconImage(it)
            pointAnnotationManager?.create(pointAnnotationOptions)
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    fun createFence(lat: Double, lon: Double) {
        if (!hasLocationPermission()) {
            Snackbar.make(requireView(), "Geofence failed, permissions not granted.", Snackbar.LENGTH_SHORT).show()
            return
        }
        val geofenceIntent = PendingIntent.getBroadcast(
            requireContext(), 0,
            Intent(requireContext(), GeofenceBroadcastReceiver::class.java),
            FLAG_UPDATE_CURRENT or FLAG_MUTABLE
        )

        val request = GeofencingRequest.Builder().apply {
            addGeofence(
                Geofence.Builder()
                    .setRequestId("geofence " + loggedInUser.uid)
                    .setCircularRegion(lat, lon, 300F)
                    .setExpirationDuration(1000L * 60 * 60 * 24)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            )
        }.build()

        geofencingClient.addGeofences(request, geofenceIntent).run {
            addOnSuccessListener {
                Snackbar.make(requireView(), "Geofence created.", Snackbar.LENGTH_SHORT).show()
            }
            addOnFailureListener {
                it.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setDetails(foundCompany: Element, binding: FragmentCheckInDetailBinding) {
        val companyWithMembers = companyViewModel.getCompanyById(foundCompany.id.toString())
        val users = if (companyWithMembers != null) "Users checked in: " + companyWithMembers.users else "Users checked in: 0"
        val openingHours =
            if (foundCompany.tags.opening_hours != null && foundCompany.tags.opening_hours != "")
                "Opening hours:" + "\n" + foundCompany.tags.opening_hours
                    .replace(", ", "\n")
                    .replace("; ", "\n")
            else "Opening hours not provided"

        val tel = if (foundCompany.tags.phone != null && foundCompany.tags.phone != "") "TEL: " + foundCompany.tags.phone else "TEL: Not provided"
        val web = if (foundCompany.tags.website != null && foundCompany.tags.website != "") "WEB:" else "WEB: Not provided"
        val webLink = if (foundCompany.tags.website != null && foundCompany.tags.website != "") foundCompany.tags.website else ""
        val contact = "Contact us"

        if (webLink != "") {
            val webUrl = binding.webLink
            webUrl.text = webLink
            webUrl.setTextColor(Color.BLUE)
            webUrl.setOnClickListener {
                val link: Uri = Uri.parse(webLink)
                val goToWeb = Intent(Intent.ACTION_VIEW, link)
                startActivity(goToWeb)
            }
        }

        binding.compName.text = foundCompany.tags.name
        binding.compType.text = foundCompany.tags.amenity.replace("_", " ")
        binding.openingHours.text = openingHours
        binding.tel.text = tel
        binding.web.text = web
        binding.tel.text = tel
        binding.contactUs.text = contact
        binding.users.text = users
    }

    fun setCoordinates(latitude: Double, longitude: Double) {
        val showOnMap = binding.showonmap
        showOnMap.isEnabled = true
        showOnMap.setOnClickListener {
            val queryUrl: Uri = Uri.parse("${SEARCHPREFIX}${latitude},${longitude}")
            val show = Intent(Intent.ACTION_VIEW, queryUrl)
            startActivity(show)
        }
        setPositionOnMap(latitude, longitude)
    }

    private fun setConfirmButton() {
        val confirmButton = binding.confirm
        confirmButton.setOnClickListener {
            apiService.checkInCompany(nearestCompany, this)
        }
        confirmButton.isEnabled = true
    }

    private fun cancelAnimation() {
        val animation = binding.animationView
        animation.cancelAnimation()
        animation.isVisible = false
    }

    private fun pauseAnimation() {
        val animation = binding.animationView
        animation.pauseAnimation()
        animation.isVisible = true
    }

    private fun getNearestCompany(companyList: MutableList<Element>, lat: Double, lon: Double): Element {
        val nearestCompany = companyList.sortedBy { getDistanceFromLatLon(lat, lon, it.lat, it.lon).second } as MutableList<Element>
        return nearestCompany[0]
    }

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionDenied(this, perms.first())) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("inflater.inflate(R.menu.menuicons, menu)", "com.example.zadanie.R")
    )
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menuicons, menu)
        menu.findItem(R.id.check_in).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.logout_app -> {
            findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToLoginFragment())
            apiService.logoutUser(this)
            true
        }
        R.id.manage_friends -> {
            findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToAddFriendFragment())
            true
        }
        R.id.my_company -> {
            try {
                findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToHomeFragment(
                    loggedInUser.companyId?.toLong()!!))
            }
            catch (e: Exception) {
                Snackbar.make(requireView(), "You are not checked in!", Snackbar.LENGTH_SHORT).show()
            }
            true
        }
        R.id.companies_with_members -> {
            findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToCompanyFragment())
            true
        }
        R.id.check_in -> {
            findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToCheckInFragment())
            true
        }
        else -> { super.onOptionsItemSelected(item) }
    }

}