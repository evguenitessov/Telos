package ohyeah.ohyeah;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

/**
 * Created by etessov on 03/03/2015.
 */
public class DescripcionActivity extends TabActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.descripcionlugar);

        TabHost tabHost = getTabHost();

        TabHost.TabSpec tabSpec;

        // создаем вкладку и указываем тег
        tabSpec = tabHost.newTabSpec("tag1");
        // название вкладки
        tabSpec.setIndicator("", getResources().getDrawable(R.drawable.iconoprecio));
        // указываем id компонента из FrameLayout, он и станет содержимым
        tabSpec.setContent(R.id.tvTab1);
        // добавляем в корневой элемент
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        // указываем название и картинку
        // в нашем случае вместо картинки идет xml-файл,
        // который определяет картинку по состоянию вкладки
        tabSpec.setIndicator("", getResources().getDrawable(R.drawable.iconodescripcion));
        tabSpec.setContent(R.id.tvTab2);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag3");
        // создаем View из layout-файла
        // и устанавливаем его, как заголовок
        tabSpec.setIndicator("", getResources().getDrawable(R.drawable.iconomapa));
        //tabSpec.setContent(R.id.tvTab3);
        tabSpec.setContent(new Intent(this, PathGoogleMapActivity.class));
        tabHost.addTab(tabSpec);

        // вторая вкладка будет выбрана по умолчанию
        tabHost.setCurrentTabByTag("tag2");

        // обработчик переключения вкладок
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
