package com.yijian.dzpoker.activity.game;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.yijian.dzpoker.R;
import com.yijian.dzpoker.activity.base.BaseToolbarActivity;
import com.yijian.dzpoker.activity.fragment.GameLookBackOnTableFragment;
import com.yijian.dzpoker.activity.fragment.RealTimeGameRecordFragment;
import com.yijian.dzpoker.activity.game.bean.AssuranceBean;
import com.yijian.dzpoker.activity.game.bean.Game27Bean;
import com.yijian.dzpoker.activity.game.bean.WinPlayersBean;
import com.yijian.dzpoker.activity.user.GameDetailedHistoryActivity;
import com.yijian.dzpoker.activity.user.MatchRecordDetailPageActivity;
import com.yijian.dzpoker.activity.user.StoreActivity;
import com.yijian.dzpoker.adapter.AssuranceDialogOutsCardAdapter;
import com.yijian.dzpoker.adapter.AssuranceDialogPublicCardAdapter;
import com.yijian.dzpoker.adapter.AssuranceDialogUserInfoAdapter;
import com.yijian.dzpoker.audio.RecordManager;
import com.yijian.dzpoker.baselib.debug.Logger;
import com.yijian.dzpoker.baselib.http.RetrofitApiGenerator;
import com.yijian.dzpoker.baselib.utils.UITools;
import com.yijian.dzpoker.baselib.widget.CustomCircleImageView;
import com.yijian.dzpoker.constant.Constant;
import com.yijian.dzpoker.http.getmygamerecord.GetUserGameRecordApi;
import com.yijian.dzpoker.http.getmygamerecord.GetUserGameRecordCons;
import com.yijian.dzpoker.http.getmygamerecord.UserGameRecordBean;
import com.yijian.dzpoker.http.getmyip.GetMyIpApi;
import com.yijian.dzpoker.http.getmyip.GetMyIpCons;
import com.yijian.dzpoker.http.uploadfile.UploadAudioFileApi;
import com.yijian.dzpoker.service.RecordService;
import com.yijian.dzpoker.service.SocketService;
import com.yijian.dzpoker.util.DisplayHelper;
import com.yijian.dzpoker.DzApplication;
import com.yijian.dzpoker.util.ToastUtil;
import com.yijian.dzpoker.util.Util;
import com.yijian.dzpoker.view.CircleTransform;
import com.yijian.dzpoker.view.RangeSliderBar2;
import com.yijian.dzpoker.view.TextMoveLayout;
import com.yijian.dzpoker.view.adapter.ControlInApplyAdapter;
import com.yijian.dzpoker.view.data.ApplyInfo;
import com.yijian.dzpoker.view.data.CardInfo;
import com.yijian.dzpoker.view.data.GameUser;
import com.yijian.dzpoker.view.data.PlayerHole;
import com.yijian.dzpoker.view.data.TableInfo;
import com.yijian.dzpoker.view.data.TablePlayerInfo;
import com.yijian.dzpoker.view.data.TableSeatInfo;
import com.yijian.dzpoker.view.data.WaitAction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.tasks.GetEventNotificationTaskMng;
import cn.jpush.im.api.BasicCallback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


@Keep
public class GameActivity extends BaseToolbarActivity {

    private static final String TAG = "GameActivity";
    private RelativeLayout layout_parent;
    private PopupWindow popupWindow, popMenu, popApplyWindow;
    private SocketService.SocketBinder myBinder;
    private String ip;
    private int port;
    private int operation;
    private int gameId;
    private boolean directEnterTable = false;
    private String gameHouseName;
    private boolean flag = false;//控制，初次连接的时候，进行业务操作
    private DzApplication application;
    private ImageView iv_voice, iv_info, iv_menu, iv_apply, iv_game_info;
    private LinearLayout seeNextCard;

    private GameLookBackOnTableFragment gameLookBackOnTableFragment;
    private RealTimeGameRecordFragment realTimeGameRecordFragment;
    private FragmentManager fragmentManager;

    private DrawerLayout drawer_layout;

    //    private HashMap<Integer,User> mTableUser=new HashMap<Integer,User>();//记录进入牌桌的玩家，用户ID为key
//    private HashMap<Integer,Integer> mUserSeat=new HashMap<Integer,Integer>();//seatindex 与 user映射
    private HashMap<Integer, GameUser> mGameUser = new HashMap<Integer, GameUser>();// userid 为key

    private HashMap<Integer, View> mSeatObjects = new HashMap<Integer, View>();//本地座位对象映射
    private HashMap<Integer, Boolean> mSeatOccuped = new HashMap<Integer, Boolean>();//本地座位对象映射
    private HashMap<Integer, View> mChipObjects = new HashMap<Integer, View>();//下注筹码对象映射
    private HashMap<Integer, View> mTipObjects = new HashMap<Integer, View>();//用户最后操作
    private HashMap<Integer, View> mCardBackObjects = new HashMap<Integer, View>();//用户有牌的时候的背景
    private HashMap<Integer, CountDownTimer> mTimerObjects = new HashMap<Integer, CountDownTimer>();//倒计时控件对象
    private HashMap<Integer, View> mTimerViewObjects = new HashMap<Integer, View>();//倒计时的显示
    private HashMap<Integer, View> mHoldSeatTimerViewObjects = new HashMap<Integer, View>();//倒计时的显示
    private HashMap<Integer, View> mPoolViewObjects = new HashMap<Integer, View>();//倒计时的显示
    //private HashMap<Integer,Integer> mUserSeatObject=new HashMap<Integer,Integer>();//用户ID与座位index的映射，比如 1006 坐在1号位置
    private HashMap<Integer, ImageView> mGameCardsObjects = new HashMap<Integer, ImageView>();//公牌
    private HashMap<Integer, RelativeLayout> mMyCardsObjects = new HashMap<Integer, RelativeLayout>();//自己的牌
    private HashMap<Integer, View> mPoolObjects = new HashMap<Integer, View>();//底池
    private HashMap<Integer, View> mShowCardViews= new HashMap<Integer, View>();//底池
    private HashMap<Integer, TextView> mPoolOTextbjects = new HashMap<Integer, TextView>();//底池
    private Map<Integer, UserGameRecordBean> userGameRecordMap = new HashMap<Integer, UserGameRecordBean>();

    private View mD;//庄家
    private View mMessage;//提示用户的信息
    private View mMyCards;//自己的牌，兼容奥马哈4张牌
//    private View mShowCards;//赢牌之后的翻牌
    private View mGameCards;//公牌
    private TextMoveLayout textMoveLayout;
    private TextView button_bottom, button_middle, button_top;//滑动下注的三个显示按钮，无事件操作
    private View mAction;
    private Button mButtonFold, mButtonRaise, mButtonCheck;
    private TextView mTVFold, mTVRaise, mTVCheck;
    private SeekBar seekBarRaise;
    private RelativeLayout blind_layout;
    private LinearLayout raiseLayout;
    private TextView raiseLayoutTopTv;
    private TextView raiseLayoutMoveTv;
    private TextView blind_count;


    private TextView addTimeFee;
    private LinearLayout addTimeLayout;
    private TextView shareCode, tableName, blindCount, assurance, blindGap, nextBlindTime;
    private TextView doubleBlind, tripleBlind, fourBlind;

    private RelativeLayout rootLayout;


    private DisplayHelper displayHelper;

    private TableInfo mTableInfo;//这个是记录牌桌信息的
    public CardInfo[] publicCards = new CardInfo[5];
    private AbsoluteLayout layout_game;
    private int iSeatValue[][];//座位坐标，含用户名，头像，剩余记分牌
    private int iTipLocation[][];//Tip坐标，显示用户的最后一次操作
    private int iAmountChipLocation[][];//用户已下注筹码坐标
    private int iD[][];//庄家坐标
    private int iCardBack[][];//有牌的用户加上这个背景

    private Context mContext;
    private int mSeatViewWidth = 200; //座位
    private int mSeatViewHeight = 200;
    private int mNameTextHeight = 30;
    private int mHeadImageHeight = 80;
    private int mHeadImageWidth = 80;
    private int mTipViewWidth = 60;//Tip view的宽度
    private int mTipViewHeight = 35;
    private int mAmountChipViewWidth = 90;//用户下注 view的宽度
    private int mAmountChipVieHeight = 35;
    private int mDViewWidth = 40;  //显示庄家位的view
    private int mDViewHeight = 40;
    private int mCardBackWidth = 40;
    private int mCardBackHeight = 38;
    // private  int mUserIndex=-1;//用户在游戏中的坐标
    private int mPoolWidth = 320;
    private int mPoolHeight = 300;

    private Button btnStartGame, btnReturnSeat;//开始座位，回到座位按钮
    private View mReturnSeat;
    private int mScreenWidth, mScreenHeight;


    private int minChip = 0;
    private int maxChip = 1000;

    private int step = 1;
    private int maxpaidchips = 0;
    private int actionChip = 0;//下注额
    private float downX, downY;
    private boolean isFirstBuyCore = false;//是否首次购买记分牌
    private boolean isBackToSeatOrIncreaseChips = false;
    private int mWantSeatIndex;

    private List<ApplyInfo> mlistApplyInfo = new ArrayList<ApplyInfo>();
    private RecyclerView rv_apply_info;
    private LinearLayoutManager mLayoutManager;
    private ControlInApplyAdapter mAdapter;


    private SocketService.Callback socketCallback;

    //购买记分牌的弹出窗体
    /*
    tv_buy_coin购买金币
    iv_close  关闭
    tv_message 提示消息
    tv_core 带入记分牌
    seekbar_core  选择记分牌
    tv_coin   个人财富
    tv_service_coin 服务费
    tv_in_coin 已带入/总带入
    button_apply 申请*/
    private TextView tv_buy_coin, tv_message, tv_core, tv_coin, tv_service_coin, tv_in_coin;
    private RangeSliderBar2 seekbar_core;
    private ImageView iv_close;
    private Button button_apply;
    private int takeinchips;        //当前带入筹码
    private int permittakeinchips;  //当前应许带入筹码
    private int tablecreateuserid;

    private MediaPlayer mp;
    private int currentY;
    private CountDownTimer timer;

    private HashMap<Integer, PlayerHole> mPlayerHolebjects = new HashMap<Integer, PlayerHole>();//座位和底牌的映射关系，每一把牌局中的底牌记录

    private NotificationManager notificationManager;//状态栏通知

    private Boolean bInitView = false;


    private int userIconWidth = 40;
    private int userIconHeight = 40;
    private int onSeatTextSize = 24;

    private Message waitActionInfoMsg = null;


    //收到消息之后，进行UI更新
    private final static int MESSAGE_START_GAME = 0x1002;//开始


    private final static int MESSAGE_INFO_TABLE = 0x2001;//收到桌子信息
    private final static int MESSAGE_INFO_SIT_SEAT = 0x2002;//牌局有人坐下
    private final static int MESSAGE_INFO_DOACTION = 0x2003;//牌局操作通知
    private final static int MESSAGE_INFO_START_TABLE = 0x2004;//
    private final static int MESSAGE_INFO_FLOP = 0x2005;//
    private final static int MESSAGE_INFO_TURN = 0x2006;//
    private final static int MESSAGE_INFO_RIVER = 0x2007;//
    private final static int MESSAGE_INFO_HOLE = 0x2008;//底牌
    private final static int MESSAGE_INFO_DO_POT = 0x2009;//底池
    private final static int MESSAGE_INFO_INIT_ROUND = 0x2010;//底牌
    private final static int MESSAGE_INFO_START_ROUND = 0x2011;//底池
    private final static int MESSAGE_INFO_WAIT_ACTION = 0x2012;//等待操作
    private final static int MESSAGE_INFO_SEE_NEXT_CARD = 0x2013;//看牌
    private final static int MESSAGE_INFO_SHOW_CARD = 0x2014;//看赢家的牌
    private final static int MESSAGE_INFO_LEAVE_SEAT = 0x2015;//离开座位

    private final static int MESSAGE_INFO_ADD_CHIPS = 0x2016;//购买筹码
    private final static int MESSAGE_INFO_DO_END = 0x2017;//
    private final static int MESSAGE_INFO_PAUSE_TABLE = 0x2018;//
    private final static int MESSAGE_INFO_LEAVE_TABLE = 0x2019;//
    private final static int MESSAGE_INFO_HOLD_SEAT = 0x2020;//
    private final static int MESSAGE_INFO_BACK_SEAT = 0x2021;//
    private final static int MESSAGE_INFO_DISPOSE_TABLE = 0x2022;//
    private final static int MESSAGE_INFO_TABLE_TIMEOUT = 0x2023;//
    private final static int MESSAGE_INFO_ALARM_TIMEOUT = 0x2024;//
    private final static int MESSAGE_INFO_WIN_27 = 0x2025;//
    private final static int MESSAGE_INFO_DO_ANIMATION = 0x2026;//
    private final static int MESSAGE_INFO_INFO_BUY_SURANCE = 0x2027;//
    private final static int MESSAGE_INFO_TABLE_VOICE = 0x2028;//
    private final static int MESSAGE_INFO_BUY_SURANCE = 0x2029;//
    private final static int MESSAGE_INFO_IF_BUY_ASSURANCE = 0x2030;//
    private final static int MESSAGE_INFO_DO_ASSURANCE = 0x2031;//
    private final static int MESSAGE_INFO_REQUESR_BUY_ASSURANCE = 0x2032;//
    private final static int MESSAGE_RET_BACK_SEAT = 0x2033;//
    private final static int MESSAGE_INFO_RAISE_BLIND = 0x2034;//
    private final static int MESSAGE_INFO_MATCH_STOP = 0x2035;//
    private final static int MESSAGE_INFO_PLAYER_AUTO = 0x2036;//
    private final static int MESSAGE_RET_ADD_WAIT_ACTION_TIME = 0x2037;//
    private final static int MESSAGE_INFO_ADD_WAIT_ACTION_TIME = 0x2038;//
    private final static int MESSAGE_INFO_BEST_HAND = 0x2039;//


    private final static int MESSAGE_DISMISS_POPWINDOW = 0x9001;//去掉购买记分牌界面
    private final static int MESSAGE_DISMISS_POPMENU = 0x9002;//去掉菜单的弹出界面
    private final static int MESSAGE_TAKEIN_INFO = 0x9003;//从后台请求带入信息
    private final static int MESSAGE_APPLY_INFO = 0x9004;//更新带入申请菜单
    private final static int MESSAGE_UPDATE_APPLY_INFO = 0x9005;//更新带入申请菜单的列表数据


    private final static int MESSAGE_UPDATE_RECORD_TIMER = 0x3000;




     /*//客户端向后台发的信息
    public static final String GAME_CREATE_TABLE = "createtable";
    public static final String GAME_START_TABLE = "starttablegame";
    public static final String GAME_PAUSE_TABLE = "pausetablegame";
    public static final String GAME_DISPOSE_TABLE = "disposetable";
    public static final String GAME_ENTER_TABLE = "entertable";
    public static final String GAME_SIT_SEAT = "sitseat";
    public static final String GAME_LEAVE_SEAT = "leaveseat";
    public static final String GAME_HOLD_SEAT = "holdseat";
    public static final String GAME_BACK_SEAT = "backseat";
    public static final String GAME_LEAVE_TABLE = "leavetable";
    public static final String GAME_DO_ACTION = "doaction";
    public static final String GAME_ADD_CHIPS = "addchips";
    public static final String GAME_SEE_NEXT_CARD = "seenextcard";
    public static final String GAME_SHOW_CARD = "showcard";
    public static final String GAME_DO_ANIMATION = "doanimation";
    public static final String GAME_BUY_SURANCE = "buysurance";

    //后台向客户端发送的信息
    public static final String RET_CREATE_TABLE = "createtableret";
    public static final String RET_START_GAME = "startgameret";
    public static final String RET_PAUSE_GAME = "pausegameret";
    public static final String RET_DISPOSE_TABLE = "disposetableret";
    public static final String RET_ENTER_TABLE = "entertableret";
    public static final String RET_SIT_TABLE = "sittableret";
    public static final String RET_LEAVE_SEAT = "leaveseatret";
    public static final String RET_HOLD_SEAT = "holdseatret";
    public static final String RET_BACK_SEAT = "backseatret";
    public static final String RET_LEAVE_TABLE = "leavetableret";
    public static final String RET_DO_ACTION= "doactionret";
    public static final String RET_ADD_CHIP = "addchipret";
    public static final String RET_SHOW_CARD = "showcardret";
    public static final String RET_SEE_NEXT_CARD = "seenextcardret";
    public static final String RET_DO_ANIMATION = "doanimationret";
    public static final String RET_BUY_SURANCE = "buysuranceret";


    public static final String INFO_TABLE_INFO = "inftableinfo";
    public static final String INFO_ACTION = "infaction";
    public static final String INFO_ADD_CHIPS = "infaddchips";
    public static final String INFO_WAIT_ACTION = "infwaitaction";
    public static final String INFO_INIT_ROUND = "infinitround";
    public static final String INFO_START_ROUND = "infstartround";
    public static final String INFO_DO_END = "infdoend";
    public static final String INFO_HOLE = "infhole";

    public static final String INFO_FLOP = "infflop";
    public static final String INFO_TURN = "infturn";
    public static final String INFO_RIVER = "infriver";
    public static final String INFO_START_TABLE = "infstarttablegame";
    public static final String INFO_PAUSE_TABLE = "infpausetablegame";
    public static final String INFO_ENTER_TABLE = "infentertable";
    public static final String INFO_LEAVE_TABLE = "infleavetable";
    public static final String INFO_SIT_SEAT = "infsitseat";
    public static final String INFO_HOLD_SEAT = "infholdseat";
    public static final String INFO_BACK_SEAT = "infbackseat";

    public static final String INFO_LEAVE_SEAT = "infleaveseat";
    public static final String INFO_DISPOSE_TABLE = "disposetable";
    public static final String INFO_TABLE_TIMEOUT = "inftabletimeout";
    public static final String INFO_ALARM_TIMEOUT = "infalarmtimeout";
    public static final String INFO_DO_POT = "infdopot";
    public static final String INFO_SEE_NEXT_CARD = "infseenextcard";
    public static final String INFO_SHOW_CARD  = "infshowcard";
    public static final String INFO_WIN_27 = "infwin27";
    public static final String INFO_DO_ANIMATION = "infdoanimotion";
    public static final String INFO_BUY_SURANCE= "infbuysurance";*/

     private boolean isMatch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayHelper = new DisplayHelper(this);
        //得到屏幕的真实像素宽度高度，用来计算位置
        mScreenWidth = displayHelper.getSCREEN_WIDTH_PIXELS();
        mScreenHeight = displayHelper.getSCREEN_HEIGHT_PIXELS();
        isMatch = getIntent().getBooleanExtra("is_match", false);
        setContentView(R.layout.activity_game);
        getSupportActionBar().hide();

        initUIValues();
        getMyIp();

//        if (BuildConfig.DEBUG) {
//            throw new NullPointerException("NULL");
//        }

        application = (DzApplication) getApplication();
        mContext = getApplicationContext();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        initViews();

        shareCode = layout_parent.findViewById(R.id.share_code);
        blindGap = layout_parent.findViewById(R.id.blind_gap);
        nextBlindTime = layout_parent.findViewById(R.id.blind_next_time);
        tableName = layout_parent.findViewById(R.id.table_name);
        blindCount = layout_parent.findViewById(R.id.blind_count);
        assurance = layout_parent.findViewById(R.id.assurance);


        //获得传来的值
        /*intent.putExtra("operation",1);//1表示创建牌局，2表示加入牌局
                                intent.putExtra("gameid",ssid);
                                intent.putExtra("ip",ip);
                                intent.putExtra("port",port);*/
        Intent intent = getIntent();
        operation = intent.getIntExtra("operation", 0);
        gameId = intent.getIntExtra("gameid", 0);
        ip = intent.getStringExtra("ip");
        port = intent.getIntExtra("port", 0);
        directEnterTable = intent.getBooleanExtra("direct_enter_table", false);
        fragmentManager = getSupportFragmentManager();
        gameLookBackOnTableFragment = (GameLookBackOnTableFragment) fragmentManager.findFragmentById(R.id.fg_right_menu);
        gameLookBackOnTableFragment.setmGametableid(gameId);

        realTimeGameRecordFragment = (RealTimeGameRecordFragment) fragmentManager.findFragmentById(R.id.fg_left_menu);
        realTimeGameRecordFragment.setmGametableid(gameId);

        flag = true;
        //启动service ,绑定service
        Intent startIntent = new Intent(this, SocketService.class);
        startService(startIntent);
        bindService(startIntent, connection, BIND_AUTO_CREATE);
    }

    private void getMyIp() {
        try {
            final DzApplication application = (DzApplication) getApplication();
            GetMyIpApi getMyIpApi = RetrofitApiGenerator.createRequestApi(GetMyIpApi.class);
            JSONObject param = new JSONObject();
            param.put(GetMyIpCons.PARAM_KEY_USERID, application.getUserId());
            Call<ResponseBody> call = getMyIpApi.getResponse(GetMyIpCons.FUNC_NAME, param.toString());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    try {
                        String res = response.body().string();
                        JSONObject jsonObject = new JSONObject(res);
                        application.setIp(jsonObject.optString("ip"));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void initUIValues() {

        onSeatTextSize = UITools.convertDpToPixel(14, this);
        userIconWidth = UITools.convertDpToPixel(50, this);
        userIconHeight = UITools.convertDpToPixel(50, this);
        mSeatViewWidth = UITools.convertDpToPixel(51, this);
        mSeatViewHeight = UITools.convertDpToPixel(108, this);
        mNameTextHeight = UITools.convertDpToPixel(35, this);
        mHeadImageHeight = UITools.convertDpToPixel(50, this);
        mHeadImageWidth = UITools.convertDpToPixel(50, this);
        mTipViewWidth = UITools.convertDpToPixel(30, this);
        mTipViewHeight = UITools.convertDpToPixel(20, this);
        mAmountChipViewWidth = UITools.convertDpToPixel(45, this);
        mAmountChipVieHeight = UITools.convertDpToPixel(17, this);
        mDViewWidth = UITools.convertDpToPixel(20, this);
        mDViewHeight = UITools.convertDpToPixel(20, this);
        mCardBackWidth = UITools.convertDpToPixel(20, this);
        mCardBackHeight = UITools.convertDpToPixel(19, this);
        mPoolWidth = UITools.convertDpToPixel(160, this);
        mPoolHeight = UITools.convertDpToPixel(150, this);
    }

    ImageView anim1, anim2, anim3, anim4, anim5, anim6;
    Dialog dialog;
    private int animToIndex;
    private int animId;

    private void showAnimDialog(int userId, final int index, UserGameRecordBean bean) {
        animToIndex = index;
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.game_table_anim_layout, null);
        TextView chip1 = layout.findViewById(R.id.game_table_anim_chip1);
        TextView chip2 = layout.findViewById(R.id.game_table_anim_chip2);
        TextView chip3 = layout.findViewById(R.id.game_table_anim_chip3);
        TextView chip4 = layout.findViewById(R.id.game_table_anim_chip4);
        TextView chip5 = layout.findViewById(R.id.game_table_anim_chip5);
        TextView chip6 = layout.findViewById(R.id.game_table_anim_chip6);
        Drawable drawableChip = getResources().getDrawable(R.drawable.chip);
        int pixel20 = UITools.convertDpToPixel(20, GameActivity.this);
        drawableChip.setBounds(0, 0, pixel20, pixel20);
        chip1.setCompoundDrawables(drawableChip, null, null, null);
        chip2.setCompoundDrawables(drawableChip, null, null, null);
        chip3.setCompoundDrawables(drawableChip, null, null, null);
        chip4.setCompoundDrawables(drawableChip, null, null, null);
        chip5.setCompoundDrawables(drawableChip, null, null, null);
        chip6.setCompoundDrawables(drawableChip, null, null, null);

        TextView nickName = layout.findViewById(R.id.game_table_anim_dialog_nick_name);
        nickName.setText(mGameUser.get(userId).nickName);
        TextView sign = layout.findViewById(R.id.game_table_anim_dialog_sign);
//        sign.setText(mGameUser.get(userId).);
        TextView id = layout.findViewById(R.id.game_table_anim_dialog_id);
        id.setText("ID : " + mGameUser.get(userId).userId);
        TextView totalMatches = layout.findViewById(R.id.game_table_anim_dialog_total_games);
        totalMatches.setText(bean.getAllgames() + "");
        TextView totalRounds = layout.findViewById(R.id.game_table_anim_dialog_total_hands);
        totalRounds.setText(bean.getAllrounds() + "");
        TextView getInRate = layout.findViewById(R.id.game_table_anim_dialog_get_in_rate);
        getInRate.setText(bean.getAllplaypercent() + "");
        TextView winRate = layout.findViewById(R.id.game_table_anim_dialog_win_rate);
        winRate.setText(bean.getAllwinpercent() + "");
        anim1 = layout.findViewById(R.id.anim_1);
        anim1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                animId = 1;
                sendAnimationRequest(0, index);
//                beginAnimation(index);
            }
        });
        anim2 = layout.findViewById(R.id.anim_2);
        anim2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                animId = 2;
                sendAnimationRequest(1, index);
//                beginAnimation(index);
            }
        });
        anim3 = layout.findViewById(R.id.anim_3);
        anim3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                animId = 3;
                sendAnimationRequest(2, index);
//                beginAnimation(index);
            }
        });
        anim4 = layout.findViewById(R.id.anim_4);
        anim4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                animId = 4;
                sendAnimationRequest(3, index);
//                beginAnimation(index);
            }
        });
        anim5 = layout.findViewById(R.id.anim_5);
        anim5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                animId = 5;
                sendAnimationRequest(4, index);
//                beginAnimation(index);
            }
        });
        anim6 = layout.findViewById(R.id.anim_6);
        anim6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    dialog.dismiss();
                }
                animId = 6;
                sendAnimationRequest(5, index);
//                beginAnimation(index);
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        dialog = builder.create();
        dialog.show();
    }

    private void sendAnimationRequest(int animId, int toIndex) {
        try {
            String msg = Constant.GAME_DO_ANIMATION + "|";
            JSONObject jsonSend = new JSONObject();
            jsonSend.put("userid", application.getUserId());
            jsonSend.put("tableid", gameId);
            jsonSend.put("fromseatindex", getUserIndex());
            jsonSend.put("toseatindex", toIndex);
            jsonSend.put("usechips", 20);
            jsonSend.put("animationid", animId);
            msg += jsonSend.toString().replace("$", "￥");
            msg += "$";
            myBinder.sendInfo(msg);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private Bitmap reSizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 设置想要的大小
        int newWidth = UITools.convertDpToPixel(50, this);
        int newHeight = newWidth;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                true);
        bitmap.recycle();
        bitmap = null;
        return newbm;
    }

    private Map<Integer, ImageView> winnerAnimation = new HashMap<>();
    private Map<Integer, AnimationDrawable> winnerAnimationDrawable = new HashMap<>();

    private void showAllInAnimation(int index) {
        View myHeadView = mSeatObjects.get(index);
        final ImageView shadeView;
        if (winnerAnimation.get(index) == null) {
            shadeView = new ImageView(GameActivity.this);
            int[] position = new int[2];
            int pixel20 = UITools.convertDpToPixel(23, this);
            myHeadView.getLocationOnScreen(position);
            AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(myHeadView.getLayoutParams());
            layoutParams.x = position[0];
            layoutParams.y = position[1] + pixel20;
            shadeView.setLayoutParams(layoutParams);
            int h = UITools.convertDpToPixel(50, this);
            layoutParams.height = h;
            layoutParams.width = h;
//        shadeView.setImageBitmap(reSizeBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.allin_1)));
            shadeView.setBackgroundResource(R.drawable.winner_animation);
            shadeView.setScaleType(ImageView.ScaleType.CENTER);
            shadeView.bringToFront();
            layout_game.addView(shadeView);
            winnerAnimation.put(index, shadeView);
            shadeView.setVisibility(View.VISIBLE);

        } else {
            shadeView = winnerAnimation.get(index);
            shadeView.setVisibility(View.VISIBLE);
        }

        AnimationDrawable animationDrawable;
        if (winnerAnimationDrawable.get(index) == null) {
            animationDrawable = (AnimationDrawable) shadeView.getBackground();
            animationDrawable.setOneShot(false);
            int duration = 0;
            for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
                duration += animationDrawable.getDuration(i);
            }
//        mainThreadHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                shadeView.setVisibility(View.GONE);
//            }
//        }, duration);
            winnerAnimationDrawable.put(index, animationDrawable);
        } else {
            animationDrawable = winnerAnimationDrawable.get(index);
        }
        animationDrawable.start();

    }

    private void showCardsFlyAnimation(View fromView, View toView) {


        ImageView shadeView = new ImageView(GameActivity.this);

        int pixel42 = UITools.convertDpToPixel(42, GameActivity.this);
        int pixel66 = UITools.convertDpToPixel(66, GameActivity.this);
        int pixel5 = UITools.convertDpToPixel(5, GameActivity.this);


        int width;
        int heigth;

        if (fromView == null) {
            WindowManager windowManager =
                    (WindowManager) GameActivity.this.getSystemService(Context.WINDOW_SERVICE);
            width = windowManager.getDefaultDisplay().getWidth() / 2;
            heigth = windowManager.getDefaultDisplay().getHeight() / 2;
        } else {
            int[] position = new int[2];
            fromView.getLocationOnScreen(position);
            Log.e("QIPU", "position : " + position[1] + ", " + position[0]);
            width = position[0];
            heigth = position[1];
        }
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(anim1.getLayoutParams());
        AbsoluteLayout.LayoutParams layoutParams
                = new AbsoluteLayout.LayoutParams(pixel42, pixel66, width, heigth);

        layoutParams.x = width + pixel5;
        layoutParams.y = heigth - pixel5;

        shadeView.setLayoutParams(layoutParams);
        shadeView.bringToFront();

        shadeView.setImageDrawable(getResources().getDrawable(R.drawable.card_bg));

        layout_game.addView(shadeView, layoutParams);
        startCardsFlyAnimation(width, heigth, shadeView, toView);
    }

    private void startCardsFlyAnimation(int fromX, int fromY, final View flyView, final View targetView) {

        int width;
        int heigth;

        if (targetView == null) {
            WindowManager windowManager =
                    (WindowManager) GameActivity.this.getSystemService(Context.WINDOW_SERVICE);
            width = windowManager.getDefaultDisplay().getWidth() / 2;
            heigth = windowManager.getDefaultDisplay().getHeight() / 2;
        } else {
            int[] position = new int[2];
            targetView.getLocationOnScreen(position);
            Log.e("QIPU", "position : " + position[1] + ", " + position[0]);
            width = position[0];
            heigth = position[1];
        }
        int pixel8 = UITools.convertDpToPixel(9, GameActivity.this);
        TranslateAnimation translateAnimation =
                new TranslateAnimation(0, width - fromX, 0, heigth - fromY - pixel8);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                Log.i("QIPU", "onAnimationEnd");
                flyView.setVisibility(View.GONE);
            }
        });
        translateAnimation.setDuration(200);
        flyView.startAnimation(translateAnimation);
    }

    private void showChipsFlyAnimation(View fromView, View toView) {


        ImageView shadeView = new ImageView(GameActivity.this);

        int[] position = new int[2];
        fromView.getLocationOnScreen(position);
        int pixel50 = UITools.convertDpToPixel(50, GameActivity.this);
        int pixel20 = UITools.convertDpToPixel(12, GameActivity.this);
        int pixel5 = UITools.convertDpToPixel(5, GameActivity.this);
        Log.e("QIPU", "position : " + position[1] + ", " + position[0]);
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(anim1.getLayoutParams());
        AbsoluteLayout.LayoutParams layoutParams
                = new AbsoluteLayout.LayoutParams(pixel20, pixel20, position[0], position[1]);

        layoutParams.x = position[0] + pixel5;
        layoutParams.y = position[1] - pixel5;

        shadeView.setLayoutParams(layoutParams);
        shadeView.bringToFront();

        shadeView.setImageDrawable(getResources().getDrawable(R.drawable.chip));

        layout_game.addView(shadeView, layoutParams);
        startChipFlyAnimation(position[0], position[1], shadeView, toView);
    }

    private void startChipFlyAnimation(int fromX, int fromY, final View flyView, final View targetView) {
        final int[] toPosition = new int[2];
        targetView.getLocationOnScreen(toPosition);
        int pixel8 = UITools.convertDpToPixel(9, GameActivity.this);
        TranslateAnimation translateAnimation =
                new TranslateAnimation(0, toPosition[0] - fromX, 0, toPosition[1] - fromY - pixel8);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                Log.i("QIPU", "onAnimationEnd");
                flyView.setVisibility(View.GONE);
            }
        });
        translateAnimation.setDuration(200);
        flyView.startAnimation(translateAnimation);
    }


    private void beginAnimation(int fromIndex, int toIndex, int animationId) {

        View myHeadView = mSeatObjects.get(fromIndex);
        View targetUserView = mSeatObjects.get(toIndex);

        ImageView shadeView = new ImageView(GameActivity.this);

        int[] position = new int[2];
        myHeadView.getLocationOnScreen(position);
        Log.e("QIPU", "position : " + position[1] + ", " + position[0]);
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(anim1.getLayoutParams());
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(myHeadView.getLayoutParams());
        int pixel50 = UITools.convertDpToPixel(50, GameActivity.this);
        layoutParams.x = position[0];
        layoutParams.y = position[1] - pixel50;

        shadeView.setLayoutParams(layoutParams);
        shadeView.bringToFront();

        switch (animationId + 1) {
            case 1:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_1));
                break;
            case 2:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_2));
                break;
            case 3:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_3));
                break;
            case 4:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_4));
                break;
            case 5:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_5));
                break;
            case 6:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_6));
                break;
        }
        layout_game.addView(shadeView, layoutParams);
        setAnimation4(position[0], position[1], shadeView, targetUserView, animationId + 1);
    }


    private void beginAnimation() {

        View myHeadView = mSeatObjects.get(getUserIndex());
        View targetUserView = mSeatObjects.get(animToIndex);

        ImageView shadeView = new ImageView(GameActivity.this);

        int[] position = new int[2];
        myHeadView.getLocationOnScreen(position);
        Log.e("QIPU", "position : " + position[1] + ", " + position[0]);
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(anim1.getLayoutParams());
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(myHeadView.getLayoutParams());
        int pixel50 = UITools.convertDpToPixel(50, GameActivity.this);
        layoutParams.x = position[0];
        layoutParams.y = position[1] - pixel50;

        shadeView.setLayoutParams(layoutParams);
        shadeView.bringToFront();

        switch (animId) {
            case 1:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_1));
                break;
            case 2:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_2));
                break;
            case 3:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_3));
                break;
            case 4:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_4));
                break;
            case 5:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_5));
                break;
            case 6:
                shadeView.setImageDrawable(getResources().getDrawable(R.drawable.anim_6));
                break;
        }
        layout_game.addView(shadeView, layoutParams);
        setAnimation4(position[0], position[1], shadeView, targetUserView, animId);
    }

    public void setAnimation4(int fromx, int fromy, final View v, final View targetView, final int animationId) {
        Random random = new Random();

        int[] fromPosition = new int[2];
        final int[] toPosition = new int[2];
        targetView.getLocationOnScreen(toPosition);
        v.getLocationOnScreen(fromPosition);
        int pixel50 = UITools.convertDpToPixel(25, GameActivity.this);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, toPosition[0] - fromx, 0,

                toPosition[1] - fromy + pixel50);
