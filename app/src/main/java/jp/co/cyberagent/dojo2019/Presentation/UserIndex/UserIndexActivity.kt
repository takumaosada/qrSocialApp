package jp.co.cyberagent.dojo2019.Presentation.UserIndex

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.cyberagent.dojo2019.*
import jp.co.cyberagent.dojo2019.Database.AppDatabase
import jp.co.cyberagent.dojo2019.Entity.User
import kotlinx.coroutines.*

class UserIndexActivity : AppCompatActivity() {

    private var database: AppDatabase? = null
    private var userList = mutableListOf<User>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserIndexAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_index)

        database = AppDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.user_index)

        recyclerView.setHasFixedSize(true)
        adapter = UserIndexAdapter(this, userList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val swipeToDismissTouchHelper = getSwipeToDismissTouchHelper(adapter)
        swipeToDismissTouchHelper.attachToRecyclerView(recyclerView)

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                userList = database?.userDao()?.getAll()!!.toMutableList()
                adapter.updateUserList(userList)
            }
        }
    }

    private fun getSwipeToDismissTouchHelper(adapter: RecyclerView.Adapter<UserIndexViewHolder>) =
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                lifecycleScope.launch {
                    val uid = userList[position].uid
                    userList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    database?.userDao()?.deleteByUid(uid)
                    adapter.notifyDataSetChanged()
                }
//                adapter.notifyItemRemoved(position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive)
                val itemView = viewHolder.itemView
                val background = ColorDrawable()
                background.color = Color.parseColor("#f44336")
                if (dX < 0)
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                else
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                background.draw(c)
            }
        })
}