package com.oddlyspaced.surge.app.customer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oddlyspaced.surge.app.common.modal.Provider
import com.oddlyspaced.surge.app.customer.databinding.ItemProviderDetailBinding

class ProviderListAdapter(private val data: ArrayList<Provider>): RecyclerView.Adapter<ProviderListAdapter.ViewHolder>() {
    inner class ViewHolder(private val item: ItemProviderDetailBinding): RecyclerView.ViewHolder(item.root) {
        fun bind(param: Provider) {

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