//        TranslateAnimation translateAnimation = new TranslateAnimation(
//                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, toPosition[0],
//                Animation.ABSOLUTE, 0, Animation.ABSOLUTE, toPosition[1]);
        int flag = Math.max(Math.abs(toPosition[0] - fromx), Math.abs(toPosition[1] - fromy));
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                Log.i("QIPU", "onAnimationEnd");
                //在动画结束时，重新启动动画
//                setAnimation4(icon);
                v.setVisibility(View.GONE);
                final ImageView animView = new ImageView(GameActivity.this);

                AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(targetView.getLayoutParams());
                int pixel50 = UITools.convertDpToPixel(25, GameActivity.this);
                layoutParams.x = toPosition[0];
                layoutParams.y = toPosition[1] - pixel50;

                animView.setLayoutParams(layoutParams);
                animView.bringToFront();
                layout_game.addView(animView, layoutParams);

//                animView.setImageDrawable(getResources().getDrawable(R.drawable.anim_1));
                switch (animationId) {
                    case 1:
                        animView.setBackgroundResource(R.drawable.throw_anim_1);
                        mp = MediaPlayer.create(GameActivity.this, R.raw.anim_1_audio);
                        mp.start();
                        break;
                    case 2:
                        animView.setBackgroundResource(R.drawable.throw_anim_2);
                        mp = MediaPlayer.create(GameActivity.this, R.raw.anim_2_audio);
                        mp.start();
                        break;
                    case 3:
                        animView.setBackgroundResource(R.drawable.throw_anim_3);
                        mp = MediaPlayer.create(GameActivity.this, R.raw.anim_3_audio);
                        mp.start();
                        break;
                    case 4:
                        animView.setBackgroundResource(R.drawable.throw_anim_4);
                        mp = MediaPlayer.create(GameActivity.this, R.raw.anim_4_audio);
                        mp.start();
                        break;
                    case 5:
                        animView.setBackgroundResource(R.drawable.throw_anim_5);
                        mp = MediaPlayer.create(GameActivity.this, R.raw.anim_5_audio);
                        mp.start();
                        break;
                    case 6:
                        animView.setBackgroundResource(R.drawable.throw_anim_6);
                        mp = MediaPlayer.create(GameActivity.this, R.raw.anim_6_audio);
                        mp.start();
                        break;
                }
                AnimationDrawable animationDrawable = (AnimationDrawable) animView.getBackground();
                animationDrawable.setOneShot(true);
                int duration = 0;
                for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
                    duration += animationDrawable.getDuration(i);
                }
                mainThreadHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animView.setVisibility(View.GONE);
                    }
                }, duration);
                animationDrawable.start();


            }
        });
        translateAnimation.setDuration(getDuration(flag));
        v.startAnimation(translateAnimation);
    }

    //处理实现匀速
    public int getDuration(int flag) {
        return 500;
    }

    private void getUserInfo(final int toIndex, final int userId) {
        GetUserGameRecordApi getUserGameRecordApi = RetrofitApiGenerator.createRequestApi(GetUserGameRecordApi.class);

        try {
            final JSONObject params = new JSONObject();
            params.put(GetUserGameRecordCons.PARAM_KEY_USERID, userId);

            Call<UserGameRecordBean> call = getUserGameRecordApi.getResponse(GetUserGameRecordCons.FUNC_NAME, params.toString());
            call.enqueue(new Callback<UserGameRecordBean>() {
                @Override
                public void onResponse(Call<UserGameRecordBean> call, retrofit2.Response<UserGameRecordBean> response) {
//                    Logger.d(TAG, "onResponse : " + response.body());
                    UserGameRecordBean bean = response.body();
                    userGameRecordMap.put(userId, bean);
                    showAnimDialog(userId, toIndex, bean);

                }

                @Override
                public void onFailure(Call<UserGameRecordBean> call, Throwable t) {

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "exception e : " + e);
        }
    }

    class VoiceTouch implements View.OnTouchListener {

        private float down_y;
        private boolean isCanceled = false;
        private CountDownTimer timer;
        TextView recordCountDown;
        TextView moveUpCancel;
        private Handler mainHandler;
        private boolean hasPermission = false;

        public VoiceTouch() {
            mainHandler = new Handler(getMainLooper()) {

                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MESSAGE_UPDATE_RECORD_TIMER:
                            recordCountDown.setText(msg.obj + "");
                            break;
                    }
                }
            };

            timer = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Message message = new Message();
                    message.what = MESSAGE_UPDATE_RECORD_TIMER;
                    message.obj = millisUntilFinished / 1000;
                    mainHandler.sendMessage(message);
                }

                @Override
                public void onFinish() {
                    RecordManager.getInstance().stopRecord();
                    Intent intentq = new Intent();
                    intentq.setClass(GameActivity.this, RecordService.class);
                    stopService(intentq);
                    Log.i("record_test", "upload");
                    recordCountDown.setVisibility(View.GONE);
                    moveUpCancel.setVisibility(View.GONE);
                    String fileName = "tmp.wav";
                    String filePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    filePath += "/DzPoker/" + fileName;
                    File uploadFile = new File(filePath);
                    if (uploadFile.exists()) {
                        uploadAudioFile(uploadFile);
                    }

                }
            };
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (ContextCompat.checkSelfPermission(GameActivity.this, Manifest.permission.RECORD_AUDIO)
                            == PackageManager.PERMISSION_GRANTED) {


                        int width = UITools.convertDpToPixel(60, GameActivity.this);
                        int height = UITools.convertDpToPixel(40, GameActivity.this);
                        int pixle80 = UITools.convertDpToPixel(80, GameActivity.this);
                        int pixle64 = UITools.convertDpToPixel(64, GameActivity.this);
                        int pixle20 = UITools.convertDpToPixel(20, GameActivity.this);
                        int pixle14 = UITools.convertDpToPixel(16, GameActivity.this);
                        int pixle12 = UITools.convertDpToPixel(12, GameActivity.this);

                        recordCountDown = new TextView(GameActivity.this);
                        recordCountDown.setBackgroundResource(R.drawable.pool_bg1);
                        recordCountDown.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixle14);
                        recordCountDown.setTextColor(Color.WHITE);
                        recordCountDown.setText("10");
                        recordCountDown.setGravity(Gravity.CENTER);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        layoutParams.setMargins(pixle20, 0, 0, pixle80);
                        layoutParams.addRule(RelativeLayout.ABOVE, iv_voice.getId());
                        recordCountDown.setLayoutParams(layoutParams);
                        layout_parent.addView(recordCountDown, layoutParams);
                        recordCountDown.setVisibility(View.VISIBLE);
                        recordCountDown.bringToFront();

                        moveUpCancel = new TextView(GameActivity.this);
                        moveUpCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixle12);
                        moveUpCancel.setTextColor(Color.WHITE);
                        moveUpCancel.setText("上滑取消录音");
                        moveUpCancel.setGravity(Gravity.CENTER);
                        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    layoutParams1.addRule(RelativeLayout.BELOW, recordCountDown.getId());
                        layoutParams1.addRule(RelativeLayout.ALIGN_LEFT, recordCountDown.getId());
                        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        moveUpCancel.setLayoutParams(layoutParams1);
                        layout_parent.addView(moveUpCancel, layoutParams1);
                        layoutParams1.setMargins(pixle20, 0, 0, pixle64);
                        moveUpCancel.setVisibility(View.VISIBLE);
                        moveUpCancel.bringToFront();

                        down_y = motionEvent.getY();
                        Intent intent = new Intent();
                        intent.setClass(GameActivity.this, RecordService.class);
                        startService(intent);
                        timer.start();
                        hasPermission = true;
                    } else {
                        String[] permisions = {Manifest.permission.RECORD_AUDIO};
                        ActivityCompat.requestPermissions(GameActivity.this, permisions, 100);
                        hasPermission = false;
                    }

                    break;
                case MotionEvent.ACTION_UP:
//                    RecordManager.getInstance().stopRecord();
//                    Intent intent = new Intent();
//                    intent.setClass(GameActivity.this, RecordService.class);
//                    intent.putExtra("stop", true);
//                    startService(intent);
                    Intent intentq = new Intent();
                    intentq.setClass(GameActivity.this, RecordService.class);
                    stopService(intentq);

                    if (hasPermission) {
                        if (null != recordCountDown && null != moveUpCancel) {
                            recordCountDown.setVisibility(View.GONE);
                            moveUpCancel.setVisibility(View.GONE);
                        }
                        if (null != timer) {
                            timer.cancel();
                        }


//                        String fileName = "tmp.wav";
//                        String filePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
//                        filePath += "/DzPoker/" + fileName;
//                        File uploadFile = new File(filePath);

                        String fileName = "tmp.wav";
//            filePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                        String filePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                        File uploadFile = new File(filePath, fileName);
                        if (isCanceled) {
                            if (uploadFile.exists()) {
                                uploadFile.delete();
                            }
                            Log.i("record_test", "cancel");
                        } else {
                            if (uploadFile.exists()) {
                                uploadAudioFile(uploadFile);
                                Log.i("record_test", "upload");
                            }

                        }
                    }

                    break;
                case MotionEvent.ACTION_CANCEL: // 首次开权限时会走这里，录音取消
                    recordCountDown.setVisibility(View.GONE);
                    moveUpCancel.setVisibility(View.GONE);
//                    RecordManager.getInstance().stopRecord();
                    timer.cancel();
                    Log.i("record_test", "权限影响录音录音");
                    isCanceled = true;
                    break;

                case MotionEvent.ACTION_MOVE: // 滑动手指
                    float moveY = motionEvent.getY();
                    if (down_y - moveY > 100) {
                        isCanceled = true;
                    }
                    break;

            }
            return true;
        }

    }

    private void uploadAudioFile(final File file) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                UploadFileUtils.uploadFile(file, null);
//            }
//        }).start();

        try {

//            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
//            builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"userfile1\";filename=\"tmp.wav\""), RequestBody.create(MediaType.parse("audio/wav"), file));
//            RequestBody body = builder.build();
//
//            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
//
//            String fileName = file.getName();
//                                /* form的分割线,自己定义 */
//            String boundary = "xx--------------------------------------------------------------xx";
//            MultipartBody mBody = new MultipartBody.Builder(boundary).setType(MultipartBody.FORM)
//                                /* 底下上传文件 */
//                    .addFormDataPart("file", fileName, fileBody)
//                    .build();
//
//                             /* 下边的就和post一样了 */
//            Request request = new Request.Builder().url(getString(R.string.url_upload)).post(body).build();
            //同步取数据

//            Response response = DzApplication.getHttpClient().newCall(request).execute();
//            if (!response.isSuccessful()) {
//                ToastUtil.showToastInScreenCenter(GameActivity.this, "上传头像失败，请稍后重试!错误原因为：" + response.body().string());
//                return;
//            }

//

//            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
//            RequestBody requestBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart("application/octet-stream", file.getName(), fileBody)
//                    .build();
//            Request request = new Request.Builder()
//                    .url("http://106.14.221.253:85/uploadfile.aspx")
//                    .post(requestBody)
//                    .build();
//
//            DzApplication.getHttpClient().newCall(request).enqueue(new okhttp3.Callback() {
//
//                @Override
//                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                    try {
////                        Log.v("Upload", "success : " + response.body().string());
////                        file.delete();
//                        JSONObject jsonObject = new JSONObject(response.body().string());
//                        try {
//                            String msg = Constant.TABLE_VOICE + "|";
//                            JSONObject jsonSend = new JSONObject();
//                            jsonSend.put("userid", application.getUserId());
//                            jsonSend.put("tableid", gameId);
//                            jsonSend.put("voicepath", jsonObject.optString("path"));
//                            jsonSend.put("rate", 8000);
//                            jsonSend.put("code", 16);
//                            msg += jsonSend.toString().replace("$", "￥");
//                            msg += "$";
//                            myBinder.sendInfo(msg);
//                        } catch (Throwable e) {
//                            e.printStackTrace();
//                        }
//
//                    } catch (Throwable e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(okhttp3.Call call, IOException e) {
//
//                }
//
//            });


//
            UploadAudioFileApi uploadAudioFileApi = RetrofitApiGenerator.createRequestApi(UploadAudioFileApi.class);
            // 创建 RequestBody，用于封装构建RequestBody
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file);

// MultipartBody.Part  和后端约定好Key，这里的partName是用image
//            MultipartBody.Part body =
//                    MultipartBody.Part.create(requestFile);

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("uploadwav", file.getName(), requestFile);

// 添加描述
            String descriptionString = "hello, 这是文件描述";
            RequestBody description =
                    RequestBody.create(
                            MediaType.parse("multipart/form-data"), descriptionString);

// 执行请求
            Call<ResponseBody> call = uploadAudioFileApi.upload(body);
            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    try {
//                        Log.v("Upload", "success : " + response.body().string());
//                        file.delete();
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        try {
                            String msg = Constant.TABLE_VOICE + "|";
                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put("userid", application.getUserId());
                            jsonSend.put("tableid", gameId);
                            jsonSend.put("voicepath", jsonObject.optString("path"));
                            jsonSend.put("rate", 8000);
                            jsonSend.put("code", 16);
                            msg += jsonSend.toString().replace("$", "￥");
                            msg += "$";
                            myBinder.sendInfo(msg);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Upload error:", t.getMessage());
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("WrongViewCast")
    private void initViews() {

        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                Gravity.END);

        drawer_layout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {

            }

            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {
                drawer_layout.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        layout_parent = (RelativeLayout) findViewById(R.id.layout_main);
        //此处设置成clickable =false,在INFOtable中放开，怕接受服务器消息的时间差
        iv_voice = (ImageView) findViewById(R.id.iv_voice);
        addTimeFee = (TextView) findViewById(R.id.game_table_add_time_fee);
        addTimeLayout = (LinearLayout) findViewById(R.id.game_table_add_time_layout);
        addTimeFee.setVisibility(View.INVISIBLE);
        addTimeLayout.setVisibility(View.INVISIBLE);
        addTimeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String msg = Constant.GAME_ADD_WAIT_ACTION_TIME + "|";
                    JSONObject jsonSend = new JSONObject();
                    jsonSend.put("userid", application.getUserId());
                    jsonSend.put("tableid", gameId);
                    jsonSend.put("seatindex", getUserIndex());
                    jsonSend.put("addtime", 20);
                    msg += jsonSend.toString().replace("$", "￥");
                    msg += "$";
                    myBinder.sendInfo(msg);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String lastAddTimeFee = addTimeFee.getText().toString();
                                if ("免费".equalsIgnoreCase(lastAddTimeFee)) {
                                    addTimeFee.setText(String.valueOf(5));
                                } else {
                                    addTimeFee.setText((Integer.valueOf(lastAddTimeFee) * 2) + "");
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
//        iv_voice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent();
//                intent.setClass(GameActivity.this, RecordService.class);
//                startService(intent);
//            }
//        });
        seeNextCard = (LinearLayout) findViewById(R.id.see_next_card);
        seeNextCard.setVisibility(View.INVISIBLE);
        iv_voice.setOnTouchListener(new VoiceTouch());
        iv_info = (ImageView) findViewById(R.id.iv_info);
        iv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer_layout.openDrawer(Gravity.LEFT);
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                        Gravity.START);
                realTimeGameRecordFragment.refreshUI();
            }
        });

        iv_menu = (ImageView) findViewById(R.id.iv_menu);
        iv_menu.setClickable(false);

        iv_apply = (ImageView) findViewById(R.id.iv_apply);
        iv_apply.setClickable(false);
        iv_game_info = (ImageView) findViewById(R.id.iv_game_info);
        iv_game_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer_layout.openDrawer(Gravity.RIGHT);
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                        Gravity.END);
                gameLookBackOnTableFragment.refreshUI();
            }
        });


        iv_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getTakeInChipsFromServer();

                // ToastUtil.showToastInScreenCenter(GameActivity.this,"hahahahah1");
            }
        });

        //点击弹出申请界面
        iv_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先到后台去取数据
                //发送申请
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //拼装url字符串

                            DzApplication applicatoin = (DzApplication) getApplication();
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("userid", applicatoin.getUserId());
                            jsonObj.put("tableid", gameId);


                            String strURL = getString(R.string.url_remote);
                            strURL += "func=getrequesttakeininfo&param=" + jsonObj.toString();

                            URL url = new URL(strURL);
                            Request request = new Request.Builder().url(strURL).build();
                            Response response = DzApplication.getHttpClient().newCall(request).execute();
                            String result = response.body().string();
                            //{"requesttakeininfo":[{"requestid":16,"requestuserid":1008,"usernickname":"我是无名","requesttakeinchips":200,"requestpermittakeinchips":200,"ispermit":2}]}
                            JSONObject jsonObject = new JSONObject(result);
                            mlistApplyInfo = new ArrayList<ApplyInfo>();
                            JSONArray jsonApplyArray = new JSONArray(jsonObject.getString("requesttakeininfo"));
                            for (int i = 0; i < jsonApplyArray.length(); i++) {
                                JSONObject jsonApplyInfo = new JSONObject(jsonApplyArray.get(i).toString());
                                ApplyInfo applyInfo = new ApplyInfo();
                                applyInfo.tableId = gameId;
                                applyInfo.tablename = gameHouseName;
                                applyInfo.requestid = jsonApplyInfo.getInt("requestid");
                                applyInfo.requestuserid = jsonApplyInfo.getInt("requestuserid");
                                applyInfo.usernickname = jsonApplyInfo.getString("usernickname");
                                applyInfo.requesttakeinchips = jsonApplyInfo.getInt("requesttakeinchips");
                                applyInfo.requestpermittakeinchips = jsonApplyInfo.getInt("requestpermittakeinchips");
                                applyInfo.ispermit = jsonApplyInfo.getInt("ispermit");
                                mlistApplyInfo.add(applyInfo);
//                                mlistApplyInfo.add(applyInfo);
//                                mlistApplyInfo.add(applyInfo);
                            }
                            mainThreadHandler.sendEmptyMessage(MESSAGE_APPLY_INFO);

                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "查询申请带入异常，请稍后重试!" + e.toString());
                        }

                    }
                });
                thread.start();


            }
        });


        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View contentView = getPopupMenuWindowContentView();
                //根据值来设置菜单
                //先判断是否坐下，根据muserindex
                if (getUserIndex() == -1 || isMatch) {
                    //没有坐下
                    View view = contentView.findViewById(R.id.layout_4);
                    view.setVisibility(View.GONE);
                    view = contentView.findViewById(R.id.layout_5);
                    view.setVisibility(View.GONE);
                    view = contentView.findViewById(R.id.layout_6);
                    view.setVisibility(View.GONE);
                }
//                if (mTableInfo.createuserid != application.getUserId()) {
//                    View view = contentView.findViewById(R.id.layout_7);
//                    view.setVisibility(View.GONE);
//                }
                //规则说明
                TextView tv_1 = contentView.findViewById(R.id.tv_1);
                tv_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);

                    }
                });

                //设置
                TextView tv_2 = contentView.findViewById(R.id.tv_2);
                tv_2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);

                    }
                });

                //商城
                TextView tv_3 = contentView.findViewById(R.id.tv_3);
                tv_3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);

                    }
                });

                //补充记分牌
                TextView tv_4 = contentView.findViewById(R.id.tv_4);
                tv_4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);
                        //调用请求
                        isFirstBuyCore = false;
                        getTakeInChipsFromServer();
                        isBackToSeatOrIncreaseChips = true;

                    }
                });

                //站起围观
                TextView tv_5 = contentView.findViewById(R.id.tv_5);
                tv_5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /* public class LeaveSeatParam
                        {
                            public int userid;
                            public int tableid;
                            public int seatindex;
                        }*/
                        mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);
                        try {
                            //ToastUtil.showToastInScreenCenter(GameActivity.this,v.getTag().toString());
                            String msg = Constant.GAME_LEAVE_SEAT + "|";

                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put("userid", application.getUserId());
                            jsonSend.put("tableid", gameId);
                            jsonSend.put("seatindex", getUserIndex());
                            msg += jsonSend.toString().replace("$", "￥");
                            msg += "$";
                            myBinder.sendInfo(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "离开座位出错！");

                        }


                    }
                });

                //保位留桌
                TextView tv_6 = contentView.findViewById(R.id.tv_6);
                tv_6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);
                        try {
                            //ToastUtil.showToastInScreenCenter(GameActivity.this,v.getTag().toString());
                            String msg = Constant.GAME_HOLD_SEAT + "|";

                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put("userid", application.getUserId());
                            jsonSend.put("tableid", gameId);
                            jsonSend.put("seatindex", getUserIndex());
                            jsonSend.put("holdseconds", 0);
                            msg += jsonSend.toString().replace("$", "￥");
                            msg += "$";
                            myBinder.sendInfo(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "离开座位出错！");

                        }


                    }
                });

                //解散房间
//                TextView tv_7 = contentView.findViewById(R.id.tv_7);
//                tv_7.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);
//                        new AlertDialog.Builder(GameActivity.this).setTitle("系统提示")//设置对话框标题
//
//                                .setMessage("确认解散牌局？")//设置显示的内容
//
//                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
//
//                                    @Override
//
//                                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
//
//                                        try {
//                                            //ToastUtil.showToastInScreenCenter(GameActivity.this,v.getTag().toString());
//                                            String msg = Constant.GAME_DISPOSE_TABLE + "|";
//                                            JSONObject jsonSend = new JSONObject();
//                                            jsonSend.put("userid", application.getUserId());
//                                            jsonSend.put("tableid", gameId);
//                                            msg += jsonSend.toString().replace("$", "￥");
//                                            msg += "$";
//                                            myBinder.sendInfo(msg);
//                                        } catch (Exception e) {
//                                            ToastUtil.showToastInScreenCenter(GameActivity.this, "解散房间出错！");
//
//                                        }
//                                    }
//
//                                }).setNegativeButton("返回", new DialogInterface.OnClickListener() {//添加返回按钮
//
//                            @Override
//
//                            public void onClick(DialogInterface dialog, int which) {//响应事件
//
//                                // TODO Auto-generated method stub
//
//                                Log.i("alertdialog", " 请保存数据！");
//
//                            }
//
//                        }).show();//在按键响应事件中显示此对话框
//
//
//                    }
//                });

                //退出牌局
                TextView tv_8 = contentView.findViewById(R.id.tv_8);
                tv_8.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);
                            //ToastUtil.showToastInScreenCenter(GameActivity.this,v.getTag().toString());
                            String msg = Constant.GAME_LEAVE_TABLE + "|";
                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put("userid", application.getUserId());
                            jsonSend.put("tableid", gameId);
                            msg += jsonSend.toString().replace("$", "￥");
                            msg += "$";
                            myBinder.sendInfo(msg);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
//                            ToastUtil.showToastInScreenCenter(GameActivity.this, "离开座位出错！");

                        }

                    }
                });


                int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                contentView.measure(width, height);
                int height1 = contentView.getMeasuredHeight();
                int width1 = contentView.getMeasuredWidth();
                popMenu = new PopupWindow(contentView, width1, height1, true);

                popMenu.setFocusable(true);
                // 设置允许在外点击消失
                popMenu.setOutsideTouchable(true);
                // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
                popMenu.setBackgroundDrawable(new ColorDrawable());
                // 设置好参数之后再show
                popMenu.showAsDropDown(v);
                //popMenu.showAtLocation(GameActivity.this.getWindow().getDecorView(), Gravity.CENTER,0,0);

                //contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                //int xOffset = parent.getWidth()  - contentView.getMeasuredWidth() ;
                //popupWindow.showAsDropDown(parent,xOffset,20);    // 在mButton2的中间显示
                // int windowPos[] = calculatePopWindowPos((View)parent, contentView);
                //popupWindow.showAtLocation(GameActivity.this.getWindow().getDecorView(), Gravity.CENTER,0,0);
                // popupWindow.showAtLocation(parent,Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);
            }
        });

        layout_game = (AbsoluteLayout) findViewById(R.id.layout_game);

        //先根据最大数目9来创建控件,此处，将创建和展示分开，9个座位可以先展示
        int maxNumber = 9;

        iSeatValue = new int[maxNumber][2];
        iTipLocation = new int[maxNumber][2];//大小盲注,用户操作的展现位置
        iAmountChipLocation = new int[maxNumber][2];//已下注金额的展现位置
        iD = new int[maxNumber][2];//庄家D位置
        iCardBack = new int[maxNumber][2];//有牌的用户背景

        initSeatXY(maxNumber);

        for (int i = 0; i < maxNumber; i++) {

            //xml中写死了宽度和高度，忽略名字长短引起的变化

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.layout_seat, null);
            //iv_user_head  //tv_user_name tv_goldcoin
            final CustomCircleImageView iv = view.findViewById(R.id.iv_user_head);
            iv.post(new Runnable() {
                @Override
                public void run() {

                }
            });
            TextView counDownTimer = view.findViewById(R.id.seat_countdown_timer_tv);
            counDownTimer.setVisibility(View.INVISIBLE);
            ImageView recordAnimation = view.findViewById(R.id.seat_record_animation);
            recordAnimation.setVisibility(View.INVISIBLE);
            TextView autoPlay = view.findViewById(R.id.seat_auto_play_tv);
            autoPlay.setVisibility(View.INVISIBLE);
            TextView tv_name = view.findViewById(R.id.tv_user_name);
            TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);
            TextView winChips = view.findViewById(R.id.win_chips);
            winChips.setVisibility(View.INVISIBLE);
            ImageView winIcon = view.findViewById(R.id.win_icon);
            winIcon.setVisibility(View.INVISIBLE);
            tv_name.setVisibility(View.INVISIBLE);//INVISIBLE继续占用布局空间
            tv_goldcoin.setVisibility(View.INVISIBLE);
            iv.setImageBitmap(getEmptySeatBitMap(userIconWidth, userIconHeight, "空位"));
            iv.setTag(i + "");
            //此处响应事件在需要的时候加
            final int finalI = i;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //给空位置加上响应，这个后面做坐下的时候来处理
//                    int iRealIndex;
//                    if (mUserIndex==-1){
//                        iRealIndex=(int)v.getTag();
//                    }else{
//                        iRealIndex=((int)v.getTag()-mUserIndex+mTableInfo.seats.length)%mTableInfo.seats.length;
//                    }


                    if (mSeatOccuped.get(finalI)) {
                        for (Map.Entry<Integer, GameUser> entry : mGameUser.entrySet()) {
                            GameUser user = entry.getValue();
                            if (user.seatindex == finalI) {
                                getUserInfo(finalI, user.userId);
                            }
                        }
//                        showAnimDialog(finalI);
                    } else {

                        if (isMatch) {
                            return;
                        }

                        //发送坐下
                        try {
                            //ToastUtil.showToastInScreenCenter(GameActivity.this,v.getTag().toString());
                            String msg = Constant.GAME_SIT_SEAT + "|";
                        /* public class SitSeatParam
                        {
                            public int userid;
                            public int tableid;
                            public int seatindex;
                            public int intochips;
                            public string ip;
                            public double gpsx;
                            public double gpsy;
                        }*/
                        if (mTableInfo.isgpsrestrict) {
                            if (ContextCompat.checkSelfPermission(GameActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                String[] permisions = {Manifest.permission.ACCESS_FINE_LOCATION};
                                ActivityCompat.requestPermissions(GameActivity.this, permisions, 101);
//                                return;
                            }
                        }

                            String ip = application.getIp();
                            if (TextUtils.isEmpty(ip)) {
                                ip = Util.getLocalIpAddress();
                            }
                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put("userid", application.getUserId());
                            jsonSend.put("tableid", gameId);
                            mWantSeatIndex = Integer.parseInt((String) v.getTag());
                            jsonSend.put("seatindex", mWantSeatIndex);
                            jsonSend.put("intochips", 0);
                            jsonSend.put("ip", ip);
                            jsonSend.put("gpsx", application.getLongitude());
                            jsonSend.put("gpsy", application.getLatitude());
                            msg += jsonSend.toString().replace("$", "￥");
                            msg += "$";
                            myBinder.sendInfo(msg);
//                            mSeatOccuped.put(finalI, true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "坐下座位出错！");

                        }
                    }

                }
            });
            setPosition(view, mSeatViewWidth, mSeatViewHeight, iSeatValue[i][0], iSeatValue[i][1]);

            view.setTag(i + "");
            layout_game.addView(view);
            mSeatObjects.put(i, view);//记录view对象
            mSeatOccuped.put(i, false);
//            final Handler mainHandler = new Handler(getMainLooper());
//            final Runnable drawCicle = new Runnable() {
//                @Override
//                public void run() {
////                    iv.setStartAnimation(true, 0);
//                    iv.startAnimation();
////                    mainThreadHandler.postDelayed(this, 500);
//                }
//            };
//            mainThreadHandler.postDelayed(drawCicle, 100);
        }

        //开始按钮
        addStartButon();
//        //回到座位按钮
//        addReturnSeatButton();
        //信息布局
        addLayoutMessage();
        //增加5张公牌
        addGameCards();
//        mGameCards.setVisibility(View.VISIBLE);
        ImageView iv7 = mGameCards.findViewById(R.id.iv_card1);
        mGameCardsObjects.put(0, iv7);
        ImageView iv8 = mGameCards.findViewById(R.id.iv_card2);
        mGameCardsObjects.put(1, iv8);
        ImageView iv9 = mGameCards.findViewById(R.id.iv_card3);
        mGameCardsObjects.put(2, iv9);
        ImageView iv10 = mGameCards.findViewById(R.id.iv_card4);
        mGameCardsObjects.put(3, iv10);
        ImageView iv11 = mGameCards.findViewById(R.id.iv_card5);
        mGameCardsObjects.put(4, iv11);


        // 增加自己的牌
        addMyCards();
        RelativeLayout iv1 = mMyCards.findViewById(R.id.mycard_iv_card1_layout);
        mMyCardsObjects.put(0, iv1);
        RelativeLayout iv2 = mMyCards.findViewById(R.id.mycard_iv_card2_layout);
        mMyCardsObjects.put(1, iv2);
        RelativeLayout iv3 = mMyCards.findViewById(R.id.mycard_iv_card3_layout);
        mMyCardsObjects.put(2, iv3);
        RelativeLayout iv4 = mMyCards.findViewById(R.id.mycard_iv_card4_layout);
        mMyCardsObjects.put(3, iv4);

//        setPosition(mShowCards,120,80,iSeatValue[2][0],iSeatValue[2][1]+mNameTextHeight);
//        ImageView iv5=(ImageView)mShowCards.findViewById(R.id.iv_card1);
//        iv5.setImageBitmap(drawSingleCard(3,11));
//        ImageView iv6=(ImageView)mShowCards.findViewById(R.id.iv_card2);
//        iv6.setImageBitmap(drawSingleCard(2,9));

        //加筹码位置
        for (int i = 0; i < maxNumber; i++) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View viewChip = inflater.inflate(R.layout.layout_chip, null);
            TextView tv = viewChip.findViewById(R.id.tv_chip);
            tv.setText("000");
            setPosition(viewChip, mAmountChipViewWidth, mAmountChipVieHeight, iAmountChipLocation[i][0], iAmountChipLocation[i][1]);
            layout_game.addView(viewChip);
            mChipObjects.put(i, viewChip);
            viewChip.setVisibility(View.INVISIBLE);
        }

        //cardback
        for (int i = 0; i < maxNumber; i++) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.layout_cardback, null);
            ImageView ivCardBack = view.findViewById(R.id.iv_cardback);
            ivCardBack.setImageBitmap(drawCardBack());
            setPosition(view, mCardBackWidth, mCardBackHeight, iCardBack[i][0], iCardBack[i][1]);
            layout_game.addView(view);
            mCardBackObjects.put(i, view);
            view.setVisibility(View.INVISIBLE);
        }


        //tip
        for (int i = 0; i < maxNumber; i++) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.layout_tip, null);
            TextView tv_tip = view.findViewById(R.id.tv_tip);
            tv_tip.setText("盲注");
            setPosition(view, mTipViewWidth, mTipViewHeight, iTipLocation[i][0], iTipLocation[i][1]);
            layout_game.addView(view);
            mTipObjects.put(i, view);
            view.setVisibility(View.INVISIBLE);
        }

        //增加赢家翻牌
//        addshowcards();

        for (int i = 0; i < maxNumber; i++) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View showCardView = inflater.inflate(R.layout.layout_showcard, null);
            setPosition(showCardView, mTipViewWidth, mTipViewHeight, iTipLocation[i][0], iTipLocation[i][1]);

            int cardsWidth = UITools.convertDpToPixel(60, GameActivity.this);
            int cardsHeight = UITools.convertDpToPixel(44, GameActivity.this);
            setPosition(showCardView, cardsWidth, cardsHeight, iSeatValue[i][0], iSeatValue[i][1] + mNameTextHeight);
            layout_game.addView(showCardView);
            mShowCardViews.put(i, showCardView);
            showCardView.setVisibility(View.INVISIBLE);
        }

//        addTimer(2,60);
//        addTimer(6,30);
//        mTimerObjects.get(2).start();
//        mTimerObjects.get(6).start();


        //庄家位
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mD = inflater.inflate(R.layout.layout_button, null);
        layout_game.addView(mD);
        mD.setVisibility(View.INVISIBLE);

