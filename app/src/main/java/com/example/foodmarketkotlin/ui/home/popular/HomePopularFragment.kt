package com.example.foodmarketkotlin.ui.home.popular

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmarketkotlin.databinding.FragmentHomeNewTasteBinding
import com.example.foodmarketkotlin.data.model.dummy.HomeVerticalModel
import com.example.foodmarketkotlin.ui.detail.DetailActivity
import com.example.foodmarketkotlin.ui.home.newtaste.HomeNewTasteAdapter


class HomePopularFragment : Fragment(), HomeNewTasteAdapter.ItemAdapterCallback {

    private var _binding: FragmentHomeNewTasteBinding? = null
    private val binding get() = _binding!!


    private var foodList : ArrayList<HomeVerticalModel> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeNewTasteBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initDataDummy()
        var adapter = HomeNewTasteAdapter(foodList, this)
        var layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(activity)
        binding.rcList.layoutManager = layoutManager
        binding.rcList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initDataDummy() {
        foodList.add(HomeVerticalModel("Cherry Healthy", "Rp 28.000", src = "" , 5f))
        foodList.add(HomeVerticalModel("Burger Tamayo", "Rp 40.000",src = ""  ,4f))
        foodList.add(HomeVerticalModel("Bakwan Cihuy", "Rp 15.000", src = "" ,3f))
    }

    override fun onCLick(v: View, data: HomeVerticalModel) {
        val detail = Intent(activity, DetailActivity::class.java)
        startActivity(detail)
    }

}