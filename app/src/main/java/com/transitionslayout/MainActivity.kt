package com.transitionslayout

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val items by lazy {
        val titles = mutableListOf<String>()
        titles.add(getString(R.string.main_list_bounce))
        titles.add(getString(R.string.main_list_burst_layout))
        titles.add(getString(R.string.main_list_balloon))
        return@lazy titles
    }
    private lateinit var mainListAdapter: MainListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainListAdapter = MainListAdapter(baseContext, items)
        mainListAdapter.setOnItemClickListener { position ->
            when(position){
                0 -> TestBouncingBallActivity.launch(this)
                1 -> TestBurstLayoutActivity.launch(this)
                2 -> TestBalloonActivity.launch(this)
            }

        }
        recyclerViewTest.layoutManager = LinearLayoutManager(baseContext)
        recyclerViewTest.adapter = mainListAdapter
    }
}

class MainListAdapter(private val context: Context, private val items: MutableList<String>)
    : RecyclerView.Adapter<MainListAdapter.ViewHolder>() {
    private var callback: ((Int) -> Unit)? = null
    fun setOnItemClickListener(callback: (Int) -> Unit) {
        this.callback = callback
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.render(getData(position), position, callback)

    override fun getItemCount(): Int = items.size

    private fun getData(position: Int) = items.get(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.cell_item, parent, false))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle by lazy { itemView.findViewById<TextView>(R.id.textTitle) }

        fun render(title: String, position: Int, callback: ((Int) -> Unit)?) {
            textTitle.text = title
            textTitle.setOnClickListener {
                callback?.invoke(position)
            }
        }
    }
}