//        显示庄家位
//        mD.setVisibility(View.VISIBLE);
//        setPosition(mD,mDViewWidth,mDViewHeight,iD[3][0],iD[3][1]);

        //用户操作的按钮view
        addActionView();
        //底池显示
        addPoolView();

        //回到座位按钮
        addReturnSeatButton();

    }



    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (SocketService.SocketBinder) service;
            SocketService socketService = myBinder.getService();
            socketCallback = new SocketService.Callback() {
                @Override
                public void onReciveData(String data) {
                    //收到数据，进行处理，这里都是处理之后的数据
                    Logger.i(TAG, "onReciveData data : " + data);
                    try {
                        String[] recData = data.split("\\|");
                        if (recData[0].equals(Constant.RET_CREATE_TABLE)) {
                            //创建牌局返回,的调用进去牌桌接口
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") == 0) {
                                String msg = Constant.GAME_ENTER_TABLE + "|";
                                JSONObject jsonSend = new JSONObject();
                                jsonSend.put("userid", application.getUserId());
                                jsonSend.put("tableid", gameId);
                                jsonSend.put("nickname", application.getUser().nickname);
                                jsonSend.put("headpic", application.getUser().headpic);
                                msg += jsonSend.toString().replace("$", "￥");
                                msg += "$";
                                myBinder.sendInfo(msg);


                            } else {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "创建牌局失败，错误原因为：" + jsonReturn.getString("msg"));
                            }

                        } else if (recData[0].equals(Constant.RET_ENTER_TABLE)) {
                            //进入牌局返回
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {

                                ToastUtil.showToastInScreenCenter(GameActivity.this, "进入牌桌失败，错误原因为：" + jsonReturn.getString("msg"));
                                finish();
                            }
                        } else if (recData[0].equals(Constant.INFO_ENTER_TABLE)) {
                            //收到别人进牌局的消息，如果是自己，则不作任何处理，是别人的，记录下来，也可以将来做提示，某人进入了牌桌
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            GameUser user = new GameUser();
                            user.userId = jsonReturn.getInt("userid");
                            user.nickName = jsonReturn.getString("nickname");
                            user.userHeadPic = jsonReturn.getString("headpic");
                            user.seatindex = -1;
                            //jsonReturn.getInt("userid")这里不保留桌号
                            if (!mGameUser.containsKey(user.userId)) {
                                mGameUser.put(user.userId, user);
                            }

                        } else if (recData[0].equals(Constant.INFO_TABLE_INFO)) {
                            //收到牌桌信息,进入牌桌的时候都会收到，有可能是正在进行中的游戏数据
                            //根据state来判断显示，如果是自己的要显示开始按钮之类的
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            mTableInfo = new TableInfo();
                            mTableInfo.state = jsonReturn.getInt("state");//游戏状态

                            JSONArray potsRet = jsonReturn.getJSONArray("pots");
                            mTableInfo.pots = new int[potsRet.length()];//底池
                            for (int i = 0; i < potsRet.length(); i++) {
                                mTableInfo.pots[i] = potsRet.getInt(i);
                            }

//                            int nullIndex = 0;
//                            if (mTableInfo.comunitycards != null) {
//                                for (int i = 0; i < mTableInfo.comunitycards.length; i++) {
//                                    if (mTableInfo.comunitycards[i] == null) {
//                                        nullIndex = i;
//                                        break;
//                                    }
//                                }
//                                temp = jsonReturn.getJSONArray("comunitycards");
//                                mTableInfo.comunitycards = new CardInfo[temp.length()];//公牌
//                                for (int i = 0; i < temp.length(); i++) {
//                                    //这里要判断，是否有值，否则报异常
//                                    if (!temp.get(i).toString().equals("null")) {
//
//                                        JSONObject jsonCard = new JSONObject(temp.get(i).toString());
//                                        CardInfo cardInfo = new CardInfo();
//                                        cardInfo.suit = jsonCard.getInt("suit");
//                                        cardInfo.member = jsonCard.getInt("member");
//                                        cardInfo.name = jsonCard.getString("name");
//                                        mTableInfo.comunitycards[nullIndex++] = cardInfo;
//                                    }
//                                }
//                            }
                            JSONArray comunitycardsRet  = jsonReturn.getJSONArray("comunitycards");
                            mTableInfo.comunitycards = new CardInfo[comunitycardsRet.length()];//公牌
                            for (int i = 0; i < comunitycardsRet.length(); i++) {
                                //这里要判断，是否有值，否则报异常
                                if (!comunitycardsRet.get(i).toString().equals("null")) {

                                    JSONObject comunitycards = new JSONObject(comunitycardsRet.get(i).toString());
                                    CardInfo cardInfo = new CardInfo();
                                    cardInfo.suit = comunitycards.getInt("suit");
                                    cardInfo.member = comunitycards.getInt("member");
                                    cardInfo.name = comunitycards.getString("name");
                                    mTableInfo.comunitycards[i] = cardInfo;
                                }
                            }


                            JSONArray seatsRet = jsonReturn.getJSONArray("seats");
                            mTableInfo.seats = new TableSeatInfo[seatsRet.length()];
                            for (int i = 0; i < seatsRet.length(); i++) {
                                if (!seatsRet.get(i).toString().equals("null")) {
                                    JSONObject seats = new JSONObject(seatsRet.get(i).toString());
                                    TableSeatInfo tableSeatInfo = new TableSeatInfo();

                                    tableSeatInfo.isbutton = seats.getBoolean("isbutton");
                                    tableSeatInfo.issb = seats.getBoolean("issb");
                                    tableSeatInfo.isbb = seats.getBoolean("isbb");
                                    tableSeatInfo.state = seats.getInt("state");
                                    tableSeatInfo.curholdtime = seats.getInt("curholdtime");
                                    tableSeatInfo.holdtime = seats.getInt("holdtime");
                                    tableSeatInfo.needholdtime = seats.getInt("needholdtime");
                                    tableSeatInfo.userid = seats.getInt("userid");
                                    mTableInfo.seats[i] = tableSeatInfo;


//                                    mTableInfo.seats[i].isbutton = jsonTemp.getBoolean("isbutton");
//                                    mTableInfo.seats[i].issb = jsonTemp.getBoolean("issb");
//                                    mTableInfo.seats[i].isbb = jsonTemp.getBoolean("isbb");
//                                    mTableInfo.seats[i].state = jsonTemp.getInt("state");
//                                    mTableInfo.seats[i].curholdtime = jsonTemp.getInt("curholdtime");
//                                    mTableInfo.seats[i].userid = jsonTemp.getInt("userid");
                                }
                            }

                            JSONArray playersRet = jsonReturn.getJSONArray("players");
                            mTableInfo.players = new TablePlayerInfo[playersRet.length()];
                            for (int i = 0; i < playersRet.length(); i++) {
                                if (!playersRet.get(i).toString().equals("null")) {
                                    JSONObject players = new JSONObject(playersRet.get(i).toString());
                                    TablePlayerInfo tablePlayerInfo = new TablePlayerInfo();

                                    tablePlayerInfo.userid = players.getInt("userid");
                                    tablePlayerInfo.nickname = players.getString("nickname");
                                    tablePlayerInfo.headpic = players.getString("headpic");
                                    tablePlayerInfo.remainchips = players.getInt("remainchips");
                                    tablePlayerInfo.takeinchips = players.getInt("takeinchips");
                                    tablePlayerInfo.amountchips = players.getInt("amountchips");


                                    if (!players.get("cards").toString().equals("null")) {
                                        //这里后台传来的牌是空的
                                        JSONArray cards = players.getJSONArray("cards");

                                        tablePlayerInfo.cards = new CardInfo[cards.length()];
                                        for (int j = 0; j < cards.length(); j++) {
                                            if (!cards.get(j).toString().equals("null")) {
                                                JSONObject jsonTemp = new JSONObject(cards.get(j).toString());
                                                CardInfo cardInfo = new CardInfo();
                                                cardInfo.suit = jsonTemp.getInt("suit");
                                                cardInfo.member = jsonTemp.getInt("member");
                                                cardInfo.name = jsonTemp.getString("name");
                                                tablePlayerInfo.cards[j] = cardInfo;

//                                                tablePlayerInfo.cards[i].suit = jsonTemp.getInt("suit");
//                                                tablePlayerInfo.cards[i].member = jsonTemp.getInt("member");
//                                                tablePlayerInfo.cards[i].name = jsonTemp.getString("name");
                                            }
                                        }
                                    }

                                    tablePlayerInfo.seatindex = players.getInt("seatindex");
                                    tablePlayerInfo.curwaitactiontime = players.getInt("curwaitactiontime");
                                    tablePlayerInfo.waitactiontime = players.getInt("waitactiontime");
                                    tablePlayerInfo.needwaitactiontime = players.getInt("needwaitactiontime");
                                    tablePlayerInfo.isconnected = players.getBoolean("isconnected");
                                    tablePlayerInfo.lastplayaction = players.getInt("lastplayaction");
                                    if (!players.get("waitactionparam").toString().equals("null")) {
                                        JSONObject jsonWaitAction = new JSONObject(players.get("waitactionparam").toString());
                                        WaitAction waitAction = new WaitAction();

                                        waitAction.userid = jsonWaitAction.getInt("userid");
                                        waitAction.seatindex = jsonWaitAction.getInt("seatindex");
                                        waitAction.tableid = jsonWaitAction.getInt("tableid");
                                        waitAction.waitseconds = jsonWaitAction.getInt("waitseconds");
                                        List<Integer> listAction = new ArrayList<Integer>();
                                        JSONArray jsonAction = new JSONArray(jsonWaitAction.get("needaction").toString());
                                        for (int iii = 0; iii < jsonAction.length(); iii++) {
                                            listAction.add(jsonAction.getInt(iii));
                                        }
                                        waitAction.needaction = listAction;
                                        waitAction.maxpaidchips = jsonWaitAction.getInt("maxpaidchips");
                                        maxpaidchips = jsonWaitAction.getInt("maxpaidchips");
                                        waitAction.sb = jsonWaitAction.optInt("sb");
                                        waitAction.bb = jsonWaitAction.optInt("bb");
                                        waitAction.lastraisechips = jsonWaitAction.optInt("lastraisechips");
                                        waitAction.addwaitactiontimes = jsonWaitAction.optInt("addwaitactiontimes");
                                        tablePlayerInfo.waitactionparam = waitAction;
                                        Message message = new Message();
                                        message.obj = jsonWaitAction.toString();
                                        tablePlayerInfo.waitActionParamsMsg = message;
                                    }

                                    mTableInfo.players[i] = tablePlayerInfo;
//                                    //加入mGameUser
//                                    GameUser user=new GameUser();
//                                    user.userId=jsonTemp.getInt("userid");
//                                    user.nickName=jsonTemp.getString("nickname");
//                                    user.userHeadPic=jsonTemp.getString("headpic");
//                                    user.seatindex=-1;
//                                    //jsonReturn.getInt("userid")这里不保留桌号
//
//                                    if (!mTableUser.containsKey( user.userId)){
//                                        mTableUser.put(user.userId,user);
//                                    }
                                }
                            }
                            mTableInfo.starttime = jsonReturn.getString("starttime");
                            mTableInfo.gametype = jsonReturn.getInt("gametype");
                            mTableInfo.duration = jsonReturn.getInt("duration");
                            mTableInfo.smallblind = jsonReturn.getInt("smallblind");
                            mTableInfo.bigblind = jsonReturn.getInt("bigblind");
                            mTableInfo.mintakeinchips = jsonReturn.getInt("mintakeinchips");
                            mTableInfo.issurance = jsonReturn.getBoolean("isinsurance");
                            mTableInfo.isstraddle = jsonReturn.getBoolean("isstraddle");
                            mTableInfo.is27 = jsonReturn.getBoolean("is27");
                            mTableInfo.ante = jsonReturn.getInt("ante");
                            mTableInfo.isgpsrestrict = jsonReturn.getBoolean("isgpsrestrict");
                            mTableInfo.isiprestrict = jsonReturn.getBoolean("isiprestrict");
                            mTableInfo.iscontroltakein = jsonReturn.getBoolean("iscontroltakein");
                            mTableInfo.maxtakeinchips = jsonReturn.getInt("maxtakeinchips");
                            mTableInfo.takeinchipsuplimit = jsonReturn.getInt("takeinchipsuplimit");
                            mTableInfo.createuserid = jsonReturn.getInt("createuserid");
                            mTableInfo.curactionseatindex = jsonReturn.getInt("curactionseatindex");
                            mTableInfo.curactionwaittime = jsonReturn.getInt("curactionwaittime");
                            mTableInfo.tablename = jsonReturn.getString("tablename");
                            mTableInfo.shareCode = jsonReturn.getInt("sharecode");
                            mTableInfo.blindlevel = jsonReturn.optInt("blindlevel");
                            mTableInfo.blindraisetime = jsonReturn.optInt("blindraisetime");
                            mTableInfo.nextraisetime = jsonReturn.optString("nextraisetime");
                            gameHouseName = mTableInfo.tablename;

                            realTimeGameRecordFragment.setStartTime(mTableInfo.starttime);
                            realTimeGameRecordFragment.setDuration(mTableInfo.duration);

                            //此处注册接受消息


                            JMessageClient.registerEventReceiver(GameActivity.this);

                            //根据tableinfo设置界面
                            mainThreadHandler.sendEmptyMessage(MESSAGE_INFO_TABLE);
                        } else if (recData[0].equals(Constant.RET_START_GAME)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_START_GAME;
                            mainThreadHandler.sendMessage(message);


                        } else if (recData[0].equals(Constant.INFO_SIT_SEAT)) {
                            //收到坐下的消息，通知界面隐藏购买的popwindow
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_SIT_SEAT;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equals(Constant.RET_SIT_TABLE)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            /*  public int ret; //0成功
                                public string msg;*/
                            if (jsonReturn.getInt("ret") == 0) {
                                if (null != tv_service_coin && null != tv_core) {
                                    application.getUser().goldcoin -= Integer.parseInt(tv_service_coin.getText().toString()) + Integer.parseInt(tv_core.getText().toString());
                                }
                                mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPWINDOW);

                            } else if (jsonReturn.getInt("ret") == 1) {
                                if (jsonReturn.getString("msg").equals("剩余记分牌为零")) {
                                    isFirstBuyCore = true;
                                    getTakeInChipsFromServer();
                                } else {
                                    ToastUtil.showToastInScreenCenter(GameActivity.this, jsonReturn.getString("msg"));
                                }

                            }
//                            else {
//                                mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPWINDOW);
//                                ToastUtil.showToastInScreenCenter(GameActivity.this, "处理服务器返回的坐下数据异常,错误信息：" + jsonReturn.getString("msg"));
//                            }

                        } else if (recData[0].equals(Constant.INFO_ACTION)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_DOACTION;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_START_TABLE)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_START_TABLE;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_HOLE)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_HOLE;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_FLOP)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_FLOP;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_RIVER)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_RIVER;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_TURN)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_TURN;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_DO_POT)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_DO_POT;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_START_ROUND)) {
                            //牌回合开始
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_START_ROUND;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equals(Constant.INFO_INIT_ROUND)) {
                            //收到初始化牌局信息

                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_INIT_ROUND;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_WAIT_ACTION)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_WAIT_ACTION;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_SEE_NEXT_CARD)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_SEE_NEXT_CARD;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.INFO_SHOW_CARD)) {
                            //收到action
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_SHOW_CARD;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equals(Constant.RET_DO_ACTION)) {
                            //收到action操作返回
                            if (timer != null) {
                                timer.cancel();
                            }
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "操作失败，错误原因为：" + jsonReturn.getString("msg"));
                            }
                        } else if (recData[0].equals(Constant.RET_LEAVE_SEAT)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "离开座位操作失败，错误原因为：" + jsonReturn.getString("msg"));
                            }
                        } else if (recData[0].equals(Constant.RET_HOLD_SEAT)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "保位留桌操作失败，错误原因为：" + jsonReturn.getString("msg"));
                            }
                        } else if (recData[0].equals(Constant.RET_DISPOSE_TABLE)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "解散牌桌操作失败，错误原因为：" + jsonReturn.getString("msg"));
                            }
                        } else if (recData[0].equals(Constant.RET_LEAVE_TABLE)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "离开牌桌操作失败，错误原因为：" + jsonReturn.getString("msg"));
                            }
                        } else if (recData[0].equals(Constant.RET_ADD_CHIP)) {

                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "购买记分牌成功失败，错误原因为：" + jsonReturn.getString("msg"));
                            } else {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "购买记分牌成功！");
                            }
                            mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPWINDOW);
                        } else if (recData[0].equals(Constant.INFO_ADD_CHIPS)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_ADD_CHIPS;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equals(Constant.RET_BACK_SEAT)) {

                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
//                                ToastUtil.showToastInScreenCenter(GameActivity.this, "回到座位操作失败，错误原因为：" + jsonReturn.getString("msg"));
//                                showWindow(layout_parent);
                                Message message = Message.obtain();
                                message.obj = recData[1];
                                message.what = MESSAGE_RET_BACK_SEAT;
                                mainThreadHandler.sendMessage(message);
                                getTakeInChipsFromServer();
                                isBackToSeatOrIncreaseChips = true;
                            } else {

                            }

                        } else if (recData[0].equals(Constant.RET_SHOW_CARD)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "show card操作失败，错误原因为：" + jsonReturn.getString("msg"));
                            }

                        } else if (recData[0].equals(Constant.RET_SEE_NEXT_CARD)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "SEE_NEXT_CARD操作失败，错误原因为：" + jsonReturn.getString("msg"));
                            }

                        } else if (recData[0].equals(Constant.RET_PAUSE_GAME)) {
                            JSONObject jsonReturn = new JSONObject(recData[1]);
                            if (jsonReturn.getInt("ret") != 0) {
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "SEE_NEXT_CARD操作失败，错误原因为：" + jsonReturn.getString("msg"));
                            }

                        } else if (recData[0].equals(Constant.INFO_DO_END)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_DO_END;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equals(Constant.INFO_LEAVE_SEAT)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_LEAVE_SEAT;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equals(Constant.INFO_HOLD_SEAT)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_HOLD_SEAT;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equals(Constant.INFO_BACK_SEAT)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_BACK_SEAT;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.RET_DO_ANIMATION)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_DO_ANIMATION;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_TABLE_VOICE)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_TABLE_VOICE;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_BUY_SURANCE)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_BUY_SURANCE;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_IF_BUY_ASSURANCE)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_IF_BUY_ASSURANCE;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_DO_ASSURANCE)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_DO_ASSURANCE;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_REQUESR_BUY_ASSURANCE)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_REQUESR_BUY_ASSURANCE;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_TABLE_TIMEOUT)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_TABLE_TIMEOUT;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_ALARM_TIMEOUT)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_ALARM_TIMEOUT;
                            mainThreadHandler.sendMessage(message);
                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_WIN_27)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_WIN_27;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_DO_ANIMATION)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_DO_ANIMATION;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_RAISE_BLIND)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_RAISE_BLIND;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_MATCH_STOP)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_MATCH_STOP;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_DISPOSE_TABLE)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_DISPOSE_TABLE;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_PLAYER_AUTO)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_PLAYER_AUTO;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.RET_ADD_WAIT_ACTION_TIME)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_RET_ADD_WAIT_ACTION_TIME;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_ADD_WAIT_ACTION_TIME)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_ADD_WAIT_ACTION_TIME;
                            mainThreadHandler.sendMessage(message);

                        } else if (recData[0].equalsIgnoreCase(Constant.INFO_BEST_HAND)) {
                            Message message = Message.obtain();
                            message.obj = recData[1];
                            message.what = MESSAGE_INFO_BEST_HAND;
                            mainThreadHandler.sendMessage(message);

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理服务器返回的数据异常！");

                    }

                }

                @Override
                public void onSocketError() {
                    //socket异常，提示用户是否重连 弹出对话框，是否重连，不连接，则退出游戏界面
                    Logger.e(TAG, "onSocketError");
                    try {
                        if (!GameActivity.this.isFinishing()) {
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "网络异常，请重新打开");
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
//                    myBinder.connect(ip, port);


                }

                @Override
                public void onScoketConnectSuccess() {
                    //成功连接
                    if (flag) {
                        flag = false;
                        if (operation == 1) {
                            //创建牌局
                            try {
                                if (directEnterTable) {
                                    String msg = Constant.GAME_ENTER_TABLE + "|";
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("userid", application.getUserId());
                                    jsonObject.put("tableid", gameId);
                                    jsonObject.put("nickname", application.getUser().nickname);
                                    jsonObject.put("headpic", application.getUser().headpic);
                                    msg += jsonObject.toString().replace("$", "￥");
                                    msg += "$";
                                    Logger.i(TAG, "enter table msg : " + msg);
                                    myBinder.sendInfo(msg);
                                } else {
                                    String msg = Constant.GAME_CREATE_TABLE + "|";
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("userid", application.getUserId());
                                    jsonObject.put("tableid", gameId);
                                    msg += jsonObject.toString().replace("$", "￥");
                                    msg += "$";
                                    Logger.i(TAG, "create table msg : " + msg);
                                    myBinder.sendInfo(msg);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "创建牌局失败！");
                            }

                        } else if (operation == 2) {
                            //加入牌局,要判断自己是否还在牌局中
                            try {
                                String msg = Constant.GAME_ENTER_TABLE + "|";
                                JSONObject jsonSend = new JSONObject();
                                jsonSend.put("userid", application.getUserId());
                                jsonSend.put("tableid", gameId);
                                jsonSend.put("nickname", application.getUser().nickname);
                                jsonSend.put("headpic", application.getUser().headpic);
                                msg += jsonSend.toString().replace("$", "￥");
                                msg += "$";
                                myBinder.sendInfo(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "加入牌局失败！");
                            }

                        }

                    }


                }


            };
            socketService.setCallback(socketCallback);

            //绑定成功之后，进行socket连接操作,启动读，和传数据的接口
            myBinder.connect(ip, port);

        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mainThreadHandler = new Handler() {

        // 该方法运行在主线程中
        // 接收到handler发送的消息，对UI进行操作
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MESSAGE_INFO_TABLE:
                    Log.v("RABBY", "MESSAGE_INFO_TABLE");
                    //游戏牌桌的信息
//                    initGameInfo();
                    iv_voice.setClickable(true);
                    iv_info.setClickable(true);
                    iv_menu.setClickable(true);
                    iv_apply.setClickable(true);
                    iv_game_info.setClickable(true);
                    initGameInfo();

                    break;
                case MESSAGE_INFO_SIT_SEAT:
                    //收到坐下的信息，包含自己的坐下
                    try {
                        mp = MediaPlayer.create(GameActivity.this, R.raw.chairsitsound);
                        mp.start();
                        String dataInfo = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_SIT_SEAT:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataInfo);
                        int userid = jsonReturn.getInt("userid");
                        int seatIndex = jsonReturn.getInt("seatindex");
                        int intochips = jsonReturn.getInt("intochips");

                        mGameUser.get(userid).seatindex = seatIndex;
                        mGameUser.get(userid).remainchips = mGameUser.get(userid).remainchips + intochips;
                        mSeatOccuped.put(seatIndex, true);
                        if (userid == application.getUserId()) {
                            //本人进座位，则移位置
                            int iSeatnumber = mTableInfo.seats.length;
                            for (int i = 0; i < iSeatnumber; i++) {
                                int moveto = (i - seatIndex + iSeatnumber) % iSeatnumber;

                                setPosition(mSeatObjects.get(i), mSeatViewWidth, mSeatViewHeight, iSeatValue[moveto][0], iSeatValue[moveto][1]);
                                View view = mSeatObjects.get(i);
                                mSeatOccuped.put(seatIndex, true);
                                ImageView iv = view.findViewById(R.id.iv_user_head);
//                                iv.setClickable(false);
                                setPosition(mChipObjects.get(i), mAmountChipViewWidth, mAmountChipVieHeight, iAmountChipLocation[moveto][0], iAmountChipLocation[moveto][1]);
                                setPosition(mTipObjects.get(i), mTipViewWidth, mTipViewHeight, iTipLocation[moveto][0], iTipLocation[moveto][1]);
                                setPosition(mCardBackObjects.get(i), mCardBackWidth, mCardBackHeight, iCardBack[moveto][0], iCardBack[moveto][1]);
                            }
                        }

                        //坐下
                        View view = mSeatObjects.get(seatIndex);
                        view.setVisibility(View.VISIBLE);
                        ImageView iv = view.findViewById(R.id.iv_user_head);
                        TextView tv_name = view.findViewById(R.id.tv_user_name);
                        TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);

                        tv_name.setText(mGameUser.get(userid).nickName);
                        tv_name.setVisibility(View.VISIBLE);

                        tv_goldcoin.setText(mGameUser.get(userid).remainchips + "");
                        tv_goldcoin.setVisibility(View.VISIBLE);

                        if (mGameUser.get(userid).userHeadPic != null && !mGameUser.get(userid).userHeadPic.equals("")) {
                            Picasso.with(getApplicationContext())
                                    .load(mGameUser.get(userid).userHeadPic)
                                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                    .resize(mHeadImageWidth, mHeadImageHeight)
                                    .error(R.drawable.seat_empty)
                                    .transform(new CircleTransform(GameActivity.this))
                                    .into(iv);
                        }
                        iv.setImageDrawable(getResources().getDrawable(R.drawable.default_female_head));
                        mSeatOccuped.put(seatIndex, true);
//                        iv.setClickable(false);


                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理收到的牌桌信息错误，错误内容为处理用户坐下信息错误" + e.getMessage());

                    }

                    break;
                case MESSAGE_TAKEIN_INFO:
                    //从服务器收到带入的相关信息
                    showWindow(layout_parent, false);
                    break;
                case MESSAGE_DISMISS_POPWINDOW:
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    break;
                case MESSAGE_DISMISS_POPMENU:
                    if (popMenu != null) {
                        popMenu.dismiss();
                    }
                    break;
                case MESSAGE_INFO_DOACTION:
                    //收到操作信息
                    /*{
                                public int userid;
                                public int seatindex;
                                public PlayAction action;
                                public int amountchips;
                                public int remainchips;
                                public RoundState rstate;
                                public DateTime actiontime;
                            }*/
                    try {

//                        try {
//                            for (int key : mTimerViewObjects.keySet()) {
//                                mTimerViewObjects.get(key).setVisibility(View.INVISIBLE);
//                            }
//                        } catch (Throwable e) {
//                            e.printStackTrace();
//                        }
//
//                        try {
//                            for (int key : mHoldSeatTimerViewObjects.keySet()) {
//                                mHoldSeatTimerViewObjects.get(key).setVisibility(View.INVISIBLE);
//                            }
//                        } catch (Throwable w) {
//                            w.printStackTrace();
//                        }

                        for (int key : mSeatObjects.keySet()) {
//                            CustomCircleImageView imageView =
//                                    (CustomCircleImageView) (mSeatObjects.get(key).findViewById(R.id.head_icon_iv));
//                            if (imageView != null) {
//                                imageView.stopAnimation();
//                            }
                            mSeatObjects.get(key).findViewById(R.id.seat_countdown_timer_tv).setVisibility(View.INVISIBLE);
                        }

                        addTimeLayout.setVisibility(View.INVISIBLE);
                        addTimeFee.setVisibility(View.INVISIBLE);

                        String dataInfo = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_DOACTION:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataInfo);
                        int userid = jsonReturn.getInt("userid");
                        int seatIndex = jsonReturn.getInt("seatindex");
                        int action = jsonReturn.getInt("action");
                        int amountchips = jsonReturn.getInt("amountchips");
                        int remainchips = jsonReturn.getInt("remainchips");
                        mGameUser.get(userid).remainchips = remainchips;
                        mGameUser.get(userid).amountchips = amountchips;
                        //收到操作，做展现
                        //更改用户的筹码额度
                        if (userid == application.getUserId()) {
                            //自己操作，则隐藏按钮
//                            seekBarRaise.setVisibility(View.INVISIBLE);
                            mButtonRaise.setVisibility(View.INVISIBLE);
                            mButtonCheck.setVisibility(View.INVISIBLE);
                            mButtonFold.setVisibility(View.INVISIBLE);
                            blind_layout.setVisibility(View.INVISIBLE);
                            mTVRaise.setVisibility(View.INVISIBLE);
                            mTVCheck.setVisibility(View.INVISIBLE);
                            mTVFold.setVisibility(View.INVISIBLE);
                        }

                        int iRealIndex = seatIndex;
                        if (getUserIndex() != -1) {
                            iRealIndex = (iRealIndex + mTableInfo.seats.length - getUserIndex()) % mTableInfo.seats.length;
                        }

                        if (holdSeatTimers.containsKey(seatIndex)) {
                            holdSeatTimers.get(seatIndex).cancel();
                        }

                        if (mTimerObjects.containsKey(seatIndex)) {
                            mTimerObjects.get(seatIndex).cancel();
//                            mTimerViewObjects.get(iRealIndex).setVisibility(View.INVISIBLE);
//                            mHoldSeatTimerViewObjects.get(iRealIndex).setVisibility(View.INVISIBLE);

                            mSeatObjects.get(seatIndex).findViewById(R.id.seat_countdown_timer_tv).setVisibility(View.INVISIBLE);
                        }


                        View view = mSeatObjects.get(seatIndex);
                        TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);
                        tv_goldcoin.setText(remainchips + "");
                        View viewTip = mTipObjects.get(seatIndex);
                        TextView tv_tip = viewTip.findViewById(R.id.tv_tip);
                        View viewChip = mChipObjects.get(seatIndex);
                        viewChip.setVisibility(View.VISIBLE);
                        TextView tv_chip = viewChip.findViewById(R.id.tv_chip);
                        if (amountchips > 0) {
                            tv_chip.setVisibility(View.VISIBLE);
                            tv_chip.setText(amountchips + "");
                        } else {
                            viewChip.setVisibility(View.INVISIBLE);
                        }
                        View viewCardBack = mCardBackObjects.get(seatIndex);
