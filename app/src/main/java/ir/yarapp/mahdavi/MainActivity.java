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
        applyBars();base("یــار مهدوی");
        FrameLayout hero=new FrameLayout(this);hero.setClipToOutline(true);hero.setBackground(shape(green,30));
        AnimatedBackdrop backdrop=new AnimatedBackdrop();hero.addView(backdrop,new FrameLayout.LayoutParams(-1,245));
        LinearLayout intro=column();intro.setGravity(Gravity.CENTER);intro.setPadding(12,26,12,22);
        intro.addView(txt("السَّلامُ عَلَیْکَ یا صاحِبَ الزَّمان",20,Color.WHITE,true));
        intro.addView(txt("به یار مهدوی خوش آمدید",22,Color.WHITE,true));
        intro.addView(txt("همراهی آرام در مسیر انتظار",14,0xffe2eadf,false));
        hero.addView(intro,new FrameLayout.LayoutParams(-1,-1));addCard(hero);
        action("۞","دعاها و زیارات","متن دعا و ابزار مطالعه",()->prayers());
        action("✧","روایات","گزیده روایات مهدوی",()->simple("روایات"));
        action("☾","زندگینامه امام زمان (عج)","آشنایی با زندگی و سیره",()->simple("زندگینامه امام زمان (عج)"));
        action("▤","مقالات","مطالعه و پژوهش",()->simple("مقالات"));
        action("▦","تقویم فارسی","امروز: "+jalaliToday(),()->calendarPage());
        action("⚙","تنظیمات","ظاهر، قلم و اندازه نوشته‌ها",()->settings());
        LinearLayout bottom=column();bottom.setPadding(12,12,12,12);
        bottom.addView(txt("پرکاربرد",17,text(),true));
        bottom.addView(txt("این بخش آزمایشی است و در به‌روزرسانی‌های بعدی فعال می‌شود.",13,text(),false));
        addCard(bottom);
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
        ScrollView outer=new ScrollView(this);readingScroll=outer;
        page=column();page.setPadding(16,16,16,24);page.setBackgroundColor(bg());
        outer.addView(page);setContentView(outer);applyBars();
        TextView h=txt(name,22,Color.WHITE,true);h.setBackground(shape(green,25));page.addView(h);
        page.addView(txt("متن دعا و ترجمه",14,text(),true));
        String warning="متن کامل و ترجمهٔ مقابله‌شدهٔ این دعا هنوز به بانک محتوای تأییدشدهٔ برنامه افزوده نشده است.";
        page.addView(txt(warning,16,text(),false));
        page.addView(txt("نمونهٔ قالب نمایش دعا",14,gold,true));
        page.addView(txt("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",arabicSize,text(),true));
        page.addView(txt("به نام خداوند بخشنده مهربان",persianSize,text(),false));
        page.addView(txt("پس از افزودن متن معتبر، هر بخش عربی همراه با ترجمهٔ همان بخش نمایش داده خواهد شد.",persianSize,text(),false));
        LinearLayout controls=column();controls.setPadding(12,12,12,12);
        controls.addView(txt("ابزار مطالعه",16,text(),true));
        Button start=button(scrolling?"توقف اسکرول":"شروع اسکرول خودکار",()->toggleScroll());
        controls.addView(start);
        SeekBar speed=new SeekBar(this);speed.setMax(90);speed.setProgress(35);controls.addView(txt("سرعت اسکرول",13,text(),false));controls.addView(speed);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int p,boolean u){scrollDelay=Math.max(5,95-p);}
            public void onStartTrackingTouch(SeekBar b){} public void onStopTrackingTouch(SeekBar b){}
        });
        Button audio=button("افزودن صوت دعا در نسخه‌های بعد",()->toast("پخش صوت پس از فراهم‌شدن فایل‌های مجاز اضافه می‌شود."));
        controls.addView(audio);page.addView(controls);addBack();
        scrollTask=()->{if(scrolling&&readingScroll!=null){readingScroll.smoothScrollBy(0,2);handler.postDelayed(scrollTask,scrollDelay);}};
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
        base("تقویم فارسی");
        Calendar c=Calendar.getInstance();
        page.addView(txt(jalaliToday(),27,gold,true));
        page.addView(txt(new SimpleDateFormat("EEEE، d MMMM yyyy",new Locale("fa")).format(c.getTime()),16,text(),true));
        page.addView(txt("تقویم روزانهٔ فارسی — نمایش تاریخ امروز دستگاه",13,text(),false));addBack();
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