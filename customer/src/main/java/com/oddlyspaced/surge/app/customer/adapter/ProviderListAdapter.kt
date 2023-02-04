package com.oddlyspaced.surge.app.customer.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.oddlyspaced.surge.app.common.modal.Location
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.common.modal.distanceTo
import com.oddlyspaced.surge.app.customer.databinding.ItemProviderDetailBinding
import kotlin.math.roundToInt


class ProviderListAdapter(private val data: ArrayList<Provider>, private val basePoint: Location?): RecyclerView.Adapter<ProviderListAdapter.ViewHolder>() {
    inner class ViewHolder(private val item: ItemProviderDetailBinding): RecyclerView.ViewHolder(item.root) {
        fun bind(param: Provider) {
            item.txProviderInfoName.text = param.name
            param.services.forEach { service ->
                item.chipGroupProviderInfoService.addView(Chip(item.root.context).apply {
                    text = service
                })
            }
            item.txProviderInfoDistance.isVisible = basePoint != null
            basePoint?.let {
                item.txProviderInfoDistance.text = "${it.distanceTo(param.location).roundToInt()} km"
            }
            item.fabProviderCall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${param.phone.phoneNumber}")
                item.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProviderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}