//                        viewCardBack.setVisibility(View.VISIBLE);
//                        ImageView ivCardBack=(ImageView)view.findViewById(R.id.iv_cardback);

                        if (amountchips != 0 && action != 2 && action != 3) {
                            showChipsFlyAnimation(
                                    mSeatObjects.get(seatIndex).findViewById(R.id.iv_user_head)
                                    , viewChip.findViewById(R.id.iv_chip));
                        }

                        switch (action) {
                            case 0:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("下注");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();
                                break;
                            case 1:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("跟注");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();

                                break;
                            case 2:
                                mp = MediaPlayer.create(GameActivity.this, R.raw.foldcardsound);
                                mp.start();
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("弃牌");
                                viewCardBack.setVisibility(View.INVISIBLE);
                                showCardsFlyAnimation(mSeatObjects.get(seatIndex), null);
                                break;

                            case 3:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("看牌");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.checksound);
                                mp.start();
                                break;
                            case 4:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("加注");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();
                                break;
                            case 6:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("AllIn");
                                showAllInAnimation(seatIndex);
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();
                                break;
                            case 8:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("小盲");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();
                                break;
                            case 9:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("大盲");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();
                                break;
                            case 10:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("Straddle");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();
                                break;
                            case 11:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("补盲");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();
                                break;
                            case 12:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("Ante");
                                mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_table);
                                mp.start();
                                break;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理用户操作信息错误，错误内容为" + e.getMessage());
                    }
                    break;
                case MESSAGE_INFO_START_TABLE:
                    /* public class StartTableGameParam
                    {
                        public int userid;
                        public int tableid;
                    }*/
                    String dataInfo = (String) msg.obj;
                    //这里收到就好，不做任何处理

                    break;
                case MESSAGE_INFO_HOLE:
                    try {
                        mp = MediaPlayer.create(GameActivity.this, R.raw.dealcardhole);
                        mp.start();
                        //先清空对象
                        mPlayerHolebjects = new HashMap<Integer, PlayerHole>();
                        String dataPlayerHole = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_HOLE:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataPlayerHole);

                        JSONArray temp = jsonReturn.getJSONArray("playerhole");

                        for (int i = 0; i < temp.length(); i++) {
                            //这里要判断，是否有值，否则报异常
                            if (!temp.get(i).toString().equals("null")) {
                                JSONObject jsonPlayerHole = new JSONObject(temp.get(i).toString());//每一个人的底牌
                                PlayerHole playerHole = new PlayerHole();
                                playerHole.seatindex = jsonPlayerHole.getInt("seatindex");
                                playerHole.userid = jsonPlayerHole.getInt("userid");

                                JSONArray jsonCards = jsonPlayerHole.getJSONArray("hole");

                                if (!jsonCards.toString().equals("null")) {
                                    CardInfo[] playerHoleCards = new CardInfo[jsonCards.length()];
                                    for (int j = 0; j < jsonCards.length(); j++) {
                                        JSONObject jsonCard = new JSONObject(jsonCards.get(j).toString());
                                        CardInfo cardInfo = new CardInfo();
                                        cardInfo.suit = jsonCard.getInt("suit");
                                        cardInfo.member = jsonCard.getInt("member");
                                        cardInfo.name = jsonCard.getString("name");
                                        playerHoleCards[j] = cardInfo;

                                    }
                                    playerHole.hole = playerHoleCards;
                                    showCardsFlyAnimation(null, mSeatObjects.get(playerHole.seatindex));

                                }

                                mCardBackObjects.get(playerHole.seatindex).setVisibility(View.VISIBLE);
                                mPlayerHolebjects.put(playerHole.userid, playerHole);
                                if (playerHole.userid == application.getUserId()) {
                                    //显示两张底牌
                                    mMyCards.setVisibility(View.VISIBLE);
                                    for (int key : mMyCardsObjects.keySet()) {
                                        mMyCardsObjects.get(key).setVisibility(View.INVISIBLE);
                                    }
                                    for (int j = 0; j < playerHole.hole.length; j++) {
                                        mMyCardsObjects.get(j).setVisibility(View.VISIBLE);
                                        switch (j) {
                                            case 0:
                                                ((ImageView)mMyCardsObjects.get(j).findViewById(R.id.mycard_iv_card1))
                                                        .setImageBitmap(drawSingleCard(playerHole.hole[j].suit, playerHole.hole[j].member));
                                                break;
                                            case 1:
                                                ((ImageView)mMyCardsObjects.get(j).findViewById(R.id.mycard_iv_card2))
                                                        .setImageBitmap(drawSingleCard(playerHole.hole[j].suit, playerHole.hole[j].member));
                                                break;
                                            case 2:
                                                ((ImageView)mMyCardsObjects.get(j).findViewById(R.id.mycard_iv_card3))
                                                        .setImageBitmap(drawSingleCard(playerHole.hole[j].suit, playerHole.hole[j].member));
                                                break;
                                            case 3:
                                                ((ImageView)mMyCardsObjects.get(j).findViewById(R.id.mycard_iv_card4))
                                                        .setImageBitmap(drawSingleCard(playerHole.hole[j].suit, playerHole.hole[j].member));
                                                break;

                                        }

                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理底牌信息错误，错误内容为" + e.getMessage());
                    }

                    break;
                case MESSAGE_INFO_FLOP:
                    try {
                        mp = MediaPlayer.create(GameActivity.this, R.raw.dealcardflop);
                        mp.start();
                        String dataFlop = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_FLOP:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataFlop);
                        JSONArray temp = jsonReturn.getJSONArray("flopcards");
                        mGameCards.setVisibility(View.VISIBLE);
                        for (int key : mGameCardsObjects.keySet()) {
                            mGameCardsObjects.get(key).setVisibility(View.INVISIBLE);
                        }

//                        int nullIndex = 0;
//                        for (int i = 0; i < mTableInfo.comunitycards.length; i++) {
//                            if (mTableInfo.comunitycards[i] == null) {
//                                nullIndex = i;
//                                break;
//                            }
//                        }
                        CardInfo[] flopCards = new CardInfo[temp.length()];//公牌
                        for (int i = 0; i < temp.length(); i++) {
                            //这里要判断，是否有值，否则报异常
                            if (!temp.get(i).toString().equals("null")) {
                                JSONObject jsonCard = new JSONObject(temp.get(i).toString());
                                CardInfo cardInfo = new CardInfo();
                                cardInfo.suit = jsonCard.getInt("suit");
                                cardInfo.member = jsonCard.getInt("member");
                                cardInfo.name = jsonCard.getString("name");
                                flopCards[i] = cardInfo;
                                publicCards[i] = cardInfo;
                                mGameCardsObjects.get(i).setVisibility(View.VISIBLE);
                                mGameCardsObjects.get(i).setImageBitmap(drawSingleCard(flopCards[i].suit, flopCards[i].member));
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理公牌信息错误，错误内容为" + e.getMessage());
                    }
                    break;
                case MESSAGE_INFO_TURN:
                    //{"turncard":
                    try {
                        mp = MediaPlayer.create(GameActivity.this, R.raw.dealcard);
                        mp.start();
                        String dataTurn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_TURN:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataTurn);
                        JSONObject jsonCard = new JSONObject(jsonReturn.getString("turncard"));
                        CardInfo turncard = new CardInfo();//转牌
                        turncard.suit = jsonCard.getInt("suit");
                        turncard.member = jsonCard.getInt("member");
                        turncard.name = jsonCard.getString("name");
                        publicCards[3] = turncard;
                        mGameCardsObjects.get(3).setVisibility(View.VISIBLE);
                        mGameCardsObjects.get(3).setImageBitmap(drawSingleCard(turncard.suit, turncard.member));


                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理turn信息错误，错误内容为" + e.getMessage());
                    }
                    break;

                case MESSAGE_INFO_RIVER:
                    try {
                        mp = MediaPlayer.create(GameActivity.this, R.raw.dealcard);
                        mp.start();
                        String dataRiver = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_RIVER:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataRiver);
                        JSONObject jsonCard = new JSONObject(jsonReturn.getString("rivercard"));
                        CardInfo rivercard = new CardInfo();//转牌
                        rivercard.suit = jsonCard.getInt("suit");
                        rivercard.member = jsonCard.getInt("member");
                        rivercard.name = jsonCard.getString("name");
                        publicCards[4] = rivercard;
                        mGameCardsObjects.get(4).setVisibility(View.VISIBLE);
                        mGameCardsObjects.get(4).setImageBitmap(drawSingleCard(rivercard.suit, rivercard.member));

                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理turn信息错误，错误内容为" + e.getMessage());
                    }
                    break;
                case MESSAGE_INFO_DO_POT:
                    try {
                        //所有用户chip清空，不展现
                        mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_pot);
                        mp.start();

                        for (Map.Entry<Integer, GameUser> entry : mGameUser.entrySet()) {
                            if (entry.getValue().amountchips > 0) {
                                showChipsFlyAnimation(
                                        mChipObjects.get(entry.getValue().seatindex).findViewById(R.id.iv_chip),
                                        mPoolOTextbjects.get(0));
                            }
                        }

                        for (int key : mChipObjects.keySet()) {
                            mChipObjects.get(key).setVisibility(View.INVISIBLE);
                        }

                        clearAllAmountChips();
                        String pots = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_DO_POT:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(pots);
                        JSONArray jsonPots = jsonReturn.getJSONArray("pots");
                        int[] potsInfo = new int[jsonPots.length()];//底池
                        for (int i = 0; i < jsonPots.length(); i++) {
                            potsInfo[i] = jsonPots.getInt(i);
                            mPoolObjects.get(i).setVisibility(View.VISIBLE);
                            mPoolOTextbjects.get(i).setText(potsInfo[i] + "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理底池信息错误，错误内容为" + e.getMessage());
                    }

                    break;
                case MESSAGE_INFO_INIT_ROUND:
                    Log.v("RABBY", "MESSAGE_INFO_INIT_ROUND:");
                    //庄家位
                    hasShowCards =false;
                    List<Integer> index = new ArrayList<>();
                    List<Integer> dismissAutoPlay = new ArrayList<>();
                    try {
                        String retMsg = (String) msg.obj;
                        JSONObject retJson = new JSONObject(retMsg);
                        JSONArray seatsRet = retJson.optJSONArray("seats");
                        for (int i = 0; i < seatsRet.length(); i++) {
                            if (!("null".equalsIgnoreCase(seatsRet.get(i).toString()))) {
                                JSONObject seats = new JSONObject(seatsRet.get(i).toString());
                               int state =  seats.getInt("state");
                               if (state == 3) {
                                   index.add(i);
                               }
                               if (state == 6) {
                                   dismissAutoPlay.add(i);
                               }
                            }
                        }

                        clearTableViews(index, dismissAutoPlay);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }


                    break;
                case MESSAGE_INFO_START_ROUND:
                    /* public class InfStartRoundParam
                     {
                         //标识庄家和盲注位
                         public int buttonseatindex;
                         public int smallblindseatindex;
                         public int bigblindseatindex;
                         public int straddleseatindex;

                     }*/
                    try {
                        //设置庄家位, 只做D的选择
                        int buttonseatindex, smallblindseatindex, bigblindseatindex, straddleseatindex;
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_START_ROUND:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);
                        buttonseatindex = jsonReturn.getInt("buttonseatindex");
                        if (getUserIndex() != -1) {
                            buttonseatindex = (buttonseatindex - getUserIndex() + mTableInfo.seats.length) % mTableInfo.seats.length;
                        }
                        // 显示庄家位
                        mD.setVisibility(View.VISIBLE);
                        setPosition(mD, mDViewWidth, mDViewHeight, iD[buttonseatindex][0], iD[buttonseatindex][1]);


                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "设置庄家信息错误，错误内容为" + e.getMessage());
                    }

                    break;
                case MESSAGE_INFO_WAIT_ACTION:

                    waitActionInfoMsg = msg;

                    handleInfoWaitAction(msg, 0);

                    break;
                case MESSAGE_INFO_SEE_NEXT_CARD:

                    try {
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_SEE_NEXT_CARD:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);
                        int roundState = jsonReturn.getInt("state");

                        JSONArray temp = jsonReturn.getJSONArray("card");
                        String nickName = jsonReturn.getString("nickname");
                        ToastUtil.showToastInScreenCenter(GameActivity.this, nickName + "想看下面的牌");
                        switch (roundState) {
                            case 4:
                                //3张牌
                                CardInfo[] flopCards = new CardInfo[temp.length()];//公牌
                                for (int i = 0; i < 3; i++) {
                                    if (!temp.get(i).toString().equals("null")) {
                                        JSONObject jsonCard = new JSONObject(temp.get(i).toString());//每一个人的底牌
                                        CardInfo cardInfo = new CardInfo();
                                        cardInfo.suit = jsonCard.getInt("suit");
                                        cardInfo.member = jsonCard.getInt("member");
                                        cardInfo.name = jsonCard.getString("name");
                                        flopCards[i] = cardInfo;
                                        mGameCardsObjects.get(i).setVisibility(View.VISIBLE);
                                        mGameCardsObjects.get(i).setImageBitmap(drawSingleCard(flopCards[i].suit, flopCards[i].member));
                                    }
                                }
                                break;
                            case 6:
                                //1张牌
                                CardInfo[] turnCards = new CardInfo[1];//公牌
//                                for (int i = 0; i < 1; i++) {
                                if (!temp.get(0).toString().equals("null")) {
                                    JSONObject jsonCard = new JSONObject(temp.get(0).toString());//每一个人的底牌
                                    CardInfo cardInfo = new CardInfo();
                                    cardInfo.suit = jsonCard.getInt("suit");
                                    cardInfo.member = jsonCard.getInt("member");
                                    cardInfo.name = jsonCard.getString("name");
                                    turnCards[0] = cardInfo;
                                    mGameCardsObjects.get(3).setVisibility(View.VISIBLE);
                                    mGameCardsObjects.get(3).setImageBitmap(drawSingleCard(turnCards[0].suit, turnCards[0].member));
                                }
//                                }
                                break;
                            case 8:
                                //1张牌
                                //1张牌
                                CardInfo[] riverCards = new CardInfo[temp.length()];//公牌
                                for (int i = 0; i < 1; i++) {
                                    if (!temp.get(i).toString().equals("null")) {
                                        JSONObject jsonCard = new JSONObject(temp.get(i).toString());//每一个人的底牌
                                        CardInfo cardInfo = new CardInfo();
                                        cardInfo.suit = jsonCard.getInt("suit");
                                        cardInfo.member = jsonCard.getInt("member");
                                        cardInfo.name = jsonCard.getString("name");
                                        riverCards[i] = cardInfo;
                                        mGameCardsObjects.get(4).setVisibility(View.VISIBLE);
                                        mGameCardsObjects.get(4).setImageBitmap(drawSingleCard(riverCards[i].suit, riverCards[i].member));
                                    }
                                }
                                break;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理seenextcard信息错误，错误内容为" + e.getMessage());
                    }


                    break;
                case MESSAGE_INFO_SHOW_CARD:

                    try {
                        //等待操作
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_SHOW_CARD:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);
                        int seatindex = jsonReturn.getInt("seatindex");
                        int userid = jsonReturn.getInt("userid");
                        if (userid != application.getUserId()) {
                            View showCardView = mShowCardViews.get(seatindex);
                            showCardView.setVisibility(View.VISIBLE);
                            ImageView iv1 = showCardView.findViewById(R.id.iv_card1);
                            ImageView iv2 = showCardView.findViewById(R.id.iv_card2);
                            ImageView iv3 = showCardView.findViewById(R.id.iv_card3);
                            ImageView iv4 = showCardView.findViewById(R.id.iv_card4);
                            iv1.setVisibility(View.INVISIBLE);
                            iv2.setVisibility(View.INVISIBLE);
                            iv3.setVisibility(View.INVISIBLE);
                            iv4.setVisibility(View.INVISIBLE);
                            int iRealIndex = seatindex;
                            if (getUserIndex() != -1) {
                                iRealIndex = (seatindex - getUserIndex() + mTableInfo.seats.length) % (mTableInfo.seats.length);
                            }

                            int cardsWidth = UITools.convertDpToPixel(60, GameActivity.this);
                            int cardsHeight = UITools.convertDpToPixel(44, GameActivity.this);
                            setPosition(showCardView, cardsWidth, cardsHeight, iSeatValue[iRealIndex][0], iSeatValue[iRealIndex][1] + mNameTextHeight);
                            if (mTableInfo.gametype == 0) {
                                iv1.setVisibility(View.VISIBLE);
                                iv2.setVisibility(View.VISIBLE);
//                                ImageView iv5 = mShowCards.findViewById(R.id.iv_card1);
                                for (Map.Entry entry : mPlayerHolebjects.entrySet()) {
                                    Logger.e("QIPU", "entry key : " + entry.getKey() + ", value : " + entry.getValue());
                                }
                                Logger.e("QIPU", "userId : " + userid);
                                iv1.setImageBitmap(drawSingleCard(mPlayerHolebjects.get(userid).hole[0].suit, mPlayerHolebjects.get(userid).hole[0].member));
//                                ImageView iv6 = mShowCards.findViewById(R.id.iv_card2);
                                iv2.setImageBitmap(drawSingleCard(mPlayerHolebjects.get(userid).hole[1].suit, mPlayerHolebjects.get(userid).hole[1].member));
                            } else {
                                iv1.setVisibility(View.VISIBLE);
                                iv2.setVisibility(View.VISIBLE);
                                iv3.setVisibility(View.VISIBLE);
                                iv4.setVisibility(View.VISIBLE);
//                                ImageView iv5 = mShowCards.findViewById(R.id.iv_card1);
                                iv1.setImageBitmap(drawSingleCard(mPlayerHolebjects.get(userid).hole[0].suit, mPlayerHolebjects.get(userid).hole[0].member));
//                                ImageView iv6 = mShowCards.findViewById(R.id.iv_card2);
                                iv2.setImageBitmap(drawSingleCard(mPlayerHolebjects.get(userid).hole[1].suit, mPlayerHolebjects.get(userid).hole[1].member));
                                iv3.setImageBitmap(drawSingleCard(mPlayerHolebjects.get(userid).hole[2].suit, mPlayerHolebjects.get(userid).hole[2].member));
                                iv4.setImageBitmap(drawSingleCard(mPlayerHolebjects.get(userid).hole[3].suit, mPlayerHolebjects.get(userid).hole[3].member));
                            }
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "showcard错误，错误内容为" + e.getMessage());
                    }
                    break;
                case MESSAGE_START_GAME:
                    try {
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_START_GAME:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);
                        if (jsonReturn.getInt("ret") == 0) {
                            //开始游戏按钮隐藏
                            btnStartGame.setVisibility(View.INVISIBLE);
                            mMessage.setVisibility(View.INVISIBLE);
                            //判断桌上有几个用户，如果只有一个，那么提示等待别的用户开始
                        } else {
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "游戏开始异常！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "showcard错误，错误内容为" + e.getMessage());
                    }
                    break;
                case MESSAGE_INFO_LEAVE_SEAT:
                    //离开座位
                    //头像去掉，显示隐藏，相关的数据都要清空
                    /*   public int userid;
                        public int tableid;
                         public int seatindex;*/
                    try {
//                        for (int key : mTimerViewObjects.keySet()) {
//                            mTimerViewObjects.get(key).setVisibility(View.INVISIBLE);
//                        }
//
//                        for (int key : mHoldSeatTimerViewObjects.keySet()) {
//                            mHoldSeatTimerViewObjects.get(key).setVisibility(View.INVISIBLE);
//                        }

//                        for (int key : mSeatObjects.keySet()) {
//                            CustomCircleImageView imageView =
//                                    (CustomCircleImageView) (mSeatObjects.get(key).findViewById(R.id.head_icon_iv));
//                            if (imageView != null) {
//                                imageView.stopAnimation();
//                            }
//                        }


                        mp = MediaPlayer.create(GameActivity.this, R.raw.chairstandsound);
                        mp.start();
                        //等待操作
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_LEAVE_SEAT:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);
                        int seatindex = jsonReturn.getInt("seatindex");
                        int userid = jsonReturn.getInt("userid");

                        mSeatObjects.get(seatindex).findViewById(R.id.seat_countdown_timer_tv).setVisibility(View.INVISIBLE);
                        mSeatObjects.get(seatindex).findViewById(R.id.seat_auto_play_tv).setVisibility(View.INVISIBLE);

                        View view = mSeatObjects.get(seatindex);
                        ImageView iv = view.findViewById(R.id.iv_user_head);
                        TextView tv_name = view.findViewById(R.id.tv_user_name);
                        TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);
                        tv_name.setText("");
                        tv_name.setVisibility(View.INVISIBLE);
                        tv_goldcoin.setText("");
                        tv_goldcoin.setVisibility(View.INVISIBLE);
                        iv.setImageResource(R.drawable.seat_empty);

                        View viewTip = mTipObjects.get(seatindex);
                        viewTip.setVisibility(View.INVISIBLE);

                        mGameUser.get(userid).seatindex = -1;

                        mTipObjects.get(seatindex).setVisibility(View.INVISIBLE);
                        mChipObjects.get(seatindex).setVisibility(View.INVISIBLE);
                        mCardBackObjects.get(seatindex).setVisibility(View.INVISIBLE);

                        //先离开座位，然后判断是否自己，如果是自己，别的空位置按钮全部可以坐下
                        if (userid == application.getUserId()) {

                            if (null != mReturnSeat) {
                                mReturnSeat.setVisibility(View.INVISIBLE);
                            }

                            //将空座位设置成可坐 ，就是说可以click
                            for (int i = 0; i < mTableInfo.seats.length; i++) {
                                View viewSeat = mSeatObjects.get(i);
                                ImageView ivHead = viewSeat.findViewById(R.id.iv_user_head);
                                ivHead.setClickable(true);
                                mSeatOccuped.put(i, false);

                            }
                            for (int key : mGameUser.keySet()) {
                                if (mGameUser.get(key).seatindex != -1) {
                                    View viewSeat = mSeatObjects.get(mGameUser.get(key).seatindex);
                                    ImageView ivHead = viewSeat.findViewById(R.id.iv_user_head);
//                                    ivHead.setClickable(false);
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "离开座位错误，错误内容为" + e.getMessage());
                    }


                    break;
                case MESSAGE_INFO_ADD_CHIPS:
                    try {

                        //等待操作
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_ADD_CHIPS:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);

                        int userid = jsonReturn.getInt("userid");
                        int chips = jsonReturn.getInt("chips");
                        mGameUser.get(userid).remainchips += chips;
                        View view = mSeatObjects.get(mGameUser.get(userid).seatindex);
                        TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);
                        tv_goldcoin.setText(String.valueOf(mGameUser.get(userid).remainchips));
                        ToastUtil.showToastInScreenCenter(GameActivity.this,
                                mGameUser.get(userid).nickName + "补充筹码" + chips);

                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理INFO_ADD_CHIPS错误，错误内容为" + e.getMessage());
                    }


                    break;
                case MESSAGE_INFO_HOLD_SEAT:
                    try {
                        //收到保位留桌
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_HOLD_SEAT:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);

                        int userid = jsonReturn.getInt("userid");
                        int seatindex = jsonReturn.getInt("seatindex");
                        int holdseconds = jsonReturn.getInt("holdseconds");


                        View view = mSeatObjects.get(seatindex);
                        ImageView iv = view.findViewById(R.id.iv_user_head);
                        TextView tv_name = view.findViewById(R.id.tv_user_name);
                        TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);
//                        tv_name.setText("");
//                        tv_name.setVisibility(View.INVISIBLE);
//                        tv_goldcoin.setText("");
//                        tv_goldcoin.setVisibility(View.INVISIBLE);
//                        iv.setImageResource(R.drawable.seat_empty);


//                        mTipObjects.get(seatindex).setVisibility(View.INVISIBLE);
//                        mChipObjects.get(seatindex).setVisibility(View.INVISIBLE);
//                        mCardBackObjects.get(seatindex).setVisibility(View.INVISIBLE);

                        //先离开座位，然后判断是否自己，如果是自己，显示回到座位按钮，别人则显示倒计时
                        if (userid == application.getUserId()) {
                            //显示保位留桌
//                            addTimer(seatindex, 0, holdseconds);
                            addHoldSeatTimer(seatindex, 0, holdseconds);
                            mReturnSeat.setVisibility(View.VISIBLE);
                            mReturnSeat.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //发送回到座位
                                    try {
                                        String msg = Constant.GAME_BACK_SEAT + "|";
                                        JSONObject jsonSend = new JSONObject();
                                        jsonSend.put("userid", application.getUserId());
                                        jsonSend.put("tableid", gameId);
                                        jsonSend.put("seatindex", getUserIndex());
                                        msg += jsonSend.toString().replace("$", "￥");
                                        msg += "$";
                                        myBinder.sendInfo(msg);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ToastUtil.showToastInScreenCenter(GameActivity.this, "返回座位出错！");

                                    }

                                }
                            });
                            mReturnSeat.bringToFront();

                        } else {

                            int realIndex = seatindex;
                            if (getUserIndex() != -1) {
                                realIndex = (seatindex - getUserIndex() + mTableInfo.seats.length) % mTableInfo.seats.length;
                            }
                            //展现时钟
//                            addTimer(seatindex, realIndex, holdseconds);
                            addHoldSeatTimer(seatindex, realIndex, holdseconds);

                        }

                            View viewTip=  mTipObjects.get(seatindex);
                            viewTip.setVisibility(View.VISIBLE);
                            TextView tv_tip = (TextView) viewTip.findViewById(R.id.tv_tip);
                            tv_tip.setText("留座");

                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理INFO_ADD_CHIPS错误，错误内容为" + e.getMessage());
                    }


                    break;
                case MESSAGE_INFO_DO_END:
                    //动画显示谁赢了，加上筹码
                    try {

                        mp = MediaPlayer.create(GameActivity.this, R.raw.chips_to_pot);
                        mp.start();

                        String retData = (String) msg.obj;

                        Gson gson = new Gson();

                        Type type = new TypeToken<WinPlayersBean>() {
                        }.getType();

                        WinPlayersBean winPlayersBean = gson.fromJson(retData, type);

                        for (WinPlayersBean.WinplayersBean bean : winPlayersBean.getWinplayers()) {
                            int remainChips = mGameUser.get(bean.getUserid()).remainchips;
                            mGameUser.get(bean.getUserid()).remainchips = remainChips + bean.getWinchips();
                            View view = mSeatObjects.get(bean.getSeatindex());
                            view.findViewById(R.id.win_icon).setVisibility(View.VISIBLE);
                            TextView winChips = view.findViewById(R.id.win_chips);
                            winChips.setVisibility(View.VISIBLE);
                            winChips.setText("+" + bean.getWinchips());
//                            showWin(bean.getSeatindex());
                            TextView gold = (TextView) view.findViewById(R.id.tv_goldcoin);
                            gold.setText(mGameUser.get(bean.getUserid()).remainchips + "");
                            showChipsFlyAnimation(mPoolOTextbjects.get(0), mChipObjects.get(bean.getSeatindex()).findViewById(R.id.iv_chip));
                        }

                        int nullIndex = 5;
                        for (int i = 0; i < publicCards.length; i++) {
                            if (publicCards[i] == null) {
                                nullIndex = i;
                                break;
                            }
                        }


                        try {
                            for (int i = 0; i < mPoolObjects.size(); i++) {
                                mPoolObjects.get(i).setVisibility(View.INVISIBLE);
//                                mPoolOTextbjects.get(i).setVisibility(View.INVISIBLE);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }

                        if (nullIndex < 5) {
                            seeNextCard.setVisibility(View.VISIBLE);
                            seeNextCard.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {

                                        String msg = Constant.GAME_SEE_NEXT_CARD + "|";
                                        JSONObject jsonSend = new JSONObject();
                                        jsonSend.put("userid", application.getUserId());
                                        jsonSend.put("seatindex", getUserIndex());
                                        jsonSend.put("tableid", gameId);
                                        msg += jsonSend.toString().replace("$", "￥");
                                        msg += "$";
                                        myBinder.sendInfo(msg);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ToastUtil.showToastInScreenCenter(GameActivity.this, "操作出错！");
                                    }
                                }
                            });
                        }

                        Logger.i(TAG, "do end retData : " + retData);

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    break;

                case MESSAGE_INFO_BACK_SEAT:

                    /*
                    public class BackSeatParam
                    {
                        public int userid;
                        public int tableid;
                        public int seatindex;
                    }
                    */
                    //TODO : QIPU 隐藏托管字样

                    try {
//                        for (int key : mTimerViewObjects.keySet()) {
//                            mTimerViewObjects.get(key).setVisibility(View.INVISIBLE);
//                        }
//
//                        for (int key : mHoldSeatTimerViewObjects.keySet()) {
//                            mHoldSeatTimerViewObjects.get(key).setVisibility(View.INVISIBLE);
//                        }
//
//                        for (int key : mSeatObjects.keySet()) {
//                            CustomCircleImageView imageView =
//                                    (CustomCircleImageView) (mSeatObjects.get(key).findViewById(R.id.head_icon_iv));
//                            if (imageView != null) {
//                                imageView.stopAnimation();
//                            }
//                        }
//                        for (int key : mSeatObjects.keySet()) {
//                            mSeatObjects.get(key).findViewById(R.id.seat_countdown_timer_tv).setVisibility(View.INVISIBLE);
//                        }
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_BACK_SEAT:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);

                        int userid = jsonReturn.getInt("userid");
                        int seatindex = jsonReturn.getInt("seatindex");

                        mSeatObjects.get(seatindex).findViewById(R.id.seat_auto_play_tv).setVisibility(View.INVISIBLE);
                        if (userid == application.getUserId()) {
                            mReturnSeat.setVisibility(View.INVISIBLE);
                        }
                        mSeatObjects.get(seatindex).findViewById(R.id.seat_countdown_timer_tv).setVisibility(View.INVISIBLE);

                        int iRealIndex = seatindex;
                        if (getUserIndex() != -1) {
                            iRealIndex = (iRealIndex + mTableInfo.seats.length - getUserIndex()) % mTableInfo.seats.length;
                        }
                        if (holdSeatTimers.containsKey(seatindex)) {
                            holdSeatTimers.get(seatindex).cancel();
                        }
                        if (mTimerObjects.containsKey(seatindex)) {
                            mTimerObjects.get(seatindex).cancel();
//                            mTimerViewObjects.get(iRealIndex).setVisibility(View.INVISIBLE);
//                            mHoldSeatTimerViewObjects.get(iRealIndex).setVisibility(View.INVISIBLE);
                            mSeatObjects.get(seatindex).findViewById(R.id.seat_countdown_timer_tv).setVisibility(View.INVISIBLE);

                        }

                        View viewTip = mTipObjects.get(seatindex);
                        viewTip.setVisibility(View.INVISIBLE);

                        View view = mSeatObjects.get(seatindex);
                        view.setVisibility(View.VISIBLE);
                        ImageView iv = view.findViewById(R.id.iv_user_head);
                        TextView tv_name = view.findViewById(R.id.tv_user_name);
                        TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);

                        tv_name.setText(mGameUser.get(userid).nickName);
                        tv_name.setVisibility(View.VISIBLE);
                        tv_goldcoin.setText(String.valueOf(mGameUser.get(userid).remainchips));

                        View viewChip = mChipObjects.get(seatindex);
                        TextView tv_chip = viewChip.findViewById(R.id.tv_chip);
                        tv_chip.setVisibility(View.INVISIBLE);

                        mTipObjects.get(seatindex).setVisibility(View.INVISIBLE);


                        tv_goldcoin.setVisibility(View.VISIBLE);

                        int ivWidth = UITools.convertDpToPixel(50, GameActivity.this);
                        int ivHeight = UITools.convertDpToPixel(50, GameActivity.this);

                        if (mGameUser.get(userid).userHeadPic != null && !mGameUser.get(userid).userHeadPic.equals("")) {
                            Picasso.with(getApplicationContext())
                                    .load(mGameUser.get(userid).userHeadPic)
                                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                    .resize(ivWidth, ivHeight)
                                    .error(R.drawable.seat_empty)
                                    .transform(new CircleTransform(GameActivity.this))
                                    .into(iv);
                        }
