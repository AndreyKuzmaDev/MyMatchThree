package com.example.mymatchthree.ui.records

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymatchthree.R
import com.example.mymatchthree.data.model.GameMode
import com.example.mymatchthree.data.model.GameRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecordsAdapter
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        initializeViews()
        setupRecyclerView()
        loadRecords()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recordsRecyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnClearRecords).setOnClickListener {
            clearAllRecords()
        }
    }

    private fun setupRecyclerView() {
        adapter = RecordsAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecordsActivity)
            adapter = this@RecordsActivity.adapter
            addItemDecoration(
                DividerItemDecoration(
                    this@RecordsActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    private fun loadRecords() {
        val demoRecords = listOf(
            GameRecord(
                playerName = "Andrey",
                score = 2025,
                mode = GameMode.Classic,
                dateAchieved = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000)
            ),
            GameRecord(
                playerName = "Also Andrey",
                score = 2020,
                mode = GameMode.Infinite,
                dateAchieved = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)
            ),
            GameRecord(
                playerName = "Andrey again",
                score = 420,
                mode = GameMode.Classic,
                dateAchieved = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)
            ),
            GameRecord(
                playerName = "Nobody plays it",
                score = 220,
                mode = GameMode.Infinite,
                dateAchieved = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000)
            ),
            GameRecord(
                playerName = "EvilArthas",
                score = 141,
                mode = GameMode.Classic,
                dateAchieved = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000)
            )
        )

        val sortedRecords = demoRecords.sortedByDescending { it.score }
        adapter.submitList(sortedRecords)

        if (sortedRecords.isEmpty()) {
            tvEmpty.visibility = TextView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            tvEmpty.visibility = TextView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
        }
    }

    private fun clearAllRecords() {
        adapter.submitList(emptyList())
        tvEmpty.visibility = TextView.VISIBLE
        recyclerView.visibility = RecyclerView.GONE
    }
}

// Адаптер для RecyclerView
class RecordsAdapter : RecyclerView.Adapter<RecordsAdapter.RecordViewHolder>() {

    private var records: List<GameRecord> = emptyList()

    fun submitList(newRecords: List<GameRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecordViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(records[position], position + 1)
    }

    override fun getItemCount(): Int = records.size

    class RecordViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvPosition: TextView = itemView.findViewById(R.id.tvPosition)
        private val tvPlayerName: TextView = itemView.findViewById(R.id.tvPlayerName)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        private val tvMode: TextView = itemView.findViewById(R.id.tvMode)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(record: GameRecord, position: Int) {
            tvPosition.text = "$position"
            tvPlayerName.text = record.playerName
            tvScore.text = "Score: ${record.score}"
            tvMode.text = when (record.mode) {
                GameMode.Classic -> "Classic"
                GameMode.Infinite -> "Infinite"
                GameMode.Timed -> "Timed"
            }

            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            tvDate.text = dateFormat.format(Date(record.dateAchieved))

            when (position) {
                1 -> {
                    tvPosition.setBackgroundResource(R.color.gold)
                    tvPosition.setTextColor(android.graphics.Color.WHITE)
                }
                2 -> {
                    tvPosition.setBackgroundResource(R.color.silver)
                    tvPosition.setTextColor(android.graphics.Color.WHITE)
                }
                3 -> {
                    tvPosition.setBackgroundResource(R.color.bronze)
                    tvPosition.setTextColor(android.graphics.Color.WHITE)
                }
                else -> {
                    tvPosition.setBackgroundResource(android.R.color.transparent)
                    tvPosition.setTextColor(android.graphics.Color.BLACK)
                }
            }
        }
    }
}