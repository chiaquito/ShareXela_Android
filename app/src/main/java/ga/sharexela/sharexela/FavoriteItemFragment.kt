package ga.sharexela.sharexela

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_favorite_item.*
import kotlinx.android.synthetic.main.fragment_my_list.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"




class FavoriteItemFragment : Fragment() {

    private var itemObjectsSelialized: ItemObjectsSerialized? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemObjectsSelialized = it.getSerializable(ARG_PARAM1) as ItemObjectsSerialized
            param2 = it.getString(ARG_PARAM2)
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_item, container, false)
    }



    override fun onResume() {
        super.onResume()

        var dataArrayList :ArrayList<ItemSerializerModel> = arrayListOf()
        for (ele in itemObjectsSelialized!!.itemObjects){
            dataArrayList.add(ele)
        }


        val divider = androidx.recyclerview.widget.DividerItemDecoration(MyApplication.appContext, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL)
        recyclerViewFavoriteItem.apply { addItemDecoration(divider) }

        //RecyclerViewをセットする
        val layoutManager = LinearLayoutManager(MyApplication.appContext, RecyclerView.VERTICAL, false)
        recyclerViewFavoriteItem.layoutManager = layoutManager

        val adapter = MyItemVerticalCardRecyclerViewAdapter(dataArrayList=dataArrayList, myListener=null, favListener=listener )
        recyclerViewFavoriteItem.adapter = adapter

    }




    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }



    override fun onDetach() {
        super.onDetach()
        listener = null
    }



    interface OnFragmentInteractionListener {

        fun launchDetailActivity(selectedItem:ItemSerializerModel)

    }


    companion object {

        @JvmStatic
        fun newInstance(itemObjectsSelialized: ItemObjectsSerialized, param2: String) =
            FavoriteItemFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, itemObjectsSelialized)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}