//                        iv.setClickable(false);
                        mSeatOccuped.put(seatindex, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理IMESSAGE_INFO_BACK_SEAT错误，错误内容为" + e.getMessage());
                    }


                    break;
                case MESSAGE_INFO_DISPOSE_TABLE:
                    //弹出提示，确认之后退出

                    try {
                        String dataReturn = (String) msg.obj;
                        Log.v("RABBY", "MESSAGE_INFO_DISPOSE_TABLE:" + msg.obj);
                        JSONObject jsonReturn = new JSONObject(dataReturn);

                        int userid = jsonReturn.getInt("userid");

                        //TODO: QIPU 清空当前牌桌信息，然后收到inftableinfo之后重新绘制牌桌
                        mTableInfo = null;

                        clearTableViews(new ArrayList<Integer>(), new ArrayList<Integer>());

//                        if (userid == application.getUserId()) {
//                            new AlertDialog.Builder(GameActivity.this).setTitle("系统提示")//设置对话框标题
//
//                                    .setMessage("牌局已被解散，点确定退出牌局！")//设置显示的内容
//
//                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
//
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
//
//                                            // TODO Auto-generated method stub
//                                            finish();
//
//                                        }
//
//                                    }).show();//在按键响应事件中显示此对话框
//
//                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "处理MESSAGE_INFO_DISPOSE_TABLE错误，错误内容为" + e.getMessage());
                    }


                    break;

                case MESSAGE_APPLY_INFO:

                    //popApplyWindow
                    int layoutId = R.layout.popupwindow_apply;   // 布局ID
                    View contentView = LayoutInflater.from(GameActivity.this).inflate(layoutId, null);
                    //绑定数据
                    ImageView iv_close_Apply = contentView.findViewById(R.id.iv_close);
                    iv_close_Apply.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //关闭popwndow
                            popApplyWindow.dismiss();
                        }
                    });
                    rv_apply_info = contentView.findViewById(R.id.rv_apply_info);
                    mLayoutManager = new LinearLayoutManager(GameActivity.this);
                    rv_apply_info.setLayoutManager(mLayoutManager);
                    //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
                    rv_apply_info.setHasFixedSize(true);
                    rv_apply_info.addItemDecoration(new DividerItemDecoration(
                            GameActivity.this, DividerItemDecoration.VERTICAL));
                    //创建并设置Adapter


                    //mAdapter = new SelectCityAdapter(mCity);
                    mAdapter = new ControlInApplyAdapter(GameActivity.this, new ControlInApplyAdapter.onButtonClick() {
                        @Override
                        public void onButtonClick(final ApplyInfo applyInfo) {
                            //提交后台
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        //拼装url字符串

                                        DzApplication applicatoin = (DzApplication) getApplication();
                                        JSONObject jsonObj = new JSONObject();
                                        jsonObj.put("userid", applyInfo.requestuserid);
                                        jsonObj.put("requestid", applyInfo.requestid);
                                        Boolean bPermit = false;
                                        if (applyInfo.ispermit == 0) {
                                            bPermit = false;
                                        } else if (applyInfo.ispermit == 1) {
                                            bPermit = true;
                                        }
                                        jsonObj.put("ispermit", bPermit);


                                        String strURL = getString(R.string.url_remote);
                                        strURL += "func=setrequesttakeininfo&param=" + jsonObj.toString();

                                        URL url = new URL(strURL);
                                        Request request = new Request.Builder().url(strURL).build();
                                        Response response = DzApplication.getHttpClient().newCall(request).execute();
                                        String result = response.body().string();
                                        //{"requesttakeininfo":[{"requestid":16,"requestuserid":1008,"usernickname":"我是无名","requesttakeinchips":200,"requestpermittakeinchips":200,"ispermit":2}]}
                                        JSONObject jsonObject = new JSONObject(result);
                                        if (jsonObject.getInt("ret") == 0) {
                                            //告诉申请者，审核结果RequestTakeInRet|{‘IsPermit’:true,’permittakein’:300}
                                            JSONObject json = new JSONObject();
                                            json.put("ispermit", bPermit);
                                            json.put("permittakein", applyInfo.requesttakeinchips);
                                            String sendstr = "requesttakeinret|" + json.toString();
                                            cn.jpush.im.android.api.model.Message message = JMessageClient.createSingleTextMessage(applyInfo.requestuserid + "", getString(R.string.app_key), sendstr);

                                            message.setOnSendCompleteCallback(new BasicCallback() {
                                                @Override
                                                public void gotResult(int responseCode, String responseDesc) {
                                                    if (responseCode == 0) {

                                                        // 消息发送成功
                                                    } else {
                                                        // 消息发送失败
                                                        ToastUtil.showToastInScreenCenter(GameActivity.this, "发送审核信息失败，请重新申请！");
                                                    }
                                                }
                                            });
                                            JMessageClient.sendMessage(message);
                                            jsonObj = new JSONObject();
                                            jsonObj.put("userid", applicatoin.getUserId());
                                            jsonObj.put("tableid", gameId);


                                            strURL = getString(R.string.url_remote);
                                            strURL += "func=getrequesttakeininfo&param=" + jsonObj.toString();

                                            url = new URL(strURL);
                                            request = new Request.Builder().url(strURL).build();
                                            response = DzApplication.getHttpClient().newCall(request).execute();
                                            result = response.body().string();
                                            //{"requesttakeininfo":[{"requestid":16,"requestuserid":1008,"usernickname":"我是无名","requesttakeinchips":200,"requestpermittakeinchips":200,"ispermit":2}]}
                                            jsonObject = new JSONObject(result);
                                            mlistApplyInfo = new ArrayList<ApplyInfo>();
                                            JSONArray jsonApplyArray = new JSONArray(jsonObject.getString("requesttakeininfo"));
                                            for (int i = 0; i < jsonApplyArray.length(); i++) {
                                                JSONObject jsonApplyInfo = new JSONObject(jsonApplyArray.get(i).toString());
                                                ApplyInfo applyInfo = new ApplyInfo();
                                                applyInfo.tableId = gameId;
                                                applyInfo.tablename = gameHouseName;
                                                applyInfo.requestid = jsonApplyInfo.getInt("requestid");
                                                applyInfo.requestuserid = jsonApplyInfo.getInt("requestuserid");
                                                applyInfo.usernickname = jsonApplyInfo.getString("usernickname");
                                                applyInfo.requesttakeinchips = jsonApplyInfo.getInt("requesttakeinchips");
                                                applyInfo.requestpermittakeinchips = jsonApplyInfo.getInt("requestpermittakeinchips");
                                                applyInfo.ispermit = jsonApplyInfo.getInt("ispermit");
                                                mlistApplyInfo.add(applyInfo);


                                            }


                                            mainThreadHandler.sendEmptyMessage(MESSAGE_UPDATE_APPLY_INFO);
                                        } else {
                                            ToastUtil.showToastInScreenCenter(GameActivity.this, "带入申请审核不成功!");
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ToastUtil.showToastInScreenCenter(GameActivity.this, "带入申请审核异常，请稍后重试!" + e.toString());
                                    }

                                }
                            });
                            thread.start();


                        }
                    });

                    DividerItemDecoration decoration = new DividerItemDecoration(GameActivity.this, DividerItemDecoration.VERTICAL);
                    decoration.setDrawable(ContextCompat.getDrawable(GameActivity.this, R.drawable.store_user_level_list_divide_drawable));
                    rv_apply_info.addItemDecoration(decoration);
                    rv_apply_info.setAdapter(mAdapter);
                    mAdapter.setData(mlistApplyInfo);

                    int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    contentView.measure(width, height);
                    int height1 = contentView.getMeasuredHeight();
                    int width1 = contentView.getMeasuredWidth();
                    int popWidth = UITools.convertDpToPixel(300, GameActivity.this);
                    int popHeight = UITools.convertDpToPixel(310, GameActivity.this);
                    popApplyWindow = new PopupWindow(contentView, popWidth, popHeight, true);

                    popApplyWindow.setFocusable(true);
                    // 设置允许在外点击消失
                    popApplyWindow.setOutsideTouchable(true);
                    // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
                    popApplyWindow.setBackgroundDrawable(new ColorDrawable());
                    // 设置好参数之后再show
                    popApplyWindow.showAtLocation(GameActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);

                    break;
                case MESSAGE_UPDATE_APPLY_INFO:
                    mAdapter.setData(mlistApplyInfo);
                    break;
                case MESSAGE_INFO_DO_ANIMATION:
                    try {
                        JSONObject jsonObject = new JSONObject((String) msg.obj);
                        int fromseatindex = jsonObject.optInt("fromseatindex", -1);
                        int toseatindex = jsonObject.optInt("toseatindex", -1);
                        int animationid = jsonObject.optInt("animationid", -1);
                        int userid = jsonObject.optInt("userid", -1);
                        if (userid == -1) {
                            Logger.e("QIPU", "do animation ret");
//                            beginAnimation();
                        } else {
                            beginAnimation(fromseatindex, toseatindex, animationid);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_INFO_TABLE_VOICE:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        String voicePath = jsonObject.optString("voicepath");
                        int userId = jsonObject.optInt("userid");
                        int seatIndex = mGameUser.get(userId).seatindex;
                        //创建下载任务,downloadUrl就是下载链接
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(voicePath));
//指定下载路径和下载文件名

                        String voiceFilePath = getExternalFilesDir("Download") + "/" + "voice.mp3";
                        File voiceFile = new File(voiceFilePath);
                        if (voiceFile.exists()) {
                            voiceFile.delete();
                        }
//                        request.setDestinationInExternalPublicDir("Download", "voice.mp4");
                        request.setDestinationInExternalFilesDir(GameActivity.this, "Download", "voice.mp3");
//                        File downloadDir = getDir("download", Context.MODE_PRIVATE);
//                        File voiceFiel = new File(downloadDir.getAbsolutePath(), "voice.wav");
//                        request.setDestinationUri(Uri.fromFile(voiceFiel));
//获取下载管理器
                        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
//将下载任务加入下载队列，否则不会进行下载
                        downloadTaskId = downloadManager.enqueue(request);
                        for (Integer indexx : downloadBroadcastReceiverMap.keySet()) {
                            if (indexx != seatIndex) {
                                unregisterReceiver(downloadBroadcastReceiverMap.get(indexx));
                            }
                        }
                        DownloadBroadcastReceiver receiver = downloadBroadcastReceiverMap.get(seatIndex);
                        if (receiver == null) {
                            receiver = new DownloadBroadcastReceiver(seatIndex);
                            downloadBroadcastReceiverMap.put(seatIndex, receiver);
                        }
                        registerReceiver(receiver,
                                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;

                case MESSAGE_INFO_BUY_SURANCE:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        int buyUserId = jsonObject.optInt("userid");
                        int buychips = jsonObject.optInt("buychips");
                        JSONArray cardinfo = jsonObject.optJSONArray("buyouts");
//                        JSONArray jsonArray = new JSONArray(cardinfo);
                        String userName = mGameUser.get(buyUserId).nickName;
                        if (null == cardinfo) {
                            ToastUtil.showToastInScreenCenter(GameActivity.this, userName + "未购买保险");
                        } else {
                            ToastUtil.showToastInScreenCenter(GameActivity.this, userName + "购买"
                                    + cardinfo.length() + "个outs，投保" + buychips);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    if (assuranceDialog != null) {
                        assuranceDialog.dismiss();
                    }
                    break;

                case MESSAGE_INFO_IF_BUY_ASSURANCE:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        int buyUserId = jsonObject.optInt("userid");
                        int buychips = jsonObject.optInt("buychips");
                        boolean isBuyassurance = jsonObject.optBoolean("isonsurance");
                        int buyouts = jsonObject.optInt("buyouts");
                        int winchips = jsonObject.optInt("winchips");
                        if (isBuyassurance) {
                            ToastUtil.showToastInScreenCenter(GameActivity.this,
                                    mGameUser.get(buyUserId).nickName + "买中保险, 购买"
                                            + buyouts + "个outs，投保" + buychips + ", 赢的保额" + winchips);
//                            int remainChips = mGameUser.get(buyUserId).remainchips;
//                            mGameUser.get(buyUserId).remainchips = remainChips + winchips - buyouts;
                        } else {
                            ToastUtil.showToastInScreenCenter(GameActivity.this,
                                    mGameUser.get(buyUserId).nickName + "未买中保险");
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;

                case MESSAGE_INFO_DO_ASSURANCE:
                    try {
                        String retData = (String) msg.obj;
                        Logger.i(TAG, "retData : " + retData);
                        JSONObject jsonRet = new JSONObject(retData);
                        JSONArray jsonArray = jsonRet.optJSONArray("winsuranceplayers");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int userId = jsonObject.optInt("userid", 0);
                            int winchips = jsonObject.optInt("winchips", 0);
                            int remainChips = mGameUser.get(userId).remainchips;
                            int seatindex = jsonObject.optInt("seatindex", 0);
                            mGameUser.get(userId).remainchips = remainChips + winchips;
                            View view = mSeatObjects.get(seatindex);
                            TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);
                            tv_goldcoin.setText(String.valueOf(mGameUser.get(userId).remainchips));

                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_INFO_REQUESR_BUY_ASSURANCE:

                    try {
                        String retData = (String) msg.obj;
                        showAssuranceDialog(retData);
                        Logger.i(TAG, "retData : " + retData);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;

                case MESSAGE_INFO_TABLE_TIMEOUT:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        Intent intent = new Intent(GameActivity.this,
                                GameDetailedHistoryActivity.class);
                        intent.putExtra("gametableid", jsonObject.optInt("tableid"));
                        startActivity(intent);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_INFO_ALARM_TIMEOUT:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "离牌桌结束还有"
                                + jsonObject.optInt("remainminutes") + "分钟");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_INFO_WIN_27:
                    try {

                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);

                        Gson gson = new Gson();
                        Type type = new TypeToken<Game27Bean>() {
                        }.getType();
                        Game27Bean game27Bean = gson.fromJson(retData, type);
                        show27Image();

                        int winPlayerRemain = mGameUser.get(game27Bean.getUserid()).remainchips;
                        mGameUser.get(game27Bean.getUserid()).remainchips = winPlayerRemain + game27Bean.getWinchips();
                        for (Game27Bean.RoundseatsBean bean : game27Bean.getRoundseats()) {
                            int roundPlayerRemain = mGameUser.get(bean.getUserid()).remainchips;
                            mGameUser.get(bean.getUserid()).remainchips = roundPlayerRemain - game27Bean.getBb();
                        }

                        mp = MediaPlayer.create(GameActivity.this, R.raw.win27);
                        mp.start();

                        int winUserId = jsonObject.optInt("userid");
                        int seatindex = jsonObject.optInt("seatindex");
                        int winchips = jsonObject.optInt("winchips");
                        int bb = jsonObject.optInt("bb");
                        int jackpot = jsonObject.optInt("jackpot");
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    break;
                case MESSAGE_RET_BACK_SEAT:
//                    showWindow(layout_parent, true);
                    isBackToSeatOrIncreaseChips = true;
                    break;

                case MESSAGE_INFO_RAISE_BLIND:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        int sb = jsonObject.optInt("sb");
                        int bb = jsonObject.optInt("bb");
                        int fb = jsonObject.optInt("fb");
                        String nextraisetime = jsonObject.optString("nextraisetime");
                        int level = jsonObject.optInt("level");
                        ToastUtil.showToastInScreenCenter(GameActivity.this,
                                "升级到级别" + level + ", 小盲" + sb + ",大盲" + bb + ",前注" + fb);
                        blindCount.setText("盲注:L" + level + " " + sb + "/" + bb + " " + fb);
                        mTableInfo.bigblind = bb;
                        mTableInfo.smallblind = sb;
                        mTableInfo.blindlevel = level;
                        mTableInfo.nextraisetime = nextraisetime;
                        if (isMatch) {
                            String nextTime;
                            try {
                                String[] nextRaiseTimes = mTableInfo.nextraisetime.split("T");
                                String[] nextTimes = nextRaiseTimes[1].split("\\.");
                                nextTime = nextTimes[0];
                            } catch (Throwable e) {
                                e.printStackTrace();
                                nextTime = mTableInfo.nextraisetime;
                            }

                            nextBlindTime.setText("下次升盲时间:" + nextTime);
                        }
                    } catch (Throwable w) {
                        w.printStackTrace();
                    }

                    break;

                case MESSAGE_INFO_MATCH_STOP:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        int matchId = jsonObject.optInt("matchid");
                        Logger.i(TAG, "MESSAGE_INFO_MATCH_STOP matchid : " + matchId);

                        Intent intent = new Intent(GameActivity.this,
                                MatchRecordDetailPageActivity.class);
                        intent.putExtra("match_id", matchId);
                        startActivity(intent);
                    } catch (Throwable w) {
                        w.printStackTrace();
                    }
                    break;

                case MESSAGE_INFO_PLAYER_AUTO:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        int tableid = jsonObject.optInt("tableid");
                        int userid = jsonObject.optInt("userid");
                        int seatindex = jsonObject.optInt("seatindex");

                        //TODO: 显示托管（半透明）头像上
                        mSeatObjects.get(seatindex).findViewById(R.id.seat_auto_play_tv).setVisibility(View.VISIBLE);
                    } catch (Throwable w) {
                        w.printStackTrace();
                    }
                    break;

                case MESSAGE_RET_ADD_WAIT_ACTION_TIME:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        int ret = jsonObject.optInt("ret");
                        String errorMsg = jsonObject.optString("msg");
//                        addTimeLayout.setVisibility(View.INVISIBLE);
//                        addTimeFee.setVisibility(View.INVISIBLE);
                        if (ret != 0) {
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "增加时间失败: " + errorMsg);
                        }
                    } catch (Throwable w) {
                        w.printStackTrace();
                    }
                    break;

                case MESSAGE_INFO_ADD_WAIT_ACTION_TIME:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        int tableid = jsonObject.optInt("tableid");
                        int userid = jsonObject.optInt("userid");
                        int seatindex = jsonObject.optInt("seatindex");
                        int addtime = jsonObject.optInt("addtime");
                        int remainwaittime = jsonObject.optInt("remainwaittime");
                        ToastUtil.showToastInScreenCenter(GameActivity.this,
                                mGameUser.get(userid).nickName + "延迟" + addtime + "秒");
                        if (holdSeatTimers.containsKey(seatindex)) {
                            holdSeatTimers.get(seatindex).cancel();
                        }

                        if (mTimerObjects.containsKey(seatindex)) {
                            mTimerObjects.get(seatindex).cancel();
                            mSeatObjects.get(seatindex).findViewById(R.id.seat_countdown_timer_tv).setVisibility(View.INVISIBLE);
                        }
                        addTimer(seatindex, 0, remainwaittime);
                    } catch (Throwable w) {
                        w.printStackTrace();
                    }
                    break;
                case MESSAGE_INFO_BEST_HAND:
                    try {
                        String retData = (String) msg.obj;
                        JSONObject jsonObject = new JSONObject(retData);
                        String type = jsonObject.optString("type");
                        infbesthand(type);
                    } catch (Throwable w) {
                        w.printStackTrace();
                    }
                    break;

            }


        }
    };

    private void handleInfoWaitAction(Message msg, int needwaitactiontime) {
        try {
            //等待操作
            String dataReturn = (String) msg.obj;
            Log.v("RABBY", "MESSAGE_INFO_WAIT_ACTION:" + msg.obj);
            JSONObject jsonReturn = new JSONObject(dataReturn);
            final int currentUserId = application.getUserId();

            int sb = jsonReturn.optInt("sb");
            int bb = jsonReturn.optInt("bb");

            if (jsonReturn.getInt("userid") == currentUserId) {
                //自己
                mp = MediaPlayer.create(GameActivity.this, R.raw.waitaction);
                mp.start();
                int curpots = jsonReturn.optInt("curpots");
                maxpaidchips = jsonReturn.optInt("maxpaidchips");
                mAction.setVisibility(View.VISIBLE);
                mButtonRaise.setVisibility(View.INVISIBLE);
                blind_layout.setVisibility(View.INVISIBLE);
                mTVRaise.setVisibility(View.INVISIBLE);
                mButtonCheck.setVisibility(View.INVISIBLE);
                mTVCheck.setVisibility(View.INVISIBLE);
                mButtonFold.setVisibility(View.INVISIBLE);
                mTVFold.setVisibility(View.INVISIBLE);

                JSONArray jsonNeedAction = jsonReturn.getJSONArray("needaction");
                int lastraisechips = jsonReturn.optInt("lastraisechips");
                int addwaitactiontimes = jsonReturn.optInt("addwaitactiontimes");
                addTimeLayout.setVisibility(View.VISIBLE);
                addTimeFee.setVisibility(View.VISIBLE);

                if (addwaitactiontimes == 0) {
                    addTimeFee.setText("免费");
                } else {
                    addTimeFee.setText(addwaitactiontimes + "");
                }

                if (lastraisechips == 0) {
                    minChip = maxpaidchips + bb;
                    maxChip = mGameUser.get(currentUserId).remainchips + mGameUser.get(currentUserId).amountchips;
                    step = sb;
                } else {
                    minChip = maxpaidchips + lastraisechips;
                    maxChip = mGameUser.get(currentUserId).remainchips + mGameUser.get(currentUserId).amountchips;
                    step = sb;
                }
                for (int i = 0; i < jsonNeedAction.length(); i++) {
                    int action = jsonNeedAction.getInt(i);
                    TextView tv_goldcoin = mSeatObjects.get(getUserIndex()).findViewById(R.id.tv_goldcoin);
//                                maxChip = Integer.parseInt(tv_goldcoin.getText().toString());
//                    maxChip = mGameUser.get(currentUserId).remainchips;

                    switch (action) {
                        case 0:
                            //bet
                            mButtonRaise.setVisibility(View.VISIBLE);
                            mTVRaise.setVisibility(View.VISIBLE);
                            mTVRaise.setText("下注");
                            blind_layout.setVisibility(View.VISIBLE);
                            if (minChip >= mGameUser.get(currentUserId).remainchips
                                    + mGameUser.get(currentUserId).amountchips) {
                                mTVRaise.setText("AllIn");
                            }
                            quickDoActionNew(currentUserId, curpots, maxpaidchips);


                            break;
                        case 1:
                            mButtonCheck.setVisibility(View.VISIBLE);
                            mTVCheck.setVisibility(View.VISIBLE);
                            mTVCheck.setText("跟注");

                            break;
                        case 2:
                            mButtonFold.setVisibility(View.VISIBLE);
                            mTVFold.setVisibility(View.VISIBLE);
                            mTVFold.setText("弃牌");

                            break;
                        case 3:
                            mButtonCheck.setVisibility(View.VISIBLE);
                            mTVCheck.setVisibility(View.VISIBLE);
                            mTVCheck.setText("看牌");

                            break;
                        case 4:
                            mButtonRaise.setVisibility(View.VISIBLE);
                            mTVRaise.setVisibility(View.VISIBLE);
                            mTVRaise.setText("加注");
                            blind_layout.setVisibility(View.VISIBLE);
                            if (minChip >= mGameUser.get(currentUserId).remainchips
                                    + mGameUser.get(currentUserId).amountchips) {
                                mTVRaise.setText("AllIn");
                            }
                            maxpaidchips = jsonReturn.getInt("maxpaidchips");
//                                        TextView tv_chip = mChipObjects.get(getUserIndex()).findViewById(R.id.tv_chip);
//                            minChip = maxpaidchips - mGameUser.get(currentUserId).amountchips + 1;
                            quickDoActionNew(currentUserId, curpots, maxpaidchips);

                            break;
                        case 6:
                            mButtonCheck.setVisibility(View.VISIBLE);
                            mTVCheck.setVisibility(View.VISIBLE);
                            mTVCheck.setText("AllIn");
                            break;
                    }
                }
                int seatindex = jsonReturn.getInt("seatindex");
                int realIndex = seatindex;
                if (getUserIndex() != -1) {
                    realIndex = (seatindex - getUserIndex() + mTableInfo.seats.length) % mTableInfo.seats.length;
                }

//                int timeLen =

                if (needwaitactiontime > 0) {
                    addTimer(seatindex, realIndex, needwaitactiontime);
                    timer = new CountDownTimer(needwaitactiontime * 1000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            mp = MediaPlayer.create(GameActivity.this, R.raw.timeovertipsound);
                            mp.start();

                        }
                    };
                    timer.start();
                } else {
                    addTimer(seatindex, realIndex, jsonReturn.getInt("waitseconds"));
                    timer = new CountDownTimer(jsonReturn.getInt("waitseconds") * 1000, 1000) {

                        @Override
                        public void onTick(long millisUntilFinished) {

                        }

                        @Override
                        public void onFinish() {
                            mp = MediaPlayer.create(GameActivity.this, R.raw.timeovertipsound);
                            mp.start();

                        }
                    };
                    timer.start();
                }


            } else {
                int seatindex = jsonReturn.getInt("seatindex");
                int realIndex = seatindex;
                if (getUserIndex() != -1) {
                    realIndex = (seatindex - getUserIndex() + mTableInfo.seats.length) % mTableInfo.seats.length;
                }
                //展现时钟
                addTimer(seatindex, realIndex, jsonReturn.getInt("waitseconds"));
            }


        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToastInScreenCenter(GameActivity.this, "等待操作处理错误，错误内容为" + e.getMessage());
        }
    }

    private void clearTableViews(List<Integer> indexs, List<Integer> dismissAutoPlay) {
        mD.setVisibility(View.INVISIBLE);
        //游戏牌
        for (int key : mGameCardsObjects.keySet()) {
            mGameCardsObjects.get(key).setVisibility(View.INVISIBLE);
        }
        //自己的牌
        for (int key : mMyCardsObjects.keySet()) {
            mMyCardsObjects.get(key).setVisibility(View.INVISIBLE);
            switch (key) {
                case 0:
                    mMyCardsObjects.get(0).findViewById(R.id.mycard_iv_card1_chose_icon).setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    mMyCardsObjects.get(1).findViewById(R.id.mycard_iv_card2_chose_icon).setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    mMyCardsObjects.get(2).findViewById(R.id.mycard_iv_card3_chose_icon).setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    mMyCardsObjects.get(3).findViewById(R.id.mycard_iv_card4_chose_icon).setVisibility(View.INVISIBLE);
                    break;
            }
        }
        if (bestHandTv != null) {
            bestHandTv.setVisibility(View.INVISIBLE);
        }
        clearAllAmountChips();
        //tip
        for (int key : mTipObjects.keySet()) {
            mTipObjects.get(key).setVisibility(View.INVISIBLE);
        }
        //chip
        for (int key : mChipObjects.keySet()) {
            if (!indexs.contains(key)) {
                mChipObjects.get(key).setVisibility(View.INVISIBLE);
            }
        }
        mMessage.setVisibility(View.INVISIBLE);
        //pot
        for (int key : mPoolObjects.keySet()) {
            mPoolObjects.get(key).setVisibility(View.INVISIBLE);
        }

        //cardback
        for (int key : mCardBackObjects.keySet()) {
            mCardBackObjects.get(key).setVisibility(View.INVISIBLE);
        }

        for (int key : winnerAnimation.keySet()) {
            winnerAnimation.get(key).setVisibility(View.INVISIBLE);
        }

        for (int key : winnerAnimationDrawable.keySet()) {
            winnerAnimationDrawable.get(key).stop();
        }

        for (int key : mSeatObjects.keySet()) {
            mSeatObjects.get(key).findViewById(R.id.win_icon).setVisibility(View.INVISIBLE);
            mSeatObjects.get(key).findViewById(R.id.win_chips).setVisibility(View.INVISIBLE);
            if (!indexs.contains(key)) {
                if (((TextView)mSeatObjects.get(key)
                        .findViewById(R.id.seat_countdown_timer_tv)).getCurrentTextColor() != Color.RED) {

                    mSeatObjects.get(key).findViewById(R.id.seat_countdown_timer_tv).setVisibility(View.INVISIBLE);
                    if (holdSeatTimers.containsKey(key)) {
                        holdSeatTimers.get(key).cancel();
                    }
                    if (mTimerObjects.containsKey(key)) {
                        mTimerObjects.get(key).cancel();
                    }
                }
            }
            if (!dismissAutoPlay.contains(key)) {
                mSeatObjects.get(key).findViewById(R.id.seat_auto_play_tv).setVisibility(View.INVISIBLE);
            }
        }

        for (int i = 0; i < publicCards.length; i++) {
            publicCards[i] = null;
        }

        seeNextCard.setVisibility(View.INVISIBLE);

        try {
            //showcards
            for (int key : mShowCardViews.keySet()) {
                mShowCardViews.get(key).setVisibility(View.INVISIBLE);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void quickDoActionNew(final int currentUserId, int curpots, int maxpaidchips) {
        int qualValue, tripleValue, doubleValue;

        if (maxpaidchips == 0) {
            doubleValue = 2 * mTableInfo.bigblind;
            tripleValue = 3 * mTableInfo.bigblind;
            qualValue = 4 * mTableInfo.bigblind;
        } else {

            qualValue = 4 * maxpaidchips;
            tripleValue = 3 * maxpaidchips;
            doubleValue = 2 * maxpaidchips;
        }

        String doubleContent = null, tripleContent = null, qualContent = null;
        if (doubleValue >= mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips) {
            doubleContent = "allin";
            doubleValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
        }

        if (tripleValue >= mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips) {
            tripleContent = "allin";
            tripleValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
        }

        if (qualValue >= mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips) {
            qualContent = "allin";
            qualValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
        }

        final TextView doubleTV = (TextView) blind_layout.findViewById(R.id.game_action_double);
        final TextView doubleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_double_chips);
        doubleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentDoActionMessage(doubleTV, currentUserId, doubleChipsTV);
            }
        });
        doubleTV.setText(doubleContent == null ? "2倍" : doubleContent);
        doubleChipsTV.setText("" + doubleValue);
        final TextView tripleTV = (TextView) blind_layout.findViewById(R.id.game_action_triple);
        tripleTV.setText(tripleContent == null ? "3倍" : tripleContent);
        final TextView tripleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_triple_chips);
        tripleChipsTV.setText("" + tripleValue);
        tripleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentDoActionMessage(tripleTV, currentUserId, tripleChipsTV);
            }
        });
        final TextView quadrupleTV = (TextView) blind_layout.findViewById(R.id.game_action_quadruple);
        quadrupleTV.setText(qualContent == null ? "4倍" : qualContent);
        final TextView quadrupleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_quadruple_chips);
        quadrupleChipsTV.setText("" + qualValue);
        quadrupleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentDoActionMessage(quadrupleTV, currentUserId, quadrupleChipsTV);
            }
        });

        if ("AllIn".equalsIgnoreCase(doubleContent)) {
            doubleTV.setVisibility(View.INVISIBLE);
            doubleChipsTV.setVisibility(View.INVISIBLE);
        }
        if ("AllIn".equalsIgnoreCase(tripleContent)) {
            tripleTV.setVisibility(View.INVISIBLE);
            tripleChipsTV.setVisibility(View.INVISIBLE);
        }
        if ("AllIn".equalsIgnoreCase(qualContent)) {
            quadrupleTV.setVisibility(View.INVISIBLE);
            quadrupleChipsTV.setVisibility(View.INVISIBLE);
        }
    }

    private void quickDoAction(final int currentUserId, int curpots, int maxpaidchips) {
        if ((curpots / 2) <= (4 * mTableInfo.bigblind)) {
            int qualValue, tripleValue, doubleValue;

            if (4 * mTableInfo.bigblind > maxpaidchips) {
                qualValue = 4 * mTableInfo.bigblind;
            } else {
                qualValue = maxpaidchips + 1;
            }

            if (3 * mTableInfo.bigblind > maxpaidchips) {
                tripleValue = 3 * mTableInfo.bigblind;
            } else {
                tripleValue = qualValue;
            }

            if (2 * mTableInfo.bigblind > maxpaidchips) {
                doubleValue = 2 * mTableInfo.bigblind;
            } else {
                doubleValue = tripleValue;
            }

            String doubleContent = null, tripleContent = null, qualContent = null;
            if (doubleValue - mGameUser.get(currentUserId).amountchips >= mGameUser.get(currentUserId).remainchips) {
                doubleValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
                doubleContent = "allin";
            }

            if (tripleValue - mGameUser.get(currentUserId).amountchips >= mGameUser.get(currentUserId).remainchips) {
                tripleValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
                tripleContent = "allin";
            }

            if (qualValue - mGameUser.get(currentUserId).amountchips >= mGameUser.get(currentUserId).remainchips) {
                qualValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
                qualContent = "allin";
            }

            final TextView doubleTV = (TextView) blind_layout.findViewById(R.id.game_action_double);
            final TextView doubleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_double_chips);
            doubleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sentDoActionMessage(doubleTV, currentUserId, doubleChipsTV);
                }
            });
            doubleTV.setText(doubleContent == null ? "2倍大盲" : doubleContent);
            doubleChipsTV.setText("" + doubleValue);
            final TextView tripleTV = (TextView) blind_layout.findViewById(R.id.game_action_triple);
            tripleTV.setText(tripleContent == null ? "3倍大盲" : tripleContent);
            final TextView tripleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_triple_chips);
            tripleChipsTV.setText("" + tripleValue);
            tripleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sentDoActionMessage(tripleTV, currentUserId, tripleChipsTV);
                }
            });
            final TextView quadrupleTV = (TextView) blind_layout.findViewById(R.id.game_action_quadruple);
            quadrupleTV.setText(qualContent == null ? "4倍大盲" : qualContent);
            final TextView quadrupleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_quadruple_chips);
            quadrupleChipsTV.setText("" + qualValue);
            quadrupleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sentDoActionMessage(quadrupleTV, currentUserId, quadrupleChipsTV);
                }
            });
        } else {
            int qualValue, tripleValue, doubleValue;

            if (curpots > maxpaidchips) {
                qualValue = curpots;
            } else {
                qualValue = maxpaidchips + 1;
            }

            if ((2 * curpots / 3) > maxpaidchips) {
                tripleValue = (2 * curpots / 3);
            } else {
                tripleValue = qualValue;
            }

            if ((curpots / 2) > maxpaidchips) {
                doubleValue = (curpots / 2);
            } else {
                doubleValue = tripleValue;
            }

            String doubleContent = null, tripleContent = null, qualContent = null;
            if (doubleValue - mGameUser.get(currentUserId).amountchips >= mGameUser.get(currentUserId).remainchips) {
                doubleValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
                doubleContent = "allin";
            }

            if (tripleValue - mGameUser.get(currentUserId).amountchips >= mGameUser.get(currentUserId).remainchips) {
                tripleValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
                tripleContent = "allin";
            }

            if (qualValue - mGameUser.get(currentUserId).amountchips >= mGameUser.get(currentUserId).remainchips) {
                qualValue = mGameUser.get(currentUserId).amountchips + mGameUser.get(currentUserId).remainchips;
                qualContent = "allin";
            }
            final TextView doubleTV = (TextView) blind_layout.findViewById(R.id.game_action_double);
            doubleTV.setText(doubleContent == null ? "1/2底池" : doubleContent);
            final TextView doubleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_double_chips);
            doubleChipsTV.setText("" + doubleValue);
            doubleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sentDoActionMessage(doubleTV, currentUserId, doubleChipsTV);
                }
            });
            final TextView tripleTV = (TextView) blind_layout.findViewById(R.id.game_action_triple);
            tripleTV.setText(tripleContent == null ? "2/3底池" : tripleContent);
            final TextView tripleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_triple_chips);
            tripleChipsTV.setText("" + tripleValue);
            tripleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sentDoActionMessage(tripleTV, currentUserId, tripleChipsTV);
                }
            });
            final TextView quadrupleTV = (TextView) blind_layout.findViewById(R.id.game_action_quadruple);
            quadrupleTV.setText(qualContent == null ? "一倍底池" : qualContent);
            final TextView quadrupleChipsTV = (TextView) blind_layout.findViewById(R.id.game_action_quadruple_chips);
            quadrupleChipsTV.setText("" + qualValue);
            quadrupleTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sentDoActionMessage(quadrupleTV, currentUserId, quadrupleChipsTV);
                }
            });
        }
    }

    private void sentDoActionMessage(TextView doubleTV, int currentUserId, TextView doubleChipsTV) {
        try {

            String msg = Constant.GAME_DO_ACTION + "|";
            JSONObject jsonSend = new JSONObject();
            jsonSend.put("userid", application.getUserId());
            jsonSend.put("seatindex", getUserIndex());
            if (doubleTV.getText().toString().equalsIgnoreCase("allin")) {
                jsonSend.put("action", 6);
                jsonSend.put("amountchips", mGameUser.get(currentUserId).remainchips);
            } else {
                int action;
                if (mTVRaise.getText().toString().equalsIgnoreCase("下注")) {
                    action = 0;
                } else if (mTVRaise.getText().toString().equalsIgnoreCase("加注")) {
                    action = 4;
                } else if (mTVRaise.getText().toString().equalsIgnoreCase("AllIn")) {
                    action = 6;
                } else {
                    action = 4;
                }
                jsonSend.put("action", action);
                jsonSend.put("amountchips",
                        Integer.valueOf(doubleChipsTV.getText().toString()) - mGameUser.get(application.getUserId()).amountchips);
            }
            jsonSend.put("tableid", gameId);
            msg += jsonSend.toString().replace("$", "￥");
            msg += "$";
            myBinder.sendInfo(msg);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToastInScreenCenter(GameActivity.this, "弃牌出错！");
        }
    }

    private void show27Image() {
        final ImageView imageView = new ImageView(GameActivity.this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        imageView.setLayoutParams(layoutParams);
        imageView.bringToFront();
        imageView.setImageResource(R.drawable.game_27);
        layout_parent.addView(imageView);
        AlphaAnimation disAppearAnimation = new AlphaAnimation(1, 0);
        disAppearAnimation.setDuration(1000);
        imageView.startAnimation(disAppearAnimation);
        disAppearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private long downloadTaskId;

    private Map<Integer, DownloadBroadcastReceiver> downloadBroadcastReceiverMap = new HashMap<>();

    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        private int seatIndex = 0;

        public DownloadBroadcastReceiver(int seatIndex) {
            this.seatIndex = seatIndex;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
//            checkDownloadStatus();//检查下载状态
            try {
//                File tempFile = new File(Environment.getExternalStoragePublicDirectory("Download") + "/voice.mp4");
                File tempFile = new File(context.getExternalFilesDir("Download") + "/" + "voice.mp3");

                try {
                    String command = "chmod 777 " + tempFile.getAbsolutePath();
                    Runtime runtime = Runtime.getRuntime();

                    Process proc = runtime.exec(command);
                } catch (IOException e) {
                    Log.i("QIPU", "chmod fail!!!!");
                    e.printStackTrace();
                }

//                FileInputStream fis = new FileInputStream(tempFile);
                if (mp == null) {
                    mp = new MediaPlayer();
                } else {
                    mp.reset();
                }
//                mp = MediaPlayer.create(GameActivity.this, Uri.fromFile(tempFile));
                mp.setDataSource(GameActivity.this, Uri.fromFile(tempFile));
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mp.start();
                    }

                });
                final ImageView animView =  mSeatObjects.get(seatIndex).findViewById(R.id.seat_record_animation);
                animView.setVisibility(View.VISIBLE);
                animView.setBackgroundResource(R.drawable.record_play_anim);
                final AnimationDrawable animationDrawable = (AnimationDrawable) animView.getBackground();
                animationDrawable.setOneShot(false);
                int duration = 0;
                for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
                    duration += animationDrawable.getDuration(i);
                }
