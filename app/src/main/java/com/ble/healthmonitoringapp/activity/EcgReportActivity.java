package com.ble.healthmonitoringapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Toast;

import com.ble.healthmonitoringapp.BuildConfig;
import com.ble.healthmonitoringapp.Myapp;
import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.databinding.ActivityEcgReportBinding;
import com.ble.healthmonitoringapp.model.EcgHistoryData;
import com.ble.healthmonitoringapp.utils.CheckSelfPermission;
import com.ble.healthmonitoringapp.utils.FireBaseKey;
import com.ble.healthmonitoringapp.utils.PDFCreate;
import com.ble.healthmonitoringapp.utils.SchedulersTransformer;
import com.ble.healthmonitoringapp.utils.Utilities;
import com.demon.js_pdf.WebViewHelper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jstyle.blesdk2025.model.UserInfo;
import com.neurosky.AlgoSdk.NskAlgoDataType;
import com.neurosky.AlgoSdk.NskAlgoECGValueType;
import com.neurosky.AlgoSdk.NskAlgoProfile;
import com.neurosky.AlgoSdk.NskAlgoSampleRate;
import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.AlgoSdk.NskAlgoType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class EcgReportActivity extends AppCompatActivity {
    private ActivityEcgReportBinding binding;
    private Disposable disposablePdf;
    ArrayList<EcgHistoryData> ecgHistoryDataArrayList =new ArrayList<>();
    String Path="";
    private final static String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + BuildConfig.APPLICATION_ID;
    public  static String pdfPath;
    public  static String csvPath;
    boolean isCreate=false;
    String  csvFilePath="";
    String  EcgDate="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding= DataBindingUtil.setContentView(this,R.layout.activity_ecg_report);
      ecgHistoryDataArrayList=(ArrayList<EcgHistoryData>) getIntent().getSerializableExtra("ecgData");
        pdfPath=(Build.VERSION.SDK_INT >= 30? getExternalCacheDir():baseDir)+"/pdf/";
        csvPath=(Build.VERSION.SDK_INT >= 30? getExternalCacheDir():baseDir)+"/csv/";
       WebviewSetting();
       requestPermission(this);
       binding.ivBack.setOnClickListener(v->{
           finish();
        });
        binding.btnGenerate.setOnClickListener(v->{
            init();
        });
        binding.ivUpload.setOnClickListener(v->{
            if (CheckSelfPermission.isNetworkConnected(EcgReportActivity.this)) {
                if(isCreate) {
                    uploadFile();
                }else {
                    Toast.makeText(this,"Please Generate pdf report",Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
            }
        });
}

    int raw_data_index = 0;
    private NskAlgoSdk nskAlgoSdk;
    boolean enddata=false;
    List<Integer> filterEcg;
    @SuppressLint("HandlerLeak")
    private void init()  {
       Utilities.showProgress(this,getString(R.string.please_wait));
        List<Integer>  ecgData=new ArrayList<>();
        if(ecgHistoryDataArrayList.size()!=0){
            Log.e("time",ecgHistoryDataArrayList.size()+" "+ecgHistoryDataArrayList.get(ecgHistoryDataArrayList.size()-1).getTime());
            EcgDate=  ecgHistoryDataArrayList.get(ecgHistoryDataArrayList.size()-1).getTime();
            String[] ecg=ecgHistoryDataArrayList.get(ecgHistoryDataArrayList.size()-1).getArrayECGData().split(",");
                for (String e:ecg){
                    ecgData.add(Integer.parseInt(e));
                }
        }
        raw_data_index = 0;
        enddata=false;
        nskAlgoSdk = new NskAlgoSdk();
        nskAlgoSdk.NskAlgoUninit();
        loadsetup();
        setAlgos();
        nskAlgoSdk.NskAlgoStart(false);
        filterEcg=new ArrayList<>();
        nskAlgoSdk.setOnECGAlgoIndexListener(new NskAlgoSdk.OnECGAlgoIndexListener() {
            @Override
            public void onECGAlgoIndex(int type,  int value) {
                switch (type) {
                    case NskAlgoECGValueType.NSK_ALGO_ECG_VALUE_TYPE_SMOOTH:
                        if(value>8000){value=8000;}
                        if(value<-8000){value=-8000;}
                        filterEcg.add(value);
                        break;
                    case NskAlgoECGValueType.NSK_ALGO_ECG_VALUE_TYPE_ROBUST_HR:
                        break;
                    case NskAlgoECGValueType.NSK_ALGO_ECG_VALUE_TYPE_HRV:
                        break;
                }
            }
        });

        queues = new LinkedList<>();
        queues.addAll(ecgData);
        uihandlerecg= new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int value= (int) msg.obj;
                if (raw_data_index == 0 || raw_data_index % 200 == 0) {
                    short pqValue[] = {(short) 200};
                    nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ECG_PQ, pqValue, 1);
                }
                if (value >= 32768) value = value - 65536;
                short[] ecg= new short[]{(short) -value};
                raw_data_index++;
                nskAlgoSdk.NskAlgoDataStream(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ECG, ecg, 1);//this
            }
        };
        Start();
    }

    private ScheduledThreadPoolExecutor timer = null;
    private Handler uihandlerecg=null;
    private Deque queues;
    private  void Start(){
        if(null==timer||timer.isShutdown()){
            timer = new ScheduledThreadPoolExecutor(1);
            timer.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        Deque<Integer> requests = queues;
                        Integer value = requests.pollLast();
                        if (null != value) {
                            Message message = new Message();
                            message.obj=value;
                            uihandlerecg.sendMessage(message);
                        }else{
                            Close();
                           Utilities.dissMissDialog();
                            isCreate=true;
                           createFile(filterEcg);
                            //  showEcgChart(filterEcg,8000,-8000);
                        }
                    }catch (Throwable e){ e.printStackTrace(); }
                }
            },0, 1L, TimeUnit.MILLISECONDS);

        }

    }
    private void Close(){
        try {
            if(null!=timer){
                timer.shutdownNow();
                timer=null;
            }
            if(null!=uihandlerecg){
                uihandlerecg.removeCallbacksAndMessages(null);
                uihandlerecg=null;
            }
        }catch (Exception e) {

        }
    }




     private void createFile( final List<Integer> ecgData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utilities.showProgress(EcgReportActivity.this,getString(R.string.please_wait));
            }
        });
       Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                File dir = new File(pdfPath);
                File dr = new  File(csvPath);
                //有没有当前文件夹就创建一个，注意读写权限
                // Create one if there is no current folder. Pay attention to read and write permissions
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if(!dr.exists()){
                    dr.mkdirs();
                }
                csvFilePath=csvPath+"ECGReport_"+Utilities.getFIleCreateDate(EcgDate) + ".csv";
                Path= pdfPath+ "ECGReport_"+Utilities.getFIleCreateDate(EcgDate) + ".pdf";
                UserInfo userInfo = new UserInfo();
                userInfo.setGender("Gender: ");
                userInfo.setAge("Age: ");
                userInfo.setDate("Date:"+Utilities.getDeciveDate(EcgDate));
                userInfo.setName("Name:             ");
                userInfo.setHeight("Height: ");
                userInfo.setWeight("Weight: ");
                userInfo.setEcgTitle("ECG Report("+Utilities.MacAddress+")");//not null
                userInfo.setEcgReportTips(" ");//not null
                createTextFile(ecgData);
                PDFCreate.createPdf(Path, EcgReportActivity.this, ecgData, userInfo);
                emitter.onComplete();
            }
        }).compose(SchedulersTransformer.applySchedulers()).subscribe(new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable e) {
                Log.e("jssjsjj",e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.dissMissDialog();
                    }
                });
            }

            @Override
            public void onComplete() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utilities.dissMissDialog();
                        WebViewHelper.WebViewLoadPDF(binding.historyWebview, Path);
                    }
                });
            }
        });

    }
   public void createTextFile(List<Integer> data){
       File file = new File(csvFilePath);
      // FileOutputStream outputStream = null;
       StringBuilder builder =new StringBuilder();
       builder.append("ECG Report("+Utilities.MacAddress+")");
       builder.append(",");
       builder.append("\n");
       for (int i=0; i<data.size();i++){
           builder.append(data.get(i)+",");
           builder.append("\n");
       }
       try {
           BufferedWriter fw = new BufferedWriter(new FileWriter(file));
           fw.write(builder.toString());
           fw.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
       /*try {
           outputStream = new FileOutputStream(file);
       } catch (FileNotFoundException var16) {
           var16.printStackTrace();
       }

       try {
           OutputStreamWriter fw = new OutputStreamWriter(outputStream);
           fw.write(builder.toString());
           fw.close();
       } catch (IOException var15) {
           var15.printStackTrace();
       }*/
   }
    private void requestPermission(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
             //   init();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                startActivityForResult(intent, 0);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
              //  init();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }else{
           // init();
        }
    }





    public void onRequestPermissionsResult(int requestCode,

                                           String[] permissions,

                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
               // init();
            } else {

            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private  void WebviewSetting(){

        WebSettings webSettings = binding.historyWebview.getSettings();
        webSettings.setUserAgentString("User-Agent:Android");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setSupportZoom(true);//缩放开关，设置此属性，仅支持双击缩放，不支持触摸缩放
        webSettings.setBuiltInZoomControls(true);  //设置是否可缩放，会出现缩放工具（若为true则上面的设值也默认为true）
        webSettings.setDisplayZoomControls(false);
        binding.historyWebview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关闭硬件加速
        binding.historyWebview.setBackgroundColor(Color.WHITE);
    }

    public void sharePdfByPhone(Activity context, String path) {
        Uri uri = null;
        Intent shareIntent = new Intent();
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(path));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(new File(path));
        }
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("*/*");
        startActivity(Intent.createChooser(shareIntent, "share"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposablePdf != null && !disposablePdf.isDisposed()) {
            disposablePdf.dispose();
            disposablePdf=null;
        }
        try {
            if(null!=timer){
                timer.shutdownNow();
                timer=null;
            }
            if(null!=uihandlerecg){
                uihandlerecg.removeCallbacksAndMessages(null);
                uihandlerecg=null;
            }
        }catch (Exception e) {  }

    }









    private List<Integer> readFile2(int path) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(path )));
        List<String> list=new ArrayList<String>();
        try {
            String line = null;
            //因为不知道有几行数据，所以先存入list集合中
            while((line = reader.readLine()) != null){
                if(!"".equals(line)){
                    list.add(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                if(reader != null){ reader.close();
                    reader=null;}
            }catch(Exception e){ }
        }
        String valueString="";
        for (int i=0;i<list.size();i++){
            valueString+=list.get(i).trim();
        }
        String[] dataString = valueString.split(",");
        List<Integer> data = new ArrayList<>();
        for (String value : dataString) {//纯数字写进null字符串?
            // Log.i(TAG, "readFile2: "+value);
            if (TextUtils.isEmpty(value) ||"null".equals(value) ||"-".equals(value)) continue;
            if(value.contains("-")){
                if(value.lastIndexOf("-")==0)  data.add(Integer.parseInt(value));
            }else{
                Integer doubleValue=Integer.parseInt(value);
                /*if(doubleValue>8000)doubleValue=8000d;
                if(doubleValue<-8000)doubleValue=-8000d;*/
                data.add(doubleValue);
            }

        }
        return data;
    }


    private String license = "";
    private void loadsetup() {
        AssetManager assetManager = this.getAssets();
        InputStream inputStream = null;

        try {
            String prefix = "license key=\"";
            String suffix = "\"";
            String pattern = prefix + "(.+?)" + suffix;
            Pattern p = Pattern.compile(pattern);

            inputStream = assetManager.open("license.txt");
            ArrayList<String> data = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                while (true) {
                    String line = reader.readLine();
                    if (line == null || line.isEmpty())
                        break;
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        license = line.substring(m.regionStart() + prefix.length(), m.regionEnd() - suffix.length());
                        break;
                    }
                }
            } catch (IOException e) {

            }
            inputStream.close();
        } catch (IOException e) { }
        try {
            inputStream = assetManager.open("setupinfo.txt");
            ArrayList<String> data = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                while (true) {
                    String line = reader.readLine();
                    if (line == null || line.isEmpty()) {
                        break;
                    }
                    data.add(line);
                }
            } catch (IOException e) { }
            inputStream.close();
        } catch (IOException e) { }
    }


    private int activeProfile = -1;
    private int currentSelectedAlgo;
    private void setAlgos() {
        String path = this.getFilesDir().getAbsolutePath();
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_HRVFD;
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_HRVTD;
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_HRV;
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_SMOOTH;
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_HEARTAGE;
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_HEARTRATE;
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_MOOD;
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_RESPIRATORY;
        currentSelectedAlgo |= NskAlgoType.NSK_ALGO_TYPE_ECG_STRESS;
        int ret = nskAlgoSdk.NskAlgoInit(currentSelectedAlgo, path, license);
        if (ret == 0) {
        } else {
            return;
        }
        boolean b = nskAlgoSdk.setBaudRate(NskAlgoDataType.NSK_ALGO_DATA_TYPE_ECG, NskAlgoSampleRate.NSK_ALGO_SAMPLE_RATE_512);
        if (b != true) {
            return;
        }
        NskAlgoProfile[] profiles = nskAlgoSdk.NskAlgoProfiles();
        if (profiles.length == 0) {
            // create a default profile
            try {
                String dobStr = "1995-1-1";
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Date dob = df.parse(dobStr);

                NskAlgoProfile profile = new NskAlgoProfile();
                profile.name = "bob";
                profile.height = 170;
                profile.weight = 80;
                profile.gender = false;
                profile.dob = dob;
                if (nskAlgoSdk.NskAlgoProfileUpdate(profile) == false) { }

                profiles = nskAlgoSdk.NskAlgoProfiles();
                // setup the ECG config
                // assert(nskAlgoSdk.NskAlgoSetECGConfigAfib((float)3.5) == true);
                if (nskAlgoSdk.NskAlgoSetECGConfigStress(30, 30) == true);
                if (nskAlgoSdk.NskAlgoSetECGConfigHeartage(30) == true);
                if (nskAlgoSdk.NskAlgoSetECGConfigHRV(30) == true);
                if (nskAlgoSdk.NskAlgoSetECGConfigHRVTD(30, 30) == true);
                if (nskAlgoSdk.NskAlgoSetECGConfigHRVFD(30, 30) == true);

                // nskAlgoSdk.setSignalQualityWatchDog((short)20, (short)5);
                // retrieve the baseline data
                if (profiles.length > 0) {
                    activeProfile = profiles[0].userId;
                    nskAlgoSdk.NskAlgoProfileActive(activeProfile);
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                    String stringArray = settings.getString("ecgbaseline", null);
                    if (stringArray != null) {
                        String[] split = stringArray.substring(1, stringArray.length() - 1).split(", ");
                        byte[] array = new byte[split.length];
                        for (int i = 0; i < split.length; ++i) {
                            array[i] = Byte.parseByte(split[i]);
                        }
                        if (nskAlgoSdk.NskAlgoProfileSetBaseline(activeProfile, NskAlgoType.NSK_ALGO_TYPE_ECG_HEARTRATE, array) != true) {
                        }
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    private void uploadCsvFile(){
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(csvFilePath));
        } else {
            uri = Uri.fromFile(new File(csvFilePath));
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference filepath = storageRef.child(Utilities.MacAddress+"/ECGReport_Android_"+Utilities.getFIleCreateDate(EcgDate) + "." + "csv");
        filepath.putFile(uri).continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    // After uploading is done it progress
                    // dialog box will be dismissed
                    Utilities.dissMissDialog();
                    Uri downloadUri = task.getResult();

                    Map<String, Object> urlMap = new HashMap<>();
                    urlMap.put(FireBaseKey.Values, downloadUri.toString());
                    urlMap.put(FireBaseKey.FIREBASE_OS,FireBaseKey.ANDROID);
                    FirebaseFirestore  db = FirebaseFirestore.getInstance();
                    db.collection(FireBaseKey.FIREBASE_COLLECTION_NAME).
                            document(Utilities.MacAddress)
                            .collection(FireBaseKey.FIREBASE_ECG_ReportCsv)
                            .document(Utilities.getCurrentDate())
                            .set(Utilities.getTimeHashmap(urlMap,Utilities.getDeciveTime(EcgDate)),SetOptions.merge());
                    Toast.makeText(EcgReportActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Utilities.dissMissDialog();
                    Toast.makeText(EcgReportActivity.this, "UploadedFailed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void uploadFile(){
        Utilities.showProgress(this,getString(R.string.please_wait));
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(Path));
        } else {
            uri = Uri.fromFile(new File(Path));
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference filepath = storageRef.child(Utilities.MacAddress+"/ECGReport_Android_"+Utilities.getFIleCreateDate(EcgDate) + "." + "pdf");
        filepath.putFile(uri).continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    // After uploading is done it progress
                    // dialog box will be dismissed
                    Uri downloadUri = task.getResult();
                    Map<String, Object> urlMap = new HashMap<>();
                    urlMap.put(FireBaseKey.Values, downloadUri.toString());
                    urlMap.put(FireBaseKey.FIREBASE_OS,FireBaseKey.ANDROID);
                    FirebaseFirestore  db = FirebaseFirestore.getInstance();
                    db.collection(FireBaseKey.FIREBASE_COLLECTION_NAME).
                            document(Utilities.MacAddress)
                            .collection(FireBaseKey.FIREBASE_ECG_Report)
                            .document(Utilities.getCurrentDate())
                            .set(Utilities.getTimeHashmap(urlMap,Utilities.getDeciveTime(EcgDate)),SetOptions.merge());
                    uploadCsvFile();
                    //   Toast.makeText(EcgReportActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Utilities.dissMissDialog();
                    Toast.makeText(EcgReportActivity.this, "UploadedFailed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}