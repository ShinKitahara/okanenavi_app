package okanenavi.co.jp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import okanenavi.co.jp.databinding.FragmentHomeBinding
import okanenavi.co.jp.model.Record


private const val ARG_PAGE = "page"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private var page: Int? = null
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private val _recordAdapter = HomeRecyclerAdapter {
        onSelectRecord(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            page = it.getInt(ARG_PAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.recordRecycler.adapter = _recordAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val records = when (page) {
            1 -> homeViewModel.records1
            2 -> homeViewModel.records2
            3 -> homeViewModel.records3
            else -> return
        }
        records.observe(viewLifecycleOwner) {
            _recordAdapter.submitList(it)
        }
    }

    private fun onSelectRecord(record: Record) {
        Intent(context, CreateActivity::class.java).apply {
            putExtra(CreateActivity.ARG_RECORD_ID, record.id)
            startActivity(this)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(page: Int) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PAGE, page)
                }
            }
    }
}