//                mainThreadHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        animView.setVisibility(View.INVISIBLE);
//                    }
//                }, duration);
                animationDrawable.start();
                mp.prepareAsync();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (animationDrawable != null && animView != null) {
                            animationDrawable.stop();
                            animView.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

//    private void checkDownloadStatus() {
//        DownloadManager.Query query = new DownloadManager.Query();
//        query.setFilterById(downloadTaskId);//筛选下载任务，传入任务ID，可变参数
//        Cursor c = downloadManager.query(query);
//        if (c.moveToFirst()) {
//            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            switch (status) {
//                case DownloadManager.STATUS_PAUSED:
//                    MLog.i(">>>下载暂停");
//                case DownloadManager.STATUS_PENDING:
//                    MLog.i(">>>下载延迟");
//                case DownloadManager.STATUS_RUNNING:
//                    MLog.i(">>>正在下载");
//                    break;
//                case DownloadManager.STATUS_SUCCESSFUL
//                    MLog.i(">>>下载完成");
//                    //下载完成安装APK
//                    //downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + versionName;
//                    installAPK(new File(downloadPath));
//                    break;
//                case DownloadManager.STATUS_FAILED:
//                    MLog.i(">>>下载失败");
//                    break;
//            }
//        }
//    }



    private Map<Integer, CountDownTimer> holdSeatTimers = new HashMap<>();

    private void addHoldSeatTimer(final int seatIndex, final int realIndex, final int timelen) {
//        TextView tv = (TextView) mHoldSeatTimerViewObjects.get(realIndex);
//        if (tv == null) {
//            int pixel24 = UITools.convertDpToPixel(14, GameActivity.this);
//            tv = new TextView(this);
//            tv.setGravity(Gravity.CENTER);
//            tv.setText(timelen + "");
//            tv.setTextColor(Color.RED);
//            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixel24);
//            TextPaint textPaint = tv.getPaint();
//            textPaint.setFakeBoldText(true);
//            layout_game.addView(tv);
//
//            int pixel30 = UITools.convertDpToPixel(30, GameActivity.this);
//            setPosition(tv, mHeadImageWidth, mHeadImageHeight, iSeatValue[realIndex][0] + (mSeatViewWidth - mHeadImageWidth) / 2, iSeatValue[realIndex][1] + mNameTextHeight + pixel30);
//            mHoldSeatTimerViewObjects.put(realIndex, tv);
//        }
//        tv.setVisibility(View.VISIBLE);
//        tv.setText(timelen + "");
        ((TextView)(mSeatObjects.get(seatIndex)
                .findViewById(R.id.seat_countdown_timer_tv))).setVisibility(View.VISIBLE);
        ((TextView)(mSeatObjects.get(seatIndex)
                .findViewById(R.id.seat_countdown_timer_tv))).setTextColor(Color.RED);
        ((TextView)(mSeatObjects.get(seatIndex)
                .findViewById(R.id.seat_countdown_timer_tv))).setText(timelen + "");

        CountDownTimer holdTimer;

        holdTimer = new CountDownTimer(timelen * 1000, 1000) {

                @Override
                public void onTick(final long millisUntilFinished) {
//                    final Runnable drawCicle = new Runnable() {
//                        @Override
//                        public void run() {
////                    iv.setStartAnimation(true, 0);
//                            final CustomCircleImageView imageView = mSeatObjects.get(seatIndex).findViewById(R.id.iv_user_head);
//                            final double angle = 1 - ((double) (millisUntilFinished / 1000F) / timelen);
//                            mainThreadHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    imageView.setStartAnimation(true, angle);
//                                }
//                            });
//                        }
//                    };
//                    mainThreadHandler.postDelayed(drawCicle, 100);
//                    ((TextView) (mHoldSeatTimerViewObjects.get(realIndex))).setText(millisUntilFinished / 1000 + "");
                    ((TextView)(mSeatObjects.get(seatIndex)
                            .findViewById(R.id.seat_countdown_timer_tv))).setText(millisUntilFinished / 1000 + "");

                }

                @Override
                public void onFinish() {
                    ((TextView)(mSeatObjects.get(seatIndex)
                            .findViewById(R.id.seat_countdown_timer_tv))).setVisibility(View.INVISIBLE);
                }
            };
        holdTimer.start();
        holdSeatTimers.put(seatIndex, holdTimer);

    }

    private void addTimer(final int seatIndex, final int realIndex, final int timelen) {
//        TextView tv = (TextView) mTimerViewObjects.get(realIndex);
//        if (tv == null) {
//            int pixel24 = UITools.convertDpToPixel(14, GameActivity.this);
//            tv = new TextView(this);
//            tv.setGravity(Gravity.CENTER);
//            tv.setText(timelen + "");
//            tv.setTextColor(Color.YELLOW);
//            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixel24);
//            TextPaint textPaint = tv.getPaint();
//            textPaint.setFakeBoldText(true);
//            layout_game.addView(tv);
//
//            int pixel30 = UITools.convertDpToPixel(30, GameActivity.this);
//            setPosition(tv, mHeadImageWidth, mHeadImageHeight, iSeatValue[realIndex][0] + (mSeatViewWidth - mHeadImageWidth) / 2, iSeatValue[realIndex][1] + mNameTextHeight + pixel30);
//            mTimerViewObjects.put(realIndex, tv);
//        }
//        tv.setText(timelen + "");
//        tv.setVisibility(View.VISIBLE);

        ((TextView)(mSeatObjects.get(seatIndex)
                .findViewById(R.id.seat_countdown_timer_tv))).setVisibility(View.VISIBLE);
        ((TextView)(mSeatObjects.get(seatIndex)
                .findViewById(R.id.seat_countdown_timer_tv))).setTextColor(Color.YELLOW);
        ((TextView)(mSeatObjects.get(seatIndex)
                .findViewById(R.id.seat_countdown_timer_tv))).setText(timelen + "");

        CountDownTimer timer;

        timer = new CountDownTimer(timelen * 1000, 1000) {

            @Override
            public void onTick(final long millisUntilFinished) {
//                    final Runnable drawCicle = new Runnable() {
//                        @Override
//                        public void run() {
////                    iv.setStartAnimation(true, 0);
//                            final CustomCircleImageView imageView = mSeatObjects.get(seatIndex).findViewById(R.id.iv_user_head);
//                            final double angle = 1 - ((double) (millisUntilFinished / 1000F) / timelen);
//                            mainThreadHandler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    imageView.setStartAnimation(true, angle);
//                                }
//                            });
//                        }
//                    };
//                    mainThreadHandler.postDelayed(drawCicle, 100);
//                ((TextView) (mTimerViewObjects.get(realIndex))).setText(millisUntilFinished / 1000 + "");
                ((TextView)(mSeatObjects.get(seatIndex)
                        .findViewById(R.id.seat_countdown_timer_tv))).setText(millisUntilFinished / 1000 + "");

            }

            @Override
            public void onFinish() {
                ((TextView)(mSeatObjects.get(seatIndex)
                        .findViewById(R.id.seat_countdown_timer_tv))).setVisibility(View.INVISIBLE);
            }
        };
        mTimerObjects.put(seatIndex, timer);

        timer.start();


    }

    //画空座位的图
    public Bitmap getEmptySeatBitMap(int width, int height, String text) {

        //边框：#92a8bd  背景：#20364e
        //文字颜色#8ba6c1
        int line_width = 6;//设置画线的宽度
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(line_width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#92a8bd"));
        canvas.drawCircle(width / 2, height / 2, width / 2 - line_width / 2, paint);

        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setStrokeWidth(line_width);
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);
        paint2.setColor(Color.parseColor("#20364e"));
        canvas.drawCircle(width / 2, height / 2, width / 2 - line_width, paint2);
        //canvas.drawBitmap(bmp, 0, 0, null);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(onSeatTextSize);
        textPaint.setColor(Color.parseColor("#8ba6c1"));
        //canvas.drawText();text是左下角的坐标为画的起始值

        float textWidth = textPaint.measureText(text);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = (int) Math.ceil(fm.leading - fm.ascent) - 2;
        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + textHeight / 2, textPaint);
        return newBitmap;
    }

    // 收到tableinfo之后，初始化牌桌的信息
    private void initGameInfo() {
        if (!isMatch) {
            shareCode.setText("邀请码:" + mTableInfo.shareCode);
        }
        tableName.setText("牌桌名:" + mTableInfo.tablename);
        if (isMatch) {
            blindCount.setText("盲注:" + mTableInfo.smallblind + "/" + mTableInfo.bigblind + "/" + mTableInfo.blindlevel);
            blindGap.setText("升盲间隔:" + mTableInfo.blindraisetime + "分钟");
            String nextTime;
            try {
                String[] nextRaiseTimes = mTableInfo.nextraisetime.split("T");
                String[] nextTimes = nextRaiseTimes[1].split("\\.");
                nextTime = nextTimes[0];
            } catch (Throwable e) {
                e.printStackTrace();
                nextTime = mTableInfo.nextraisetime;
            }

            nextBlindTime.setText("下次升盲时间:" + nextTime);
        } else {
            nextBlindTime.setVisibility(View.GONE);
            blindGap.setVisibility(View.GONE);
            blindCount.setText("盲注:" + mTableInfo.smallblind + "/" + mTableInfo.bigblind);
        }
        String control = mTableInfo.iscontroltakein ? "控制带入 " : "";
        String assuranceMsg = mTableInfo.issurance ? "保险 " : "";
        String ass27 = mTableInfo.is27 ? "27" : "";
        assurance.setText(control + assuranceMsg + ass27);


        //先根据收到的table信息初始化桌面的控件
        //用户在创建table，或者加入别人的table，或者中途退出进入，从非活动转为活动会收到信息
        int iSeatnumber = mTableInfo.seats.length;//牌局座位数目，其中对象可以为空

//        if (!bInitView) {
        initSeatXY(iSeatnumber);//根据人数来设定座位的位置
        //重新设置座位位置  （筹码位置，D的位置，tip的位置，cardback ，翻牌位置在需要的时候设置）
        if (iSeatnumber != 9) {
            //如果不为9，重新设置控件的位置,座位的显示，chip，tip，cardback的显示位置
            for (int i = 0; i < iSeatnumber; i++) {
                View view = mSeatObjects.get(iSeatnumber);
                AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(mSeatViewWidth, mSeatViewHeight, 0, 0);
                lp.x = iSeatValue[iSeatnumber][0];
                lp.y = iSeatValue[iSeatnumber][1];
                view.setLayoutParams(lp);

            }
            //chip
            for (int i = 0; i < iSeatnumber; i++) {
                View view = mChipObjects.get(iSeatnumber);
                AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(mAmountChipViewWidth, mAmountChipVieHeight, 0, 0);
                lp.x = iAmountChipLocation[iSeatnumber][0];
                lp.y = iAmountChipLocation[iSeatnumber][1];
                view.setLayoutParams(lp);

            }

            //tip
            for (int i = 0; i < iSeatnumber; i++) {
                View view = mTipObjects.get(iSeatnumber);
                AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(mTipViewWidth, mTipViewHeight, 0, 0);
                lp.x = iTipLocation[iSeatnumber][0];
                lp.y = iTipLocation[iSeatnumber][1];
                view.setLayoutParams(lp);

            }
            //cardback都需要设置
            for (int i = 0; i < iSeatnumber; i++) {
                View view = mCardBackObjects.get(iSeatnumber);
                AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(mCardBackWidth, mCardBackHeight, 0, 0);
                lp.x = iCardBack[iSeatnumber][0];
                lp.y = iCardBack[iSeatnumber][1];
                view.setLayoutParams(lp);
            }
        }
//        }


        //底池
        for (int i = 0; i < mTableInfo.pots.length; i++) {
            View view;
            TextView textView;

            switch (i) {
                case 0:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool1);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool1);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
                case 1:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool2);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool2);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
                case 2:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool3);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool3);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
                case 3:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool4);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool2);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
                case 4:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool5);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool5);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
                case 5:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool6);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool6);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
                case 6:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool7);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool7);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
                case 7:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool8);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool8);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
                case 8:
                    if (mTableInfo.pots[i] > 0) {
                        view = findViewById(R.id.layout_pool9);
                        view.setVisibility(View.VISIBLE);
                        textView = (TextView) findViewById(R.id.tv_pool9);
                        textView.setText(mTableInfo.pots[i] + "");
                    }
                    break;
            }

        }
        //公牌
        mGameCards.setVisibility(View.VISIBLE);
        for (int i = 0; i < mTableInfo.comunitycards.length; i++) {
            ImageView iv;
            if (mTableInfo.comunitycards[i] != null) {

                switch (i) {
                    case 0:
                        iv = mGameCards.findViewById(R.id.iv_card1);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageBitmap(drawSingleCard(mTableInfo.comunitycards[i].suit, mTableInfo.comunitycards[i].member));

                        break;
                    case 1:
                        iv = mGameCards.findViewById(R.id.iv_card2);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageBitmap(drawSingleCard(mTableInfo.comunitycards[i].suit, mTableInfo.comunitycards[i].member));
                        break;
                    case 2:
                        iv = mGameCards.findViewById(R.id.iv_card3);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageBitmap(drawSingleCard(mTableInfo.comunitycards[i].suit, mTableInfo.comunitycards[i].member));
                        break;
                    case 3:
                        iv = mGameCards.findViewById(R.id.iv_card4);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageBitmap(drawSingleCard(mTableInfo.comunitycards[i].suit, mTableInfo.comunitycards[i].member));
                        break;
                    case 4:
                        iv = mGameCards.findViewById(R.id.iv_card5);
                        iv.setVisibility(View.VISIBLE);
                        iv.setImageBitmap(drawSingleCard(mTableInfo.comunitycards[i].suit, mTableInfo.comunitycards[i].member));
                        break;
                }
            }

        }

        mGameUser.clear();
        //先判断用户本身在不在座位中,同时记录 用户座位映射
        for (int ii = 0; ii < mTableInfo.players.length; ii++) {
            GameUser gameUser = new GameUser();
            gameUser.userId = mTableInfo.players[ii].userid;
            gameUser.nickName = mTableInfo.players[ii].nickname;
            gameUser.remainchips = mTableInfo.players[ii].remainchips;
            gameUser.userHeadPic = mTableInfo.players[ii].headpic;
            gameUser.seatindex = mTableInfo.players[ii].seatindex;
            gameUser.amountchips = mTableInfo.players[ii].amountchips;
            mGameUser.put(gameUser.userId, gameUser);

        }

        //如果用户在座位中，则先移位置,将用户的座位信息，下注信息，提示信息，cardback信息，做偏移
        int userindex = getUserIndex();
        if (userindex != -1) {
            //用户在座位中，则移位置
            for (int i = 0; i < iSeatnumber; i++) {
                int moveto = (i - userindex + iSeatnumber) % iSeatnumber;
                setPosition(mSeatObjects.get(i), mSeatViewWidth, mSeatViewHeight, iSeatValue[moveto][0], iSeatValue[moveto][1]);
                setPosition(mChipObjects.get(i), mAmountChipViewWidth, mAmountChipVieHeight, iAmountChipLocation[moveto][0], iAmountChipLocation[moveto][1]);
                setPosition(mTipObjects.get(i), mTipViewWidth, mTipViewHeight, iTipLocation[moveto][0], iTipLocation[moveto][1]);
                setPosition(mCardBackObjects.get(i), mCardBackWidth, mCardBackHeight, iCardBack[moveto][0], iCardBack[moveto][1]);
            }
        }

        //先根据用户状态来画座位
        for (int i = 0; i < iSeatnumber; i++) {
            if (mTableInfo.seats[i] != null) {
                //来计算，该位置的对象应该在本地位置

                int userId = mTableInfo.seats[i].userid;
                for (int j = 0; j < mTableInfo.players.length; j++) {
                    if (mTableInfo.players[j].userid == userId) {
                        int iRealIndex = i;
                        if (userindex != -1) {
                            //计算偏移位置
                            iRealIndex = (mTableInfo.players[j].seatindex - userindex + iSeatnumber) % (iSeatnumber);
                        }
                        //设置头像，金币值，名字
                        //此处不判断seatindex的值，因为是根据座位中的userid来的
                        View view = mSeatObjects.get(mTableInfo.players[j].seatindex);
                        ImageView iv = view.findViewById(R.id.iv_user_head);
                        TextView tv_name = view.findViewById(R.id.tv_user_name);
                        TextView tv_goldcoin = view.findViewById(R.id.tv_goldcoin);

                        tv_name.setText(mTableInfo.players[j].nickname);
                        tv_name.setVisibility(View.VISIBLE);

                        tv_goldcoin.setText(mTableInfo.players[j].remainchips + "");
                        tv_goldcoin.setVisibility(View.VISIBLE);

                        if (mTableInfo.players[j].headpic != null && !mTableInfo.players[j].headpic.equals("")) {
                            int ivWidth = UITools.convertDpToPixel(50, GameActivity.this);
                            int ivHeight = UITools.convertDpToPixel(50, GameActivity.this);
                            Picasso.with(getApplicationContext())
                                    .load(mTableInfo.players[j].headpic)
                                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                    .resize(ivWidth, ivHeight)
                                    .error(R.drawable.seat_empty)
                                    .transform(new CircleTransform(GameActivity.this))
                                    .into(iv);
                        }
//                        iv.setClickable(false);
                        mSeatOccuped.put(i, true);
//                        if (mTableInfo.seats[i].isbb) {
//                            //Tip为大盲
//                            View viewTip=  mTipObjects.get(iRealIndex);
//                            viewTip.setVisibility(View.VISIBLE);
//                            TextView tv_tip=(TextView)view.findViewById(R.id.tv_tip);
//                            tv_tip.setText("大盲");
//                        }
//                        if (mTableInfo.seats[i].issb) {
//                            //Tip为小盲
//                            View viewTip=  mTipObjects.get(iRealIndex);
//                            viewTip.setVisibility(View.VISIBLE);
//                            TextView tv_tip=(TextView)view.findViewById(R.id.tv_tip);
//                            tv_tip.setText("小盲");
//                        }

                        //庄家位，
                        if (mTableInfo.seats[i].isbutton) {
                            setPosition(mD, mDViewWidth, mDViewHeight, iD[iRealIndex][0], iD[iRealIndex][1]);
                            mD.setVisibility(View.VISIBLE);
                        }

                        //state  显示tip 等待，或者显示

                        if (mTableInfo.seats[i].state == 1) {
                            //Tip为大盲
                            View viewTip = mTipObjects.get(mTableInfo.players[j].seatindex);
                            viewTip.setVisibility(View.VISIBLE);
                            TextView tv_tip = viewTip.findViewById(R.id.tv_tip);
                            tv_tip.setText("等待");
                        }

                        if (mTableInfo.seats[i].state == 3) {
                            //等待回来倒计时
                            addHoldSeatTimer(mTableInfo.players[j].seatindex, iRealIndex, mTableInfo.seats[i].needholdtime);
                            if (mTableInfo.players[j].userid == application.getUserId()) {
                                mReturnSeat.setVisibility(View.VISIBLE);
                                mReturnSeat.bringToFront();
                                //
                            }
                            View viewTip = mTipObjects.get(mTableInfo.players[j].seatindex);
                            viewTip.setVisibility(View.VISIBLE);
                            TextView tv_tip = viewTip.findViewById(R.id.tv_tip);
                            tv_tip.setText("留座");

                        }

                        if (mTableInfo.seats[i].state == 6) {
                            //TODO: QIPU 显示托管
                            mSeatObjects.get(i).findViewById(R.id.seat_auto_play_tv).setVisibility(View.VISIBLE);
                        }

                        //根据用户信息来判断


                        View viewTip = mTipObjects.get(mTableInfo.players[j].seatindex);
                        TextView tv_tip = viewTip.findViewById(R.id.tv_tip);

                        /*public enum PlayAction
    {
        Bet,    //押注 - 押上筹码
        Call,   //跟进 - 跟随众人押上同等的注额
        Fold,   //收牌 / 不跟 - 放弃继续牌局的机会
        Check,  // 让牌 - 在无需跟进的情况下选择把决定“让”给下一位
        Raise,  // 加注 - 把现有的注金抬高
        Reraise,// 再加注 - 再别人加注以后回过来再加注
        Allin,   //全押 - 一次过把手上的筹码全押上
        NoAction,
        SB,
        BB,
        Straddle,
        ADDBB,  //补盲新加入局的玩家，补一个大盲。
        ANTE,

    }*/

                        switch (mTableInfo.players[j].lastplayaction) {
                            case 0:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("下注");
                                break;
                            case 1:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("跟注");
                                break;
                            case 2:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("弃牌");
                                break;
                            case 3:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("看牌");
                                break;
                            case 4:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("加注");
                                break;
                            case 6:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("All In");
                                break;
                            case 7:
                                viewTip.setVisibility(View.INVISIBLE);
                                tv_tip.setText("");
                                break;
                            case 8:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("小盲");
                                break;
                            case 9:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("大盲");
                                break;
                            case 10:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("Straddle");
                                break;
                            case 11:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("补盲");
                                break;
                            case 12:
                                viewTip.setVisibility(View.VISIBLE);
                                tv_tip.setText("Ante");
                                break;
                        }

                        //已下注
                        if (mTableInfo.players[j].amountchips > 0) {
                            View viewChip = mChipObjects.get(mTableInfo.players[j].seatindex);
                            viewChip.setVisibility(View.VISIBLE);
                            TextView tv = viewChip.findViewById(R.id.tv_chip);
                            tv.setVisibility(View.VISIBLE);
                            tv.setText(mTableInfo.players[j].amountchips + "");
                        }

                        //此处要重点测试一下
                        if (mTableInfo.players[j].needwaitactiontime > 0) {

//                            try {
//
//                                addTimer(mTableInfo.players[j].seatindex, iRealIndex, mTableInfo.players[j].needwaitactiontime);
//                                if (mTableInfo.players[j].userid == application.getUserId()) {
//                                    mp = MediaPlayer.create(GameActivity.this, R.raw.waitaction);
//                                    mp.start();
//                                    mAction.setVisibility(View.VISIBLE);
//                                    mButtonRaise.setVisibility(View.INVISIBLE);
//                                    blind_layout.setVisibility(View.INVISIBLE);
//                                    mTVRaise.setVisibility(View.INVISIBLE);
//                                    mButtonCheck.setVisibility(View.INVISIBLE);
//                                    mTVCheck.setVisibility(View.INVISIBLE);
//                                    mButtonFold.setVisibility(View.INVISIBLE);
//                                    mTVFold.setVisibility(View.INVISIBLE);
//
//                                    if (mTableInfo.players[j].waitactionparam.addwaitactiontimes != 0) {
//                                        addTimeFee.setText(String.valueOf(5));
//                                    }
//
//                                    if (mTableInfo.players[j].waitactionparam.lastraisechips == 0) {
//                                        minChip = maxpaidchips + mTableInfo.players[j].waitactionparam.bb;
//                                        maxChip = mGameUser.get(application.getUserId()).remainchips
//                                                + mGameUser.get(application.getUserId()).amountchips;
//                                        step = maxpaidchips + mTableInfo.players[j].waitactionparam.sb;
//                                    } else {
//                                        minChip = maxpaidchips + maxpaidchips + mTableInfo.players[j].waitactionparam.lastraisechips;
//                                        maxChip = mGameUser.get(application.getUserId()).remainchips
//                                                + mGameUser.get(application.getUserId()).amountchips;
//                                        step = maxpaidchips + mTableInfo.players[j].waitactionparam.sb;;
//                                    }
//
//
//                                    for (int k = 0; k < mTableInfo.players[j].waitactionparam.needaction.size(); k++) {
//                                        int action = mTableInfo.players[j].waitactionparam.needaction.get(k);
//                                        maxChip = mGameUser.get(mTableInfo.players[j].userid).remainchips;
//                                        switch (action) {
//                                            case 0:
//                                                //bet
//                                                mButtonRaise.setVisibility(View.VISIBLE);
//                                                mTVRaise.setVisibility(View.VISIBLE);
//                                                mTVRaise.setText("下注");
//                                                blind_layout.setVisibility(View.VISIBLE);
//                                                if (minChip >= mGameUser.get(application.getUserId()).remainchips
//                                                        + mGameUser.get(application.getUserId()).amountchips) {
//                                                    mTVRaise.setText("AllIn");
//                                                }
//                                                quickDoActionNew(mTableInfo.players[j].userid,
//                                                        0, mTableInfo.players[j].waitactionparam.maxpaidchips);
//
//
//                                                break;
//                                            case 1:
//                                                mButtonCheck.setVisibility(View.VISIBLE);
//                                                mTVCheck.setVisibility(View.VISIBLE);
//                                                mTVCheck.setText("跟注");
//
//                                                break;
//                                            case 2:
//                                                mButtonFold.setVisibility(View.VISIBLE);
//                                                mTVFold.setVisibility(View.VISIBLE);
//                                                mTVFold.setText("弃牌");
//
//                                                break;
//                                            case 3:
//                                                mButtonCheck.setVisibility(View.VISIBLE);
//                                                mTVCheck.setVisibility(View.VISIBLE);
//                                                mTVCheck.setText("看牌");
//
//                                                break;
//                                            case 4:
//                                                mButtonRaise.setVisibility(View.VISIBLE);
//                                                mTVRaise.setVisibility(View.VISIBLE);
//                                                mTVRaise.setText("加注");
//                                                blind_layout.setVisibility(View.VISIBLE);
//                                                if (minChip >= mGameUser.get(application.getUserId()).remainchips
//                                                        + mGameUser.get(application.getUserId()).amountchips) {
//                                                    mTVRaise.setText("AllIn");
//                                                }
//                                                quickDoActionNew(mTableInfo.players[j].userid,
//                                                        0, mTableInfo.players[j].waitactionparam.maxpaidchips);
//
//                                                break;
//                                            case 6:
//                                                mButtonCheck.setVisibility(View.VISIBLE);
//                                                mTVCheck.setVisibility(View.VISIBLE);
//                                                mTVCheck.setText("AllIn");
//                                                break;
//                                        }
//                                    }
//                                    int seatindex = mTableInfo.players[j].seatindex;
//                                    int realIndex = seatindex;
//                                    if (getUserIndex() != -1) {
//                                        realIndex = (seatindex - getUserIndex() + mTableInfo.seats.length) % mTableInfo.seats.length;
//                                    }
//
//                                    timer = new CountDownTimer(mTableInfo.players[j].needwaitactiontime * 1000, 1000) {
//
//                                        @Override
//                                        public void onTick(long millisUntilFinished) {
//
//                                        }
//
//                                        @Override
//                                        public void onFinish() {
//                                            mp = MediaPlayer.create(GameActivity.this, R.raw.timeovertipsound);
//                                            mp.start();
//
//                                        }
//                                    };
//                                    timer.start();
//                                }
//                            } catch (Throwable w) {
//                                w.printStackTrace();
//                            }
                            handleInfoWaitAction(mTableInfo.players[j].waitActionParamsMsg,
                                    mTableInfo.players[j].needwaitactiontime);
                        }


                        //有牌
                        if (mTableInfo.players[j].cards != null && mTableInfo.players[j].cards.length > 0) {
                            //用户本身有牌，显示

                            if (mTableInfo.players[j].userid == application.getUserId()) {

                                mMyCards.setVisibility(View.VISIBLE);
                                RelativeLayout myCards1 = mMyCards.findViewById(R.id.mycard_iv_card1_layout);
                                RelativeLayout myCards2 = mMyCards.findViewById(R.id.mycard_iv_card2_layout);
                                RelativeLayout myCards3 = mMyCards.findViewById(R.id.mycard_iv_card3_layout);
                                RelativeLayout myCards4 = mMyCards.findViewById(R.id.mycard_iv_card4_layout);
                                myCards1.setVisibility(View.INVISIBLE);
                                myCards2.setVisibility(View.INVISIBLE);
                                myCards3.setVisibility(View.INVISIBLE);
                                myCards4.setVisibility(View.INVISIBLE);

                                ImageView ivMyCards1 = myCards1.findViewById(R.id.mycard_iv_card1);
                                ImageView ivMyCards2 = myCards1.findViewById(R.id.mycard_iv_card2);
                                ImageView ivMyCards3 = myCards1.findViewById(R.id.mycard_iv_card3);
                                ImageView ivMyCards4 = myCards1.findViewById(R.id.mycard_iv_card4);

                                ivMyCards1.setVisibility(View.INVISIBLE);
                                ivMyCards2.setVisibility(View.INVISIBLE);
                                ivMyCards3.setVisibility(View.INVISIBLE);
                                ivMyCards4.setVisibility(View.INVISIBLE);

                                ImageView myCardsChose1 = myCards1.findViewById(R.id.mycard_iv_card1_chose_icon);
                                ImageView myCardsChose2 = myCards1.findViewById(R.id.mycard_iv_card2_chose_icon);
                                ImageView myCardsChose3 = myCards1.findViewById(R.id.mycard_iv_card3_chose_icon);
                                ImageView myCardsChose4 = myCards1.findViewById(R.id.mycard_iv_card4_chose_icon);

                                myCardsChose1.setVisibility(View.INVISIBLE);
                                myCardsChose1.setVisibility(View.INVISIBLE);
                                myCardsChose1.setVisibility(View.INVISIBLE);
                                myCardsChose1.setVisibility(View.INVISIBLE);

                                for (int k = 0; k < mTableInfo.players[j].cards.length; k++) {
//                                    ImageView ivMyCards;

                                    switch (k) {
                                        case 0:
                                            ivMyCards1.setVisibility(View.VISIBLE);
                                            ivMyCards1.setImageBitmap(drawSingleCard(mTableInfo.players[j].cards[k].suit, mTableInfo.players[j].cards[k].member));

                                            break;
                                        case 1:
                                            ivMyCards2.setVisibility(View.VISIBLE);
                                            ivMyCards2.setImageBitmap(drawSingleCard(mTableInfo.players[j].cards[k].suit, mTableInfo.players[j].cards[k].member));
                                            break;
                                        case 2:
                                            ivMyCards3.setVisibility(View.VISIBLE);
                                            ivMyCards3.setImageBitmap(drawSingleCard(mTableInfo.players[j].cards[k].suit, mTableInfo.players[j].cards[k].member));
                                            break;
                                        case 3:
                                            ivMyCards4.setVisibility(View.VISIBLE);
                                            ivMyCards4.setImageBitmap(drawSingleCard(mTableInfo.players[j].cards[k].suit, mTableInfo.players[j].cards[k].member));
                                            break;
                                    }
                                }
                            } else {
                                PlayerHole playerHole = new PlayerHole();
                                playerHole.userid = mTableInfo.players[j].userid;
                                playerHole.seatindex = mTableInfo.players[j].seatindex;
                                playerHole.hole = mTableInfo.players[j].cards;
                                mPlayerHolebjects.put(mTableInfo.players[j].userid, playerHole);
                                //显示cardback
                                View viewCardBack = mCardBackObjects.get(mTableInfo.players[j].seatindex);
                                viewCardBack.setVisibility(View.VISIBLE);

                            }
                        }
                    }
                }
            }
        }
        /*public enum TableState
     {
        prepare,
        start,
        pause,
        stop,
        timeout
     }*/
        //根据牌局状态来判断
        switch (mTableInfo.state) {
            case 0:
                if (mTableInfo.createuserid == application.getUserId()) {
                    if (!isMatch) {
                        btnStartGame.setVisibility(View.VISIBLE);
                    }

                } else {
                    mMessage.setVisibility(View.VISIBLE);
                    TextView tv_message = mMessage.findViewById(R.id.tv_message);
                    tv_message.setText("等待房主开始游戏");

                }

                break;
            case 2:
                if (mTableInfo.createuserid == application.getUserId()) {
                    if (!isMatch) {
                        btnStartGame.setVisibility(View.VISIBLE);
                    }

                } else {
                    mMessage.setVisibility(View.VISIBLE);
                    TextView tv_message = mMessage.findViewById(R.id.tv_message);
                    tv_message.setText("游戏暂停中，等待房主开始游戏");

                }
                break;

        }


//


        //此段代码不用，因为写死了不需要动态判断宽度和高度
//            int  width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//            int  height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//            view.measure(width,height);
//            int height1=view.getMeasuredHeight();
//            int  width1=view.getMeasuredWidth();

    }

    private void clearAllAmountChips() {
        for (Map.Entry<Integer, GameUser> entry : mGameUser.entrySet()) {
            entry.getValue().amountchips = 0;
        }
    }

    private void addStartButon() {
        if (isMatch) {
            return;
        }
        btnStartGame = new Button(this);
//        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(226, 60, 0, 0);
//        lp.x =(mScreenWidth-226)/2;
//        lp.y = (mScreenHeight-60)/2;
        int width = UITools.convertDpToPixel(113, this);
        int height = UITools.convertDpToPixel(30, this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        btnStartGame.setBackgroundResource(R.drawable.startgame);
        btnStartGame.setLayoutParams(layoutParams);
        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //GAME_START_TABLE
                try {
                    String msg = Constant.GAME_START_TABLE + "|";
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userid", application.getUserId());
                    jsonObject.put("tableid", gameId);
                    msg += jsonObject.toString().replace("$", "￥");
                    msg += "$";
                    myBinder.sendInfo(msg);
//                    showTestDialog();
//                    showAssuranceDialog(null);
//                    showWin(3);
//                    show27Image();
//                    showAllInAnimation();
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToastInScreenCenter(GameActivity.this, "开始牌局失败！");
                }


            }
        });
        layout_parent.addView(btnStartGame);
        btnStartGame.setVisibility(View.INVISIBLE);
    }

    private void showWin(int seatIndex) {
//        String jsonString = "{\"winplayers\":[{\"winchips\":18,\"userid\":9,\"seatindex\":2,\"besthand\":\"Pair\"},{\"winchips\":18,\"userid\":1003,\"seatindex\":3,\"besthand\":\"Pair\"}]}";
//
//        Gson gson = new Gson();
//
//        Type type = new TypeToken<WinPlayersBean>(){}.getType();
//
//        WinPlayersBean winPlayersBean = gson.fromJson(jsonString, type);

//        Logger.e(TAG, "winPlayersBean : " + winPlayersBean.getWinplayers().size());


        ImageView imageView = new ImageView(GameActivity.this);
        imageView.setImageResource(R.drawable.win);
        int pixel20 = UITools.convertDpToPixel(42, GameActivity.this);
        int pixel100 = UITools.convertDpToPixel(25, GameActivity.this);
        int pixel200 = UITools.convertDpToPixel(52, GameActivity.this);
        int[] position = new int[2];
        mSeatObjects.get(seatIndex).getLocationOnScreen(position);
        AbsoluteLayout.LayoutParams layoutParams =
                new AbsoluteLayout.LayoutParams(pixel20, pixel20, position[0], position[1]);
        imageView.setLayoutParams(layoutParams);
        layout_game.addView(imageView);
    }

    private void showTestDialog() {
        View layout = LayoutInflater.from(this).inflate(R.layout.layout_gamecard, null);
        ImageView iv;
        iv = layout.findViewById(R.id.iv_card1);
        iv.setVisibility(View.VISIBLE);
        iv.setImageBitmap(drawSingleCard(1, 2));
        ImageView iv2;
        iv2 = layout.findViewById(R.id.iv_card2);
        iv2.setVisibility(View.VISIBLE);
        iv2.setImageBitmap(drawSingleCard(1, 2));
        ImageView iv3;
        iv3 = layout.findViewById(R.id.iv_card3);
        iv3.setVisibility(View.VISIBLE);
        iv3.setImageBitmap(drawSingleCard(1, 2));
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        dialog = builder.create();
        dialog.show();

    }

//    private static Map<Integer, Float> winRateMap = new HashMap<>();
//
//    static {
//        winRateMap.put(1, 30f);
//        winRateMap.put(2, 16f);
//        winRateMap.put(3, 10f);
//        winRateMap.put(4, 8f);
//        winRateMap.put(5, 6f);
//        winRateMap.put(6, 5f);
//        winRateMap.put(7, 4f);
//        winRateMap.put(8, 3.5f);
//        winRateMap.put(9, 3f);
//        winRateMap.put(10, 2.5f);
//        winRateMap.put(11, 2.2f);
//        winRateMap.put(12, 2f);
//        winRateMap.put(13, 1.8f);
//        winRateMap.put(14, 1.6f);
//        winRateMap.put(15, 1.4f);
//        winRateMap.put(16, 1.25f);
//        winRateMap.put(17, 0.8f);
//        winRateMap.put(18, 0.7f);
//        winRateMap.put(19, 0.6f);
//    }

    private List<CardInfo> choseOuts = new ArrayList<>();

    int chooseCount = 1;

    Dialog assuranceDialog;
    int assuranceMoney = 1;


    public double getOutRate(int outs, int gameType)
    {
        if (gameType == 0)
        {
            if (outs == 0 || outs == 1) return 30;
            if (outs == 2) return 16;
            if (outs == 3) return 10;
            if (outs == 4) return 8;
            if (outs == 5) return 6;
            if (outs == 6) return 5;
            if (outs == 7) return 4;
            if (outs == 8) return 3.5;
            if (outs == 9) return 3;
            if (outs == 10) return 2.5;
            if (outs == 11) return 2.2;
            if (outs == 12) return 2;
            if (outs == 13) return 1.8;
            if (outs == 14) return 1.6;
            if (outs == 15) return 1.4;
            if (outs == 16) return 1.25;
            if (outs == 17) return 0.8;
            if (outs == 18) return 0.7;
            if (outs == 19) return 0.6;
            if (outs >=19 && outs <=30) return 0.5;
            return 0;
        }
        else
        {
            if (outs == 1) return 24;
            if (outs == 2) return 12;
            if (outs == 3) return 8;
            if (outs == 4) return 6;
            if (outs == 5) return 4.5;
            if (outs == 6) return 6;
            if (outs == 7) return 3.2;
            if (outs == 8) return 2.7;
            if (outs == 9) return 2.3;
            if (outs == 10) return 2;
            if (outs == 11) return 1.7;
            if (outs == 12) return 1.5;
            if (outs == 13) return 1.3;
            if (outs == 14) return 1.2;
            if (outs == 15) return 1.1;
            if (outs == 16) return 1;
            if (outs == 17) return 0.8;
            if (outs == 18) return 0.7;
            if (outs == 19) return 0.6;
            if (outs >= 19 && outs <= 30) return 0.5;
            return 0;
        }
    }

    private void showAssuranceDialog(String data) {
        try {
            int assuranceCount = 1;
            int payCount = 1;
            assuranceMoney = 1;


            final AssuranceDialogOutsCardAdapter outsCardAdapter;

            try {
//            data = "{\"userid\":1003,\"seatindex\":7,\"validpots\":400,\"validpotusers\":[{\"userid\":1003,\"nickname\":\"\u6D77\u98DE\u6D77\",\"hole\":[{\"suit\":2,\"member\":4,\"name\":\"Four of Hearts\"},{\"suit\":1,\"member\":4,\"name\":\"Four of Diamonds\"}],\"outs\":-1},{\"userid\":9,\"nickname\":\"\u80D6\u80D6\",\"hole\":[{\"suit\":2,\"member\":13,\"name\":\"King of Hearts\"},{\"suit\":0,\"member\":10,\"name\":\"Ten of Clubs\"}],\"outs\":14}],\"outscard\":[{\"suit\":1,\"member\":10,\"name\":\"Ten of Diamonds\"},{\"suit\":2,\"member\":10,\"name\":\"Ten of Hearts\"},{\"suit\":0,\"member\":9,\"name\":\"Nine of Clubs\"},{\"suit\":1,\"member\":13,\"name\":\"King of Diamonds\"},{\"suit\":3,\"member\":13,\"name\":\"King of Spades\"},{\"suit\":0,\"member\":14,\"name\":\"Ace of Clubs\"},{\"suit\":0,\"member\":13,\"name\":\"King of Clubs\"},{\"suit\":3,\"member\":9,\"name\":\"Nine of Spades\"},{\"suit\":2,\"member\":9,\"name\":\"Nine of Hearts\"},{\"suit\":3,\"member\":14,\"name\":\"Ace of Spades\"},{\"suit\":3,\"member\":10,\"name\":\"Ten of Spades\"},{\"suit\":1,\"member\":9,\"name\":\"Nine of Diamonds\"},{\"suit\":1,\"member\":14,\"name\":\"Ace of Diamonds\"},{\"suit\":2,\"member\":14,\"name\":\"Ace of Hearts\"}],\"maxbuychips\":100,\"canselectouts\":false,\"counddowntimes\":20}";
                Gson gson = new Gson();
                Type type = new TypeToken<AssuranceBean>() {
                }.getType();
                final AssuranceBean assuranceBean = gson.fromJson(data, type);
                List<CardInfo> outsCardInfos = new ArrayList<>();
                for (AssuranceBean.OutscardBean bean : assuranceBean.getOutscard()) {
                    outsCardInfos.add(new CardInfo(bean));
                }
                chooseCount = assuranceBean.getOutscard().size();
                if (choseOuts != null) {
                    choseOuts.clear();
                    choseOuts.addAll(outsCardInfos);
                }
                final View layout = LayoutInflater.from(this).inflate(R.layout.asurance_dialog_layout, null);
                LinearLayout notShow = (LinearLayout) layout.findViewById(R.id.assurance_dialog_not_show);
                if (assuranceBean.getUserid() != application.getUserId()) {
                    notShow.setVisibility(View.GONE);
                }
                TextView canSelectTips = (TextView) layout.findViewById(R.id.assurance_dialog_assurance_can_select_tips);
                canSelectTips.setVisibility(View.GONE);
                if (!assuranceBean.isCanselectouts()) {
                    canSelectTips.setVisibility(View.VISIBLE);
                }

                final TextView countDownTimer = (TextView) layout.findViewById(R.id.assurance_dialog_timer);
                final CountDownTimer assuranceTimer = new CountDownTimer(assuranceBean.getCounddowntimes() * 1000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        countDownTimer.setText("倒计时:" + (millisUntilFinished / 1000) + "秒");
                    }

                    @Override
                    public void onFinish() {

                        if (assuranceBean.getUserid() == application.getUserId()) {
                            try {

                                String msg = Constant.GAME_BUY_SURANCE + "|";
                                JSONObject jsonSend = new JSONObject();
                                jsonSend.put("userid", application.getUserId());
                                jsonSend.put("seatindex", getUserIndex());
                                jsonSend.put("tableid", gameId);
                                jsonSend.put("buyouts", null);
                                jsonSend.put("buychips", 0);
                                msg += jsonSend.toString().replace("$", "￥");
                                msg += "$";
                                myBinder.sendInfo(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "操作出错！");
                            }
                        }
                        if (null != assuranceDialog) {
                            assuranceDialog.dismiss();
                        }

                    }
                };
                assuranceTimer.start();

                TextView validPool = (TextView) layout.findViewById(R.id.assurance_dialog_valid_pool);
                validPool.setText(assuranceBean.getValidpots() + "");
                final TextView winRate = (TextView) layout.findViewById(R.id.assurance_dialog_win_rate);
//                winRate.setText("赔率 " + winRateMap.get(choseOuts.size()));
                winRate.setText("赔率 " + getOutRate(choseOuts.size(), mTableInfo.gametype));
                final TextView outs = (TextView) layout.findViewById(R.id.assurance_dialog_outs);
                outs.setText("OUTS " + choseOuts.size()
                        + "/" + assuranceBean.getOutscard().size());

//                assuranceCount = (int) (assuranceBean.getValidpots()
//                        / assuranceBean.getValidpotusers().size() / winRateMap.get(choseOuts.size()));
//                payCount = (int) (assuranceCount * winRateMap.get(choseOuts.size()));
                payCount = (int) (assuranceCount * getOutRate(choseOuts.size(), mTableInfo.gametype));


                final TextView assuranceAmount = (TextView) layout.findViewById(R.id.assurance_dialog_assurance_amount);
                assuranceAmount.setText("投保额 " + (assuranceCount == 0 ? 1 : assuranceCount));

                final TextView payAmount = (TextView) layout.findViewById(R.id.assurance_dialog_pay_amount);
                payAmount.setText("赔付额 " + (payCount == 0 ? 1 : payCount));

                final SeekBar seekBar = (SeekBar) layout.findViewById(R.id.assurance_dialog_amount_seek_bar);
//                seekBar.setMax((int) (assuranceBean.getValidpots() / winRateMap.get(choseOuts.size())));
                seekBar.setMax((int) (assuranceBean.getValidpots() / getOutRate(choseOuts.size(), mTableInfo.gametype)));

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                assuranceMoney = progress == 0 ? 1 : progress;
                                assuranceAmount.setText("投保额 " + assuranceMoney);
//                        payAmount.setText("赔付额 " + (int) (assuranceMoney * winRateMap.get(chooseCount)));
                                payAmount.setText("赔付额 " + (int) (assuranceMoney * getOutRate(chooseCount, mTableInfo.gametype)));
                            }
                        });

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                TextView keepCost = (TextView) layout.findViewById(R.id.assurance_dialog_keep_cost);
                TextView AllIn = (TextView) layout.findViewById(R.id.assurance_dialog_all_in);
                keepCost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        assuranceMoney = (int) (assuranceBean.getValidpots()
//                                / assuranceBean.getValidpotusers().size() / winRateMap.get(chooseCount));
                        assuranceMoney = (int) (assuranceBean.getValidpots()
                                / assuranceBean.getValidpotusers().size() /  getOutRate(chooseCount, mTableInfo.gametype));
                        assuranceAmount.setText("投保额 " + assuranceMoney);
//                    assuranceMoney = assuranceMoney;
//                        payAmount.setText("赔付额 " + (int) (assuranceMoney * winRateMap.get(chooseCount)));
                        payAmount.setText("赔付额 " + (int) (assuranceMoney *  getOutRate(chooseCount, mTableInfo.gametype)));
                        seekBar.setProgress(assuranceMoney);
                    }
                });

                AllIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seekBar.setProgress(0);
                        seekBar.setProgress(seekBar.getMax());
                    }
                });


                RecyclerView assuranceUserInfoRecyclerView
                        = (RecyclerView) layout.findViewById(R.id.assurance_dialog_user_info_recycler_view);
                assuranceUserInfoRecyclerView.setHasFixedSize(true);
                assuranceUserInfoRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
                AssuranceDialogUserInfoAdapter adapter = new AssuranceDialogUserInfoAdapter(assuranceBean.getValidpotusers(), this);
                assuranceUserInfoRecyclerView.setAdapter(adapter);

                RecyclerView assurancePublicCardRecyclerView = (RecyclerView) layout.findViewById(R.id.assurance_dialog_public_cards_recycler_view);
                assurancePublicCardRecyclerView.setHasFixedSize(true);
                assurancePublicCardRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
                List<CardInfo> publicCardsList = Arrays.asList(publicCards);
                AssuranceDialogPublicCardAdapter publicCardAdapter = new AssuranceDialogPublicCardAdapter(publicCardsList, this);
                assurancePublicCardRecyclerView.setAdapter(publicCardAdapter);

                RecyclerView assuranceOutsRecyclerView = (RecyclerView) layout.findViewById(R.id.assurance_dialog_outs_cards_recycler_view);
                assuranceOutsRecyclerView.setHasFixedSize(true);
                assuranceOutsRecyclerView.setLayoutManager(new GridLayoutManager(this, 8));

                outsCardAdapter = new AssuranceDialogOutsCardAdapter(outsCardInfos, this);
                outsCardAdapter.setCanCancel(assuranceBean.getUserid() == application.getUserId() && assuranceBean.isCanselectouts());
                outsCardAdapter.setChangedListener(new AssuranceDialogOutsCardAdapter.IChooseOutsChangedListener() {
                    @Override
                    public void onChooseOutsChanged(final List<Integer> chooseList) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateDialogUI(chooseList);
                            }
                        });
                    }

                    private void updateDialogUI(List<Integer> chooseList) {
                        chooseCount = 0;
                        for (Integer i : chooseList) {
                            if (i == 1) {
                                chooseCount++;
                            }
                        }
                        try {
                            outs.setText("OUTS " + chooseCount
                                    + "/" + assuranceBean.getOutscard().size());

//                            seekBar.setMax((int) (assuranceBean.getValidpots() / winRateMap.get(chooseCount)));
                            seekBar.setMax((int) (assuranceBean.getValidpots() / getOutRate(chooseCount, mTableInfo.gametype)));
//                            winRate.setText("赔率 " + winRateMap.get(chooseCount));
                            winRate.setText("赔率 " +  getOutRate(chooseCount, mTableInfo.gametype));
                            payAmount.setText("赔付额 " + (int) (assuranceMoney * getOutRate(chooseCount, mTableInfo.gametype)));
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }

                });
                assuranceOutsRecyclerView.setAdapter(outsCardAdapter);

                Button notBuy = (Button) layout.findViewById(R.id.assurance_dialog_not_buy);
                if (!assuranceBean.isCanselectouts()) {
                    notBuy.setClickable(false);
                    notBuy.setVisibility(View.INVISIBLE);
                }

                Button buy = (Button) layout.findViewById(R.id.assurance_dialog_buy);
                notBuy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (assuranceTimer != null) {
                                assuranceTimer.cancel();
                            }
                            String msg = Constant.GAME_BUY_SURANCE + "|";
                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put("userid", application.getUserId());
                            jsonSend.put("seatindex", getUserIndex());
                            jsonSend.put("tableid", gameId);
                            jsonSend.put("buyouts", null);
                            jsonSend.put("buychips", 0);
                            msg += jsonSend.toString().replace("$", "￥");
                            msg += "$";
                            myBinder.sendInfo(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "操作出错！");
                        }
                        if (null != assuranceDialog) {
                            assuranceDialog.dismiss();
                        }
                    }
                });

                buy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (assuranceTimer != null) {
                                assuranceTimer.cancel();
                            }
                            List<Integer> chooseListIndex = outsCardAdapter.getChoseList();
                            List<CardInfo> chooseCards = new ArrayList<>();
