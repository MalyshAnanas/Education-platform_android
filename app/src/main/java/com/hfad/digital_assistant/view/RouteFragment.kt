package com.hfad.digital_assistant.view


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.google.android.material.tabs.TabLayout
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.RouteApi
import com.hfad.digital_assistant.model.api.RouteRepository
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.model.local.AppDatabase
import com.hfad.digital_assistant.viewModel.RouteViewModel
import com.hfad.digital_assistant.viewModel.RouteViewModelFactory

class RouteFragment : Fragment() {

    private lateinit var viewModel: RouteViewModel
    private lateinit var contentContainer: FrameLayout
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_route, container, false)

        userPreferences = UserPreferences(requireContext())
        val routeApi = RouteApi.RouteApiFactory.create(userPreferences)

        val db = AppDatabase.getDatabase(requireContext())
        val dao = db.moduleDao()

        val repository = RouteRepository(routeApi, dao)

        viewModel = ViewModelProvider(
            this,
            RouteViewModelFactory(repository)
        )[RouteViewModel::class.java]

        updateUserHeader(view)

        parentFragmentManager.setFragmentResultListener(
            "profile_updated",
            viewLifecycleOwner
        ) { _, _ ->
            updateUserHeader(view)
        }

        // Получаем пользователя из Preferences
        val fullName = userPreferences.getFullName()

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        contentContainer = view.findViewById(R.id.contentContainer)

        tabLayout.addTab(tabLayout.newTab().setText("Теория"))
        tabLayout.addTab(tabLayout.newTab().setText("Практика"))
        tabLayout.addTab(tabLayout.newTab().setText("Рефлексия"))

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

        viewModel.modules.observe(viewLifecycleOwner) {
            updateCounters(view)
            showModules("theory")
        }

        viewModel.completedModules.observe(viewLifecycleOwner) {
            updateCounters(view)
            showModules("theory")
        }

        viewModel.loadModules()

        // Открытие профиля
        val userNameText = view.findViewById<TextView>(R.id.userNameRoute)
        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerRout)

        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }

        userNameText.text = fullName
        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }

        return view
    }

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
            val btn = lessonView.findViewById<Button>(R.id.btnComplete)

            title.text = module.title
            content.visibility = View.GONE

            val isCompleted = viewModel.isCompleted(module.id)

            btn.text = if (isCompleted) {
                "Отметить как непройденный"
            } else {
                "Отметить как пройденный"
            }

            title.setTextColor(
                if (isCompleted)
                    resources.getColor(R.color.gray_for_text, null)
                else
                    resources.getColor(R.color.blue_for_text, null)
            )

            btn.setOnClickListener {
                viewModel.toggleCompleted(module.id)
            }

            btn.setBackgroundResource(
                if (isCompleted) R.drawable.button_module_completed
                else R.drawable.button_module
            )

            title.setOnClickListener {
                content.visibility =
                    if (content.visibility == View.GONE) View.VISIBLE else View.GONE
            }

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
                            item.library_file?.file?.let { openFile(it) }
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

    // Функция склонения
    private fun getModulesWord(count: Int): String {
        val mod10 = count % 10
        val mod100 = count % 100

        return when {
            mod10 == 1 && mod100 != 11 -> "модуль"
            mod10 in 2..4 && mod100 !in 12..14 -> "модуля"
            else -> "модулей"
        }
    }

    // Обновление счётчика
    private fun updateCounters(view: View) {

        val completedCounter = view.findViewById<TextView>(R.id.downloadFilesCounter)
        val completedText = view.findViewById<TextView>(R.id.downloadFilesDoc)

        val allCounter = view.findViewById<TextView>(R.id.allFilesCounter)
        val allText = view.findViewById<TextView>(R.id.allFilesDoc)

        val allModules = viewModel.modules.value ?: emptyList()
        val completedModules = viewModel.completedModules.value ?: emptySet()

        val completedCount = completedModules.size
        val allCount = allModules.size

        completedCounter.text = completedCount.toString()
        completedText.text = getModulesWord(completedCount)

        allCounter.text = allCount.toString()
        allText.text = getModulesWord(allCount)
    }

    // Для обнавления шапки пользователя на странице
    private fun updateUserHeader(view: View) {
        val userNameText = view.findViewById<TextView>(R.id.userNameRoute)
        val userPhoto = view.findViewById<ImageView>(R.id.userImageRout)

        val fullName = userPreferences.getFullName()
        val serverPhotoUrl = userPreferences.getServerPhotoUrl()
        val localPhotoUri = userPreferences.getPhotoUri()

        userNameText.text = fullName ?: "Гость"

        when {
            !serverPhotoUrl.isNullOrBlank() -> {
                // Если используешь Coil:
                userPhoto.load(serverPhotoUrl) {
                    placeholder(R.drawable.userphoto)
                    error(R.drawable.userphoto)
                }
            }

            !localPhotoUri.isNullOrBlank() -> {
                userPhoto.setImageURI(Uri.parse(localPhotoUri))
            }

            else -> {
                userPhoto.setImageResource(R.drawable.userphoto)
            }
        }
    }
}