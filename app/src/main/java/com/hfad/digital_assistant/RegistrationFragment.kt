package com.hfad.digital_assistant

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.navigation.findNavController

class RegistrationFragment : Fragment() {
    //Добавление строк для ключей
    companion object {
        val NAME_KEY = "name"
        val SURNAME_KEY = "surname"
        val PATRONYMIC_KEY = "patronymic"
        val SEND_KEY = "send"
    }

    private var name: String = ""
    private var surname: String = ""
    private var patronymic: String = ""
    private var send: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_registration, container, false)
        val name_text = view.findViewById<EditText>(R.id.name)
        val surname_text = view.findViewById<EditText>(R.id.surname)
        val patronymic_text = view.findViewById<EditText>(R.id.patronymic)
        val send_button = view.findViewById<Button>(R.id.send)


        if (savedInstanceState != null) {
            name = savedInstanceState.getString(NAME_KEY).toString()
            surname = savedInstanceState.getString(SURNAME_KEY).toString()
            patronymic = savedInstanceState.getString(PATRONYMIC_KEY).toString()
            send = savedInstanceState.getBoolean(SEND_KEY)
        }

        send_button.setOnClickListener {
            val user_name = name_text.text.toString()
            val user_surname = surname_text.text.toString()
            val user_patronymic = patronymic_text.text.toString()

            if (user_name.isNotEmpty() && user_surname.isNotEmpty() && user_patronymic.isNotEmpty()) {
                view.findNavController()
                    .navigate(R.id.action_registrationFragment_to_mainFragment)
            } else {
                // Показываем сообщение об ошибке
                name_text.error = if (user_name.isEmpty()) "Введите имя" else null
                surname_text.error = if (user_surname.isEmpty()) "Введите фамилию" else null
                patronymic_text.error =
                    if (user_patronymic.isEmpty()) "Введите отчество" else null
            }
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NAME_KEY, name)
        outState.putString(SURNAME_KEY, surname)
        outState.putString(PATRONYMIC_KEY, patronymic)
        outState.putBoolean(SEND_KEY, send)
    }
}