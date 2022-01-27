package com.netease.nim.ccsocial_nim.mainTab;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDex;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.nim.ccsocial_nim.NimApplication;
import com.netease.nim.ccsocial_nim.R;
import com.netease.nim.ccsocial_nim.common.ui.viewpager.FadeInOutPageTransformer;
import com.netease.nim.ccsocial_nim.common.ui.viewpager.PagerSlidingTabStrip;
import com.netease.nim.ccsocial_nim.config.preference.Preferences;
import com.netease.nim.ccsocial_nim.mainTab.fragment.ContactFragment.activity.AddFriendActivity;
import com.netease.nim.ccsocial_nim.login.LoginActivity;
import com.netease.nim.ccsocial_nim.login.LogoutHelper;
import com.netease.nim.ccsocial_nim.main.activity.GlobalSearchActivity;
import com.netease.nim.ccsocial_nim.main.activity.RecentSessionActivity;
import com.netease.nim.ccsocial_nim.main.activity.SettingsActivity;
import com.netease.nim.ccsocial_nim.main.adapter.MainTabPagerAdapter;
import com.netease.nim.ccsocial_nim.main.helper.CustomNotificationCache;
import com.netease.nim.ccsocial_nim.main.helper.SystemMessageUnreadManager;
import com.netease.nim.ccsocial_nim.main.model.MainTab;
import com.netease.nim.ccsocial_nim.main.reminder.ReminderItem;
import com.netease.nim.ccsocial_nim.main.reminder.ReminderManager;
import com.netease.nim.ccsocial_nim.session.SessionHelper;
import com.netease.nim.ccsocial_nim.team.TeamCreateHelper;
import com.netease.nim.ccsocial_nim.team.activity.AdvancedTeamSearchActivity;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.main.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.drop.DropManager;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.log.sdk.wrapper.NimLog;
import com.netease.nim.uikit.support.permission.MPermission;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.yunxin.nertc.model.ProfileManager;
import com.netease.yunxin.nertc.nertcvideocall.model.NERTCVideoCall;
import com.netease.yunxin.nertc.nertcvideocall.model.UIService;
import com.netease.yunxin.nertc.nertcvideocall.model.VideoCallOptions;
import com.netease.yunxin.nertc.nertcvideocall.model.impl.UIServiceManager;
import com.netease.yunxin.nertc.nertcvideocall.utils.CallParams;
import com.netease.yunxin.nertc.ui.NERTCVideoCallActivity;
import com.netease.yunxin.nertc.ui.team.TeamG2Activity;
import com.outman.framework.entity.Constants;
import com.outman.framework.event.EventManager;
import com.outman.framework.event.MessageEvent;
import com.outman.framework.utils.IToastUtils;
import com.outman.framework.utils.NetUtils.ApiService;
import com.outman.framework.utils.NetUtils.RetrofitManager;
import com.outman.framework.utils.NetUtils.bean.ResponseData;
import com.qiyukf.unicorn.ysfkit.unicorn.api.Unicorn;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends UI implements ViewPager.OnPageChangeListener,
        ReminderManager.UnreadNumChangedCallback {

    private static final String TAG = "MainActivity";
    //定义变量
    private static final String EXTRA_APP_QUIT = "APP_QUIT";

    private static final int REQUEST_CODE_NORMAL = 1;

    private static final int REQUEST_CODE_ADVANCED = 2;

    private static final int BASIC_PERMISSION_REQUEST_CODE = 100;

    private static final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};



    private PagerSlidingTabStrip tabs;

    private ViewPager pager;

    private int scrollState;

    private MainTabPagerAdapter adapter;


    private boolean isFirstIn;
    private int pagerCurrentItem;

    private Observer<Integer> sysMsgUnreadCountChangedObserver = (Observer<Integer>) unreadCount -> {
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unreadCount);
        ReminderManager.getInstance().updateContactUnreadNum(unreadCount);
    };
    //上下文
    private Context mContext;

    private FrameLayout mMainLayout;

