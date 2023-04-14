package com.example.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BLEListAdapter(private val bleList: List<ScanResult>, private val connectDevice : (ScanResult) -> Unit) :
    RecyclerView.Adapter<BLEListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val index: TextView
        val name: TextView
        val address: TextView
        val button: Button

        init {
            index = view.findViewById<TextView>(R.id.textView2)
            name = view.findViewById<TextView>(R.id.textView3)
            address = view.findViewById<TextView>(R.id.textView4)
            button = view.findViewById(R.id.button)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.component, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bleList.size
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.index.text = position.toString()
        holder.name.text = bleList[position].device.name ?: "Unnamed"
        holder.address.text = bleList[position].device.address ?: ""

        holder.button.setOnClickListener {
            connectDevice(bleList[position])
        }
    }
}