package app.vit.imgtextsteganosoftware;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import app.vit.imgtextsteganosoftware.activities.decrypt.DecryptActivity;
import app.vit.imgtextsteganosoftware.activities.encrypt.EncryptActivity;
import app.vit.imgtextsteganosoftware.activities.encrypt.EncryptImageActivity;
import app.vit.imgtextsteganosoftware.utils.ThemeHelper;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

  LinearLayout encrypt,decrypt,encryptImg;

/*  @OnClick({R.id.bAMEncrypt, R.id.bAMDecrypt})
  public void onButtonClick(View view) {
    if(view.getId() == R.id.bAMEncrypt) {
      Intent intent = new Intent(MainActivity.this, EncryptActivity.class);
      startActivity(intent);
    } else if(view.getId() == R.id.bAMDecrypt) {
      Intent intent = new Intent(MainActivity.this, DecryptActivity.class);
      startActivity(intent);
    }
  }*/

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ThemeHelper.applySavedMode(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);

    encrypt =findViewById(R.id.encodeButton);
    decrypt =findViewById(R.id.decodeButton);
    encryptImg =findViewById(R.id.encodeImageButton);


    encryptImg.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, EncryptImageActivity.class);
        startActivity(intent);
      }
    });

    encrypt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, EncryptActivity.class);
        startActivity(intent);
      }
    });
    decrypt.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, DecryptActivity.class);
        startActivity(intent);
      }
    });

    //initToolbar();
  }

/*  public void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(false);
      actionBar.setTitle("Crypto Messenger");
    }
  }*/
public boolean onCreateOptionsMenu(Menu menu) {
  getMenuInflater().inflate(R.menu.menu, menu);
  return true;
}

  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_theme) {
      showThemeChooser();
      return true;
    } else if (id == R.id.action_help) {
      Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
      i.putExtra("check", "true");
      startActivity(i);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showThemeChooser() {
    final String[] options = new String[]{
      getString(R.string.theme_follow_system),
      getString(R.string.theme_light),
      getString(R.string.theme_dark)
    };

    int selected = 0;
    int mode = ThemeHelper.getMode(this);
    if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
      selected = 1;
    } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
      selected = 2;
    }

    new MaterialAlertDialogBuilder(this)
      .setTitle(R.string.theme_picker_title)
      .setSingleChoiceItems(options, selected, (dialog, which) -> {
        switch (which) {
          case 0:
            ThemeHelper.setMode(this, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            break;
          case 1:
            ThemeHelper.setMode(this, AppCompatDelegate.MODE_NIGHT_NO);
            break;
          case 2:
            ThemeHelper.setMode(this, AppCompatDelegate.MODE_NIGHT_YES);
            break;
        }
        dialog.dismiss();
      })
      .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
      .show();
  }
}
