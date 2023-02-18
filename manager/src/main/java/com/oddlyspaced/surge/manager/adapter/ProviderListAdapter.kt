package com.oddlyspaced.surge.manager.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.manager.databinding.ItemProviderBinding

class ProviderListAdapter(private val items: List<Provider>) : RecyclerView.Adapter<ProviderListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemProviderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Provider) {
            binding.txItemProviderName.text = data.name
            binding.txItemProviderPhone.text = "${data.phone.countryCode} ${data.phone.phoneNumber}"
            binding.txItemProviderServices.text = data.services.joinToString(", ")
            binding.txItemProviderStatus.text = if (data.isActive) "Active" else "Inactive"
            binding.txItemProviderStatus.setTextColor(if (data.isActive) Color.GREEN else Color.RED)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProviderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}