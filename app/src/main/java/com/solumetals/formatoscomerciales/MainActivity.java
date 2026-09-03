package com.solumetals.formatoscomerciales;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    final int TEAL=Color.rgb(0,168,150), NAVY=Color.rgb(11,23,32), PALE=Color.rgb(239,248,247);
    android.content.SharedPreferences prefs; LinearLayout root, form; ScrollView scroll; FrameLayout screen;
    ImageView logo; Uri logoUri; String currentType=""; EditText focused; int textColor=Color.DKGRAY; float textSize=16; int fontStyle=Typeface.NORMAL;
    final ArrayList<EditText> descriptions=new ArrayList<>(), quantities=new ArrayList<>(), prices=new ArrayList<>(), amounts=new ArrayList<>();
    EditText subtotalField,discountField,taxField,totalField;
    EditText clientField,phoneField,rucField,contentField,conditionsField;ImageView letterheadPreview;
    boolean darkMode=false,documentScreen=false;
    int selectedTemplate=0;
    final String[] TYPES={"Recibo","Factura","Cotización","Membrete","Notas","Tarjeta de presentación"};
    final String[] TEMPLATE_NAMES={"Corporativo","Minimalista","Ejecutivo","Creativo","Elegante","Técnico"};
    final int[] TEMPLATE_COLORS={Color.rgb(19,51,75),Color.rgb(0,150,136),Color.rgb(34,42,57),Color.rgb(232,72,111),Color.rgb(119,92,153),Color.rgb(30,119,180)};

    @Override public void onCreate(Bundle b){
        super.onCreate(b);WindowCompat.setDecorFitsSystemWindows(getWindow(),false);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);prefs=getSharedPreferences("formatos",MODE_PRIVATE);darkMode=prefs.getBoolean("dark_mode",false);
        if(Build.VERSION.SDK_INT<29 && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=getPackageManager().PERMISSION_GRANTED)
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},44);
        showHome();
    }
    int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    void showScreen(){
        screen=new FrameLayout(this);scroll.setFillViewport(true);screen.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        if(documentScreen){Button actions=new Button(this);actions.setText("⋮");actions.setTextSize(25);actions.setTextColor(Color.WHITE);actions.setGravity(Gravity.CENTER);actions.setPadding(0,0,0,dp(3));actions.setMinWidth(0);actions.setMinHeight(0);actions.setBackground(box(Color.rgb(255,109,0),dp(24)));actions.setElevation(dp(8));
        FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.BOTTOM|Gravity.END);fp.setMargins(0,0,dp(16),dp(16));screen.addView(actions,fp);actions.setOnClickListener(v->showActionsMenu(actions));}
        ViewCompat.setOnApplyWindowInsetsListener(screen,(v,insets)->{androidx.core.graphics.Insets safe=insets.getInsets(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.displayCutout());androidx.core.graphics.Insets keyboard=insets.getInsets(WindowInsetsCompat.Type.ime());int bottom=Math.max(safe.bottom,keyboard.bottom);v.setPadding(safe.left,safe.top+dp(12),safe.right,bottom+dp(6));if(keyboard.bottom>0&&focused!=null)revealField(focused);return insets;});
        ViewCompat.requestApplyInsets(screen);
        setContentView(screen);
    }
    void showActionsMenu(View anchor){
        PopupMenu menu=new PopupMenu(this,anchor);menu.getMenu().add("Limpiar");menu.getMenu().add("Inicio");menu.getMenu().add("Salir");
        menu.setOnMenuItemClickListener(item->{String action=item.getTitle().toString();if(action.equals("Limpiar"))confirmClear();else if(action.equals("Inicio"))showHome();else finishAffinity();return true;});menu.show();
    }
    void confirmClear(){
        new AlertDialog.Builder(this).setTitle("Limpiar texto").setMessage("¿Deseas borrar los campos visibles de este formato?").setNegativeButton("Cancelar",null).setPositiveButton("Borrar",(d,w)->clearVisibleFields()).show();
    }
    void clearVisibleFields(){
        if(root==null)return;clearEdits(root);recalculate();Toast.makeText(this,"Campos limpios",Toast.LENGTH_SHORT).show();
    }
    void clearEdits(View view){if(view instanceof EditText){EditText e=(EditText)view;if(e.isEnabled())e.setText("");return;}if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++)clearEdits(g.getChildAt(i));}}
    TextView text(String s,int sp,int color){ TextView v=new TextView(this); v.setText(s); v.setTextSize(sp); v.setTextColor(color); v.setPadding(12,12,12,12); return v; }
    GradientDrawable box(int color,float radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
    GradientDrawable bordered(int color,int stroke,float radius){GradientDrawable g=box(color,radius);g.setStroke(2,stroke);return g;}
    Button button(String s){ Button b=new Button(this); b.setText(s); b.setTextColor(Color.WHITE); b.setBackground(box(TEAL,22)); b.setAllCaps(false); b.setPadding(16,14,16,14); b.setElevation(3); return b; }
    LinearLayout base(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(24,24,24,24); l.setBackgroundColor(darkMode?Color.rgb(18,23,28):PALE); return l; }
    void addSpace(int h){ Space s=new Space(this); root.addView(s,new LinearLayout.LayoutParams(1,h)); }

    void showHome(){
        documentScreen=false;
        root=base(); scroll=new ScrollView(this); scroll.addView(root); showScreen();
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(16),dp(14),dp(16),dp(14));header.setBackground(box(NAVY,30));
        ImageView appLogo=new ImageView(this);appLogo.setImageResource(com.solumetals.formatoscomerciales.R.drawable.app_icon);appLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);header.addView(appLogo,new LinearLayout.LayoutParams(dp(76),dp(76)));
        TextView title=text("FORMATOS\nCOMERCIALES",26,Color.WHITE);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setGravity(Gravity.CENTER);header.addView(title,new LinearLayout.LayoutParams(0,-2,1));root.addView(header);
        TextView sub=text("Tu papelería profesional, lista para crear",16,Color.DKGRAY); sub.setGravity(Gravity.CENTER); root.addView(sub); addSpace(18);
        Button profile=button("⚙  Configurar mi negocio y logo"); root.addView(profile); profile.setOnClickListener(v->showProfile()); addSpace(20);
        String[] icons={"▣  ","▤  ","◇  ","▱  ","✎  ","▰  "};int ix=0;
        for(String t:TYPES){ Button b=button(icons[ix++]+t);b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setTextColor(darkMode?Color.WHITE:NAVY);b.setBackground(bordered(darkMode?Color.rgb(38,45,52):Color.WHITE,darkMode?Color.rgb(75,85,94):Color.rgb(210,224,228),24)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,120); p.setMargins(0,8,0,8); root.addView(b,p); b.setTextSize(18); b.setOnClickListener(v->openDocument(t)); }
        TextView foot=text("Los documentos se guardan en Descargas/FORMATOS COMERCIALES",13,Color.GRAY); foot.setGravity(Gravity.CENTER); root.addView(foot);
    }

    void showThemeMenu(View anchor){PopupMenu m=new PopupMenu(this,anchor);m.getMenu().add("Modo normal");m.getMenu().add("Modo oscuro");m.setOnMenuItemClickListener(item->{darkMode=item.getTitle().toString().contains("oscuro");prefs.edit().putBoolean("dark_mode",darkMode).apply();showHome();return true;});m.show();}

    void showTemplates(String type){
        documentScreen=false;
        root=base();scroll=new ScrollView(this);scroll.addView(root);showScreen();Button back=button("‹ Volver");root.addView(back);back.setOnClickListener(v->showHome());
        TextView h=text("Elige un diseño",28,NAVY);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(h);root.addView(text("Vista previa de "+type+" · podrás cambiarla después",15,Color.DKGRAY));
        for(int i=0;i<TEMPLATE_NAMES.length;i++){final int pick=i;LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.HORIZONTAL);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(16,16,16,16);card.setBackground(bordered(Color.WHITE,Color.rgb(210,222,228),24));TemplatePreview preview=new TemplatePreview(this,i,type);card.addView(preview,new LinearLayout.LayoutParams(190,250));LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);TextView name=text(TEMPLATE_NAMES[i],20,NAVY);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);words.addView(name);words.addView(text("Diseño profesional listo para personalizar",13,Color.GRAY));Button use=button("Usar este diseño");words.addView(use);card.addView(words,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,10,0,10);root.addView(card,cp);View.OnClickListener choose=v->{selectedTemplate=pick;prefs.edit().putInt("template_"+type,pick).apply();openDocument(type);};card.setOnClickListener(choose);use.setOnClickListener(choose);}
    }

    class TemplatePreview extends View{
        Paint p=new Paint(1);int style;String kind;TemplatePreview(Context c,int s,String k){super(c);style=s;kind=k;setLayerType(View.LAYER_TYPE_SOFTWARE,null);setElevation(4);}
        protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();p.setColor(Color.WHITE);c.drawRoundRect(4,4,w-4,h-4,12,12,p);int col=TEMPLATE_COLORS[style];p.setColor(col);if(style==1)c.drawRect(4,4,18,h-4,p);else if(style==2)c.drawRect(4,4,w-4,58,p);else if(style==3){c.drawCircle(w-18,18,55,p);c.drawRect(4,4,16,h-4,p);}else if(style==4)c.drawRoundRect(12,12,w-12,64,10,10,p);else if(style==5){c.drawRect(4,4,w-4,38,p);c.drawRect(4,h-26,w-4,h-4,p);}else c.drawRoundRect(4,4,w-4,68,12,12,p);p.setColor(style==1?col:Color.WHITE);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(11);c.drawText("SU NEGOCIO",style==1?28:18,30,p);p.setTextSize(7);c.drawText(kind.toUpperCase(Locale.getDefault()),style==1?28:18,48,p);p.setColor(Color.rgb(210,218,222));for(int y=88;y<h-34;y+=25){c.drawRoundRect(20,y,w-20,y+10,4,4,p);}p.setColor(col);c.drawRoundRect(w*.55f,h-46,w-20,h-28,5,5,p);}
    }

    void showProfile(){
        documentScreen=false;
        root=base(); scroll=new ScrollView(this); scroll.addView(root); showScreen();
        Button back=button("‹ Volver"); root.addView(back); back.setOnClickListener(v->showHome());
        TextView h=text("Identidad del negocio",26,NAVY); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); root.addView(h);
        TextView logoHelp=text("LOGO DEL NEGOCIO\nToca aquí para cargar PNG o JPG",15,Color.GRAY);logoHelp.setGravity(Gravity.CENTER);GradientDrawable upload=bordered(Color.WHITE,TEAL,24);upload.setStroke(3,TEAL,12,8);logoHelp.setBackground(upload);root.addView(logoHelp,new LinearLayout.LayoutParams(-1,120));
        logo=new ImageView(this); logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE); logo.setBackground(box(Color.WHITE,22)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,250);lp.setMargins(0,10,0,10);root.addView(logo,lp); loadLogo();
        View.OnClickListener picker=v->{ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,9); };logoHelp.setOnClickListener(picker);logo.setOnClickListener(picker);
        Button pick=button("Cargar o cambiar logo"); root.addView(pick); pick.setOnClickListener(picker);
        String[][] fs={{"business","Nombre del comercio"},{"owner","Nombre del propietario o representante"},{"address","Dirección"},{"phone","Teléfono"},{"email","Correo electrónico"},{"tax","RUC / identificación fiscal"},{"web","Sitio web o red social"}};
        for(String[] f:fs) root.addView(field(f[1],prefs.getString(f[0],""),f[0],false));
        Button save=button("Guardar información"); root.addView(save); save.setOnClickListener(v->{ Toast.makeText(this,"Información guardada",Toast.LENGTH_SHORT).show(); showHome(); });
    }

    EditText field(String hint,String value,String key,boolean multiline){
        EditText e=new EditText(this); e.setHint(hint); e.setText(value); e.setTextSize(textSize); e.setTextColor(textColor); e.setBackground(bordered(Color.WHITE,Color.rgb(165,183,190),18)); e.setPadding(20,16,20,16); e.setSingleLine(!multiline); if(multiline){e.setMinLines(4);e.setGravity(Gravity.TOP);e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);}
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,7,0,7); e.setLayoutParams(p); e.setOnFocusChangeListener((v,has)->{if(has){focused=e;revealField(e);}});
        if(key!=null) e.setOnEditorActionListener((v,a,event)->{ prefs.edit().putString(key,e.getText().toString()).apply(); return false;});
        if(key!=null) e.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){} public void onTextChanged(CharSequence s,int a,int b,int c){prefs.edit().putString(key,s.toString()).apply();}public void afterTextChanged(android.text.Editable x){}});
        return e;
    }

    void revealField(EditText e){if(scroll==null||screen==null||e==null)return;scroll.postDelayed(()->{int[] pos=new int[2];e.getLocationOnScreen(pos);int visibleBottom=screen.getHeight()-screen.getPaddingBottom()-dp(24);int fieldBottom=pos[1]+e.getHeight();if(fieldBottom>visibleBottom)scroll.smoothScrollBy(0,fieldBottom-visibleBottom+dp(32));else{Rect area=new Rect(0,0,e.getWidth(),e.getHeight()+dp(80));scroll.requestChildRectangleOnScreen(e,area,true);}},260);}

    void openDocument(String type){
        documentScreen=true;
        currentType=type;descriptions.clear();quantities.clear();prices.clear();amounts.clear();subtotalField=discountField=taxField=totalField=null;clientField=phoneField=rucField=contentField=conditionsField=null; root=base(); scroll=new ScrollView(this); scroll.addView(root); showScreen();
        if(!type.equals("Tarjeta de presentación"))addBusinessHeader();
        if(!type.equals("Membrete")&&!type.equals("Tarjeta de presentación")){TextView h=text(type.toUpperCase(Locale.getDefault()),24,NAVY); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); root.addView(h);}
        String date=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());String hour=new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(new Date()); int n=prefs.getInt("seq_"+type,1);
        if(!type.equals("Tarjeta de presentación")){String metaText=type.equals("Membrete")?"FECHA "+date:"N.º "+String.format(Locale.getDefault(),"%05d",n)+"     FECHA "+date+(type.equals("Recibo")?"     HORA "+hour:"");TextView meta=text(metaText,15,type.equals("Membrete")?NAVY:Color.RED);meta.setBackground(box(Color.rgb(228,240,245),18));meta.setPadding(20,22,20,22); root.addView(meta);}
        form=new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL);if(type.equals("Factura")||type.equals("Cotización")||type.equals("Recibo")){form.setPadding(dp(16),dp(16),dp(16),dp(18));form.setBackground(bordered(Color.WHITE,Color.rgb(28,62,86),dp(18)));LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2);fp.setMargins(0,dp(10),0,dp(16));root.addView(form,fp);}else root.addView(form);
        buildFields(type);
        if(!type.equals("Tarjeta de presentación"))addToolbar();
        Button pdf=button("Guardar PDF"); root.addView(pdf); pdf.setOnClickListener(v->createPdf(false));
        Button share=button("Compartir PDF"); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,12,0,20);root.addView(share,sp);share.setOnClickListener(v->createPdf(true));
    }

    void addBusinessHeader(){
        int accent=TEMPLATE_COLORS[Math.max(0,Math.min(selectedTemplate,TEMPLATE_COLORS.length-1))];LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.HORIZONTAL);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(22,22,22,22);card.setBackground(box(accent,28));
        ImageView mark=new ImageView(this);mark.setScaleType(ImageView.ScaleType.CENTER_INSIDE);Bitmap b=getLogo();if(b!=null)mark.setImageBitmap(b);else mark.setImageResource(android.R.drawable.ic_menu_gallery);card.addView(mark,new LinearLayout.LayoutParams(120,120));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);TextView name=text(prefs.getString("business","NOMBRE DEL NEGOCIO"),22,Color.WHITE);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);info.addView(name);String owner=safe(prefs.getString("owner",""));String phone=safe(prefs.getString("phone",""));if(!owner.isEmpty()||!phone.isEmpty())info.addView(text(owner+(owner.isEmpty()||phone.isEmpty()?"":" · ")+(phone.isEmpty()?"":"Cel. "+phone),12,Color.WHITE));info.addView(text(prefs.getString("address","Configura los datos del negocio"),12,Color.WHITE));card.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,14,0,20);root.addView(card,cp);
    }

    void addF(String hint,boolean multi){ form.addView(field(hint,"",null,multi)); }
    void buildFields(String t){
        if(t.equals("Recibo")){addF("Nombre del cliente",false);addF("Recibo por / motivo o servicio",false);addF("Concepto / trabajo realizado",true);EditText total=moneyField("TOTAL RECIBIDO B/.",true);total.setText("0.00");addF("Método de pago",false);addF("Recibido por / firma",false);}
        else if(t.equals("Factura")||t.equals("Cotización")){clientField=field("Cliente","",null,false);form.addView(clientField);LinearLayout row=new LinearLayout(this);phoneField=field("Tel.","",null,false);rucField=field("RUC","",null,false);row.addView(phoneField,new LinearLayout.LayoutParams(0,-2,1));row.addView(rucField,new LinearLayout.LayoutParams(0,-2,1));form.addView(row);for(int i=1;i<=4;i++)addItem(i);subtotalField=moneyField("Subtotal automático",false);taxField=moneyField("ITBMS %",true);totalField=moneyField("TOTAL $",false);taxField.setText("7");watchMath(taxField);conditionsField=field("Condiciones y observaciones","",null,true);form.addView(conditionsField);if(t.equals("Factura")){TextView sign=text("____________________________\nRECIBÍ CONFORME",14,NAVY);sign.setGravity(Gravity.CENTER);form.addView(sign);}recalculate();}
        else if(t.equals("Membrete")){contentField=field("Escriba aquí la correspondencia","",null,true);contentField.setMinLines(20);form.addView(contentField);}
        else if(t.equals("Notas")){addF("Título de la nota",false);addF("Contenido",true);addF("Nombre o firma",false);}
        else {CardPreview preview=new CardPreview(this);form.addView(preview,new LinearLayout.LayoutParams(-1,dp(245)));TextView help=text("La tarjeta usa automáticamente el logo y los datos configurados al inicio.",13,Color.GRAY);help.setGravity(Gravity.CENTER);form.addView(help);}
    }

    class CardPreview extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);CardPreview(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setElevation(dp(4));}
        protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();p.setColor(Color.WHITE);c.drawRoundRect(8,8,w-8,h-8,24,24,p);p.setColor(NAVY);c.drawRoundRect(8,8,w*.34f,h-8,24,24,p);p.setColor(TEAL);c.drawCircle(w*.28f,h*1.02f,w*.24f,p);Bitmap bm=getLogo();if(bm!=null)c.drawBitmap(bm,null,new RectF(w*.07f,h*.18f,w*.28f,h*.53f),p);p.setColor(NAVY);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(dp(18));c.drawText(safe(prefs.getString("business","NOMBRE DEL NEGOCIO")),w*.39f,h*.23f,p);p.setColor(Color.DKGRAY);p.setTypeface(Typeface.DEFAULT);p.setTextSize(dp(13));c.drawText(safe(prefs.getString("owner","Julio Fernández")),w*.39f,h*.35f,p);p.setTextSize(dp(11));float y=h*.51f;String[] lines={"Tel. "+safe(prefs.getString("phone","")),safe(prefs.getString("email","")),safe(prefs.getString("web","")),safe(prefs.getString("address",""))};for(String line:lines){if(!line.trim().isEmpty()){c.drawText(line,w*.39f,y,p);y+=dp(22);}}}
    }

    EditText moneyField(String hint,boolean editable){EditText e=field(hint,"",null,false);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setEnabled(editable);if(!editable){e.setTextColor(NAVY);e.setTypeface(Typeface.DEFAULT,Typeface.BOLD);e.setBackground(box(Color.rgb(230,241,245),18));}form.addView(e);return e;}
    void addItem(int n){TextView label=text("RENGLÓN "+n+"   Cant. · Descripción · V/Unit. · V/Total",13,NAVY);label.setTypeface(Typeface.DEFAULT,Typeface.BOLD);form.addView(label);EditText d=field("Descripción","",null,false);form.addView(d);EditText q=moneyField("Cant.",true),p=moneyField("V/Unit.",true),a=moneyField("V/Total automático",false);descriptions.add(d);quantities.add(q);prices.add(p);amounts.add(a);watchMath(q);watchMath(p);}
    void watchMath(EditText e){e.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){recalculate();}public void afterTextChanged(android.text.Editable x){}});}
    double number(EditText e){try{return Double.parseDouble(e.getText().toString().replace(",","."));}catch(Exception x){return 0;}}
    void recalculate(){if(subtotalField==null)return;double sub=0;for(int i=0;i<quantities.size();i++){double value=number(quantities.get(i))*number(prices.get(i));sub+=value;amounts.get(i).setText(String.format(Locale.US,"%.2f",value));}double taxPct=number(taxField),tax=sub*taxPct/100d,total=sub+tax;subtotalField.setText(String.format(Locale.US,"%.2f",sub));totalField.setText(String.format(Locale.US,"%.2f",total));}

    void addToolbar(){
        TextView lab=text("Herramientas de texto",14,NAVY); lab.setTypeface(Typeface.DEFAULT,Typeface.BOLD); root.addView(lab);
        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER); root.addView(bar);
        String[] names={"A−","A+","B","Color"}; for(String s:names){Button b=button(s);bar.addView(b,new LinearLayout.LayoutParams(0,100,1));
            b.setOnClickListener(v->{ if(focused==null){Toast.makeText(this,"Toca primero un campo",Toast.LENGTH_SHORT).show();return;} String x=((Button)v).getText().toString(); if(x.equals("A−")){textSize=Math.max(10,focused.getTextSize()/getResources().getDisplayMetrics().scaledDensity-2);focused.setTextSize(textSize);} else if(x.equals("A+")){textSize=Math.min(34,focused.getTextSize()/getResources().getDisplayMetrics().scaledDensity+2);focused.setTextSize(textSize);} else if(x.equals("B")){fontStyle=fontStyle==Typeface.BOLD?Typeface.NORMAL:Typeface.BOLD;focused.setTypeface(Typeface.DEFAULT,fontStyle);} else chooseColor(); });}
    }
    void chooseColor(){ final int[] cs={Color.BLACK,Color.DKGRAY,Color.rgb(0,90,170),Color.rgb(0,130,95),Color.rgb(170,30,45)};String[] ns={"Negro","Gris","Azul","Verde","Rojo"};new AlertDialog.Builder(this).setTitle("Color del texto").setItems(ns,(d,w)->{textColor=cs[w];focused.setTextColor(textColor);}).show(); }

    void loadLogo(){ String s=prefs.getString("logo",""); if(!s.isEmpty()){try{logoUri=Uri.parse(s);logo.setImageURI(logoUri);}catch(Exception ignored){}} else logo.setImageResource(android.R.drawable.ic_menu_gallery); }
    void loadLetterhead(){String s=prefs.getString("letterhead","");if(!s.isEmpty()&&letterheadPreview!=null)try{letterheadPreview.setImageURI(Uri.parse(s));}catch(Exception ignored){}}
    @Override protected void onActivityResult(int r,int c,Intent data){super.onActivityResult(r,c,data);if((r==9||r==10)&&c==RESULT_OK&&data!=null){Uri u=data.getData();try{getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}if(r==9){logoUri=u;prefs.edit().putString("logo",u.toString()).apply();if(logo!=null)logo.setImageURI(u);}else{prefs.edit().putString("letterhead",u.toString()).apply();if(letterheadPreview!=null)letterheadPreview.setImageURI(u);}}}

    Bitmap getLogo(){ String s=prefs.getString("logo","");if(s.isEmpty())return null;try(InputStream in=getContentResolver().openInputStream(Uri.parse(s))){return BitmapFactory.decodeStream(in);}catch(Exception e){return null;} }
    Bitmap getLetterhead(){String s=prefs.getString("letterhead","");if(s.isEmpty())return null;try(InputStream in=getContentResolver().openInputStream(Uri.parse(s))){return BitmapFactory.decodeStream(in);}catch(Exception e){return null;}}
    String safe(String s){return s==null?"":s.trim();}
    void createPdf(boolean share){
        boolean half=currentType.equals("Recibo")||currentType.equals("Notas");boolean card=currentType.equals("Tarjeta de presentación");int W=card?360:(half?396:612),H=card?216:(half?612:792);PdfDocument doc=new PdfDocument();PdfDocument.Page page=doc.startPage(new PdfDocument.PageInfo.Builder(W,H,1).create());Canvas c=page.getCanvas();Paint p=new Paint(1);p.setColor(Color.WHITE);c.drawRect(0,0,W,H,p);int accent=NAVY;int y=34;
        if(currentType.equals("Membrete")){y=drawPdfBusinessHeader(c,p,34,W,false);String date=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());p.setColor(Color.DKGRAY);p.setTextSize(10);c.drawText("Fecha: "+date,42,y+18,p);y+=54;p.setColor(Color.DKGRAY);p.setTextSize(13);p.setTypeface(Typeface.DEFAULT);drawWrapped(c,p,contentField==null?"":contentField.getText().toString(),42,y,W-84);p.setColor(accent);c.drawRect(36,H-38,W-36,H-36,p);
        }else if(card){drawBusinessCard(c,p,W,H);
        }else{
            y=drawPdfBusinessHeader(c,p,30,W,currentType.equals("Recibo"));String date=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault()).format(new Date());String hour=new SimpleDateFormat("hh:mm a",Locale.getDefault()).format(new Date());int n=prefs.getInt("seq_"+currentType,1);
            p.setColor(Color.rgb(232,243,248));c.drawRoundRect(28,y,W-28,y+46,10,10,p);p.setColor(NAVY);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(currentType.equals("Recibo")?13:16);c.drawText(currentType.toUpperCase(Locale.getDefault())+(currentType.equals("Recibo")?" NÚMERO":""),42,y+29,p);p.setColor(Color.RED);p.setTextSize(16);c.drawText(String.format(Locale.getDefault(),"%05d",n),W-105,y+29,p);y+=72;p.setColor(Color.DKGRAY);p.setTypeface(Typeface.DEFAULT);p.setTextSize(9);c.drawText("FECHA  "+date,36,y,p);if(currentType.equals("Recibo"))c.drawText("HORA  "+hour,W-145,y,p);y+=30;
            if(currentType.equals("Factura")||currentType.equals("Cotización")){y=drawInvoice(c,p,y,W);}else if(currentType.equals("Recibo")){y=drawReceipt(c,p,y,W);}else{for(int i=0;i<form.getChildCount();i++){View child=form.getChildAt(i);if(!(child instanceof EditText))continue;EditText e=(EditText)child;String label=e.getHint()==null?"":e.getHint().toString();String val=e.getText().toString();p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(NAVY);p.setTextSize(9);c.drawText(label,30,y,p);y+=13;p.setTypeface(Typeface.DEFAULT);p.setColor(Color.DKGRAY);p.setTextSize(11);y=drawWrapped(c,p,val,30,y,W-60);p.setColor(Color.LTGRAY);c.drawLine(30,y+2,W-30,y+2,p);y+=18;if(y>H-55)break;}}
        }
        doc.finishPage(page);int n=prefs.getInt("seq_"+currentType,1);
        String filename=currentType.replace(" ","-")+"-"+String.format(Locale.getDefault(),"%05d",n)+".pdf";try{Uri out=savePdf(doc,filename);prefs.edit().putInt("seq_"+currentType,n+1).apply();Toast.makeText(this,"Guardado en Descargas/FORMATOS COMERCIALES",Toast.LENGTH_LONG).show();if(share&&out!=null){Intent i=new Intent(Intent.ACTION_SEND);i.setType("application/pdf");i.putExtra(Intent.EXTRA_STREAM,out);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(i,"Compartir PDF"));}}catch(Exception e){Toast.makeText(this,"No se pudo crear: "+e.getMessage(),Toast.LENGTH_LONG).show();}finally{doc.close();}
    }
    int drawPdfBusinessHeader(Canvas c,Paint p,int y,int W,boolean compact){Bitmap bm=getLogo();if(bm!=null){float sc=Math.min((compact?70f:95f)/bm.getWidth(),(compact?48f:62f)/bm.getHeight());c.drawBitmap(bm,null,new RectF(30,y,30+bm.getWidth()*sc,y+bm.getHeight()*sc),p);}float x=compact?110:150;p.setColor(NAVY);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(compact?15:19);c.drawText(safe(prefs.getString("business","Mi negocio")),x,y+20,p);p.setTypeface(Typeface.DEFAULT);p.setTextSize(compact?8:10);String owner=safe(prefs.getString("owner","")),phone=safe(prefs.getString("phone",""));c.drawText(owner+(owner.isEmpty()||phone.isEmpty()?"":" · ")+(phone.isEmpty()?"":"Cel. "+phone),x,y+38,p);c.drawText(safe(prefs.getString("address","")),x,y+53,p);return y+(compact?72:86);}
    void drawBusinessCard(Canvas c,Paint p,int W,int H){p.setColor(Color.WHITE);c.drawRect(0,0,W,H,p);p.setColor(NAVY);c.drawRoundRect(0,0,W*.34f,H,0,0,p);p.setColor(TEAL);c.drawCircle(W*.27f,H*1.02f,W*.25f,p);Bitmap bm=getLogo();if(bm!=null)c.drawBitmap(bm,null,new RectF(25,35,105,105),p);float x=142;p.setColor(NAVY);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(15);c.drawText(safe(prefs.getString("business","NOMBRE DEL NEGOCIO")),x,45,p);p.setColor(Color.DKGRAY);p.setTypeface(Typeface.DEFAULT);p.setTextSize(10);c.drawText(safe(prefs.getString("owner","Julio Fernández")),x,64,p);p.setTextSize(8);int y=91;String[] lines={"Tel. "+safe(prefs.getString("phone","")),safe(prefs.getString("email","")),safe(prefs.getString("web","")),safe(prefs.getString("address",""))};for(String line:lines){if(!line.trim().isEmpty()){c.drawText(line,x,y,p);y+=19;}}}
    int drawReceipt(Canvas c,Paint p,int y,int W){ArrayList<EditText> fields=new ArrayList<>();for(int i=0;i<form.getChildCount();i++)if(form.getChildAt(i) instanceof EditText)fields.add((EditText)form.getChildAt(i));for(int i=0;i<fields.size();i++){EditText e=fields.get(i);String label=e.getHint()==null?"":e.getHint().toString();String val=e.getText().toString();if(i==3){p.setColor(Color.rgb(232,243,248));c.drawRoundRect(38,y-15,W-38,y+38,9,9,p);p.setColor(NAVY);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(10);c.drawText("TOTAL RECIBIDO",52,y+15,p);p.setTextSize(16);c.drawText("B/. "+val,W-130,y+16,p);y+=72;continue;}p.setColor(NAVY);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(9);c.drawText(label.toUpperCase(Locale.getDefault()),42,y,p);y+=17;p.setColor(Color.DKGRAY);p.setTypeface(Typeface.DEFAULT);p.setTextSize(11);y=drawWrapped(c,p,val,42,y,W-84);p.setColor(Color.rgb(155,176,186));c.drawLine(42,y+3,W-42,y+3,p);y+=28;}p.setColor(Color.rgb(155,176,186));c.drawLine(105,y+10,W-105,y+10,p);p.setColor(Color.GRAY);p.setTextSize(8);p.setTextAlign(Paint.Align.CENTER);c.drawText("Firma autorizada",W/2f,y+28,p);p.setTextAlign(Paint.Align.LEFT);return y+34;}
    int drawInvoice(Canvas c,Paint p,int y,int W){p.setColor(NAVY);p.setTextSize(10);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText("CLIENTE",34,y,p);c.drawText(safe(clientField==null?"":clientField.getText().toString()),105,y,p);c.drawText("TEL.",330,y,p);c.drawText(safe(phoneField==null?"":phoneField.getText().toString()),365,y,p);y+=22;c.drawText("RUC",34,y,p);c.drawText(safe(rucField==null?"":rucField.getText().toString()),72,y,p);y+=20;float[] x={30,86,360,448,W-30};p.setColor(NAVY);c.drawRect(x[0],y-14,x[4],y+8,p);p.setColor(Color.WHITE);p.setTextSize(9);c.drawText("CANT.",35,y,p);c.drawText("DESCRIPCIÓN",92,y,p);c.drawText("V/UNIT.",368,y,p);c.drawText("V/TOTAL",456,y,p);y+=28;p.setColor(Color.DKGRAY);p.setTypeface(Typeface.DEFAULT);for(int i=0;i<descriptions.size();i++){c.drawText(safe(quantities.get(i).getText().toString()),40,y,p);c.drawText(safe(descriptions.get(i).getText().toString()),92,y,p);c.drawText(safe(prices.get(i).getText().toString()),368,y,p);c.drawText(safe(amounts.get(i).getText().toString()),456,y,p);p.setColor(Color.LTGRAY);c.drawLine(30,y+7,W-30,y+7,p);p.setColor(Color.DKGRAY);y+=24;}double sub=number(subtotalField),rate=number(taxField),tax=sub*rate/100d,total=number(totalField);int tx=W-215;p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(NAVY);c.drawText("Subtotal",tx,y,p);c.drawText(String.format(Locale.US,"B/. %.2f",sub),W-105,y,p);y+=20;c.drawText("ITBMS "+String.format(Locale.US,"%.2f",rate)+"%",tx,y,p);c.drawText(String.format(Locale.US,"B/. %.2f",tax),W-105,y,p);y+=22;p.setColor(NAVY);c.drawRoundRect(tx-8,y-15,W-28,y+8,7,7,p);p.setColor(Color.WHITE);c.drawText("TOTAL $",tx,y,p);c.drawText(String.format(Locale.US,"B/. %.2f",total),W-105,y,p);y+=38;if(conditionsField!=null){p.setColor(Color.DKGRAY);p.setTypeface(Typeface.DEFAULT);p.setTextSize(9);y=drawWrapped(c,p,conditionsField.getText().toString(),34,y,W-68);}if(currentType.equals("Factura")){y+=28;p.setColor(Color.DKGRAY);c.drawLine(34,y,220,y,p);p.setTextSize(9);c.drawText("RECIBÍ CONFORME",75,y+14,p);}return y;}
    int drawWrapped(Canvas c,Paint p,String s,float x,int y,float width){if(s.isEmpty()){return y+8;}String[] paras=s.split("\\n",-1);for(String para:paras){String line="";for(String word:para.split(" ")){String test=line.isEmpty()?word:line+" "+word;if(p.measureText(test)>width&&!line.isEmpty()){c.drawText(line,x,y,p);y+=18;line=word;}else line=test;}c.drawText(line,x,y,p);y+=18;}return y;}
    Uri savePdf(PdfDocument doc,String name)throws Exception{
        if(Build.VERSION.SDK_INT>=29){ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,name);v.put(MediaStore.Downloads.MIME_TYPE,"application/pdf");v.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/FORMATOS COMERCIALES");Uri u=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);if(u==null)throw new IOException("No se pudo abrir Descargas");try(OutputStream o=getContentResolver().openOutputStream(u)){doc.writeTo(o);}return u;}
        File dir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"FORMATOS COMERCIALES");if(!dir.exists()&&!dir.mkdirs())throw new IOException("No se pudo crear la carpeta");File f=new File(dir,name);try(OutputStream o=new FileOutputStream(f)){doc.writeTo(o);}return FileProvider.getUriForFile(this,getPackageName()+".files",f);
    }
}
