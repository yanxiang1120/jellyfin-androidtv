package org.jellyfin.androidtv.ui.startup

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_alert_dialog.view.*
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.data.repository.ConnectedState
import org.jellyfin.androidtv.data.repository.ConnectingState
import org.jellyfin.androidtv.data.repository.UnableToConnectState
import org.jellyfin.androidtv.ui.shared.AlertFragment
import org.jellyfin.androidtv.ui.shared.KeyboardFocusChangeListener
import org.jellyfin.androidtv.util.toUUID
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class AddServerFragment(
	private val onServerAdded: (serverId: UUID) -> Unit = {},
	private val onCancelCallback: () -> Unit = {},
	private val onClose: () -> Unit = {}
) : AlertFragment(
	title = R.string.lbl_enter_server_address,
	description = R.string.lbl_valid_server_address,
	onCancelCallback = onCancelCallback,
	onClose = onClose
) {
	private val loginViewModel: LoginViewModel by viewModel()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = super.onCreateView(inflater, container, savedInstanceState)!!

		// Build the url field
		val address = EditText(requireContext())
		address.hint = requireActivity().getString(R.string.lbl_ip_hint)
		address.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
		address.isSingleLine = true
		address.onFocusChangeListener = KeyboardFocusChangeListener()
		address.nextFocusDownId = R.id.confirm
		address.requestFocus()

		// Add the url field to the content view
		view.content.addView(address)

		// Build the error text field
		val errorText = TextView(requireContext())
		view.content.addView(errorText)

		// Override the default confirm button click listener to return the address field text
		view.confirm.setOnClickListener {
			if (address.text.isNotBlank()) {
				loginViewModel.addServer(address.text.toString()).observe(viewLifecycleOwner) { state ->
					when (state) {
						ConnectingState -> errorText.text = "CONNECTING"// TODO error message
						is UnableToConnectState -> errorText.text = "NO CONNECT (${state.error.message})"// TODO error message
						is ConnectedState -> {
							onServerAdded(state.publicInfo.id.toUUID())
							onClose()
						}
					}
				}
			} else {
				// TODO error message
				errorText.text = "EMPTY FIELD"
			}
		}

		return view
	}
}
