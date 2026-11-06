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
    private LinearLayout page;
    private int green=Color.rgb(17,83,61), gold=Color.rgb(210,174,91);
    private boolean dark;
    private float arabicSize=28f, persianSize=16f;
    private Typeface arabicFace=Typeface.DEFAULT, uiFace=Typeface.DEFAULT;
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
    private void loadSavedFonts(){
        android.content.SharedPreferences p=getSharedPreferences(PREF,0);
        String a=p.getString("arabic_path",null),u=p.getString("ui_path",null);
        try{if(a!=null)arabicFace=Typeface.createFromFile(a);}catch(Exception ignored){}
        try{if(u!=null)uiFace=Typeface.createFromFile(u);}catch(Exception ignored){}
    }
    private TextView txt(String s,float size,int color,boolean bold){
        TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);
        v.setGravity(Gravity.CENTER);v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setTypeface(uiFace,bold?Typeface.BOLD:Typeface.NORMAL);v.setPadding(12,10,12,10);
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
        page=column(); page.setPadding(12,12,12,22); page.setBackgroundColor(bg());
        outer.addView(page); setContentView(outer);
        FrameLayout hero=new FrameLayout(this);
        hero.setBackground(shape(green,28)); hero.setClipToOutline(true);
        AnimatedBackdrop backdrop=new AnimatedBackdrop();
        hero.addView(backdrop,new FrameLayout.LayoutParams(-1,460));
        LinearLayout intro=column(); intro.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL);
        intro.setPadding(14,30,14,12);
        intro.addView(txt("یــار مهدوی",25,Color.WHITE,true));
        intro.addView(txt("السَّلامُ عَلَیْکَ یا صاحِبَ الزَّمان",20,Color.WHITE,true));
        intro.addView(txt("به یاد امام عصر (عج)",14,0xffe7eadc,false));
        hero.addView(intro,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,460);
        hp.setMargins(0,0,0,12); page.addView(hero,hp);

        HorizontalScrollView menuScroll=new HorizontalScrollView(this);
        menuScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout menu=new LinearLayout(this); menu.setOrientation(LinearLayout.HORIZONTAL);
        menu.setGravity(Gravity.CENTER_VERTICAL);
        addMenuTile(menu,"۞","دعاها",()->prayers());
        addMenuTile(menu,"✧","روایات",()->simple("روایات"));
        addMenuTile(menu,"☾","زندگینامه",()->simple("زندگینامه امام زمان (عج)"));
        addMenuTile(menu,"▤","مقالات",()->simple("مقالات"));
        addMenuTile(menu,"▦","تقویم",()->calendarPage());
        addMenuTile(menu,"⚙","تنظیمات",()->settings());
        menuScroll.addView(menu); page.addView(menuScroll);
        LinearLayout bottom=column(); bottom.setPadding(12,10,12,10);
        bottom.addView(txt("پرکاربرد",16,text(),true));
        bottom.addView(txt("این بخش آزمایشی است و در به‌روزرسانی‌های بعدی فعال می‌شود.",12,text(),false));
        addCard(bottom);
    }
    private void addMenuTile(LinearLayout menu,String icon,String title,Runnable run){
        LinearLayout tile=new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER); tile.setPadding(5,7,5,5);
        tile.setBackground(shape(card(),17)); tile.setElevation(3);
        TextView symbol=txt(icon,28,gold,true);
        symbol.setBackground(shape(dark?0xff203c31:0xffedf3ed,13));
        LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(66,62);
        tile.addView(symbol,ip);
        TextView caption=txt(title,12,text(),true); caption.setPadding(2,5,2,2);
        tile.addView(caption);
        tile.setOnClickListener(v->run.run());
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(82,100);
        tp.setMargins(4,0,4,0); menu.addView(tile,tp);
    }
    private void prayers(){
        base("دعاها و زیارات");
        action("۞","دعای عهد","",()->reader("دعای عهد"));
        action("✦","دعای فرج","",()->reader("دعای فرج"));
        action("♡","دعای سلامتی امام زمان","",()->reader("دعای سلامتی امام زمان"));
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
        if(scrolling){if(scrollTask!=null)handler.post(scrollTask);}else handler.removeCallbacks(scrollTask);
        reader(currentPrayer);
    }
    private Button button(String s,Runnable r){
        Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));b.setOnClickListener(v->r.run());return b;
    }
    private void addBack(){Button b=button("بازگشت",()->home());page.addView(b);}
    private void simple(String title){base(title);page.addView(txt("محتوای این بخش در حال آماده‌سازی است و در نسخهٔ محتوایی بعدی تکمیل می‌شود.",16,text(),false));addBack();}
    private void settings(){
        base("تنظیمات برنامه");
        Switch theme=new Switch(this);theme.setText("حالت شب");theme.setTextColor(text());theme.setChecked(dark);
        theme.setPadding(16,14,16,14);theme.setOnCheckedChangeListener((v,on)->{dark=on;getSharedPreferences(PREF,0).edit().putBoolean("dark",on).apply();home();});addCard(theme);
        sizeControl("اندازه فونت عربی",true);sizeControl("اندازه فونت فارسی",false);
        action("Aa","فونت متن‌های عربی","انتخاب فایل TTF/OTF از گوشی",()->pickFont(PICK_ARABIC));
        action("آ","فونت تیترها و متن اصلی","انتخاب قلم رابط کاربری",()->pickFont(PICK_UI));
        page.addView(txt("قلم IRAmir در این نسخه به‌صورت پیش‌فرض ضمیمه نشده، چون فایل اصلی فونت در مخزن موجود نیست. با انتخاب فایل فونت از گوشی می‌توان آن را اعمال کرد.",13,text(),false));
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
            getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File f=new File(getFilesDir(),req==PICK_ARABIC?"user_arabic_font.ttf":"user_ui_font.ttf");
            try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(f)){
                byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);
            }
            Typeface face=Typeface.createFromFile(f);
            if(req==PICK_ARABIC){arabicFace=face;getSharedPreferences(PREF,0).edit().putString("arabic_path",f.getAbsolutePath()).apply();}
            else{uiFace=face;getSharedPreferences(PREF,0).edit().putString("ui_path",f.getAbsolutePath()).apply();}
            toast("فونت با موفقیت اعمال شد.");settings();
        }catch(Exception e){toast("فایل فونت معتبر نیست یا قابل خواندن نیست.");}
    }
    private void calendarPage(){
        base("تقویم سالانه ۱۴۰۵");
        page.addView(txt("نمای کامل سال هجری شمسی؛ روزهای تعطیل با رنگ طلایی مشخص شده‌اند.",13,text(),false));
        String[] months={"فروردین","اردیبهشت","خرداد","تیر","مرداد","شهریور","مهر","آبان","آذر","دی","بهمن","اسفند"};
        String[] weekdays={"ش","ی","د","س","چ","پ","ج"};
        int[][] holidays={{1,1},{1,2},{1,3},{1,4},{1,12},{1,13},{1,25},{3,6},{3,14},{3,15},{4,3},{4,4},{5,13},{5,21},{5,22},{5,30},{6,8},{8,22},{10,2},{10,16},{11,4},{11,22},{12,9},{12,19},{12,20},{12,29}};
        String[] holidayNames={"نوروز و عید فطر","نوروز و عید فطر","نوروز","نوروز","روز جمهوری اسلامی","روز طبیعت","شهادت امام جعفر صادق (ع)","عید قربان","رحلت امام خمینی (ره) و عید غدیر","قیام ۱۵ خرداد","تاسوعای حسینی","عاشورای حسینی","اربعین حسینی","رحلت پیامبر (ص) و شهادت امام حسن (ع)","شهادت امام رضا (ع)","شهادت امام حسن عسکری (ع)","میلاد پیامبر (ص) و امام صادق (ع)","شهادت حضرت فاطمه زهرا (س)","ولادت امام علی (ع) و روز پدر","مبعث پیامبر اکرم (ص)","ولادت امام مهدی (عج) و نیمه شعبان","پیروزی انقلاب اسلامی","شهادت امام علی (ع)","عید فطر","تعطیلی عید فطر","ملی شدن صنعت نفت"};
        for(int m=1;m<=12;m++){
            LinearLayout monthBox=column(); monthBox.setPadding(10,8,10,10);
            monthBox.addView(txt(months[m-1],18,gold,true));
            GridLayout grid=new GridLayout(this); grid.setColumnCount(7); grid.setUseDefaultMargins(true);
            for(String w:weekdays){TextView v=txt(w,11,gold,true);GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=30;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);grid.addView(v,lp);}
            int[] first=jalaliToGregorian(1405,m,1); Calendar fc=Calendar.getInstance();fc.clear();fc.set(first[0],first[1]-1,first[2]);
            int offset=fc.get(Calendar.DAY_OF_WEEK)%7, count=jalaliMonthLength(1405,m);
            for(int k=0;k<offset;k++){TextView blank=txt("",10,text(),false);GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=34;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);grid.addView(blank,lp);}
            for(int d=1;d<=count;d++){
                boolean holiday=false;String event="";
                for(int z=0;z<holidays.length;z++)if(holidays[z][0]==m&&holidays[z][1]==d){holiday=true;event=holidayNames[z];break;}
                final int fm=m,fd=d; TextView cell=txt(String.valueOf(d),12,holiday?Color.WHITE:text(),holiday);
                if(holiday)cell.setBackground(shape(0xffa67c32,12));
                else if((offset+d-1)%7==6)cell.setTextColor(0xffc56d62);
                GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.height=34;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);grid.addView(cell,lp);
                if(holiday){final String ev=event;cell.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(fd+" "+months[fm-1]+" ۱۴۰۵").setMessage(ev).setPositiveButton("باشه",null).show());}
            }
            monthBox.addView(grid);
            TextView note=txt("تعطیلات رسمی: "+monthHolidayCount(holidays,m),11,text(),false);
            monthBox.addView(note); addCard(monthBox);
        }
        page.addView(txt("توجه: تاریخ مناسبت‌های قمری ممکن است با اعلام رسمی رؤیت هلال تغییر کند.",12,text(),false));
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
    @Override public void onBackPressed(){if(scrolling){scrolling=false;handler.removeCallbacks(scrollTask);}home();}

    private class AnimatedBackdrop extends View{
        Paint p=new Paint(3);long start=System.currentTimeMillis();
        AnimatedBackdrop(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float w=getWidth(),h=getHeight();long t=System.currentTimeMillis()-start;
            p.setShader(new LinearGradient(0,0,w,h,0xff0b382b,0xff17634b,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
            for(int i=0;i<7;i++){
                float x=(float)((w*(i+1)/8.0)+Math.sin(t/1900.0+i)*22);
                float y=(float)(h*(.18+i%3*.29)+Math.cos(t/2300.0+i)*13);
                p.setColor(i%2==0?0x33ffffff:0x22d8bd78);p.setShadowLayer(28,0,0,0x557ee0b5);
                c.drawCircle(x,y,24+(i%3)*13,p);p.clearShadowLayer();
            }
            p.setColor(0x22ffffff);p.setStrokeWidth(1.2f);
            for(int i=0;i<5;i++){float y=(i+1)*h/6;c.drawLine(0,y,w,y+((i%2)*9),p);}
            postInvalidateDelayed(50);
        }
    }
}