package com.keelim.mygrade.ui.center.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.oss.licenses.OssLicensesActivity
import com.google.android.material.snackbar.Snackbar
import com.keelim.data.model.Result
import com.keelim.mygrade.R
import com.keelim.mygrade.databinding.FragmentMainBinding
import com.keelim.mygrade.ui.GradeActivity
import com.keelim.mygrade.utils.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {
    @Inject
    lateinit var themeManager: ThemeManager
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentMainBinding.inflate(inflater, container, false)
            .apply {
                btnSubmit.setOnClickListener {
                    if (validation()) {
                        viewModel.submit(
                            valueOrigin.text.toString().toFloat(),
                            valueAverage.text.toString().toFloat(),
                            valueNumber.text.toString().toFloat(),
                            valueStudent.text.toString().toInt(),
                            true
                        )
                    }
                }
                notification.setOnClickListener {
                    findNavController().navigate(R.id.notificationFragment)
                }
                history.setOnClickListener {
                    findNavController().navigate(R.id.historyFragment)
                }
                footer.setOnClickListener {
                    startActivity(Intent(requireContext(), OssLicensesActivity::class.java))
                }
            }
            .also {
                _binding = it
            }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun observeState() = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.state.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect {
                when (it) {
                    is MainState.UnInitialized -> Unit
                    is MainState.Loading -> {
                        Snackbar.make(binding.root, "잠시만 기다려주세요", Snackbar.LENGTH_SHORT).show()
                    }
                    is MainState.Success -> {
                        if (validation()) {
                            when {
                                it.value < 30 -> "A"
                                it.value < 60 -> "B"
                                it.value < 80 -> "C"
                                it.value < 100 -> "D"
                                else -> "F"
                            }.also { grade ->
                                startActivity(
                                    Intent(
                                        requireContext(),
                                        GradeActivity::class.java
                                    ).apply {
                                        putExtra(
                                            "data", Result(
                                                grade,
                                                getLevel(
                                                    (it.value * binding.valueStudent.text.toString()
                                                        .toInt()) / 100
                                                )
                                            )
                                        )
                                    })
                            }
                        }
                    }
                    is MainState.Error -> {
                        Snackbar
                            .make(binding.root, "오류가 발생했습니다", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }

            }
    }

    private fun getLevel(level: Int): String = level.toString() + " / " + binding.valueStudent.text

    private fun validation(): Boolean = with(binding) {
        if (valueOrigin.text.toString().isEmpty()) {
            valueOrigin.error = "원 점수를 입력해주세요"
            return false
        }
        if (valueAverage.text.toString().isEmpty()) {
            valueAverage.error = "평균 값을 입력해주세요"
            return false
        }
        if (valueNumber.text.toString().isEmpty()) {
            valueNumber.error = "표준 편차를 입력해주세요"
            return false
        }
        if (valueStudent.text.toString().isEmpty()) {
            valueStudent.error = "학생 수를 입력해주세요"
            return false
        }
        return true
    }
}