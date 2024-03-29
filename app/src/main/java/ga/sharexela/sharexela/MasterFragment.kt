package ga.sharexela.sharexela

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_master.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"




class MasterFragment : Fragment(){

    private var itemObjectsSerialized: ItemObjectsSerialized? = null
    private var categoryNumber: String? = null
    private var localStatus: Boolean? = null

    private var listener: OnFragmentInteractionListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            itemObjectsSerialized = it.getSerializable(ARG_PARAM1) as ItemObjectsSerialized
            categoryNumber = it.getString(ARG_PARAM2)
            localStatus = it.getBoolean(ARG_PARAM3)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        menu.apply {
            findItem(R.id.menuSearch).isVisible = true
            findItem(R.id.menuGoHome).isVisible = false
            findItem(R.id.action_settings).isVisible = false
            findItem(R.id.menuDone).isVisible = false
            findItem(R.id.menuSync).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menuSearch -> {
                listener!!.onSearchMenuSelected()
            }
        }
        return true
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_master, container, false)
        setHasOptionsMenu(true)
        return view
    }




    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setUpRecyclerView()
        swiperefresh.setOnRefreshListener { updateItemObjects() }

    }




    override fun onResume() {
        super.onResume()

        //setUpNavigationDrawer()
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
        //使用確認済み
        fun onSearchMenuSelected()

        fun launchDetailActivity(selectedItem: ItemSerializerModel)


    }


    companion object {

        @JvmStatic
        fun newInstance(itemObjectsSerialized: ItemObjectsSerialized?, categoryNumber: String?, localStatus: Boolean) =
            MasterFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, itemObjectsSerialized)
                    putString(ARG_PARAM2, categoryNumber)
                    putBoolean(ARG_PARAM3, localStatus)
                }
            }
    }


    fun updateItemObjects() {
        //データの更新
        print("ココは通るか？")
        println(categoryNumber)

        //そのカテゴリーに応じて取得するデータを変更したい
        if (localStatus == false) retrieveItemObjects()
        if (localStatus == true ) retrieveItemObjectsLocal()

    }


    fun retrieveItemObjects(){
        println("通信テスト")

        val service = setService()
        service.getItemCategoryListAPIView(categoryName=categoryNumber!!).enqueue(object : Callback<ItemUniversalListAPIView> {

            override fun onResponse(call: Call<ItemUniversalListAPIView>, response: Response<ItemUniversalListAPIView>) {

                var itemObjects: List<ItemSerializerModel> = response.body()?.ITEM_OBJECTS!!
                itemObjectsSerialized = ItemObjectsSerialized(itemObjects = itemObjects)
                setUpRecyclerView()
                swiperefresh.isRefreshing = false
            }

            override fun onFailure(call: Call<ItemUniversalListAPIView>, t: Throwable) {
                println("onFailureの結果　：　")
                println(t)
            }
        })
    }


    fun retrieveItemObjectsLocal(){
        println("通信テスト")

        val service = setService()
        service.getItemCategoryLocalListAPIView(categoryName=categoryNumber!!, authTokenHeader=sessionData.authTokenHeader!! ).enqueue(object : Callback<ItemUniversalListAPIView> {

            override fun onResponse(call: Call<ItemUniversalListAPIView>, response: Response<ItemUniversalListAPIView>) {

                var itemObjects: List<ItemSerializerModel> = response.body()?.ITEM_OBJECTS!!
                itemObjectsSerialized = ItemObjectsSerialized(itemObjects = itemObjects)
                setUpRecyclerView()
                swiperefresh.isRefreshing = false
            }

            override fun onFailure(call: Call<ItemUniversalListAPIView>, t: Throwable) {
                println("onFailureの結果　：　")
                println(t)
            }
        })
    }




    fun setUpRecyclerView(){

        var dataArrayList: ArrayList<ItemSerializerModel> = arrayListOf()
        val itemObjects = itemObjectsSerialized!!.itemObjects
        for (numero in 0..itemObjects.size-1){
            //for (numero in 0..item_objects_count){
            //println("番号 : "+ numero)
            //println(itemSerializerList[numero])
            var id    = itemObjects[numero].id
            var title = itemObjects[numero].title
            //var description = itemSerializerList[numero].description
            var category = itemObjects[numero].category!!.number
            //var created_at = itemSerializerList[numero].created_at
            var image = itemObjects[numero].image1
            var deadline = itemObjects[numero].deadline

            dataArrayList.add(
                ItemSerializerModel(
                    id    = id,
                    title = title,
                    //description = description,
                    category = CategorySerializerModel(number=category),
                    //created_at = created_at,
                    image1 = image,
                    deadline = deadline
                ))

        }
        val divider = androidx.recyclerview.widget.DividerItemDecoration(MyApplication.appContext, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL)
        recyclerView.apply {
            addItemDecoration(divider)
        }

        //val layoutManager = GridLayoutManager(this@MasterFragment.context, 3)
        val layoutManager = GridLayoutManager(this@MasterFragment.activity, 3)
        recyclerView.layoutManager = layoutManager

        val adapter = MyRecyclerViewAdapter(dataArrayList=dataArrayList, myListener=listener)
        recyclerView.adapter = adapter


    }



}


fun setUpNavigationDrawer(activity: Activity){

    //if (navigationDrawerInit == true) return

    //ナビゲーションドロワーの編集
    val nav_view = activity.findViewById<NavigationView>(R.id.nav_view)
    val h_view = nav_view.getHeaderView(0)
    val userProfileImageView = h_view.findViewById<ImageView>(R.id.iv_profile)
    val tv_userName = h_view.findViewById<TextView>(R.id.tv_userName)
    val tv_emailAddress = h_view.findViewById<TextView>(R.id.tv_emailAddress)

    if (sessionData.profileObj != null){

        val profileImageUrl = BASE_URL + sessionData.profileObj!!.image!!.substring(1)
        //Glide.with(MyApplication.appContext).load(profileImageUrl).into(userProfileImageView)
        Glide.with(MyApplication.appContext).load(profileImageUrl).circleCrop().into(userProfileImageView)
        tv_userName.text = sessionData.profileObj!!.user!!.username
        tv_emailAddress.visibility = View.VISIBLE
        tv_emailAddress.text = sessionData.profileObj!!.user!!.email
        //ナビゲーションドロワーメニューの編集(ログインメニューの削除)
        //val menu = nav_view.menu
        //menu.removeItem(R.id.menuSignIn)
        //menu.


    }else if (sessionData.profileObj == null){
        tv_userName.setText(activity.getString(R.string.drawer_header_logOutStatus)) //"未ログイン"
        tv_emailAddress.visibility = View.GONE
        //ナビゲーションドロワーメニューの編集(ログインメニューの削除)
        //val menu = nav_view.menu
        //menu.removeItem(R.id.menuSignOut)

    }
    //navigationDrawerInit = true

}



