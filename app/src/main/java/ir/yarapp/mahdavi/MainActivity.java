package ir.yarapp.mahdavi;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final int green = Color.rgb(17, 83, 61);
    private final int pale = Color.rgb(242, 247, 243);
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(green);
        getWindow().setNavigationBarColor(Color.BLACK);
        home();
    }

    private TextView label(String text, int size, int color) {
        TextView v = new TextView(this);
        v.setText(text); v.setTextSize(size); v.setTextColor(color);
        v.setGravity(Gravity.CENTER); v.setPadding(18, 16, 18, 16);
        v.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        return v;
    }

    private Button button(String text, Runnable action) {
        Button b = new Button(this);
        b.setText(text); b.setTextSize(16); b.setAllCaps(false);
        b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void base(String title) {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 18, 18, 24);
        root.setBackgroundColor(pale);
        scroll.addView(root);
        setContentView(scroll);
        TextView heading = label(title, 24, Color.WHITE);
        heading.setBackgroundColor(green);
        root.addView(heading);
    }

    private void addButton(String text, Runnable action) {
        Button b = button(text, action);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 8, 0, 8); root.addView(b, p);
    }

    private void home() {
        base("یار مهدوی");
        root.addView(label("همراهی روزانه با یاد امام زمان (عج)", 17, green));
        addButton("دعاها و زیارات", () -> section("دعاها و زیارات"));
        addButton("روایات", () -> section("روایات"));
        addButton("زندگینامه امام زمان (عج)", () -> section("زندگینامه امام زمان (عج)"));
        addButton("مقالات", () -> section("مقالات"));
        addButton("تنظیمات", () -> section("تنظیمات"));
        root.addView(label("نسخه آزمایشی ۰.۹ — در حال توسعه", 13, Color.DKGRAY));
    }

    private void section(String title) {
        base(title);
        if (title.equals("دعاها و زیارات")) {
            addButton("دعای عهد", () -> detail("دعای عهد"));
            addButton("دعای فرج", () -> detail("دعای فرج"));
            addButton("دعای سلامتی امام زمان", () -> detail("دعای سلامتی امام زمان"));
            root.addView(label("متن کامل دعاها و ترجمه‌ها در مرحله بعد به بانک محتوای تأییدشده افزوده می‌شوند.", 15, Color.DKGRAY));
        } else if (title.equals("تنظیمات")) {
            root.addView(label("انتخاب فونت، اندازه متن، صوت و تنظیمات مطالعه در نسخه‌های بعدی تکمیل می‌شود.", 15, Color.DKGRAY));
            addButton("بازگشت به صفحه اصلی", this::home);
        } else {
            root.addView(label("این بخش در نسخه آزمایشی آماده‌سازی شده و محتوای کامل آن در به‌روزرسانی بعدی افزوده می‌شود.", 16, Color.DKGRAY));
            addButton("بازگشت به صفحه اصلی", this::home);
        }
        addButton("بازگشت", this::home);
    }

    private void detail(String title) {
        base(title);
        root.addView(label("متن عربی و ترجمه فارسی این دعا هنوز به بانک محتوای برنامه اضافه نشده است.", 16, Color.DKGRAY));
        addButton("بازگشت به دعاها", () -> section("دعاها و زیارات"));
    }
}