//    private LinearLayout llIndex;
//    private ImageView ivIndex;
//    private TextView tvIndex;
//    private IndexFragment mIndexFragment = null;
//    private FragmentTransaction mIndexFragmentTransaction = null;
//
//    private LinearLayout llContact;
//    private ImageView ivContact;
//    private TextView tvContact;
//    private ContactFragment mContactFragment = null;
//    private FragmentTransaction mContactFragmentTransaction = null;
//
//    private LinearLayout llChat;
//    private ImageView ivChat;
//    private TextView tvChat;
//    private ChatFragment mChatFragment = null;
//    private FragmentTransaction mChatFragmentTransaction = null;
//
//    private LinearLayout llMe;
//    private ImageView ivMe;
//    private TextView tvMe;
//    private MeFragment mMeFragment = null;
//    private FragmentTransaction mMeFragmentTransaction = null;

    private Disposable disposable;

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, com.netease.nim.ccsocial_nim.mainTab.MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);

    }
    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 判断用户登录状态
        if(Constants.SP_USER_DATA == null){
            Intent intent = new Intent(getContext(),LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            finish();
        }


        // 低流量请求
//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }
        // 初始化MultiDex
        MultiDex.install(this);
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        ImmersionBar.with(this)
                .statusBarDarkFont(true)
                .fitsSystemWindows(true)  //使用该属性,必须指定状态栏颜色
                .statusBarColor(R.color.colorBgLightGray)
                .init();

//        initView();
        setToolBar(R.id.toolbar, R.string.app_index_label_slogen);
        isFirstIn = true;
        //不保留后台活动，从厂商推送进聊天页面，会无法退出聊天页面
        if (savedInstanceState == null && parseIntent()) {
            return;
        }
        init();

        // 初始化G2组件
        initG2();


//        pager.setCurrentItem(pagerCurrentItem);
//        if(getPagerCurrentItem.hasExtra("pagerCurrentItem")){
//
//        }else{
//            System.out.println(getPagerCurrentItem.getExtras());
//            System.out.println("9098");
//        }
    }

    private void initG2() {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(new Observer<StatusCode>() {
            @Override
            public void onEvent(StatusCode statusCode) {
                if (statusCode == StatusCode.LOGINED) {
                    NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(this, false);

                    // TODO G2 用户根据实际配置方式获取
                    LoginInfo loginInfo = NimApplication.getLoginInfo();
                    if (loginInfo == null) {
                        return;
                    }

                    String imAccount = loginInfo.getAccount();
                    String imToken = loginInfo.getToken();

                    ApplicationInfo appInfo = null;
                    try {
                        // TODO G2 用户根据实际配置方式获取
                        appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                        String appKey = appInfo.metaData.getString("com.netease.nim.appKey");

                        NERTCVideoCall.sharedInstance().setupAppKey(getApplicationContext(), appKey, new VideoCallOptions(null, new UIService() {
                            @Override
                            public Class getOneToOneAudioChat() {
                                return NERTCVideoCallActivity.class;
                            }

                            @Override
                            public Class getOneToOneVideoChat() {
                                return NERTCVideoCallActivity.class;
                            }

                            @Override
                            public Class getGroupVideoChat() {
                                return TeamG2Activity.class;
                            }

                            @Override
                            public int getNotificationIcon() {
                                return R.drawable.ic_logo;
                            }

                            @Override
                            public int getNotificationSmallIcon() {
                                return R.drawable.ic_logo;
                            }

                            @Override
                            public void startContactSelector(Context context, String teamId, List<String> excludeUserList, int requestCode) {

                            }
                        }, ProfileManager.getInstance()));

                        NERTCVideoCall.sharedInstance().login(imAccount, imToken, new RequestCallback<LoginInfo>() {
                            @Override
                            public void onSuccess(LoginInfo param) {

                            }

                            @Override
                            public void onFailed(int code) {

                            }

                            @Override
                            public void onException(Throwable exception) {

                            }
                        });

                        // 请求 rtc token 服务，若非安全模式不需设置，安全模式按照官网实现 token 服务通过如下接口设置回组件
                        NERTCVideoCall.sharedInstance().setTokenService((uid, callback) -> {
                            //获取token
//                        Result result = network.requestToken(uid);
//                        if (result.success) {
//                            callback.onSuccess(result.token);
//                        } else if (result.exception != null) {
//                            callback.onException(result.exception);
//                        } else {
//                            callback.onFailed(result.code);
//                        }
                        });
                        Intent intent = getIntent();
                        NimLog.d(TAG, String.format("onNotificationClicked INVENT_NOTIFICATION_FLAG:%s", intent.hasExtra(CallParams.INVENT_NOTIFICATION_FLAG)));
                        if (intent.hasExtra(CallParams.INVENT_NOTIFICATION_FLAG) && intent.getBooleanExtra(CallParams.INVENT_NOTIFICATION_FLAG, false)) {
                            Bundle extraIntent = intent.getBundleExtra(CallParams.INVENT_NOTIFICATION_EXTRA);
                            intent.removeExtra(CallParams.INVENT_NOTIFICATION_FLAG);
                            intent.removeExtra(CallParams.INVENT_NOTIFICATION_EXTRA);

                            Intent avChatIntent = new Intent();
                            for (String key : CallParams.CallParamKeys) {
                                avChatIntent.putExtra(key, extraIntent.getString(key));
                            }

                            String callType = extraIntent.getString(CallParams.INVENT_CALL_TYPE);
                            String channelType = extraIntent.getString(CallParams.INVENT_CHANNEL_TYPE);
                            NimLog.d(TAG, String.format("onNotificationClicked callType:%s channelType:%s", callType, channelType));

                            if (TextUtils.equals(String.valueOf(CallParams.CallType.TEAM), callType)) {
                                avChatIntent.setClass(MainActivity.this, UIServiceManager.getInstance().getUiService().getGroupVideoChat());

                                try {
                                    String userIdsBase64 = extraIntent.getString(CallParams.INVENT_USER_IDS);
                                    String userIdsJson = new String(Base64.decode(userIdsBase64, Base64.DEFAULT));
                                    JSONArray jsonArray = new JSONArray(userIdsJson);

                                    ArrayList<String> userIds = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        String userId = jsonArray.getString(i);
                                        userIds.add(userId);
                                    }

                                    String fromAccountId = extraIntent.getString(CallParams.INVENT_FROM_ACCOUNT_ID);
                                    userIds.add(fromAccountId);

                                    avChatIntent.putExtra(CallParams.INVENT_USER_IDS, userIds);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    NimLog.e(TAG, "onNotificationClicked Exception:" + e);
                                }
                            } else {
                                if (TextUtils.equals(String.valueOf(ChannelType.AUDIO.getValue()), channelType)) {
                                    avChatIntent.setClass(MainActivity.this, UIServiceManager.getInstance().getUiService().getOneToOneAudioChat());
                                } else {
                                    avChatIntent.setClass(MainActivity.this, UIServiceManager.getInstance().getUiService().getOneToOneVideoChat());
                                }
                            }

                            avChatIntent.putExtra(CallParams.INVENT_CALL_RECEIVED, true);
                            startActivity(avChatIntent);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, true);
    }

    private String readFully(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            return "";
        }

        ByteArrayOutputStream byteArrayOutputStream;

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();

            final byte[] buffer = new byte[1024];
            int available;

            while ((available = bufferedInputStream.read(buffer)) >= 0) {
                byteArrayOutputStream.write(buffer, 0, available);
            }

            return byteArrayOutputStream.toString();

        } finally {
            bufferedInputStream.close();
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        pagerCurrentItem = getIntent().getIntExtra("pagerCurrentItem",0);
        System.out.println("9099");
        System.out.println(pagerCurrentItem);
        if(pagerCurrentItem != 0){
            pager.setCurrentItem(pagerCurrentItem);
            this.onPageSelected(pagerCurrentItem);
        }


    }

    private void init() {
        observerSyncDataComplete();
        findViews();
        setupPager();
        setupTabs();
        registerMsgUnreadInfoObserver(true);
        registerSystemMessageObservers(true);
        registerCustomMessageObservers(true);
        requestSystemMessageUnreadCount();
        initUnreadCover();
        requestBasicPermission();
    }



    private boolean parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            intent.removeExtra(EXTRA_APP_QUIT);
            onLogout();
            return true;
        }
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) intent.getSerializableExtra(
                    NimIntent.EXTRA_NOTIFY_CONTENT);
            intent.removeExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    SessionHelper.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
            }
            return true;
        }
        return false;
    }

    private void observerSyncDataComplete() {
        boolean syncCompleted = LoginSyncDataStatusObserver.getInstance()
                .observeSyncDataCompletedEvent(
                        (Observer<Void>) v -> DialogMaker
                                .dismissProgressDialog());
        //如果数据没有同步完成，弹个进度Dialog
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(MainActivity.this, getString(R.string.prepare_data))
                    .setCanceledOnTouchOutside(false);
        }
    }

    private void findViews() {
        tabs = findView(R.id.tabs);
        pager = findView(R.id.main_tab_pager);
    }

    private void setupPager() {
        adapter = new MainTabPagerAdapter(getSupportFragmentManager(), this, pager);
        pager.setOffscreenPageLimit(adapter.getCacheCount());
        pager.setPageTransformer(true, new FadeInOutPageTransformer());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(this);


    }

    private void setupTabs() {
        tabs.setOnCustomTabListener(new PagerSlidingTabStrip.OnCustomTabListener() {

            @Override
            public int getTabLayoutResId(int position) {
                return R.layout.tab_layout_main;
            }

            @Override
            public boolean screenAdaptation() {
                return true;
            }
        });
        tabs.setViewPager(pager);
        tabs.setOnTabClickListener(adapter);
        tabs.setOnTabDoubleTapListener(adapter);
    }

    /**
     * 注册未读消息数量观察者
     */
    private void registerMsgUnreadInfoObserver(boolean register) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this);
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this);
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     */
    private void registerSystemMessageObservers(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeUnreadCountChange(
                sysMsgUnreadCountChangedObserver, register);
    }

    // sample
    Observer<CustomNotification> customNotificationObserver = (Observer<CustomNotification>) notification -> {
        // 处理自定义通知消息
        LogUtil.i("demo", "receive custom notification: " + notification.getContent() + " from :" +
                notification.getSessionId() + "/" + notification.getSessionType() +
                "unread=" + (notification.getConfig() == null ? "" : notification.getConfig().enableUnreadCount +  " " + "push=" +
                notification.getConfig().enablePush + " nick=" +
                notification.getConfig().enablePushNick));
        try {
            JSONObject obj = JSONObject.parseObject(notification.getContent());
            if (obj != null && obj.getIntValue("id") == 2) {
                // 加入缓存中
                CustomNotificationCache.getInstance().addCustomNotification(notification);
                // Toast
                String content = obj.getString("content");
                String tip = String.format("自定义消息[%s]：%s", notification.getFromAccount(), content);
                ToastHelper.showToast(MainActivity.this, tip);
            }
        } catch (JSONException e) {
            LogUtil.e("demo", e.getMessage());
        }
    };

    private void registerCustomMessageObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(
                customNotificationObserver, register);
    }

    /**
     * 查询系统消息未读数
     */
    private void requestSystemMessageUnreadCount() {
        int unread = NIMClient.getService(SystemMessageService.class)
                .querySystemMessageUnreadCountBlock();
        SystemMessageUnreadManager.getInstance().setSysMsgUnreadCount(unread);
        ReminderManager.getInstance().updateContactUnreadNum(unread);
    }

    //初始化未读红点动画
    private void initUnreadCover() {
        DropManager.getInstance().init(this, findView(R.id.unread_cover), (id, explosive) -> {
            if (id == null || !explosive) {
                return;
            }
            if (id instanceof RecentContact) {
                RecentContact r = (RecentContact) id;
                NIMClient.getService(MsgService.class).clearUnreadCount(r.getContactId(),
                        r.getSessionType());
                return;
            }
            if (id instanceof String) {
                if (((String) id).contentEquals("0")) {
                    NIMClient.getService(MsgService.class).clearAllUnreadCount();
                } else if (((String) id).contentEquals("1")) {
                    NIMClient.getService(SystemMessageService.class)
                            .resetSystemMessageUnreadCount();
                }
            }
        });
    }

    private void requestBasicPermission() {
        MPermission.printMPermissionResult(true, this, BASIC_PERMISSIONS);
        MPermission.with(MainActivity.this).setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(BASIC_PERMISSIONS).request();
    }

    private void onLogout() {
        Preferences.saveUserToken("");
        // 清理缓存&注销监听
        LogoutHelper.logout();
        // 启动登录
        LoginActivity.start(this);
        finish();
    }

    @SuppressLint("ResourceType")
    private void selectPage() {
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            adapter.onPageSelected(pager.getCurrentItem());
            System.out.println("333");
            System.out.println(pager.getCurrentItem());
            Menu menu = getToolBar().getMenu();
            switch (pager.getCurrentItem()){
                case 0:
                    setToolBarTitle(R.id.toolbar, R.string.app_index_label_slogen,0xff6270DD);
//                    findViewById(R.id.toolbar).setBackground(getDrawable(R.color.colorBgLightGray));
//                    ImmersionBar.with(this).statusBarColor(R.color.colorBgLightGray).init();
                    break;
                case 1:
                    setToolBarTitle(R.id.toolbar, R.string.app_relation_label_slogen,0xff6270DD);
                    break;
                case 2:
                    setToolBarTitle(R.id.toolbar, R.string.app_chat_label_slogen,0xff6270DD);
                    break;
                case  3:
                    setToolBarTitle(R.id.toolbar, R.string.app_me_label_slogen,0xff6270DD);
                    break;
            }
        }
    }

    /**
     * 设置最近联系人的消息为已读
     * <p>
     * account, 聊天对象帐号，或者以下两个值：
     * {@link MsgService#MSG_CHATTING_ACCOUNT_ALL} 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
     * {@link MsgService#MSG_CHATTING_ACCOUNT_NONE} 目前没有与任何人对话，需要状态栏消息通知
     */
    private void enableMsgNotification(boolean enable) {
        boolean msg = (pager.getCurrentItem() != MainTab.RECENT_CONTACTS.tabIndex);
        if (enable | msg) {
            NIMClient.getService(MsgService.class).setChattingAccount(
                    MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(
                    MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }
    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.view_cloud_session:
                RecentSessionActivity.start(this);
                break;
            case R.id.create_normal_team:
                ContactSelectActivity.Option option = TeamHelper.getCreateContactSelectOption(null,
                                                                                              50);
                NimUIKit.startContactSelector(MainActivity.this, option, REQUEST_CODE_NORMAL);
                break;
            case R.id.create_regular_team:
                ContactSelectActivity.Option advancedOption = TeamHelper
                        .getCreateContactSelectOption(null, 50);
                NimUIKit.startContactSelector(MainActivity.this, advancedOption,
                                              REQUEST_CODE_ADVANCED);
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(MainActivity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(MainActivity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(MainActivity.this);
                break;
            case R.id.enter_ysf:
                Unicorn.openServiceActivity(this, "测试", null);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); //这里改过
        setIntent(intent);
        parseIntent();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 第一次 ， 三方通知唤起进会话页面之类的，不会走初始化过程
        boolean temp = isFirstIn;
        isFirstIn = false;
        if (pager == null && temp) {
            return;
        }
        //如果不是第一次进 ， eg: 其他页面back
        if (pager == null) {
            init();
        }
        enableMsgNotification(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pager == null) {
            return;
        }
        enableMsgNotification(true);
    }

    @Override
    public void onDestroy() {
        registerMsgUnreadInfoObserver(false);
        registerSystemMessageObservers(false);
        registerCustomMessageObservers(false);
        DropManager.getInstance().destroy();
        super.onDestroy();
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_NORMAL) {
            final ArrayList<String> selected = data.getStringArrayListExtra(
                    ContactSelectActivity.RESULT_DATA);
            if (selected != null && !selected.isEmpty()) {
                TeamCreateHelper.createNormalTeam(MainActivity.this, selected, false, null);
            } else {
                ToastHelper.showToast(MainActivity.this, "请选择至少一个联系人！");
            }
        } else if (requestCode == REQUEST_CODE_ADVANCED) {
            final ArrayList<String> selected = data.getStringArrayListExtra(
                    ContactSelectActivity.RESULT_DATA);
            TeamCreateHelper.createAdvancedTeam(MainActivity.this, selected);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        tabs.onPageScrolled(position, positionOffset, positionOffsetPixels);
        adapter.onPageScrolled(position);
    }

    @Override
    public void onPageSelected(int position) {
        tabs.onPageSelected(position);
        selectPage();
        enableMsgNotification(false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        tabs.onPageScrollStateChanged(state);
        scrollState = state;
        selectPage();
    }

    //未读消息数量观察者实现
    @Override
    public void onUnreadNumChanged(ReminderItem item) {
        MainTab tab = MainTab.fromReminderId(item.getId());
        if (tab != null) {
            tabs.updateTab(tab.tabIndex, item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        try {
            ToastHelper.showToast(this, "授权成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        try {
            ToastHelper.showToast(this, "未全部授权，部分功能可能无法正常运行！");
        } catch (Exception e) {
            e.printStackTrace();
        }
        MPermission.printMPermissionResult(false, this, BASIC_PERMISSIONS);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

//    private void initView() {
////        requestPermiss();
//        mMainLayout = (FrameLayout) findViewById(R.id.mMainLayout);
//
//        llIndex = (LinearLayout) findViewById(R.id.ll_index);
//        ivIndex = (ImageView) findViewById(R.id.iv_index);
//        tvIndex = (TextView) findViewById(R.id.tv_index);
//
//
//        llContact = (LinearLayout) findViewById(R.id.ll_contact);
//        ivContact = (ImageView) findViewById(R.id.iv_contact);
//        tvContact = (TextView) findViewById(R.id.tv_contact);
//
//        llChat = (LinearLayout) findViewById(R.id.ll_chat);
//        ivChat = (ImageView) findViewById(R.id.iv_chat);
//        tvChat = (TextView) findViewById(R.id.tv_chat);
//
//        llMe = (LinearLayout) findViewById(R.id.ll_me);
//        ivMe = (ImageView) findViewById(R.id.iv_me);
//        tvMe = (TextView) findViewById(R.id.tv_me);
//
//        llIndex.setOnClickListener(this);
//        llContact.setOnClickListener(this);
//        llChat.setOnClickListener(this);
//        llMe.setOnClickListener(this);
//
//        initFragment();
//
//        checkMainTab(0);
//
//    }

//    private void initFragment() {
//        //首页
//        if(mIndexFragment == null){
//            mIndexFragment = new IndexFragment();
//            mIndexFragmentTransaction = getSupportFragmentManager().beginTransaction();
//            mIndexFragmentTransaction.add(R.id.mMainLayout,mIndexFragment);
//            mIndexFragmentTransaction.commit();
//        }
//        //联系
//        if(mContactFragment == null){
//            mContactFragment = new ContactFragment();
//            mContactFragmentTransaction = getSupportFragmentManager().beginTransaction();
//            mContactFragmentTransaction.add(R.id.mMainLayout,mContactFragment);
//            mContactFragmentTransaction.commit();
//        }
//        //聊天
//        if(mChatFragment == null){
//            mChatFragment = new ChatFragment();
//            mChatFragmentTransaction = getSupportFragmentManager().beginTransaction();
//            mChatFragmentTransaction.add(R.id.mMainLayout,mChatFragment);
//            mChatFragmentTransaction.commit();
//        }
//        //我的
//        if(mMeFragment == null){
//            mMeFragment = new MeFragment();
//            mMeFragmentTransaction = getSupportFragmentManager().beginTransaction();
//            mMeFragmentTransaction.add(R.id.mMainLayout,mMeFragment);
//            mMeFragmentTransaction.commit();
//        }
//    }

//    /**
//     * 展示fragment
//     * @param fragment
//     */
//    private void showFragment(Fragment fragment){
//        if(fragment != null){
//            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//            hideAllFragment(fragmentTransaction);
//            fragmentTransaction.show(fragment);
//            fragmentTransaction.commitAllowingStateLoss();
//        }
//    }
//    /**
//     * 隐藏所有的Fragment
//     *
//     * @param transaction
//     */
//    private void hideAllFragment(FragmentTransaction transaction) {
//        if (mIndexFragment != null) {
//            transaction.hide(mIndexFragment);
//        }
//        if (mContactFragment != null) {
//            transaction.hide(mContactFragment);
//        }
//        if (mChatFragment != null) {
//            transaction.hide(mChatFragment);
//        }
//        if (mMeFragment != null) {
//            transaction.hide(mMeFragment);
//        }
//    }
//    /**
//     * 防止重叠
//     * 当应用的内存紧张的时候，系统会回收掉Fragment对象
//     * 再一次进入的时候会重新创建Fragment
//     * 非原来对象，我们无法控制，导致重叠
//     *
//     * @param fragment
//     */
//    @Override
//    public void onAttachFragment(Fragment fragment) {
//        if (mIndexFragment != null && fragment instanceof IndexFragment) {
//            mIndexFragment = (IndexFragment) fragment;
//        }
//        if (mContactFragment != null && fragment instanceof ContactFragment) {
//            mContactFragment = (ContactFragment) fragment;
//        }
//        if (mChatFragment != null && fragment instanceof ChatFragment) {
//            mChatFragment = (ChatFragment) fragment;
//        }
//        if (mMeFragment != null && fragment instanceof MeFragment) {
//            mMeFragment = (MeFragment) fragment;
//        }
//    }
//
//
//    /**
//     * 切换主页选项卡
//     *
//     * @param index 0：首页
//     *              1：联系
//     *              2：聊天
//     *              3：我的
//     */
//    @SuppressLint("ResourceAsColor")
//    private void checkMainTab(int index) {
//        switch (index) {
//            case 0:
//                showFragment(mIndexFragment);
//
//                ivIndex.setImageResource(R.drawable.icon_index_show);
//                ivContact.setImageResource(R.drawable.icon_contact_hide);
//                ivChat.setImageResource(R.drawable.icon_chat_hide);
//                ivMe.setImageResource(R.drawable.icon_me_hide);
//
//                tvIndex.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
//                tvContact.setTextColor(R.color.colorPrimaryGray);
//                tvChat.setTextColor(R.color.colorPrimaryGray);
//                tvMe.setTextColor(R.color.colorPrimaryGray);
//
//                break;
//            case 1:
//                showFragment(mContactFragment);
//
//                ivContact.setImageResource(R.drawable.icon_contact_show);
//                ivIndex.setImageResource(R.drawable.icon_index_hide);
//                ivChat.setImageResource(R.drawable.icon_chat_hide);
//                ivMe.setImageResource(R.drawable.icon_me_hide);
//
//                tvContact.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
//                tvIndex.setTextColor(R.color.colorPrimaryGray);
//                tvChat.setTextColor(R.color.colorPrimaryGray);
//                tvMe.setTextColor(R.color.colorPrimaryGray);
//
//                break;
//            case 2:
//                showFragment(mChatFragment);
//
//                ivChat.setImageResource(R.drawable.icon_chat_show);
//                ivContact.setImageResource(R.drawable.icon_contact_hide);
//                ivIndex.setImageResource(R.drawable.icon_index_hide);
//                ivMe.setImageResource(R.drawable.icon_me_hide);
//
//                tvChat.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
//                tvIndex.setTextColor(R.color.colorPrimaryGray);
//                tvContact.setTextColor(R.color.colorPrimaryGray);
//                tvMe.setTextColor(R.color.colorPrimaryGray);
//
//                break;
//            case 3:
//                showFragment(mMeFragment);
//
//                ivMe.setImageResource(R.drawable.icon_me_show);
//                ivContact.setImageResource(R.drawable.icon_contact_hide);
//                ivIndex.setImageResource(R.drawable.icon_index_hide);
//                ivChat.setImageResource(R.drawable.icon_chat_hide);
//
//                tvMe.setTextColor(getResources().getColor(R.color.colorPrimaryBlue));
//                tvIndex.setTextColor(R.color.colorPrimaryGray);
//                tvChat.setTextColor(R.color.colorPrimaryGray);
//                tvContact.setTextColor(R.color.colorPrimaryGray);
//
//                break;
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.ll_index:
//                checkMainTab(0);
//                break;
//            case R.id.ll_contact:
//                checkMainTab(1);
//                break;
//            case R.id.ll_chat:
//                checkMainTab(2);
//                break;
//            case R.id.ll_me:
//                checkMainTab(3);
//                break;
//        }
//    }
//=====================================================================
//    /**
//     * 请求权限
//     */
//    private void requestPermiss() {
//        //危险权限
//        request(new OnPermissionsResult() {
//            @Override
//            public void OnSuccess() {
//
//            }
//
//            @Override
//            public void OnFail(List<String> noPermissions) {
//                LogUtils.i("noPermissions:" + noPermissions.toString());
//            }
//        });
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.getType()) {
            case EventManager.EVENT_REFRE_TOKEN_STATUS:

                break;
        }
    }

}
