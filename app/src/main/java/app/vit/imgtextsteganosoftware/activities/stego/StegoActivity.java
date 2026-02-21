package app.vit.imgtextsteganosoftware.activities.stego;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import java.io.File;
import app.vit.imgtextsteganosoftware.R;
import app.vit.imgtextsteganosoftware.utils.Constants;
import app.vit.imgtextsteganosoftware.utils.StandardMethods;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StegoActivity extends AppCompatActivity implements StegoView {


  @BindView(R.id.ivStegoImage)
  ImageView ivStegoImage;

  @OnClick({R.id.bStegoSave, R.id.bStegoShare})
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.bStegoSave:
        if(!isSaved) {
          isSaved = mPresenter.saveStegoImage(stegoImagePath);
        } else {
          showToast(R.string.image_is_saved);
        }
        break;
      case R.id.bStegoShare:
        shareStegoImage(stegoImagePath);
        break;
    }
  }


  private ProgressDialog progressDialog;
  private StegoPresenter mPresenter;

  private String stegoImagePath = "";
  private boolean isSaved = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stego);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle("Encrypted Image");

    ButterKnife.bind(this);

    //initToolbar();

    Intent intent = getIntent();

    if (intent != null) {
      Bundle bundle = intent.getExtras();
      stegoImagePath = bundle.getString(Constants.EXTRA_STEGO_IMAGE_PATH);
    }

    setStegoImage(stegoImagePath);

    mPresenter = new StegoPresenterImpl(this);

    progressDialog = new ProgressDialog(StegoActivity.this);
    progressDialog.setMessage("Please wait...");
  }

/*  @Override
  public void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Stego Image");
    }
  }*/

  @Override
  public void setStegoImage(String path) {
    showProgressDialog();
    Picasso.get()
      .load(new File(path))
      .fit()
      .placeholder(R.drawable.ic_upload)
      .into(ivStegoImage);
    stopProgressDialog();
  }

  @Override
  public void showToast(int message) {
    StandardMethods.showToast(this, message);
  }

  @Override
  public void showProgressDialog() {
    if (progressDialog != null && !progressDialog.isShowing()) {
      progressDialog.show();
    }
  }

  @Override
  public void stopProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  @Override
  public void shareStegoImage(String path) {
    if(stegoImagePath != null) {
      File file = new File(path);
      Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
      Intent share = new Intent(Intent.ACTION_SEND);
      share.setType("image/png");
      share.putExtra(Intent.EXTRA_STREAM, uri);
      share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      startActivity(Intent.createChooser(share, getString(R.string.b_stego_image_share)));
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if(!isSaved) {
      if(stegoImagePath != null) {
        new File(stegoImagePath).delete();
      }
    }
  }
}
