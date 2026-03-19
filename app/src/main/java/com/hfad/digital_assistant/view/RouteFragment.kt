package com.hfad.digital_assistant.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayout
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.RouteApi
import com.hfad.digital_assistant.model.api.RouteRepository
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.viewModel.RouteViewModel
import com.hfad.digital_assistant.viewModel.RouteViewModelFactory

class RouteFragment : Fragment() {

    private lateinit var viewModel: RouteViewModel
    private lateinit var contentContainer: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_route, container, false)

        val userPreferences = UserPreferences(requireContext())
        val routeApi = RouteApi.RouteApiFactory.create(userPreferences)
        val repository = RouteRepository(routeApi)

        viewModel = ViewModelProvider(
            this,
            RouteViewModelFactory(repository)
        )[RouteViewModel::class.java]

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        contentContainer = view.findViewById(R.id.contentContainer)

        // вкладки
        tabLayout.addTab(tabLayout.newTab().setText("Теория"))
        tabLayout.addTab(tabLayout.newTab().setText("Практика"))
        tabLayout.addTab(tabLayout.newTab().setText("Рефлексия"))

        // переключение вкладок
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showModules("theory")
                    1 -> showModules("practice")
                    2 -> showModules("reflection")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // наблюдение за модулями
        viewModel.modules.observe(viewLifecycleOwner) {
            showModules("theory") // стартовая вкладка
        }

        viewModel.loadModules()

        return view
    }

    // ГЛАВНАЯ ФУНКЦИЯ
    private fun showModules(type: String) {

        contentContainer.removeAllViews()

        val container = LinearLayout(requireContext())
        container.orientation = LinearLayout.VERTICAL

        val modules = viewModel.modules.value
            ?.filter { it.type == type }
            ?.sortedBy { it.order }
            ?: emptyList()

        modules.forEach { module ->

            val lessonView = layoutInflater.inflate(R.layout.item_module, container, false)

            val title = lessonView.findViewById<TextView>(R.id.lessonTitle)
            val content = lessonView.findViewById<LinearLayout>(R.id.lessonContent)

            title.text = module.title
            content.visibility = View.GONE

            // цвет (пройден / нет)
            val completed = viewModel.completedModules.value?.contains(module.id) == true
            title.setTextColor(
                if (completed)
                    resources.getColor(R.color.gray_for_text, null)
                else
                    resources.getColor(R.color.blue_for_text, null)
            )

            // раскрытие + отметка
            title.setOnClickListener {
                content.visibility =
                    if (content.visibility == View.GONE) View.VISIBLE else View.GONE

                viewModel.markCompleted(module.id)
            }

            // наполнение урока
            module.items.sortedBy { it.order }.forEach { item ->

                when (item.type) {

                    "text" -> {
                        val tv = TextView(requireContext())
                        tv.text = item.text ?: ""
                        tv.setTextColor(resources.getColor(R.color.black, null))
                        tv.textSize = 16f
                        tv.setPadding(8, 8, 8, 8)
                        content.addView(tv)
                    }

                    "file" -> {
                        val tv = TextView(requireContext())
                        tv.text = item.library_file?.title ?: "Открыть файл"
                        tv.setTextColor(resources.getColor(R.color.blue_for_text, null))

                        tv.setOnClickListener {
                            item.library_file?.file?.let { url ->
                                openFile(url)
                            }
                        }

                        content.addView(tv)
                    }
                }
            }

            container.addView(lessonView)
        }

        contentContainer.addView(container)
    }

    private fun openFile(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}