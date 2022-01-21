package com.sunayanpradhan.notesnap.fragments

import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.sunayanpradhan.notesnap.R
import com.sunayanpradhan.notesnap.activities.MainActivity
import com.sunayanpradhan.notesnap.adapters.RvNotesAdapter
import com.sunayanpradhan.notesnap.databinding.ActivityMainBinding
import com.sunayanpradhan.notesnap.databinding.FragmentNoteBinding
import com.sunayanpradhan.notesnap.utils.SwipeToDelete
import com.sunayanpradhan.notesnap.utils.hideKeyboard
import com.sunayanpradhan.notesnap.viewModel.NoteActivityViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class noteFragment : Fragment(R.layout.fragment_note) {
    private lateinit var noteBinding: FragmentNoteBinding
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    private lateinit var rvAdapter: RvNotesAdapter

    private var backPressedTime: Long = 0
    private var backToast: Toast? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteBinding = FragmentNoteBinding.bind(view)
        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)
        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)

            //activity.window.statusBarColor = Color.WHITE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")
        }
        noteBinding.addNoteFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(noteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment())
        }
        noteBinding.innerFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(noteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment())
        }



        recyclerViewDisplay()

        swipeToDelete(noteBinding.rvNote)


        //implements Search here

        noteBinding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                noteBinding.noData.isVisible = false
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    val text = s.toString()
                    val query = "%$text%"
                    if (query.isNotEmpty()) {
                        noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner) {

                            rvAdapter.submitList(it)

                        }
                    } else {
                        observerDataChanges()
                    }
                } else {
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        noteBinding.search.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }


        noteBinding.rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when {
                scrollX > oldScrollY -> {
                    noteBinding.chatFabText.isVisible = false
                }
                scrollX == scrollY -> {
                    noteBinding.chatFabText.isVisible = true
                }
                else -> {
                    noteBinding.chatFabText.isVisible = true
                }
            }
        }


    }

    private fun swipeToDelete(rvNote: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note = rvAdapter.currentList[position]
                var actionBtnTapped = false
                noteActivityViewModel.deleteNote(note)
                noteBinding.search.apply {
                    hideKeyboard()
                    clearFocus()
                }
                if (noteBinding.search.text.toString().isEmpty()) {
                    observerDataChanges()
                }

                val snackbar = Snackbar.make(
                    requireView(), "Note Deleted", Snackbar.LENGTH_LONG
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {

                        transientBottomBar?.setAction("UNDO") {
                            noteActivityViewModel.saveNote(note)
                            actionBtnTapped = true
                            noteBinding.noData.isVisible = false
                        }

                        super.onShown(transientBottomBar)

                    }
                }).apply {
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.add_note_fab)

                }
                snackbar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellowOrange
                    )
                )
                snackbar.show()

            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvNote)


    }

    private fun observerDataChanges() {
        noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner) { list ->
            noteBinding.noData.isVisible = list.isEmpty()
            rvAdapter.submitList(list)

        }

    }

    private fun recyclerViewDisplay() {
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> setUpRecyclerVIew(2)
            Configuration.ORIENTATION_LANDSCAPE -> setUpRecyclerVIew(3)
        }

    }

    private fun setUpRecyclerVIew(spanCount: Int) {

        noteBinding.rvNote.apply {
            layoutManager =
                StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            rvAdapter = RvNotesAdapter()
            rvAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = rvAdapter
            postponeEnterTransition(300L, TimeUnit.MICROSECONDS)

            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }

        }

        observerDataChanges()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val callback= object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    activity?.moveTaskToBack(true)
                    activity?.finish()
                    return
                } else {
                   val backToast = Toast.makeText(context, "Double press to Exit", Toast.LENGTH_SHORT)
                    backToast.show()
                }
                backPressedTime = System.currentTimeMillis()

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)



        return inflater.inflate(R.layout.fragment_note,container,false)

        //return super.onCreateView(inflater, container, savedInstanceState)
    }


}