//                        ObjectMapper mapper = new ObjectMapper();
                            JSONArray jsonArray = new JSONArray();
                            for (int i = 0; i < chooseListIndex.size(); i++) {
                                if (chooseListIndex.get(i) == 1) {
//                                JSONObject jsonObject = new JSONObject(mapper.writeValueAsString(new CardInfo(assuranceBean.getOutscard().get(i))));
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("member", assuranceBean.getOutscard().get(i).getMember());
                                    jsonObject.put("name", assuranceBean.getOutscard().get(i).getName());
                                    jsonObject.put("suit", assuranceBean.getOutscard().get(i).getSuit());
                                    jsonArray.put(jsonObject);
                                    chooseCards.add(new CardInfo(assuranceBean.getOutscard().get(i)));
                                }
                            }

                            String msg = Constant.GAME_BUY_SURANCE + "|";
                            JSONObject jsonSend = new JSONObject();
                            jsonSend.put("userid", application.getUserId());
                            jsonSend.put("seatindex", getUserIndex());
                            jsonSend.put("tableid", gameId);
                            jsonSend.put("buyouts", jsonArray);
                            jsonSend.put("buychips", assuranceMoney);
                            msg += jsonSend.toString().replace("$", "￥");
                            msg += "$";
                            myBinder.sendInfo(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                            ToastUtil.showToastInScreenCenter(GameActivity.this, "操作出错！");
                        }
                        if (null != assuranceDialog) {
                            assuranceDialog.dismiss();
                        }
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(layout);
                assuranceDialog = builder.create();
                assuranceDialog.setCancelable(false);
                assuranceDialog.show();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void addPoolView() {
        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(mPoolWidth, mPoolHeight, 0, 0);
        lp.x = (mScreenWidth - mPoolWidth) / 2;
        lp.y = mSeatViewHeight + mAmountChipVieHeight + UITools.convertDpToPixel(10, GameActivity.this);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.layout_pool, null);
        view.setLayoutParams(lp);
        layout_game.addView(view);
        //将底池，边池的控件设为不可见
        LinearLayout layout_pool1 = (LinearLayout) findViewById(R.id.layout_pool1);
        layout_pool1.setVisibility(View.INVISIBLE);
        mPoolObjects.put(0, layout_pool1);
        LinearLayout layout_pool2 = (LinearLayout) findViewById(R.id.layout_pool2);
        layout_pool2.setVisibility(View.INVISIBLE);
        mPoolObjects.put(1, layout_pool2);
        LinearLayout layout_pool3 = (LinearLayout) findViewById(R.id.layout_pool3);
        layout_pool3.setVisibility(View.INVISIBLE);
        mPoolObjects.put(2, layout_pool3);
        LinearLayout layout_pool4 = (LinearLayout) findViewById(R.id.layout_pool4);
        layout_pool4.setVisibility(View.INVISIBLE);
        mPoolObjects.put(3, layout_pool4);
        LinearLayout layout_pool5 = (LinearLayout) findViewById(R.id.layout_pool5);
        layout_pool5.setVisibility(View.INVISIBLE);
        mPoolObjects.put(4, layout_pool5);
        LinearLayout layout_pool6 = (LinearLayout) findViewById(R.id.layout_pool6);
        layout_pool6.setVisibility(View.INVISIBLE);
        mPoolObjects.put(5, layout_pool6);
        LinearLayout layout_pool7 = (LinearLayout) findViewById(R.id.layout_pool7);
        layout_pool7.setVisibility(View.INVISIBLE);
        mPoolObjects.put(6, layout_pool7);
        LinearLayout layout_pool8 = (LinearLayout) findViewById(R.id.layout_pool8);
        layout_pool8.setVisibility(View.INVISIBLE);
        mPoolObjects.put(7, layout_pool8);
        LinearLayout layout_pool9 = (LinearLayout) findViewById(R.id.layout_pool9);
        layout_pool9.setVisibility(View.INVISIBLE);
        mPoolObjects.put(8, layout_pool9);
        //textview
        TextView textView = (TextView) findViewById(R.id.tv_pool1);
        mPoolOTextbjects.put(0, textView);
        textView = (TextView) findViewById(R.id.tv_pool2);
        mPoolOTextbjects.put(1, textView);
        textView = (TextView) findViewById(R.id.tv_pool3);
        mPoolOTextbjects.put(2, textView);
        textView = (TextView) findViewById(R.id.tv_pool4);
        mPoolOTextbjects.put(3, textView);
        textView = (TextView) findViewById(R.id.tv_pool5);
        mPoolOTextbjects.put(4, textView);
        textView = (TextView) findViewById(R.id.tv_pool6);
        mPoolOTextbjects.put(5, textView);
        textView = (TextView) findViewById(R.id.tv_pool7);
        mPoolOTextbjects.put(6, textView);
        textView = (TextView) findViewById(R.id.tv_pool8);
        mPoolOTextbjects.put(7, textView);
        textView = (TextView) findViewById(R.id.tv_pool9);
        mPoolOTextbjects.put(8, textView);

    }

    private void addActionView() {
        addActionButon();
//        addChipSelector();

        //增加按钮处理事件
        mButtonRaise.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Logger.e("QIPU", "onTouch : " + event.getAction());
//                seekBarRaise.setVisibility(View.VISIBLE);
//                return seekBarRaise.onTouchEvent(event);
                if (mTVRaise.getText().toString().equalsIgnoreCase("AllIn") && event.getAction() == 0) {
                    try {
                        //按钮上有两种状态，bet raise  ，bet的时候可以下全部，但是action是bet，raise的时候，allin 要发allin，amountchips要差值
                        String msg = Constant.GAME_DO_ACTION + "|";
                        JSONObject jsonSend = new JSONObject();
                        jsonSend.put("userid", application.getUserId());
                        jsonSend.put("seatindex", getUserIndex());
                        jsonSend.put("action", 6);
                        jsonSend.put("amountchips", mGameUser.get(application.getUserId()).remainchips);
                        jsonSend.put("tableid", gameId);
                        msg += jsonSend.toString().replace("$", "￥");
                        msg += "$";
                        myBinder.sendInfo(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastUtil.showToastInScreenCenter(GameActivity.this, "操作出错！");
                    }
                    blind_layout.setVisibility(View.INVISIBLE);
                    mainThreadHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAction.setVisibility(View.INVISIBLE);
                        }
                    }, 100);
                    return false;
                }
                blind_layout.setVisibility(View.INVISIBLE);
                raiseLayout.setVisibility(View.VISIBLE);
                float y = event.getY();
                float x = event.getX();
                final int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        downX = x;
                        downY = y;
                        currentY = (int) y;
//                        textMoveLayout.setVisibility(View.VISIBLE);
//                        button_bottom.setVisibility(View.VISIBLE);
//                        button_bottom.setText(minChip + "");
//                        button_top.setText(maxChip + "");
//                        button_middle.setVisibility(View.GONE);
                        raiseLayoutMoveTv.setVisibility(View.VISIBLE);
                        raiseLayoutTopTv.setText(maxChip + "");
                        raiseLayoutMoveTv.setText(minChip + "");

                        raiseLayoutMoveTv.post(new Runnable() {
                            @Override
                            public void run() {
                                raiseMoveRight = raiseLayoutMoveTv.getRight();
                                raiseMoveLeft = raiseLayoutMoveTv.getLeft();
                                raiseMoveTop = raiseLayoutMoveTv.getTop();
                                raiseMoveBottom = raiseLayoutMoveTv.getBottom();
                            }
                        });
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (currentY - y > 20 || y - currentY > 20) {
                            if (mp != null) {
                                mp.stop();
                                mp.reset();
                                mp = MediaPlayer.create(GameActivity.this, R.raw.slider);
                                //mp.setLooping(true);
                                //mp.prepare();
                                mp.start();


                            }
                            currentY = (int) y;
                        }


//                        mp.stop();
//                        mp.reset();
//                        mp.set(R.raw.slider);
//                        mediaPlayer.prepare();
//                        mediaPlayer.start();
//                        mp=MediaPlayer.create(GameActivity.this,R.raw.slider);
//                        mp.start();
                        if (y > downY) {
//                            button_bottom.setVisibility(View.VISIBLE);
//                            button_middle.setVisibility(View.GONE);
                            raiseLayoutMoveTv.setText(0 + "");
                            actionChip = minChip;

                        } else {
                            try {
                                int p90 = UITools.convertDpToPixel(36, GameActivity.this);
                                int p50 = UITools.convertDpToPixel(25, GameActivity.this);
                                int p350 = UITools.convertDpToPixel(130, GameActivity.this);
                                int p400 = UITools.convertDpToPixel(200, GameActivity.this);
                                if (downY - y > p350) {
//                                button_bottom.setVisibility(View.GONE);
//                                button_middle.setText("AllIn");
//                                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(button_middle.getLayoutParams());
//                                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                                    raiseLayoutTopTv.setText("AllIn");
                                    raiseLayoutMoveTv.setVisibility(View.INVISIBLE);
                                    actionChip = maxChip;

                                } else {
                                    raiseLayoutMoveTv.setVisibility(View.VISIBLE);
//                                button_bottom.setVisibility(View.GONE);
//                                button_middle.setVisibility(View.VISIBLE);
//                                button_top.setText(maxChip + "");
                                    raiseLayoutTopTv.setText(maxChip + "");
                                    int intY = (int) y;
                                    int pixel30 = UITools.convertDpToPixel(30, GameActivity.this);
                                    int intDownY = (int) downY;
                                    int stepCount = (maxChip - minChip) / step;
                                    Logger.d("QIPU", "maxChip :" + maxChip);
                                    Logger.d("QIPU", "minChip :" + minChip);
                                    Logger.d("QIPU", "step :" + step);
                                    Logger.d("QIPU", "stepCount :" + stepCount);
                                    int moveStep = p350 / stepCount;
                                    int movedSteps = (intDownY - intY) / moveStep;
                                    actionChip = minChip + (movedSteps * step);
                                    raiseLayoutMoveTv.setText(actionChip + "");
//                                button_middle.setText(actionChip + "");
//                                button_middle.layout(0, (int) (p350 - (downY - y)), p90, (int) (p400 - (downY - y)));
                                    int moveDistance = movedSteps * moveStep;
                                    Logger.d(TAG, "QIPU moveDistance : " + moveDistance);
                                    Logger.d(TAG, "QIPU raiseMoveLeft : " + raiseMoveLeft);
                                    Logger.d(TAG, "QIPU raiseMoveTop : " + raiseMoveTop);
                                    Logger.d(TAG, "QIPU raiseMoveRight : " + raiseMoveRight);
                                    Logger.d(TAG, "QIPU raiseMoveTop : " + raiseMoveTop);
                                    if (moveDistance > 0) {
                                        raiseLayoutMoveTv.layout((int) raiseMoveLeft,
                                                (int) (raiseMoveTop - moveDistance),
                                                (int) raiseMoveRight,
                                                (int) (raiseMoveTop - moveDistance) + pixel30);
                                    }
                                }
                            } catch (Throwable w) {
                                w.printStackTrace();
                            }
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        //
                        if (actionChip == maxChip) {
                            mp = MediaPlayer.create(GameActivity.this, R.raw.slider_top);
                            mp.start();
                        }
//                        textMoveLayout.setVisibility(View.GONE);
                        raiseLayoutMoveTv.layout((int) raiseMoveLeft,
                                (int) (raiseMoveTop),
                                (int) raiseMoveRight,
                                (int) (raiseMoveBottom));
                        raiseLayout.setVisibility(View.INVISIBLE);
                        if (actionChip >= minChip) {
                            try {
                                //按钮上有两种状态，bet raise  ，bet的时候可以下全部，但是action是bet，raise的时候，allin 要发allin，amountchips要差值
                                String msg = Constant.GAME_DO_ACTION + "|";
                                JSONObject jsonSend = new JSONObject();
                                jsonSend.put("userid", application.getUserId());
                                jsonSend.put("seatindex", getUserIndex());
                                if (actionChip == maxChip && mTVRaise.getText().toString().equals("下注")) {
                                    jsonSend.put("action", 6);
                                } else if (mTVRaise.getText().toString().equals("加注")) {
                                    if (actionChip == maxChip) {
                                        jsonSend.put("action", 6);
                                    } else {
                                        jsonSend.put("action", 4);
                                    }
                                } else {
                                    jsonSend.put("action", 0);
                                }
                                jsonSend.put("amountchips",
                                        actionChip - mGameUser.get(application.getUserId()).amountchips);
                                jsonSend.put("tableid", gameId);
                                msg += jsonSend.toString().replace("$", "￥");
                                msg += "$";
                                myBinder.sendInfo(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "操作出错！");
                            }
                        }
                        //发送下注
                        break;

                }
                return true;

            }
        });

        mButtonFold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弃牌按钮
                try {
                            /*public class InfDoActionParam
                            {
                                public int userid;
                                public int seatindex;
                                public PlayAction action;
                                public int amountchips;
                                public int tableid;
                            }*/
                    String msg = Constant.GAME_DO_ACTION + "|";
                    JSONObject jsonSend = new JSONObject();
                    jsonSend.put("userid", application.getUserId());
                    jsonSend.put("seatindex", getUserIndex());
                    jsonSend.put("action", 2);
                    jsonSend.put("amountchips", 0);
                    jsonSend.put("tableid", gameId);
                    msg += jsonSend.toString().replace("$", "￥");
                    msg += "$";
                    myBinder.sendInfo(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToastInScreenCenter(GameActivity.this, "弃牌出错！");
                }

            }
        });

        mButtonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //右侧按钮
                try {

                    String msg = Constant.GAME_DO_ACTION + "|";
                    JSONObject jsonSend = new JSONObject();
                    jsonSend.put("userid", application.getUserId());
                    jsonSend.put("seatindex", getUserIndex());
                    switch (mTVCheck.getText().toString()) {
                        case "跟注":
                            jsonSend.put("action", 1);
                            jsonSend.put("amountchips",
                                    maxpaidchips - mGameUser.get(application.getUserId()).amountchips);

                            break;
                        case "看牌":
                            jsonSend.put("action", 3);
                            jsonSend.put("amountchips", 0);
                            break;
                        case "AllIn":
                            jsonSend.put("action", 6);
                            jsonSend.put("amountchips", mGameUser.get(application.getUserId()).remainchips);
                            break;
                    }
                    jsonSend.put("tableid", gameId);
                    msg += jsonSend.toString().replace("$", "￥");
                    msg += "$";
                    myBinder.sendInfo(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToastInScreenCenter(GameActivity.this, "弃牌出错！");
                }


            }
        });

        mAction.setVisibility(View.GONE);
    }

    private void addActionButon() {

        int pixel400 = UITools.convertDpToPixel(200, GameActivity.this);
//        int pixel300 = UITools.convertDpToPixel(160, GameActivity.this);
        int pixel300 = UITools.convertDpToPixel(290, GameActivity.this);
        int pixel260 = UITools.convertDpToPixel(130, GameActivity.this);
        int pixel180 = UITools.convertDpToPixel(90, GameActivity.this);
        int pixel20 = UITools.convertDpToPixel(10, GameActivity.this);

//        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(pixel300, pixel600, 0, 0);
//        lp.x = (mScreenWidth - pixel300) / 2;
//        lp.y = (mScreenHeight - pixel80) / 2 - pixel180;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mAction = inflater.inflate(R.layout.layout_actionbutton, null);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pixel400, pixel300);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.bottomMargin = pixel260;
        mAction.setLayoutParams(layoutParams);
        layout_parent.addView(mAction);
        //给三个按钮加响应事件
//        seekBarRaise = mAction.findViewById(R.id.game_action_raise_seekbar);
//        seekBarRaise.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
////                blind_count.setText("100" + progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
////                blind_count.setText("DONE");
//
//            }
//        });
//        seekBarRaise.setProgress(0);
        mButtonFold = mAction.findViewById(R.id.game_action_abandon);
        mButtonRaise = mAction.findViewById(R.id.game_action_raise);
        mButtonCheck = mAction.findViewById(R.id.game_action_call);
        mTVFold = mAction.findViewById(R.id.tv_fold);
        mTVRaise = mAction.findViewById(R.id.tv_raise);
        mTVCheck = mAction.findViewById(R.id.tv_check);
        blind_layout = mAction.findViewById(R.id.blind_layout);
        raiseLayout = mAction.findViewById(R.id.game_action_raise_layout);
        raiseLayoutTopTv = mAction.findViewById(R.id.game_action_raise_layout_top_tv);
        raiseLayoutMoveTv = mAction.findViewById(R.id.game_action_raise_layout_move_tv);
        raiseLayout.setVisibility(View.INVISIBLE);
        raiseLayoutMoveTv.post(new Runnable() {
            @Override
            public void run() {
                raiseMoveRight = raiseLayoutMoveTv.getRight();
                raiseMoveLeft = raiseLayoutMoveTv.getLeft();
                raiseMoveTop = raiseLayoutMoveTv.getTop();
                raiseMoveBottom = raiseLayoutMoveTv.getBottom();
            }
        });
//        blind_count = mAction.findViewById(R.id.seek_text);

//        doubleBlind = mAction.findViewById(R.id.game_action_double);
//        doubleBlind.setOnClickListener();
//        tripleBlind = mAction.findViewById(R.id.game_action_triple);
//        fourBlind = mAction.findViewById(R.id.game_action_quadruple);

    }

    float raiseMoveRight;
    float raiseMoveLeft;
    float raiseMoveTop;
    float raiseMoveBottom;

    private void addChipSelector() {
        int pixel300 = UITools.convertDpToPixel(150, GameActivity.this);
        int pixel400 = UITools.convertDpToPixel(200, GameActivity.this);
        int pixel50 = UITools.convertDpToPixel(25, GameActivity.this);
        int pixel90 = UITools.convertDpToPixel(45, GameActivity.this);
        int pixel72 = UITools.convertDpToPixel(60, GameActivity.this);
        int pixel36 = UITools.convertDpToPixel(30, GameActivity.this);
        int pixel4 = UITools.convertDpToPixel(2, GameActivity.this);
        int pixel24 = UITools.convertDpToPixel(12, GameActivity.this);
        int pixel560 = UITools.convertDpToPixel(240, GameActivity.this);
        int pixel150 = UITools.convertDpToPixel(75, GameActivity.this);

        //添加textMoveLayout ,宽度 90，高度200
        //按钮宽度90，高度50
        //虚线 宽度4，高度200，置于底部
//        int width=90;
//        int height=400;
//        int button_height=50;
//        int button_width=90;
//        int line_width=4;
        ViewGroup.LayoutParams layoutParams;

        textMoveLayout = new TextMoveLayout(this);
//        textMoveLayout.setBackgroundColor(Color.RED);
//        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(pixel90, pixel400, 0, 0);
        RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(pixel72, pixel400);
//        layoutParams1.bottomMargin = pixel4;
        layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        layoutParams1.addRule(RelativeLayout.ABOVE, blind_layout.getId());
        layoutParams1.bottomMargin = pixel560;
        layoutParams1.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

//        lp.x = (pixel300 - pixel90) / 2;
//        lp.y = 0;//  (mScreenHeight-80)/2-60;;
        textMoveLayout.setLayoutParams(layoutParams1);
//        RelativeLayout.LayoutParams layoutParams1 =
        ((RelativeLayout) layout_parent).addView(textMoveLayout);

        //虚线
        ImageView iv_line = new ImageView(this);
        layoutParams = new ViewGroup.LayoutParams(pixel4, pixel400);
        textMoveLayout.addView(iv_line, layoutParams);
        iv_line.setBackgroundResource(R.drawable.line_bj);
        iv_line.layout((pixel72 - pixel4) / 2, 0, (pixel72 - pixel4) / 2 + pixel4, pixel400);


        //三个按钮
        button_bottom = new TextView(this);
        button_bottom.setText("12");
        button_bottom.setTextColor(Color.WHITE);
        button_bottom.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixel24);
        button_bottom.setBackgroundResource(R.drawable.bottom_bj);
//        ViewGroup.LayoutParams layoutParams2 = new ViewGroup.LayoutParams(pixel72, pixel36);
        ViewGroup.LayoutParams layoutParams2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        button_bottom.setLayoutParams(layoutParams2);
        button_bottom.setGravity(Gravity.CENTER);
        textMoveLayout.addView(button_bottom);
        button_bottom.layout(0, pixel400 - pixel36, pixel72, pixel400);


        button_top = new TextView(this);
        button_top.setText("12");
        button_top.setTextColor(Color.WHITE);
        button_top.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixel24);
        button_top.setBackgroundResource(R.drawable.top_bj);
        button_top.setGravity(Gravity.CENTER);
        ViewGroup.LayoutParams layoutParams3 = new ViewGroup.LayoutParams(pixel72, pixel36);
        button_top.setLayoutParams(layoutParams3);
        textMoveLayout.addView(button_top);
        button_top.layout(0, 0, pixel72, pixel36);


        button_middle = new TextView(this);
        button_middle.setText("12");
        button_middle.setTextColor(Color.WHITE);
        button_middle.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixel24);
        button_middle.setBackgroundResource(R.drawable.middle_bj);
        ViewGroup.LayoutParams layoutParams4 = new ViewGroup.LayoutParams(pixel72, pixel36);
        button_middle.setLayoutParams(layoutParams4);
        button_middle.setGravity(Gravity.CENTER);
        textMoveLayout.addView(button_middle);
        button_middle.layout(0, pixel150, pixel72, pixel150 + pixel36);

        textMoveLayout.setVisibility(View.GONE);


        /* button=new Button(this);
        button.setText("12");
        button.setTextColor(Color.WHITE);
        button.setTextSize(12);
        button.setBackgroundResource(R.drawable.middle_bj);

//        text = new TextView(this);
//        text.setTextColor(Color.WHITE);
//        text.setTextSize(12);
//        text.setSingleLine(true);
//        text.setText("20");
//        //text.setGravity(Gravity.CENTER);
//        //text.setGravity(Gravity.CENTER);
//        text.setBackgroundResource(R.drawable.middle_bj);
        layoutParams = new ViewGroup.LayoutParams(90,50);
        textMoveLayout.addView(button, layoutParams);
        button.layout(100,320,190,370);
*/


    }

    private void addReturnSeatButton() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mReturnSeat = inflater.inflate(R.layout.layout_returnseat, layout_game, false);
        TextView tv = mReturnSeat.findViewById(R.id.tv_returnseat);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送回到座位
                try {
                    String msg = Constant.GAME_BACK_SEAT + "|";
                    JSONObject jsonSend = new JSONObject();
                    jsonSend.put("userid", application.getUserId());
                    jsonSend.put("tableid", gameId);
                    jsonSend.put("seatindex", getUserIndex());
                    msg += jsonSend.toString().replace("$", "￥");
                    msg += "$";
                    myBinder.sendInfo(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToastInScreenCenter(GameActivity.this, "返回座位出错！");

                }

            }
        });

        int pixel35 = UITools.convertDpToPixel(17, GameActivity.this);
        int pixel90 = UITools.convertDpToPixel(60, GameActivity.this);

        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(pixel90, pixel35, 0, 0);
        lp.x = iSeatValue[0][0] - mSeatViewWidth;
        lp.y = iSeatValue[0][1] + (mSeatViewHeight - pixel35) / 2;
        mReturnSeat.setLayoutParams(lp);
        layout_game.addView(mReturnSeat);
        mReturnSeat.setVisibility(View.INVISIBLE);
    }

    private void addLayoutMessage() {
        int pixel60 = UITools.convertDpToPixel(30, GameActivity.this);
        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(mScreenWidth, pixel60, 0, 0);
        lp.x = 0;
        lp.y = (mScreenHeight - pixel60) / 2;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mMessage = inflater.inflate(R.layout.layout_message, null);

        TextView tv_message = mMessage.findViewById(R.id.tv_message);
        tv_message.setText("等待开始");
        mMessage.setLayoutParams(lp);
        layout_game.addView(mMessage);
        mMessage.setVisibility(View.INVISIBLE);

    }

    //赢家翻牌，只有一个控件
    private void addshowcards() {

//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        mShowCards = inflater.inflate(R.layout.layout_showcard, null);
//        layout_game.addView(mShowCards);
//        mShowCards.setVisibility(View.INVISIBLE);

    }

    private TextView bestHandTv;

    private void addMyCards() {

        int pixel216 = UITools.convertDpToPixel(90, GameActivity.this);
        int pixel132 = UITools.convertDpToPixel(66, GameActivity.this);
        int pixel6 = UITools.convertDpToPixel(3, GameActivity.this);
        int pixel60 = UITools.convertDpToPixel(60, GameActivity.this);
        int pixel35 = UITools.convertDpToPixel(28, GameActivity.this);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        mMyCards = inflater.inflate(R.layout.layout_mycard, null);
//        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(pixel280, pixel110, 0, 0);
//        lp.x = iSeatValue[0][0] + mSeatViewWidth;
//        lp.y = iSeatValue[0][1] + mNameTextHeight;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pixel216, pixel132);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.leftMargin = pixel6;
        layoutParams.rightMargin = pixel60;
        layoutParams.bottomMargin = pixel35;
        mMyCards.setLayoutParams(layoutParams);
        layout_parent.addView(mMyCards);
//        mMyCards.setLayoutParams(lp);
//        layout_game.addView(mMyCards);
        mMyCards.setVisibility(View.GONE);
        mMyCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = -1;
                for (int key : mMyCardsObjects.keySet()) {
                    if (mMyCardsObjects.get(key).getVisibility() == View.VISIBLE) {
                        index++;
                    }
                }
                showCards(!hasShowCards);
                final int finalIndex = index;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch(finalIndex) {
                            case 0:
                                if (!hasShowCards) {
                                    mMyCardsObjects.get(finalIndex).findViewById
                                            (R.id.mycard_iv_card1_chose_icon).setVisibility(View.VISIBLE);
                                } else {
                                    mMyCardsObjects.get(finalIndex).findViewById
                                            (R.id.mycard_iv_card1_chose_icon).setVisibility(View.INVISIBLE);
                                }
                                break;

                            case 1:
                                if (!hasShowCards) {
                                    mMyCardsObjects.get(finalIndex).findViewById
                                            (R.id.mycard_iv_card2_chose_icon).setVisibility(View.VISIBLE);
                                } else {
                                    mMyCardsObjects.get(finalIndex).findViewById
                                            (R.id.mycard_iv_card2_chose_icon).setVisibility(View.INVISIBLE);
                                }
                                break;
                            case 2:
                                if (!hasShowCards) {
                                    mMyCardsObjects.get(finalIndex).findViewById
                                            (R.id.mycard_iv_card3_chose_icon).setVisibility(View.VISIBLE);
                                } else {
                                    mMyCardsObjects.get(finalIndex).findViewById
                                            (R.id.mycard_iv_card3_chose_icon).setVisibility(View.INVISIBLE);
                                }
                                break;
                            case 3:
                                if (!hasShowCards) {
                                    mMyCardsObjects.get(finalIndex).findViewById
                                            (R.id.mycard_iv_card4_chose_icon).setVisibility(View.VISIBLE);
                                } else {
                                    mMyCardsObjects.get(finalIndex).findViewById
                                            (R.id.mycard_iv_card4_chose_icon).setVisibility(View.INVISIBLE);
                                }
                                break;
                        }
                    }
                });
                hasShowCards = !hasShowCards;
            }
        });

