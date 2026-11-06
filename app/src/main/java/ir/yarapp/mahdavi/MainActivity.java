package ir.yarapp.mahdavi;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final String PREF="yar_settings";
    private static final int PICK_ARABIC=101, PICK_UI=102;
    private boolean activityDestroyed=false;
    private LinearLayout page;
    private int green=Color.rgb(17,83,61), gold=Color.rgb(210,174,91);
    private boolean dark;
    private float arabicSize=28f, persianSize=16f;
    private Typeface arabicFace=Typeface.DEFAULT, uiFace=Typeface.DEFAULT, titleFace=Typeface.DEFAULT;
    private Handler handler=new Handler();
    private Runnable scrollTask;
    private ScrollView readingScroll;
    private boolean scrolling=false;
    private int scrollDelay=35;
    private String currentPrayer="دعای عهد";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        android.content.SharedPreferences p=getSharedPreferences(PREF,0);
        dark=p.getBoolean("dark",false);
        arabicSize=p.getFloat("arabic_size",28f);
        persianSize=p.getFloat("persian_size",16f);
        loadBundledFonts();
        loadSavedFonts();
        applyBars();
        home();
    }
    private int bg(){return dark?Color.rgb(12,25,22):Color.rgb(241,246,241);}
    private int card(){return dark?Color.rgb(25,43,37):Color.WHITE;}
    private int text(){return dark?Color.rgb(239,244,238):Color.rgb(30,48,40);}
    private void applyBars(){
        getWindow().setStatusBarColor(dark?Color.rgb(8,20,17):green);
        getWindow().setNavigationBarColor(Color.BLACK);
        if(Build.VERSION.SDK_INT>=29)getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
    private void loadBundledFonts(){
        try{uiFace=Typeface.createFromAsset(getAssets(),"fonts/iramir.ttf");}catch(Exception ignored){}
        try{titleFace=Typeface.createFromAsset(getAssets(),"fonts/iramir.ttf");}catch(Exception ignored){}
        try{arabicFace=Typeface.createFromAsset(getAssets(),"fonts/qurantaha.ttf");}catch(Exception ignored){}
    }
    private void loadSavedFonts(){
        android.content.SharedPreferences p=getSharedPreferences(PREF,0);
        String a=p.getString("arabic_path",null),u=p.getString("ui_path",null);
        try{if(a!=null)arabicFace=Typeface.createFromFile(a);}catch(Exception ignored){}
        try{if(u!=null)uiFace=Typeface.createFromFile(u);}catch(Exception ignored){}
    }
    private TextView txt(String s,float size,int color,boolean bold){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);
        v.setGravity(Gravity.CENTER);v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setTypeface(bold?titleFace:uiFace,Typeface.NORMAL);v.setPadding(12,10,12,10);
        return v;
    }
    private GradientDrawable shape(int color,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);
        d.setStroke(1,dark?Color.rgb(65,91,77):Color.rgb(223,231,222));return d;
    }
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private void base(String title){
        ScrollView outer=new ScrollView(this);
        page=column();page.setPadding(16,16,16,26);page.setBackgroundColor(bg());
        outer.addView(page);setContentView(outer);
        TextView h=txt(title,23,Color.WHITE,true);h.setBackground(shape(green,28));
        page.addView(h,new LinearLayout.LayoutParams(-1,-2));
    }
    private void addCard(View v){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,9,0,9);
        v.setBackground(shape(card(),24));v.setElevation(5);page.addView(v,p);
    }
    private void action(String icon,String title,String sub,Runnable r){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(14,8,14,8);
        TextView ic=txt(icon,27,gold,true);row.addView(ic,new LinearLayout.LayoutParams(58,62));
        LinearLayout labels=column();TextView t=txt(title,17,text(),true);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        TextView st=txt(sub,12,dark?0xffb6c8bc:0xff718078,false);st.setGravity(Gravity.RIGHT);
        labels.addView(t);labels.addView(st);row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        row.setOnClickListener(v->r.run());addCard(row);
    }
    private void home(){
        applyBars();
        ScrollView outer=new ScrollView(this);
        outer.setFillViewport(true);
        page=column(); page.setPadding(0,0,0,18); page.setBackgroundColor(dark?bg():Color.WHITE);
        outer.addView(page); setContentView(outer);
        // Header inspired by the supplied reference
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(18,8,18,8);
        TextView mail=txt("✉",27,text(),false),brand=txt("وَالعَصر",25,gold,true),headset=txt("♧",27,text(),false);
        top.addView(mail,new LinearLayout.LayoutParams(48,64));
        LinearLayout.LayoutParams center=new LinearLayout.LayoutParams(0,64,1);top.addView(brand,center);
        top.addView(headset,new LinearLayout.LayoutParams(48,64));page.addView(top);
        FrameLayout hero=new FrameLayout(this);
        hero.setBackground(shape(0xff159f98,0));hero.setClipToOutline(true);
        AnimatedBackdrop backdrop=new AnimatedBackdrop();hero.addView(backdrop,new FrameLayout.LayoutParams(-1,238));
        LinearLayout dateInfo=column();dateInfo.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);dateInfo.setPadding(18,10,18,10);
        TextView weekday=txt(todayWeekday(),17,Color.WHITE,true);weekday.setGravity(Gravity.RIGHT);
        TextView bigDay=txt(todayJalaliDay(),76,Color.WHITE,true);bigDay.setGravity(Gravity.RIGHT);
        TextView month=txt(todayJalaliMonth()+"  "+todayJalaliYear(),24,Color.WHITE,true);month.setGravity(Gravity.RIGHT);
        TextView greg=txt(new SimpleDateFormat("d MMMM yyyy",new Locale("fa")).format(new Date()),16,Color.WHITE,true);greg.setGravity(Gravity.LEFT);
        dateInfo.addView(weekday);dateInfo.addView(bigDay);dateInfo.addView(month);dateInfo.addView(greg);
        hero.addView(dateInfo,new FrameLayout.LayoutParams(-1,-1,Gravity.RIGHT|Gravity.CENTER_VERTICAL));
        page.addView(hero,new LinearLayout.LayoutParams(-1,238));
        // Main navigation: prayers are the primary feature.
        String[][] labels={{"ادعیه و زیارات","قرآن کریم","نهج‌البلاغه","صحیفه سجادیه"},
            {"تقویم","زندگی‌نامه امام","احادیث","مفاتیح‌الجنان"},
            {"نشان‌شده‌ها","ختم قرآن","تنظیمات","درباره برنامه"}};
        String[][] icons={{"۞","۩","❖","❀"},{"▦","☾","✦","✧"},{"☆","۞","⚙","ⓘ"}};
        Runnable[][] actions={{()->prayers(),()->simple("قرآن کریم"),()->simple("نهج‌البلاغه"),()->simple("صحیفه سجادیه")},
            {()->calendarPage(),()->biography(),()->hadiths(),()->prayers()},
            {()->simple("نشان‌شده‌ها"),()->simple("ختم قرآن"),()->settings(),()->simple("درباره برنامه")}};
        for(int r=0;r<labels.length;r++){
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);row.setPadding(5,12,5,4);
            for(int c=0;c<4;c++)addMenuTile(row,icons[r][c],labels[r][c],actions[r][c]);
            page.addView(row);
        }
        TextView prayerFeature=txt("قرار روزانه با امام زمان (عج)\nمطالعه دعاهای کامل همراه ترجمه فارسی",17,Color.WHITE,true);
        prayerFeature.setBackground(shape(0xff145b43,24));
        prayerFeature.setPadding(16,22,16,22);
        prayerFeature.setOnClickListener(v->prayers());
        LinearLayout.LayoutParams featureLp=new LinearLayout.LayoutParams(-1,-2);
        featureLp.setMargins(14,18,14,8);page.addView(prayerFeature,featureLp);
        TextView settingsShortcut=txt("تنظیمات قلم و نمایش",14,gold,true);
        settingsShortcut.setOnClickListener(v->settings());page.addView(settingsShortcut);
    }
    private String[] jalaliParts(){Calendar c=Calendar.getInstance();return new String[]{String.valueOf(gregorianToJalali(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))[0]),String.valueOf(gregorianToJalali(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))[1]),String.valueOf(gregorianToJalali(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))[2])};}
    private String todayJalaliYear(){return jalaliParts()[0];}
    private String todayJalaliMonth(){String[] m={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"};return m[Integer.parseInt(jalaliParts()[1])-1];}
    private String todayJalaliDay(){return jalaliParts()[2];}
    private String todayWeekday(){return new SimpleDateFormat("EEEE",new Locale("fa")).format(new Date());}
    private void biography(){
        base("زندگی‌نامه امام زمان (عج)");
        page.addView(txt("حضرت مهدی، امام دوازدهم شیعیان",19,gold,true));
        String[][] items={
            {"ولادت","بنابر باور مشهور شیعه، حضرت محمد بن حسن عسکری (عج) در نیمه شعبان سال ۲۵۵ هجری قمری در سامرا دیده به جهان گشودند. پدر ایشان امام حسن عسکری (ع) و مادرشان نرجس خاتون است."},
            {"دوران کودکی","به دلیل شرایط سیاسی و مراقبت حکومت عباسی، ولادت و زندگی ایشان از دید عموم پنهان نگه داشته شد. گروهی از نزدیکان و خواص، ایشان را زیارت کردند."},
            {"آغاز امامت","پس از شهادت امام حسن عسکری (ع) در سال ۲۶۰ هجری قمری، امامت حضرت مهدی (عج) آغاز شد."},
            {"غیبت صغری","از سال ۲۶۰ تا ۳۲۹ هجری قمری، ارتباط شیعیان با امام از طریق چهار نایب خاص برقرار بود: عثمان بن سعید، محمد بن عثمان، حسین بن روح و علی بن محمد سمری."},
            {"غیبت کبری","با درگذشت نایب چهارم در سال ۳۲۹ هجری قمری، غیبت کبری آغاز شد. در این دوران، شیعیان برای شناخت احکام و اداره امور دینی به عالمان واجد شرایط مراجعه می‌کنند."},
            {"انتظار و ظهور","در باور شیعه، امام مهدی (عج) زنده‌اند و به اذن خداوند از دیدگان پنهان هستند. ظهور ایشان با گسترش عدالت، مبارزه با ستم و برپایی حق همراه خواهد بود."}
        };
        for(String[] it:items){LinearLayout box=column();box.setPadding(12,8,12,8);box.addView(txt(it[0],17,0xff159f98,true));TextView body=txt(it[1],14,text(),false);body.setGravity(Gravity.RIGHT);body.setLineSpacing(5,1.15f);box.addView(body);addCard(box);}
        page.addView(txt("یادداشت: تاریخ‌ها و جزئیات زندگی امام در منابع تاریخی و حدیثی با تفاوت‌هایی نقل شده‌اند؛ این متن خلاصه‌ای از روایت مشهور امامیه است.",12,text(),false));
        addBack();
    }
    private void addMenuTile(LinearLayout menu,String icon,String title,Runnable run){
        LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);tile.setPadding(2,5,2,2);
        TextView symbol=txt(icon,29,Color.WHITE,true);symbol.setBackground(shape(0xff20a9a1,22));
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(74,74);tile.addView(symbol,ip);
        TextView caption=txt(title,12,text(),false);caption.setPadding(0,5,0,2);tile.addView(caption);
        tile.setOnClickListener(v->run.run());
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,112,1);tp.setMargins(3,0,3,0);menu.addView(tile,tp);
    }
    private void prayers(){
        base("ادعیه و زیارات");
        page.addView(txt("متن عربی و ترجمه فارسی؛ برای مطالعه با اندازه قلم و اسکرول قابل تنظیم.",13,gold,true));
        action("۞","دعای عهد","تجدید پیمان با امام زمان (عج)",()->reader("دعای عهد"));
        action("✦","دعای فرج؛ الهی عظم البلاء","درخواست گشایش و فرج",()->reader("دعای فرج"));
        action("♡","دعای سلامتی امام زمان","اللهم کن لولیک",()->reader("دعای سلامتی امام زمان"));
        addBack();
    }
    private void hadiths(){
        base("احادیث مهدوی");
        String[][] h={{"أَكْثِرُوا الدُّعَاءَ بِتَعْجِيلِ الْفَرَجِ","برای تعجیل فرج بسیار دعا کنید. (دعای امام زمان، احتجاج طبرسی)"},
        {"وَأَمَّا الْحَوَادِثُ الْوَاقِعَةُ فَارْجِعُوا فِيهَا إِلَى رُوَاةِ حَدِيثِنَا","در رخدادهای تازه به راویان حدیث ما مراجعه کنید. (توقیع شریف، کمال‌الدین)"},
        {"إِنَّا غَيْرُ مُهْمِلِينَ لِمُرَاعَاتِكُمْ وَلَا نَاسِينَ لِذِكْرِكُمْ","ما در رسیدگی به حال شما کوتاهی نمی‌کنیم و یاد شما را از خاطر نمی‌بریم. (توقیع شریف، احتجاج طبرسی)"}};
        for(String[] x:h){LinearLayout b=column();b.setPadding(12,8,12,8);b.addView(txt(x[0],21,text(),true));TextView tr=txt(x[1],14,text(),false);tr.setGravity(Gravity.RIGHT);b.addView(tr);addCard(b);}
        addBack();
    }
    private void reader(String name){
        currentPrayer=name;
        ScrollView outer=new ScrollView(this); readingScroll=outer;
        page=column(); page.setPadding(16,16,16,24); page.setBackgroundColor(bg());
        outer.addView(page); setContentView(outer); applyBars();
        TextView h=txt(name,22,Color.WHITE,true); h.setBackground(shape(green,25)); page.addView(h);
        page.addView(txt("متن عربی و ترجمهٔ فارسی",14,gold,true));
        String[][] parts=prayerData(name);
        for(String[] pair:parts){
            LinearLayout segment=column(); segment.setPadding(12,8,12,8);
            TextView ar=txt(pair[0],arabicSize,text(),true);
            ar.setTypeface(arabicFace,Typeface.NORMAL); ar.setGravity(Gravity.RIGHT);
            ar.setLineSpacing(8,1.2f); segment.addView(ar);
            TextView fa=txt(pair[1],persianSize,text(),false);
            fa.setGravity(Gravity.RIGHT); fa.setLineSpacing(5,1.15f); segment.addView(fa);
            addCard(segment);
        }
        LinearLayout controls=column(); controls.setPadding(12,12,12,12);
        controls.addView(txt("ابزار مطالعه",16,text(),true));
        controls.addView(button(scrolling?"توقف اسکرول":"شروع اسکرول خودکار",()->toggleScroll()));
        SeekBar speed=new SeekBar(this); speed.setMax(90); speed.setProgress(35);
        controls.addView(txt("سرعت اسکرول",13,text(),false)); controls.addView(speed);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int p,boolean u){scrollDelay=Math.max(5,95-p);}
            public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){}
        });
        controls.addView(button("صوت دعا؛ در به‌روزرسانی بعدی",()->toast("فایل صوتی هنوز به برنامه اضافه نشده است.")));
        page.addView(controls); addBack();
        scrollTask=()->{if(scrolling&&readingScroll!=null){readingScroll.smoothScrollBy(0,2);handler.postDelayed(scrollTask,scrollDelay);}};
    }
    private String[][] prayerData(String name){
        if(name.equals("دعای سلامتی امام زمان")) return new String[][]{
            {"اللّهُمَّ كُنْ لِوَلِيِّكَ الحُجَّةِ بْنِ الحَسَنِ صَلَواتُكَ عَلَيْهِ وَعَلَى آبائِهِ",
             "خدایا، برای ولیّ خود، حجت بن الحسن، که درودهایت بر او و پدرانش باد،"},
            {"فِي هذِهِ السَّاعَةِ وَفِي كُلِّ ساعَةٍ وَلِيّاً وَحافِظاً وَقائِداً وَناصِراً وَدَلِيلاً وَعَيْناً",
             "در این ساعت و در هر ساعت، سرپرست و نگهبان و پیشوا و یاور و راهنما و دیده‌بان او باش؛"},
            {"حَتّى تُسْكِنَهُ أَرْضَكَ طَوْعاً وَتُمَتِّعَهُ فِيها طَوِيلاً",
             "تا آن‌گاه که او را با خواست و خشنودی در زمینت جای دهی و زمانی طولانی از آن بهره‌مندش سازی."}
        };
        if(name.equals("دعای فرج")) return new String[][]{
            {"إِلَهِي عَظُمَ الْبَلاءُ وَبَرِحَ الْخَفَاءُ وَانْكَشَفَ الْغِطَاءُ وَانْقَطَعَ الرَّجَاءُ وَضَاقَتِ الْأَرْضُ وَمُنِعَتِ السَّمَاءُ وَأَنْتَ الْمُسْتَعَانُ وَإِلَيْكَ الْمُشْتَكَى وَعَلَيْكَ الْمُعَوَّلُ فِي الشِّدَّةِ وَالرَّخَاءِ",
             "خدایا، بلا بزرگ شده، نهان آشکار گشته، پرده کنار رفته، امیدها گسسته و زمین تنگ شده و آسمان از بخشش بازمانده است. تویی یاور و پناه؛ شکایت به سوی توست و در سختی و آسایش بر تو تکیه می‌کنیم."},
            {"اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَآلِ مُحَمَّدٍ أُولِي الْأَمْرِ الَّذِينَ فَرَضْتَ عَلَيْنَا طَاعَتَهُمْ وَعَرَّفْتَنَا بِذَلِكَ مَنْزِلَتَهُمْ فَفَرِّجْ عَنَّا بِحَقِّهِمْ فَرَجاً عَاجِلاً قَرِيباً كَلَمْحِ الْبَصَرِ أَوْ هُوَ أَقْرَبُ",
             "خدایا، بر محمد و خاندان محمد، صاحبان فرمانی که اطاعتشان را بر ما واجب کردی و جایگاهشان را به ما شناساندی، درود فرست؛ و به حق آنان، گشایشی فوری و نزدیک، چون چشم برهم‌زدنی یا نزدیک‌تر، برای ما فراهم کن."},
            {"يَا مُحَمَّدُ يَا عَلِيُّ يَا عَلِيُّ يَا مُحَمَّدُ اكْفِيَانِي فَإِنَّكُمَا كَافِيَانِ وَانْصُرَانِي فَإِنَّكُمَا نَاصِرَانِ",
             "ای محمد، ای علی؛ ای علی، ای محمد؛ مرا کفایت کنید که شما دو تن کفایت‌کننده‌اید و یاری‌ام دهید که شما دو تن یاورید."},
            {"يَا مَوْلَانَا يَا صَاحِبَ الزَّمَانِ الْغَوْثَ الْغَوْثَ الْغَوْثَ أَدْرِكْنِي أَدْرِكْنِي أَدْرِكْنِي السَّاعَةَ السَّاعَةَ السَّاعَةَ الْعَجَلَ الْعَجَلَ الْعَجَلَ يَا أَرْحَمَ الرَّاحِمِينَ بِحَقِّ مُحَمَّدٍ وَآلِهِ الطَّاهِرِينَ",
             "ای مولای ما، ای صاحب‌الزمان، پناه و فریادرس! به فریادم برس، همین اکنون، شتاب کن، ای مهربان‌ترین مهربانان، به حق محمد و خاندان پاکش."}
        };
        return new String[][]{
            {"اللَّهُمَّ رَبَّ النُّورِ الْعَظِيمِ وَرَبَّ الْكُرْسِيِّ الرَّفِيعِ وَرَبَّ الْبَحْرِ الْمَسْجُورِ وَمُنْزِلَ التَّوْرَاةِ وَالْإِنْجِيلِ وَالزَّبُورِ وَرَبَّ الظِّلِّ وَالْحَرُورِ وَمُنْزِلَ الْقُرْآنِ الْعَظِيمِ وَرَبَّ الْمَلَائِكَةِ الْمُقَرَّبِينَ وَالْأَنْبِيَاءِ وَالْمُرْسَلِينَ",
             "خدایا، ای پروردگار نور بزرگ، کرسی بلند، دریای آکنده و جوشان؛ ای فرستنده تورات و انجیل و زبور، پروردگار سایه و گرمای آفتاب، نازل‌کننده قرآن بزرگ و پروردگار فرشتگان مقرب و پیامبران و فرستادگان."},
            {"اللَّهُمَّ إِنِّي أَسْأَلُكَ بِوَجْهِكَ الْكَرِيمِ وَبِنُورِ وَجْهِكَ الْمُنِيرِ وَمُلْكِكَ الْقَدِيمِ يَا حَيُّ يَا قَيُّومُ أَسْأَلُكَ بِاسْمِكَ الَّذِي أَشْرَقَتْ بِهِ السَّمَاوَاتُ وَالْأَرَضُونَ وَبِاسْمِكَ الَّذِي يَصْلُحُ بِهِ الْأَوَّلُونَ وَالْآخِرُونَ",
             "خدایا، از تو می‌خواهم به ذات کریمت، به نور روشنی‌بخش چهره‌ات و فرمانروایی دیرینه‌ات؛ ای زنده و پاینده! به نامت که آسمان‌ها و زمین‌ها با آن روشن شدند و به نامت که پیشینیان و پسینیان به صلاح می‌رسند."},
            {"يَا حَيّاً قَبْلَ كُلِّ حَيٍّ وَيَا حَيّاً بَعْدَ كُلِّ حَيٍّ وَيَا حَيّاً حِينَ لَا حَيَّ يَا مُحْيِيَ الْمَوْتَى وَمُمِيتَ الْأَحْيَاءِ يَا حَيُّ لَا إِلَهَ إِلَّا أَنْتَ",
             "ای زنده پیش از هر زنده، ای زنده پس از هر زنده و ای زنده آن‌گاه که هیچ زنده‌ای نبود؛ ای زنده‌کننده مردگان و میراننده زندگان؛ ای زنده‌ای که معبودی جز تو نیست."},
            {"اللَّهُمَّ بَلِّغْ مَوْلَانَا الْإِمَامَ الْهَادِيَ الْمَهْدِيَّ الْقَائِمَ بِأَمْرِكَ صَلَوَاتُ اللَّهِ عَلَيْهِ وَعَلَى آبَائِهِ الطَّاهِرِينَ عَنْ جَمِيعِ الْمُؤْمِنِينَ وَالْمُؤْمِنَاتِ فِي مَشَارِقِ الْأَرْضِ وَمَغَارِبِهَا سَهْلِهَا وَجَبَلِهَا وَبَرِّهَا وَبَحْرِهَا وَعَنِّي وَعَنْ وَالِدَيَّ مِنَ الصَّلَوَاتِ زِنَةَ عَرْشِ اللَّهِ وَمِدَادَ كَلِمَاتِهِ وَمَا أَحْصَاهُ عِلْمُهُ وَأَحَاطَ بِهِ كِتَابُهُ",
             "خدایا، از سوی همه مردان و زنان باایمان در شرق و غرب زمین، دشت و کوه، خشکی و دریا، و نیز از سوی من و پدر و مادرم، به مولایمان امام هدایتگر و هدایت‌شده، قیام‌کننده به فرمانت، درودهایی به شمار وزن عرش خدا و مرکب کلماتش و به اندازه آنچه دانش او شمرده و کتابش فراگرفته است، برسان."},
            {"اللَّهُمَّ إِنِّي أُجَدِّدُ لَهُ فِي صَبِيحَةِ يَوْمِي هَذَا وَمَا عِشْتُ مِنْ أَيَّامِي عَهْداً وَعَقْداً وَبَيْعَةً لَهُ فِي عُنُقِي لَا أَحُولُ عَنْهَا وَلَا أَزُولُ أَبَداً",
             "خدایا، من در بامداد امروز و در همه روزهای زندگی‌ام، پیمان و عهد و بیعت خود را با او تازه می‌کنم؛ پیمانی بر گردن من که هرگز از آن روی نمی‌گردانم و دست نمی‌کشم."},
            {"اللَّهُمَّ اجْعَلْنِي مِنْ أَنْصَارِهِ وَأَعْوَانِهِ وَالذَّابِّينَ عَنْهُ وَالْمُسَارِعِينَ إِلَيْهِ فِي قَضَاءِ حَوَائِجِهِ وَالْمُمْتَثِلِينَ لِأَوَامِرِهِ وَالْمُحَامِينَ عَنْهُ وَالسَّابِقِينَ إِلَى إِرَادَتِهِ وَالْمُسْتَشْهَدِينَ بَيْنَ يَدَيْهِ",
             "خدایا، مرا از یاران و مددکاران و دفاع‌کنندگان از او قرار ده؛ از کسانی که در برآوردن خواسته‌هایش می‌شتابند، فرمان‌هایش را اطاعت می‌کنند، از او حمایت می‌کنند، در انجام خواست او پیشی می‌گیرند و در پیشگاهش به شهادت می‌رسند."},
            {"اللَّهُمَّ إِنْ حَالَ بَيْنِي وَبَيْنَهُ الْمَوْتُ الَّذِي جَعَلْتَهُ عَلَى عِبَادِكَ حَتْماً مَقْضِيّاً فَأَخْرِجْنِي مِنْ قَبْرِي مُؤْتَزِراً كَفَنِي شَاهِراً سَيْفِي مُجَرِّداً قَنَاتِي مُلَبِّياً دَعْوَةَ الدَّاعِي فِي الْحَاضِرِ وَالْبَادِي",
             "خدایا، اگر مرگی که آن را سرنوشتی حتمی برای بندگانت قرار داده‌ای میان من و او فاصله انداخت، مرا از قبرم بیرون آور، در حالی که کفنم را به کمر بسته، شمشیرم را آخته و نیزه‌ام را برهنه کرده‌ام و دعوت دعوت‌کننده را در شهر و بیابان پاسخ می‌دهم."},
            {"اللَّهُمَّ أَرِنِي الطَّلْعَةَ الرَّشِيدَةَ وَالْغُرَّةَ الْحَمِيدَةَ وَاكْحُلْ نَاظِرِي بِنَظْرَةٍ مِنِّي إِلَيْهِ وَعَجِّلْ فَرَجَهُ وَسَهِّلْ مَخْرَجَهُ وَأَوْسِعْ مَنْهَجَهُ وَاسْلُكْ بِي مَحَجَّتَهُ وَأَنْفِذْ أَمْرَهُ وَاشْدُدْ أَزْرَهُ",
             "خدایا، آن چهره هدایتگر و پیشانی ستوده را به من بنمایان و دیدگانم را با نگاه به او روشن کن؛ در فرجش شتاب فرما، راه خروجش را آسان و مسیرش را گسترده ساز، مرا در راه روشن او قرار ده، فرمانش را نافذ و پشتش را استوار گردان."},
            {"وَاعْمُرِ اللَّهُمَّ بِهِ بِلَادَكَ وَأَحْيِ بِهِ عِبَادَكَ فَإِنَّكَ قُلْتَ وَقَوْلُكَ الْحَقُّ ظَهَرَ الْفَسَادُ فِي الْبَرِّ وَالْبَحْرِ بِمَا كَسَبَتْ أَيْدِي النَّاسِ",
             "خدایا، سرزمین‌هایت را به دست او آباد و بندگانت را به وسیله او زنده کن؛ زیرا تو فرموده‌ای و سخنت حق است: فساد در خشکی و دریا به سبب کارهایی که مردم انجام داده‌اند آشکار شده است."},
            {"فَأَظْهِرِ اللَّهُمَّ لَنَا وَلِيَّكَ وَابْنَ بِنْتِ نَبِيِّكَ الْمُسَمَّى بِاسْمِ رَسُولِكَ حَتَّى لَا يَظْفَرَ بِشَيْءٍ مِنَ الْبَاطِلِ إِلَّا مَزَّقَهُ وَيُحِقَّ الْحَقَّ وَيُحَقِّقَهُ",
             "پس خدایا، ولیّ خود و فرزند دختر پیامبرت را که هم‌نام فرستاده توست برای ما آشکار ساز؛ تا با هیچ باطلی روبه‌رو نشود مگر آنکه آن را درهم شکند و حق را استوار و تحقق‌یافته سازد."},
            {"وَاجْعَلْهُ اللَّهُمَّ مَفْزَعاً لِمَظْلُومِ عِبَادِكَ وَنَاصِراً لِمَنْ لَا يَجِدُ لَهُ نَاصِراً غَيْرَكَ وَمُجَدِّداً لِمَا عُطِّلَ مِنْ أَحْكَامِ كِتَابِكَ وَمُشَيِّداً لِمَا وَرَدَ مِنْ أَعْلَامِ دِينِكَ وَسُنَنِ نَبِيِّكَ صَلَّى اللَّهُ عَلَيْهِ وَآلِهِ",
             "خدایا، او را پناه ستمدیدگان و یاور بی‌یاوران قرار ده؛ احکام تعطیل‌شده کتابت را زنده کند و نشانه‌های دین و سنت‌های پیامبرت را استوار سازد."},
            {"وَاجْعَلْهُ اللَّهُمَّ مِمَّنْ حَصَّنْتَهُ مِنْ بَأْسِ الْمُعْتَدِينَ اللَّهُمَّ وَسُرَّ نَبِيَّكَ مُحَمَّداً صَلَّى اللَّهُ عَلَيْهِ وَآلِهِ بِرُؤْيَتِهِ وَمَنْ تَبِعَهُ عَلَى دَعْوَتِهِ وَارْحَمِ اسْتِكَانَتَنَا بَعْدَهُ",
             "خدایا، او را از کسانی قرار ده که از گزند تجاوزگران حفظ کرده‌ای؛ پیامبرت محمد و پیروان دعوتش را با دیدار او شادمان ساز و بر ناتوانی و فروتنی ما پس از او رحم کن."},
            {"اللَّهُمَّ اكْشِفْ هَذِهِ الْغُمَّةَ عَنْ هَذِهِ الْأُمَّةِ بِحُضُورِهِ وَعَجِّلْ لَنَا ظُهُورَهُ إِنَّهُمْ يَرَوْنَهُ بَعِيداً وَنَرَاهُ قَرِيباً بِرَحْمَتِكَ يَا أَرْحَمَ الرَّاحِمِينَ الْعَجَلَ الْعَجَلَ يَا مَوْلَايَ يَا صَاحِبَ الزَّمَانِ",
             "خدایا، با حضور او این اندوه را از این امت برطرف کن و در ظهورش برای ما شتاب فرما؛ دیگران آن را دور می‌بینند و ما نزدیکش می‌دانیم. به مهربانی‌ات، ای مهربان‌ترین مهربانان؛ شتاب کن، شتاب کن، ای مولای من، ای صاحب‌الزمان."}
        };
    }
    private void toggleScroll(){
        scrolling=!scrolling;
        if(scrolling){if(scrollTask!=null){handler.removeCallbacks(scrollTask);handler.post(scrollTask);}toast("اسکرول خودکار شروع شد");}
        else {if(scrollTask!=null)handler.removeCallbacks(scrollTask);toast("اسکرول متوقف شد");}
    }
    private Button button(String s,Runnable r){
        Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));b.setOnClickListener(v->r.run());return b;
    }
    private void addBack(){Button b=button("بازگشت",()->home());page.addView(b);}
    private void simple(String title){
        base(title);
        if(title.contains("قرآن")){
            page.addView(txt("کتابخانه قرآن کریم",18,gold,true));
            page.addView(txt("کتابخانه آفلاین قرآن در حال آماده‌سازی است. در این نسخه متن کامل سوره‌ها در دسترس نیست و برای جلوگیری از نمایش متن ناقص، فعلاً فقط معرفی بخش نمایش داده می‌شود.",15,text(),false));
            page.addView(txt("منبع متن معیار برای نسخه بعدی: پروژهٔ تنزیل؛ متن عثمانی باید بدون تغییر و با حفظ مجوز و ارجاع منتشر شود.",12,text(),false));
            String[] suras={"الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال","التوبة","يونس"};
            for(String x:suras)action("۞",x,"فهرست سوره‌ها؛ متن کامل در نسخه بعدی",()->toast("متن این سوره هنوز به کتابخانه آفلاین افزوده نشده است."));
        }else if(title.contains("نهج")){
            page.addView(txt("کتابخانه نهج‌البلاغه",18,gold,true));
            page.addView(txt("گزیده‌ای از خطبه‌ها، نامه‌ها و حکمت‌ها در نسخه‌های بعدی به‌صورت متن کامل و همراه ترجمه افزوده می‌شود. در این نسخه از نمایش متن ناقص به‌عنوان متن کامل خودداری شده است.",15,text(),false));
            action("❖","خطبه‌ها","فهرست خطبه‌ها",()->toast("متن کامل خطبه‌ها هنوز در بسته آفلاین نیست."));
            action("✉","نامه‌ها","فهرست نامه‌ها",()->toast("متن کامل نامه‌ها هنوز در بسته آفلاین نیست."));
            action("✦","حکمت‌ها","فهرست حکمت‌ها",()->toast("متن کامل حکمت‌ها هنوز در بسته آفلاین نیست."));
        }else if(title.contains("صحیفه")){
            page.addView(txt("صحیفه سجادیه",18,gold,true));
            page.addView(txt("مجموعه دعاهای امام سجاد (ع). برای حفظ امانت، متن کامل دعاها تا زمان افزودن نسخه معتبر آفلاین به‌صورت کامل‌نما ارائه نشده است.",15,text(),false));
            for(int i=1;i<=54;i++){final int n=i;action("۞","دعای "+i,"صحیفه سجادیه",()->toast("متن کامل دعای "+n+" هنوز به برنامه افزوده نشده است."));}
        }else{
            page.addView(txt("این قابلیت هنوز فعال نشده است و در به‌روزرسانی‌های بعدی ارائه می‌شود.",15,text(),false));
        }
        addBack();
    }
    private void settings(){
        base("تنظیمات برنامه");
        Switch theme=new Switch(this);theme.setText("حالت شب");theme.setTextColor(text());theme.setChecked(dark);
        theme.setPadding(16,14,16,14);theme.setOnCheckedChangeListener((v,on)->{dark=on;getSharedPreferences(PREF,0).edit().putBoolean("dark",on).apply();home();});addCard(theme);
        sizeControl("اندازه فونت عربی",true);sizeControl("اندازه فونت فارسی",false);
        action("Aa","فونت متن‌های عربی","انتخاب فایل TTF/OTF از گوشی",()->pickFont(PICK_ARABIC));
        action("آ","فونت تیترها و متن اصلی","انتخاب قلم رابط کاربری",()->pickFont(PICK_UI));
        page.addView(txt("فونت‌های پیش‌فرض برنامه همراه نصب قرار می‌گیرند. در صورت تمایل می‌توانید قلم دلخواه خود را نیز از حافظه گوشی انتخاب کنید.",13,text(),false));
        page.addView(txt("امنیت و حریم خصوصی",17,gold,true));
        page.addView(txt("برنامه بدون حساب کاربری و بدون ارسال اطلاعات شخصی به سرور طراحی شده است. تنظیمات و قلم‌های انتخابی فقط در حافظهٔ داخلی همین دستگاه ذخیره می‌شوند. برای فایل‌گزینی از انتخاب‌گر امن اندروید استفاده می‌شود.",13,text(),false));
        addBack();
    }
    private void sizeControl(String title,boolean arabic){
        LinearLayout box=column();box.setPadding(14,10,14,10);box.addView(txt(title,15,text(),true));
        SeekBar s=new SeekBar(this);s.setMax(32);s.setProgress((int)((arabic?arabicSize:persianSize)-12));box.addView(s);
        TextView val=txt(Math.round(arabic?arabicSize:persianSize)+"sp",13,gold,true);box.addView(val);
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int p,boolean u){float n=12+p;val.setText(Math.round(n)+"sp");if(arabic)arabicSize=n;else persianSize=n;}
            public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){getSharedPreferences(PREF,0).edit().putFloat(arabic?"arabic_size":"persian_size",arabic?arabicSize:persianSize).apply();}
        });addCard(box);
    }
    private void pickFont(int code){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");
        startActivityForResult(i,code);
    }
    @Override protected void onActivityResult(int req,int res,Intent data){
        super.onActivityResult(req,res,data);if(res!=RESULT_OK||data==null)return;
        Uri uri=data.getData();if(uri==null)return;
        try{
            File f=new File(getFilesDir(),req==PICK_ARABIC?"user_arabic_font.ttf":"user_ui_font.ttf");
            try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(f)){
                byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);
            }
            Typeface face=Typeface.createFromFile(f);
            if(req==PICK_ARABIC){arabicFace=face;getSharedPreferences(PREF,0).edit().putString("arabic_path",f.getAbsolutePath()).apply();}
            else{uiFace=face;titleFace=face;getSharedPreferences(PREF,0).edit().putString("ui_path",f.getAbsolutePath()).apply();}
            toast("فونت با موفقیت اعمال شد.");settings();
        }catch(Exception e){toast("فایل فونت معتبر نیست یا قابل خواندن نیست.");}
    }
    private void calendarPage(){
        final int[] year={Integer.parseInt(todayJalaliYear())};
        renderCalendar(year[0]);
    }
    private void renderCalendar(int year){
        base("تقویم هجری شمسی");
        LinearLayout yr=new LinearLayout(this);yr.setGravity(Gravity.CENTER);
        yr.addView(button("سال قبل",()->renderCalendar(year-1)));
        yr.addView(txt("سال "+year,19,gold,true),new LinearLayout.LayoutParams(0,-2,1));
        yr.addView(button("سال بعد",()->renderCalendar(year+1)));
        page.addView(yr);
        page.addView(txt("هفته از شنبه آغاز می‌شود. روزهای تعطیل ثابت شمسی مشخص شده‌اند؛ مناسبت‌های قمری باید با تقویم رسمی همان سال تطبیق داده شوند.",12,text(),false));
        String[] months={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"};
        String[] weekdays={"ش","ی","د","س","چ","پ","ج"};
        for(int m=1;m<=12;m++){
            LinearLayout monthBox=column();monthBox.setPadding(10,8,10,10);
            monthBox.addView(txt(months[m-1],18,gold,true));
            GridLayout grid=new GridLayout(this);grid.setColumnCount(7);grid.setUseDefaultMargins(true);
            for(String w:weekdays){TextView v=txt(w,11,gold,true);GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=30;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);grid.addView(v,lp);}
            int[] first=jalaliToGregorian(year,m,1);Calendar fc=Calendar.getInstance();fc.clear();fc.set(first[0],first[1]-1,first[2]);
            int offset=fc.get(Calendar.DAY_OF_WEEK)%7,count=jalaliMonthLength(year,m);
            for(int k=0;k<offset;k++){TextView blank=txt("",10,text(),false);GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=38;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);grid.addView(blank,lp);}
            for(int d=1;d<=count;d++){
                final int fm=m,fd=d;
                boolean fixed=(m==1&&d<=4)||(m==1&&(d==12||d==13))||(m==3&&(d==14||d==15))||(m==11&&d==22)||(m==12&&d==29);
                boolean friday=((offset+d-1)%7)==6;
                TextView cell=txt(String.valueOf(d),13,fixed?Color.WHITE:(friday?0xffc56d62:text()),fixed);
                if(fixed)cell.setBackground(shape(0xff9b7330,12));
                GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=38;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);grid.addView(cell,lp);
                cell.setOnClickListener(v->{
                    int[] g=jalaliToGregorian(year,fm,fd);
                    String msg=String.format(Locale.US,"%04d/%02d/%02d",year,fm,fd)+"\nمعادل میلادی: "+g[0]+"/"+g[1]+"/"+g[2];
                    if(fixed)msg+="\nتعطیل رسمی ثابت";
                    else if(friday)msg+="\nجمعه";
                    new AlertDialog.Builder(this).setTitle(fd+" "+months[fm-1]+" "+year).setMessage(msg).setPositiveButton("باشه",null).show();
                });
            }
            monthBox.addView(grid);addCard(monthBox);
        }
        page.addView(txt("تعطیلات ثابت نمایش‌داده‌شده: ۱ تا ۴ فروردین، ۱۲ و ۱۳ فروردین، ۱۴ و ۱۵ خرداد، ۲۲ بهمن و ۲۹ اسفند. مناسبت‌های قمری و تعطیلات متغیر در این نسخه درج نشده‌اند تا تاریخ اشتباه نمایش داده نشود.",12,text(),false));
        addBack();
    }
    private int monthHolidayCount(int[][] dates,int month){int n=0;for(int[] d:dates)if(d[0]==month)n++;return n;}
    private int jalaliMonthLength(int jy,int jm){
        if(jm<=6)return 31; if(jm<=11)return 30;
        int[] a=jalaliToGregorian(jy,12,1), b=jalaliToGregorian(jy+1,1,1);
        Calendar x=Calendar.getInstance(), y=Calendar.getInstance();
        x.clear(); x.set(a[0],a[1]-1,a[2]); y.clear(); y.set(b[0],b[1]-1,b[2]);
        return (int)((y.getTimeInMillis()-x.getTimeInMillis())/86400000L);
    }
    private int[] jalaliToGregorian(int jy,int jm,int jd){
        jy+=1595; int days=-355668+365*jy+(jy/33)*8+((jy%33+3)/4)+jd;
        days+=(jm<7?(jm-1)*31:186+(jm-7)*30);
        int gy=400*(days/146097); days%=146097;
        if(days>36524){gy+=100*((--days)/36524);days%=36524;if(days>=365)days++;}
        gy+=4*(days/1461);days%=1461;
        if(days>365){gy+=(days-1)/365;days=(days-1)%365;}
        int gd=days+1; int[] md={0,31,((gy%4==0&&gy%100!=0)||gy%400==0)?29:28,31,30,31,30,31,31,30,31,30,31};
        int gm=1; while(gm<=12&&gd>md[gm]){gd-=md[gm];gm++;} return new int[]{gy,gm,gd};
    }
    private String jalaliToday(){
        Calendar c=Calendar.getInstance();int[] j=gregorianToJalali(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH));
        String[] months={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"};
        return j[2]+" "+months[j[1]-1]+" "+j[0];
    }
    private int[] gregorianToJalali(int gy,int gm,int gd){
        int[] gdm={0,31,59,90,120,151,181,212,243,273,304,334};
        int gy2=gm>2?gy+1:gy;int days=355666+365*gy+(gy2+3)/4-(gy2+99)/100+(gy2+399)/400+gd+gdm[gm-1];
        int jy=-1595+33*(days/12053);days%=12053;jy+=4*(days/1461);days%=1461;
        if(days>365){jy+=(days-1)/365;days=(days-1)%365;}
        int jm,jd;if(days<186){jm=1+days/31;jd=1+days%31;}else{jm=7+(days-186)/30;jd=1+(days-186)%30;}
        return new int[]{jy,jm,jd};
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onBackPressed(){if(scrolling){scrolling=false;if(scrollTask!=null)handler.removeCallbacks(scrollTask);}home();}
    @Override protected void onPause(){super.onPause();if(scrolling){scrolling=false;if(scrollTask!=null)handler.removeCallbacks(scrollTask);}}
    @Override protected void onDestroy(){activityDestroyed=true;scrolling=false;if(scrollTask!=null)handler.removeCallbacks(scrollTask);handler.removeCallbacksAndMessages(null);super.onDestroy();}

    private class AnimatedBackdrop extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);long start=System.currentTimeMillis();
        AnimatedBackdrop(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        private void fill(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float w=getWidth(),h=getHeight();long tm=System.currentTimeMillis()-start;
            p.setShader(new LinearGradient(0,0,w,h,0xff087e7a,0xff21b0a4,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
            // Soft moving light beams
            for(int i=0;i<4;i++){float x=(float)((i*w/3.0)+(Math.sin(tm/2600.0+i)*28));p.setShader(new LinearGradient(x,0,x+95,h,0x22ffffff,0x00ffffff,Shader.TileMode.CLAMP));c.drawRect(x-50,0,x+95,h,p);p.setShader(null);}
            // Distant skyline and mosque domes
            p.setColor(0x1b075d59);
            for(int i=0;i<8;i++){float x=i*w/7f;float base=h*.83f;float bh=h*(.22f+(i%3)*.035f);c.drawRect(x-12,base-bh,x+16,base,p);c.drawCircle(x+2,base-bh,11,p);}
            // central mosque silhouette
            p.setColor(0x22065a56);float base=h*.86f;
            c.drawRect(w*.30f,base-h*.22f,w*.70f,base,p);c.drawOval(w*.36f,base-h*.34f,w*.64f,base-h*.08f,p);
            p.setColor(0x22065a56);c.drawRect(w*.48f,base-h*.52f,w*.52f,base-h*.22f,p);
            Path dome=new Path();dome.moveTo(w*.48f,base-h*.52f);dome.quadTo(w*.50f,base-h*.64f,w*.52f,base-h*.52f);c.drawPath(dome,p);
            // drifting glowing particles
            for(int i=0;i<16;i++){float x=(float)((i*71+tm*(.008+i%3*.002))%(w+20));float y=(float)(h*(.12+(i*37%70)/100.0)+Math.sin(tm/900.0+i)*8);p.setColor(i%3==0?0x66fff0c2:0x44ffffff);p.setShadowLayer(10,0,0,0x55ffffff);c.drawCircle(x,y,2+(i%3),p);p.clearShadowLayer();}
            postInvalidateDelayed(45);
        }
    }}