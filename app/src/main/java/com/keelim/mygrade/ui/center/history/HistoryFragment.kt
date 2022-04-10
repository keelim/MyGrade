package com.keelim.mygrade.ui.center.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.keelim.common.repeatCallDefaultOnStarted
import com.keelim.common.toGone
import com.keelim.common.toVisible
import com.keelim.common.toast
import com.keelim.mygrade.databinding.FragmentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val viewModel: HistoryViewModel by viewModels()
    private val binding get() = _binding!!
    private val historyAdapter by lazy {
        HistoryAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentHistoryBinding.inflate(inflater, container, false).apply {
            historyRecycler.adapter = historyAdapter
        }.also {
            _binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeState() = viewLifecycleOwner.lifecycleScope.launch {
        repeatCallDefaultOnStarted {
            viewModel.state.collect {
                when (it) {
                    is HistoryState.UnInitialized -> {}
                    is HistoryState.Loading -> {
                        binding.loading.toVisible()
                    }
                    is HistoryState.Success -> {
                        binding.loading.toGone()
                        if (it.data.isEmpty()) {
                            binding.noHistory.toVisible()
                        } else {
                            binding.noHistory.toGone()
                            historyAdapter.submitList(it.data)
                        }
                    }
                    is HistoryState.Error -> {
                        binding.loading.toGone()
                        toast(it.message)
                    }
                }
            }
        }
    }
}