//        infbesthand();
    }

    private static boolean hasShowCards = false;

    private void showCards(boolean isShow) {
        try {
            String msg = Constant.GAME_SHOW_CARD + "|";
            JSONObject jsonSend = new JSONObject();
            jsonSend.put("userid", application.getUserId());
            jsonSend.put("tableid", gameId);
            jsonSend.put("seatindex", getUserIndex());
            jsonSend.put("isshow", isShow);
            msg += jsonSend.toString().replace("$", "￥");
            msg += "$";
            myBinder.sendInfo(msg);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToastInScreenCenter(GameActivity.this, "showcard出错！");

        }
    }

    private void infbesthand(String message) {
        if (bestHandTv == null) {
            int pixle14 = UITools.convertDpToPixel(12, this);
            int width = UITools.convertDpToPixel(70, this);
            int heigth = UITools.convertDpToPixel(30, this);
            int pixle15 = UITools.convertDpToPixel(15, this);
            int pixle5 = UITools.convertDpToPixel(5, this);
            int pixle90 = UITools.convertDpToPixel(90, this);
            bestHandTv = new TextView(this);
            bestHandTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixle14);
            bestHandTv.setTextColor(Color.WHITE);
            bestHandTv.setText(message);
            bestHandTv.setGravity(Gravity.CENTER);
            RelativeLayout.LayoutParams layoutParams1 = new RelativeLayout.LayoutParams(width, heigth);
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams1.leftMargin = pixle15;
            layoutParams1.rightMargin = pixle90;
            layoutParams1.bottomMargin = pixle5;
            bestHandTv.setLayoutParams(layoutParams1);
            layout_parent.addView(bestHandTv);
            bestHandTv.setVisibility(View.VISIBLE);
            bestHandTv.bringToFront();
        } else {
            bestHandTv.setText(message);
            bestHandTv.setVisibility(View.VISIBLE);
        }
    }

    private void addGameCards() {

        int pixel360 = UITools.convertDpToPixel(220, GameActivity.this);
        int pixel132 = UITools.convertDpToPixel(66, GameActivity.this);
        int pixel100 = UITools.convertDpToPixel(50, GameActivity.this);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        mGameCards = inflater.inflate(R.layout.layout_gamecard, null);
//        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(pixel370, pixel100, 0, 0);
//        lp.x = (mScreenWidth - pixel370) / 2;
//        lp.y = (mScreenHeight - pixel80) / 2 + pixel100;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(pixel360, pixel132);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        layoutParams.bottomMargin = pixel100;
        mGameCards.setLayoutParams(layoutParams);
        layout_parent.addView(mGameCards);
        mGameCards.setVisibility(View.INVISIBLE);

    }

    private void setPosition(View view, int width, int height, int px, int py) {
        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(width, height, 0, 0);
//        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(view.getLayoutParams());
        lp.x = px;
        lp.y = py;
        view.setLayoutParams(lp);

    }

    /*
    * 座位写死宽度 120 高度150px
    * */
    private void initSeatXY(int seatNum) {
//        iSeatValue=new int[mTableInfo.seats.length][2];
//        iTipLocation=new int[mTableInfo.seats.length][2];//大小盲注,用户操作的展现位置
//        iAmountChipLocation=new int[mTableInfo.seats.length][2];//已下注金额的展现位置
//        iD=new int[mTableInfo.seats.length][2];//庄家位置
//        iCardBack=new int[mTableInfo.seats.length][2];//有牌的用户背景

        int leftPading = 15;
        int rightPading = 15;
        int topPading = 20;
        int bottomPading = 10;

        leftPading = UITools.convertDpToPixel(10, this);
        rightPading = UITools.convertDpToPixel(10, this);
        topPading = UITools.convertDpToPixel(10, this);
        bottomPading = UITools.convertDpToPixel(10, this);

        switch (seatNum) {
            case 2:

                break;
            case 3:

                break;
            case 4:


                break;
            case 5:


                break;
            case 6:


                break;
            case 7:


                break;
            case 8:

                break;
            case 9:
                iSeatValue[0][0] = (mScreenWidth - mSeatViewWidth) / 2;
                iSeatValue[0][1] = mScreenHeight - mSeatViewHeight - bottomPading - UITools.convertDpToPixel(40, this);//60为手机顶部状态栏的高度
                //底部，在名字的上面标明，是否大小盲注，然后再上面是下注额度
                iAmountChipLocation[0][0] = (mScreenWidth - mAmountChipViewWidth) / 2;
                iAmountChipLocation[0][1] = iSeatValue[0][1] - mAmountChipVieHeight;
                iD[0][0] = iSeatValue[0][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[0][1] = iSeatValue[0][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[0][0] = iSeatValue[0][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[0][1] = iSeatValue[0][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[0][0] = iSeatValue[0][0] + mSeatViewWidth - mTipViewWidth;
                iTipLocation[0][1] = iSeatValue[0][1];


                iSeatValue[1][0] = 0;
                iSeatValue[1][1] = topPading + (mScreenHeight - topPading - bottomPading - mSeatViewHeight) / 4 * 3;
                iAmountChipLocation[1][0] = iSeatValue[1][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iAmountChipLocation[1][1] = iSeatValue[1][1] + (mSeatViewHeight - mAmountChipVieHeight) / 2;
                iD[1][0] = iSeatValue[1][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[1][1] = iSeatValue[1][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[1][0] = iSeatValue[1][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[1][1] = iSeatValue[1][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[1][0] = iSeatValue[1][0] + mSeatViewWidth - mTipViewWidth;
                iTipLocation[1][1] = iSeatValue[1][1];

                iSeatValue[2][0] = 0;
                iSeatValue[2][1] = topPading + (mScreenHeight - topPading - bottomPading - mSeatViewHeight) / 4 * 2;
                iAmountChipLocation[2][0] = iSeatValue[1][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iAmountChipLocation[2][1] = iSeatValue[2][1] + (mSeatViewHeight - mAmountChipVieHeight) / 2;
                iD[2][0] = iSeatValue[2][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[2][1] = iSeatValue[2][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[2][0] = iSeatValue[2][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[2][1] = iSeatValue[2][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[2][0] = iSeatValue[2][0] + mSeatViewWidth - mTipViewWidth;
                iTipLocation[2][1] = iSeatValue[2][1];

                iSeatValue[3][0] = 0;
                iSeatValue[3][1] = topPading + (mScreenHeight - topPading - bottomPading - mSeatViewHeight) / 4;
                iAmountChipLocation[3][0] = iSeatValue[3][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iAmountChipLocation[3][1] = iSeatValue[3][1] + (mSeatViewHeight - mAmountChipVieHeight) / 2;
                iD[3][0] = iSeatValue[3][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[3][1] = iSeatValue[3][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[3][0] = iSeatValue[3][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[3][1] = iSeatValue[3][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[3][0] = iSeatValue[3][0] + mSeatViewWidth - mTipViewWidth;
                iTipLocation[3][1] = iSeatValue[3][1];


                iSeatValue[4][0] = (mScreenWidth - 2 * mSeatViewWidth) / 3;
                iSeatValue[4][1] = topPading;
                //此处应该是正下方
                iAmountChipLocation[4][0] = iSeatValue[4][0] + (mSeatViewWidth - mAmountChipViewWidth) / 2;
                iAmountChipLocation[4][1] = iSeatValue[4][1] + mSeatViewHeight + 5;
                iD[4][0] = iSeatValue[4][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[4][1] = iSeatValue[4][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[4][0] = iSeatValue[4][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[4][1] = iSeatValue[4][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[4][0] = iSeatValue[4][0] + mSeatViewWidth - mTipViewWidth;
                iTipLocation[4][1] = iSeatValue[4][1];


                iSeatValue[5][0] = (mScreenWidth - 2 * mSeatViewWidth) / 3 * 2 + mSeatViewWidth;
                iSeatValue[5][1] = topPading;
                iAmountChipLocation[5][0] = iSeatValue[5][0] + (mSeatViewWidth - mAmountChipViewWidth) / 2;
                iAmountChipLocation[5][1] = iSeatValue[5][1] + mSeatViewHeight + 5;
                iD[5][0] = iSeatValue[5][0] + mSeatViewWidth - (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[5][1] = iSeatValue[5][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[5][0] = iSeatValue[5][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[5][1] = iSeatValue[5][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[5][0] = iSeatValue[5][0] + mSeatViewWidth - mTipViewWidth;
                iTipLocation[5][1] = iSeatValue[5][1];

                iSeatValue[6][0] = mScreenWidth - mSeatViewWidth;
                iSeatValue[6][1] = topPading + (mScreenHeight - topPading - bottomPading - mSeatViewHeight) / 4;
                iAmountChipLocation[6][0] = iSeatValue[6][0] - mAmountChipViewWidth + (mSeatViewWidth - mHeadImageWidth) / 2;
                iAmountChipLocation[6][1] = iSeatValue[6][1] + (mSeatViewHeight - mAmountChipVieHeight) / 2;
                iD[6][0] = iSeatValue[6][0] - mDViewWidth + (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[6][1] = iSeatValue[6][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[6][0] = iSeatValue[6][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[6][1] = iSeatValue[6][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[6][0] = iSeatValue[6][0];
                iTipLocation[6][1] = iSeatValue[6][1];


                iSeatValue[7][0] = mScreenWidth - mSeatViewWidth;
                iSeatValue[7][1] = topPading + (mScreenHeight - topPading - bottomPading - mSeatViewHeight) / 4 * 2;
                iAmountChipLocation[7][0] = iSeatValue[7][0] - mAmountChipViewWidth + (mSeatViewWidth - mHeadImageWidth) / 2;
                iAmountChipLocation[7][1] = iSeatValue[7][1] + (mSeatViewHeight - mAmountChipVieHeight) / 2;
                iD[7][0] = iSeatValue[7][0] - mDViewWidth + (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[7][1] = iSeatValue[7][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[7][0] = iSeatValue[7][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[7][1] = iSeatValue[7][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[7][0] = iSeatValue[7][0];
                iTipLocation[7][1] = iSeatValue[7][1];


                iSeatValue[8][0] = mScreenWidth - mSeatViewWidth;
                iSeatValue[8][1] = topPading + (mScreenHeight - topPading - bottomPading - mSeatViewHeight) / 4 * 3;
                iAmountChipLocation[8][0] = iSeatValue[8][0] - mAmountChipViewWidth + (mSeatViewWidth - mHeadImageWidth) / 2;
                iAmountChipLocation[8][1] = iSeatValue[8][1] + (mSeatViewHeight - mAmountChipVieHeight) / 2;
                iD[8][0] = iSeatValue[8][0] - mDViewWidth + (mSeatViewWidth - mHeadImageWidth) / 2;
                iD[8][1] = iSeatValue[8][1] + mSeatViewHeight - mDViewHeight;
                iCardBack[8][0] = iSeatValue[8][0] + (mSeatViewWidth - mHeadImageWidth) / 2;
                iCardBack[8][1] = iSeatValue[8][1] + mSeatViewHeight - mNameTextHeight - mCardBackHeight;
                iTipLocation[8][0] = iSeatValue[8][0];
                iTipLocation[8][1] = iSeatValue[8][1];

                break;

        }

    }

    public Bitmap drawCardBack() {

        int pixel150 = UITools.convertDpToPixel(75, GameActivity.this);
        int pixel142 = UITools.convertDpToPixel(71, GameActivity.this);
        int pixel40 = UITools.convertDpToPixel(20, GameActivity.this);
        int pixel5 = UITools.convertDpToPixel(3, GameActivity.this);

        Bitmap newBitmap = Bitmap.createBitmap(pixel150, pixel142, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(newBitmap);
        // 获取资源文件的引用res
        Resources res = getResources();
        // 获取图形资源文件
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.card_bg);
        // 设置canvas画布背景为白色
        //canvas.drawColor(Color.WHITE);
        // 在画布上绘制缩放之前的位图，以做对比
        //屏幕上的位置坐标是0,0
        //canvas.drawBitmap(bmp, 0, 0, null);
        // 定义矩阵对象
        Matrix matrix = new Matrix();
        // 缩放原图
        matrix.postScale(1f, 1f);
        // 向左旋转45度，参数为正则向右旋转
        matrix.postRotate(-30);
        //bmp.getWidth(), 500分别表示重绘后的位图宽高
        Bitmap dstbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),

                matrix, true);

        // 在画布上绘制旋转后的位图
        //放在坐标为0,200的位置
        canvas.drawBitmap(dstbmp, 0, 0, null);
        matrix = new Matrix();
        // 缩放原图
        matrix.postScale(1f, 1f);
        matrix.postRotate(-15);
        dstbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),

                matrix, true);

        // 在画布上绘制旋转后的位图

        canvas.drawBitmap(dstbmp, pixel40, pixel5, null);

        return newBitmap;
    }

    public Bitmap drawSingleCard(int suit, int member) {

        int pixel88 = UITools.convertDpToPixel(42, GameActivity.this);
        int pixel120 = UITools.convertDpToPixel(66, GameActivity.this);
        int pixel4 = UITools.convertDpToPixel(2, GameActivity.this);
        int pixel35 = UITools.convertDpToPixel(17, GameActivity.this);
        int pixel30 = UITools.convertDpToPixel(15, GameActivity.this);
        int pixel55 = UITools.convertDpToPixel(28, GameActivity.this);
        int pixel5 = UITools.convertDpToPixel(5, GameActivity.this);

        Bitmap newBitmap = Bitmap.createBitmap(pixel88, pixel120, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        // 获取资源文件的引用res
        Resources res = getResources();
        //三张图的位置
        int iPicLocation[][] = new int[3][2];
        iPicLocation[0][0] = pixel4;
        iPicLocation[0][1] = pixel5;
        iPicLocation[1][0] = pixel4;
        iPicLocation[1][1] = pixel35;
        iPicLocation[2][0] = pixel30;
        iPicLocation[2][1] = pixel55;

        //画背景
        Bitmap bmpBackground;
        if (member == 11) {
            bmpBackground = BitmapFactory.decodeResource(res, R.drawable.rank_fg_11);

        } else if (member == 12) {

            bmpBackground = BitmapFactory.decodeResource(res, R.drawable.rank_fg_12);

        } else if (member == 13) {
            bmpBackground = BitmapFactory.decodeResource(res, R.drawable.rank_fg_13);

        } else {
            bmpBackground = BitmapFactory.decodeResource(res, R.drawable.fg);
        }

        //画牌的背景
        Rect dest = new Rect(0, 0, newBitmap.getWidth(), newBitmap.getHeight());
        canvas.drawBitmap(bmpBackground, null, dest, null);
        switch (member) {
            case 2:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_2), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_2), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_2), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_2), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 3:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_3), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_3), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_3), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_3), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 4:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_4), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_4), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_4), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_4), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 5:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_5), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_5), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_5), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_5), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 6:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_6), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_6), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_6), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_6), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 7:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_7), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_7), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_7), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_7), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 8:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_8), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_8), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_8), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_8), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 9:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_9), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_9), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_9), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_9), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 10:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_10), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_10), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_10), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_10), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 11:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_11), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_11), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_11), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_11), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 12:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_12), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_12), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_12), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_12), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 13:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_13), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_13), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_13), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_13), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
            case 14:
                switch (suit) {
                    case 0:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_14), iPicLocation[0][0], iPicLocation[0][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_3), iPicLocation[1][0], iPicLocation[1][1], null);//梅花
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_3), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 1:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_14), iPicLocation[0][0], iPicLocation[0][1], null);//方块
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_2), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_2), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 2:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank1_14), iPicLocation[0][0], iPicLocation[0][1], null);//红心
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_1), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_1), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;
                    case 3:
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.rank2_14), iPicLocation[0][0], iPicLocation[0][1], null);//黑桃
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit1_4), iPicLocation[1][0], iPicLocation[1][1], null);
                        canvas.drawBitmap(BitmapFactory.decodeResource(res, R.drawable.suit2_4), iPicLocation[2][0], iPicLocation[2][1], null);
                        break;

                }
                break;
        }


//        // 定义矩阵对象
//        Matrix matrix = new Matrix();
//        // 缩放原图
//        matrix.postScale(1f, 1f);
//        // 向左旋转45度，参数为正则向右旋转
//        matrix.postRotate(-30);
//        //bmp.getWidth(), 500分别表示重绘后的位图宽高
//        Bitmap dstbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
//
//                matrix, true);
//
//        // 在画布上绘制旋转后的位图
//        //放在坐标为0,200的位置
//        canvas.drawBitmap(dstbmp,0, 0, null);
//        matrix = new Matrix();
//        // 缩放原图
//        matrix.postScale(1f, 1f);
//        matrix.postRotate(-15);
//        dstbmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
//
//                matrix, true);
//
//        // 在画布上绘制旋转后的位图
//        //放在坐标为0,200的位置
//        canvas.drawBitmap(dstbmp, 40, 5, null);


        return newBitmap;
    }


    private void showWindow(View parent, boolean fromBackToSeat) {

        int width = UITools.convertDpToPixel(300, this);
        int height = UITools.convertDpToPixel(310, this);

        View contentView = getPopupWindowContentView(fromBackToSeat);
//        popupWindow = new PopupWindow(contentView,getResources().getDisplayMetrics().widthPixels-160,240
//                , true);
        popupWindow = new PopupWindow(contentView, width, height
                , true);
//        popupWindow = new PopupWindow(contentView, contentView.getMeasuredWidth(),contentView.getMeasuredHeight()
//                , true);
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        // 如果不设置PopupWindow的背景，有些版本就会出现一个问题：无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        // 设置好参数之后再show

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        //int xOffset = parent.getWidth()  - contentView.getMeasuredWidth() ;
//        popupWindow.showAsDropDown(parent,xOffset,20);    // 在mButton2的中间显示
        layout_parent.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(layout_parent, Gravity.CENTER, 0, 0);

            }
        });
//        popupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
//        int windowPos[] = calculatePopWindowPos(parent, contentView);
//        popupWindow.showAtLocation(GameActivity.this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        // popupWindow.showAtLocation(parent,Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);
    }

    private View getPopupMenuWindowContentView() {
        // 一个自定义的布局，作为显示的内容
        int layoutId = R.layout.item_popmenu;   // 布局ID
        View contentView = LayoutInflater.from(this).inflate(layoutId, null);

        return contentView;
    }

    private View getPopupWindowContentView(final boolean fromBackToSeat) {
        // 一个自定义的布局，作为显示的内容
        int layoutId = R.layout.popupwindow_buy_score;   // 布局ID
        View contentView = LayoutInflater.from(this).inflate(layoutId, null);


        button_apply = (Button) contentView.findViewById(R.id.button_apply);
        tv_buy_coin = contentView.findViewById(R.id.tv_buy_coin);
        tv_buy_coin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GameActivity.this, StoreActivity.class));
            }
        });

        tv_message = contentView.findViewById(R.id.tv_message);
        tv_message.setVisibility(View.INVISIBLE);
        //TODO: mTableInfo is null???
        if (null != mTableInfo && mTableInfo.mintakeinchips > application.getUser().goldcoin) {
            tv_message.setVisibility(View.VISIBLE);
            tv_message.setText("个人财富不足，不能补充记分牌");
        }

        tv_core = contentView.findViewById(R.id.tv_core);
        tv_core.setText(mTableInfo.mintakeinchips + "");

        tv_coin = contentView.findViewById(R.id.tv_coin);
        tv_coin.setText(application.getUser().goldcoin + "");
        tv_service_coin = contentView.findViewById(R.id.tv_service_coin);
        tv_service_coin.setText(mTableInfo.mintakeinchips / 10 + "");
        tv_in_coin = contentView.findViewById(R.id.tv_in_coin);
        if (mTableInfo.iscontroltakein) {
            //控制带入
            tv_in_coin.setText(takeinchips + "/" + permittakeinchips);
            button_apply.setText("申请");
        } else {
            //非控制带入
            String value = mTableInfo.takeinchipsuplimit + "";
            if (value.equals("0")) {
                value = "无上限";
            }
            button_apply.setText("确定");
            tv_in_coin.setText(takeinchips + "/" + value);
        }


        iv_close = contentView.findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭popwndow
                popupWindow.dismiss();
            }
        });


        seekbar_core = contentView.findViewById(R.id.seekbar_core);
        seekbar_core.setRangeCount((mTableInfo.maxtakeinchips - mTableInfo.mintakeinchips) / (mTableInfo.smallblind * 100) + 1);
        seekbar_core.setOnSelectRangeBarListener(new RangeSliderBar2.OnSelectRangeBarListener() {
            @Override
            public void OnSelectRange(int rangeNum) {
                //监听，负责更新数字
                int core = (rangeNum * mTableInfo.smallblind * 100 + mTableInfo.mintakeinchips);
                if (core > application.getUser().goldcoin) {
                    tv_message.setVisibility(View.VISIBLE);
                    tv_message.setText("个人财富不足，不能补充记分牌");
                    button_apply.getText().equals("购买金币");
                } else {
                    tv_message.setVisibility(View.INVISIBLE);
                    if (mTableInfo.iscontroltakein) {
                        //控制带入要判断
                        /*if (mTableInfo.iscontroltakein){
            //控制带入
            tv_in_coin.setText(takeinchips+"/"+permittakeinchips);
        }else{
            //非控制带入
            String value=mTableInfo.takeinchipsuplimit+"";
            if (value.equals("0")){
                value="无上限";
            }
            tv_in_coin.setText(takeinchips+"/"+value);
        }*/
                        //控制带入
                        if (core + takeinchips > permittakeinchips) {
                            button_apply.setText("申请");

                        } else {
                            button_apply.setText("确定");
                        }

                    } else {
                        //非控制带入
                        button_apply.setText("确定");
                    }
                }
                tv_core.setText(rangeNum * mTableInfo.smallblind * 100 + mTableInfo.mintakeinchips + "");
                tv_service_coin.setText((rangeNum * mTableInfo.smallblind * 100 + mTableInfo.mintakeinchips) / 10 + "");


            }
        });

        button_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //处理按钮事件
                if (button_apply.getText().equals("购买金币")) {
                    //弹出金币购买界面
                } else if (button_apply.getText().equals("确定")) {
                    //提交带入申请，减去用户的金币
                    int buycore = Integer.parseInt(tv_core.getText().toString());
                    //判断已带入，总带入
//                    if ((takeinchips + buycore) > mTableInfo.maxtakeinchips) {
//
//                    } else {

                        if (!isBackToSeatOrIncreaseChips) {
                            //首次购买，发送sitseat指令
                            try {

                                String ip = application.getIp();
                                if (TextUtils.isEmpty(ip)) {
                                    ip = Util.getLocalIpAddress();
                                }
                                String msg = Constant.GAME_SIT_SEAT + "|";
                                JSONObject jsonSend = new JSONObject();
                                jsonSend.put("userid", application.getUserId());
                                jsonSend.put("tableid", gameId);
                                jsonSend.put("seatindex", mWantSeatIndex);
                                jsonSend.put("intochips", buycore);
                                jsonSend.put("ip", ip);
                                jsonSend.put("gpsx", application.getLongitude());
                                jsonSend.put("gpsy", application.getLatitude());
                                msg += jsonSend.toString().replace("$", "￥");
                                msg += "$";
                                myBinder.sendInfo(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "坐下座位出错！");
                            }


                        } else {
                            //非首次购买，发送addchip指令

                            try {
                                String msg = Constant.GAME_ADD_CHIPS + "|";
                                JSONObject jsonSend = new JSONObject();
                                jsonSend.put("userid", application.getUserId());
                                jsonSend.put("tableid", gameId);
                                jsonSend.put("chips", buycore);

                                msg += jsonSend.toString().replace("$", "￥");
                                msg += "$";
                                myBinder.sendInfo(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "坐下座位出错！");
                            }

                        }
//                    }


                } else if (button_apply.getText().equals("申请")) {
                    //发送申请
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //拼装url字符串

                                int applycore = Integer.parseInt(tv_core.getText().toString());
                                DzApplication applicatoin = (DzApplication) getApplication();
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("userid", applicatoin.getUserId());
                                jsonObj.put("tableid", gameId);
                                jsonObj.put("requesttakeinchips", applycore);
                                jsonObj.put("permitclubid", 0);
                                jsonObj.put("permituserid", 0);

                                String strURL = getString(R.string.url_remote);
                                strURL += "func=requesttakein&param=" + jsonObj.toString();

                                URL url = new URL(strURL);
                                Request request = new Request.Builder().url(strURL).build();
                                Response response = DzApplication.getHttpClient().newCall(request).execute();
                                String result = response.body().string();

                                JSONObject jsonObject = new JSONObject(result);
                                if (jsonObject.getInt("ret") == 0) {
                                    // 创建牌局成功,发送IM消息RequestTakeIn|{‘tablename’:abc} tv_buy_coin
                                    //sendMessage(Message message, MessageSendingOptions options)
//                                    HashMap<String,String> info=new HashMap<String,String>();
//                                    info.put("applyid",applicatoin.getUserId()+"");
//                                    info.put("tablename","");
                                    //createSingleCustomMessage,创建自定义消息
                                    // cn.jpush.im.android.api.model.Message message=JMessageClient.createSingleCustomMessage(mTableInfo.createuserid+"",getString(R.string.app_key),info);

                                    JSONObject json = new JSONObject();
                                    json.put("tablename", gameHouseName);
                                    String sendstr = "RequestTakeIn|" + json.toString();
                                    cn.jpush.im.android.api.model.Message message = JMessageClient.createSingleTextMessage(mTableInfo.createuserid + "", getString(R.string.app_key), sendstr);

//                                 k   MessageSendingOptions messageSendingOptions=new MessageSendingOptions();
//                                    messageSendingOptions.setShowNotification(true);
//                                    messageSendingOptions.setNotificationTitle("带入申请");
//                                    messageSendingOptions.setNotificationText("收到一条带入申请");
                                    message.setOnSendCompleteCallback(new BasicCallback() {
                                        @Override
                                        public void gotResult(int responseCode, String responseDesc) {
                                            if (responseCode == 0) {
                                                mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPWINDOW);
                                                ToastUtil.showToastInScreenCenter(GameActivity.this, "提交申请带入成功，请等待房主或管理员审核！");
                                                // 消息发送成功
                                            } else {
                                                // 消息发送失败
                                                ToastUtil.showToastInScreenCenter(GameActivity.this, "提交申请带入失败，请重新申请！");
                                            }
                                        }
                                    });
                                    JMessageClient.sendMessage(message);
                                    // JMessageClient.sendMessage(message,messageSendingOptions);


                                } else {
                                    ToastUtil.showToastInScreenCenter(GameActivity.this, "申请带入失败，错误原因：" + jsonObject.getString("msg"));
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtil.showToastInScreenCenter(GameActivity.this, "提交申请带入异常，请稍后重试!" + e.toString());
                            }

                        }
                    });
                    thread.start();

                }
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }

            }

        });

        if (mTableInfo.iscontroltakein) {
            //控制带入
            if (mTableInfo.mintakeinchips * 11 / 10 > application.getUser().goldcoin) {
                button_apply.setText("购买金币");
            } else if (Integer.valueOf(tv_core.getText().toString()) + takeinchips > permittakeinchips) {
                button_apply.setText("申请");
            } else {
                button_apply.setText("确定");
            }

        } else {
            //非控制带入
            button_apply.setText("确定");
            if (mTableInfo.mintakeinchips * 11 / 10 > application.getUser().goldcoin) {
                button_apply.setText("购买金币");
            }

        }

        return contentView;
    }

    private void getTakeInChipsFromServer() {
        //启动线程到后台去请求数据
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //拼装url字符串

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("tableid", gameId);
                    jsonObj.put("userid", application.getUserId());

                    String strURL = getString(R.string.url_remote);
                    strURL += "func=getcontroltakeininfo&param=" + jsonObj.toString();

                    URL url = new URL(strURL);
                    Request request = new Request.Builder().url(strURL).build();
                    Response response = DzApplication.getHttpClient().newCall(request).execute();
                    String result = response.body().string();

                    JSONObject jsonObject = new JSONObject(result);
                    takeinchips = jsonObject.getInt("takeinchips");
//                    seekBarRaise.setMax(takeinchips);
                    permittakeinchips = jsonObject.getInt("permittakeinchips");
                    tablecreateuserid = jsonObject.getInt("tablecreateuserid");
                    //告诉主线程，从后台取takein数据成功
                    mainThreadHandler.sendEmptyMessage(MESSAGE_TAKEIN_INFO);

                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.showToastInScreenCenter(GameActivity.this, "到服务器取牌桌带入信息失败，请稍后重试!" + e.toString());
                }

            }
        });
        thread.start();
    }


    private int getUserIndex() {
        if (mGameUser.containsKey(application.getUserId())) {
            return mGameUser.get(application.getUserId()).seatindex;
        } else {
            return -1;
        }

//        int userIndex=-1;
//        for (int key : mGameUser.keySet()) {
//            if (mGameUser.get(key).userId==application.getUserId()){
//                userIndex=mGameUser.get(key).seatindex;
//            }
//        }
//        return userIndex;
    }

    @Override
    protected void onStop() {


        super.onStop();

        for (Integer index : downloadBroadcastReceiverMap.keySet()) {
            unregisterReceiver(downloadBroadcastReceiverMap.get(index));
        }

    }

    @Override
    protected void onDestroy() {
        JMessageClient.unRegisterEventReceiver(this);
        myBinder.removeCallback(socketCallback);
        this.unbindService(connection);
//        Intent Intent = new Intent(this, SocketService.class);
//        stopService(Intent);
        super.onDestroy();
    }


    public void onEvent(GetEventNotificationTaskMng.EventEntity event) {
        //do your own business
//        int i=0;
//        i+=1;
    }

    public void onEvent(MessageEvent event) {
        //接受消息

        final cn.jpush.im.android.api.model.Message message = event.getMessage();
        if (message.getContentType() == ContentType.text) {

            String msgReturn = ((TextContent) message.getContent()).getText();//message.getContent().getStringExtra("text");
            if (msgReturn.indexOf("requestrakein") == 0 && msgReturn.indexOf("requesttakeinret") == -1) {
                //RequestTakeIn|{‘tablename’:我的房间}
                String[] recData = msgReturn.split("\\|");

                try {
                    String gameHouseName = new JSONObject(recData[1]).getString("tablename");
                    String from = message.getFromUser().getNickname();
                    String showMsg = gameHouseName + "有带入申请";
                    //新建状态栏通知
                    showNotification("带入申请", showMsg);
                } catch (Exception e) {

                }


            } else if (msgReturn.indexOf("requesttakeinret") == 0) {
                /*RequestTakeInRet|{‘IsPermit’:true,’permittakein’:300}*/
                String[] recData = msgReturn.split("\\|");

                try {
                    JSONObject jsonReturn = new JSONObject(recData[1]);
                    Boolean isPermit = jsonReturn.getBoolean("ispermit");
                    String showMsg;
                    if (isPermit) {
                        showMsg = "您的带入申请已同意，允许带入记分牌为" + jsonReturn.getInt("permittakein");
                    } else {
                        showMsg = "您的带入申请被拒绝";
                    }
                    //新建状态栏通知
                    showNotification("带入申请处理结果", showMsg);
                } catch (Exception e) {

                }
            } else if (msgReturn.indexOf("requestaddtoclubret") == 0) {
                String[] recData = msgReturn.split("\\|");

                try {
                    JSONObject jsonReturn = new JSONObject(recData[1]);
                    String clubName = jsonReturn.optString("clubname");
                    Boolean isPermit = jsonReturn.getBoolean("ispermit");
                    String showMsg;
                    if (isPermit) {
                        showMsg = "加入俱乐部" + clubName + "审核通过";
                    } else {
                        showMsg = "加入俱乐部" + clubName + "审核不通过";
                    }
                    //新建状态栏通知
                    showNotification("俱乐部审核结果", showMsg);
                } catch (Exception e) {

                }
            } else if (msgReturn.indexOf("requestaddtoclub") == 0) {
                String[] recData = msgReturn.split("\\|");

                try {
                    JSONObject jsonReturn = new JSONObject(recData[1]);
                    String nickName = jsonReturn.optString("usernickname");
                    String requestMsg = jsonReturn.optString("requestmsg");
                    String showMsg = nickName + "申请加入俱乐部(" + requestMsg + ")";
                    //新建状态栏通知
                    showNotification("申请加入俱乐部", showMsg);
                } catch (Exception e) {

                }
            }
        }


//        //若为群聊相关事件，如添加、删除群成员
//        if (message.getContentType() == ContentType.eventNotification) {
//            GroupInfo groupInfo = (GroupInfo) message.getTargetInfo();
//            long groupId = groupInfo.getGroupID();
//            EventNotificationContent.EventNotificationType type = ((EventNotificationContent) message
//                    .getContent()).getEventNotificationType();
//            if (groupId == mGroupId) {
//                switch (type) {
//                    case group_member_added:
//                        //添加群成员事件
//                        List<String> userNames = ((EventNotificationContent) message.getContent()).getUserNames();
//                        //群主把当前用户添加到群聊，则显示聊天详情按钮
//                        refreshGroupNum();
//                        if (userNames.contains(mMyInfo.getNickname()) || userNames.contains(mMyInfo.getUserName())) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mChatView.showRightBtn();
//                                }
//                            });
//                        }
//
//                        break;
//                    case group_member_removed:
//                        //删除群成员事件
//                        userNames = ((EventNotificationContent) message.getContent()).getUserNames();
//                        //群主删除了当前用户，则隐藏聊天详情按钮
//                        if (userNames.contains(mMyInfo.getNickname()) || userNames.contains(mMyInfo.getUserName())) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mChatView.dismissRightBtn();
//                                    GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
//                                    if (TextUtils.isEmpty(groupInfo.getGroupName())) {
//                                        mChatView.setChatTitle(R.string.group);
//                                    } else {
//                                        mChatView.setChatTitle(groupInfo.getGroupName());
//                                    }
//                                    mChatView.dismissGroupNum();
//                                }
//                            });
//                        } else {
//                            refreshGroupNum();
//                        }
//
//                        break;
//                    case group_member_exit:
//                        refreshGroupNum();
//                        break;
//                }
//            }
//        }
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (message.getTargetType() == ConversationType.single) {
//                    UserInfo userInfo = (UserInfo) message.getTargetInfo();
//                    String targetId = userInfo.getUserName();
//                    String appKey = userInfo.getAppKey();
//                    if (mIsSingle && targetId.equals(mTargetId) && appKey.equals(mTargetAppKey)) {
//                        cn.jpush.im.android.api.model.Message lastMsg = mChatAdapter.getLastMsg();
//                        if (lastMsg == null || message.getId() != lastMsg.getId()) {
//                            mChatAdapter.addMsgToList(message);
//                        } else {
//                            mChatAdapter.notifyDataSetChanged();
//                        }
//                    }
//                } else {
//                    long groupId = ((GroupInfo) message.getTargetInfo()).getGroupID();
//                    if (groupId == mGroupId) {
//                        cn.jpush.im.android.api.model.Message lastMsg = mChatAdapter.getLastMsg();
//                        if (lastMsg == null || message.getId() != lastMsg.getId()) {
//                            mChatAdapter.addMsgToList(message);
//                        } else {
//                            mChatAdapter.notifyDataSetChanged();
//                        }
//                    }
//                }
//            }
//        });
    }

    @Override
    protected void onPause() {
        super.onPause();
//        JMessageClient.exitConversation();
//
    }

    @Override
    protected void onResume() {

//        if (mTableInfo!=null) {
//            JMessageClient.enterSingleConversation(mTableInfo.createuserid + "", getString(R.string.app_key));
//        }
        super.onResume();

    }

    private void showNotification(String title, String showmsg) {
        // TODO Auto-generated method stub
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.icon);//设置图标
        builder.setTicker(showmsg);//手机状态栏的提示
        builder.setContentTitle(title);//设置标题
        builder.setContentText(showmsg);//设置通知内容
        builder.setWhen(System.currentTimeMillis());//设置通知时间
        builder.setContentIntent(null);//点击后的意图
        builder.setDefaults(Notification.DEFAULT_LIGHTS);//设置指示灯
        builder.setDefaults(Notification.DEFAULT_SOUND);//设置提示声音
        builder.setDefaults(Notification.DEFAULT_VIBRATE);//设置震动
        Notification notification = builder.getNotification();//4.1以上，以下要用getNotification()
        notificationManager.notify(0, notification);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        try {
            mainThreadHandler.sendEmptyMessage(MESSAGE_DISMISS_POPMENU);
            //ToastUtil.showToastInScreenCenter(GameActivity.this,v.getTag().toString());
            String msg = Constant.GAME_LEAVE_TABLE + "|";
            JSONObject jsonSend = new JSONObject();
            jsonSend.put("userid", application.getUserId());
            jsonSend.put("tableid", gameId);
            msg += jsonSend.toString().replace("$", "￥");
            msg += "$";
            myBinder.sendInfo(msg);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
//                            ToastUtil.showToastInScreenCenter(GameActivity.this, "离开座位出错！");

        }
    }
}
