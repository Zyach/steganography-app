package app.vit.imgtextsteganosoftware.activities.decrypt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import app.vit.imgtextsteganosoftware.R;
import app.vit.imgtextsteganosoftware.utils.Constants;
import app.vit.imgtextsteganosoftware.utils.ImageUtils;
import app.vit.imgtextsteganosoftware.utils.StandardMethods;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DecryptActivity extends AppCompatActivity implements DecryptView {

  @BindView(R.id.ivStegoImage)
  ImageView ivStegoImage;

  @OnClick(R.id.ivStegoImage)
  public void onStegoImageClick() {
    chooseImage();
  }

  @OnClick(R.id.bDecrypt)
  public void onButtonClick() {
    if (isSISelected) {
      mPresenter.decryptMessage();
    } else {
      showToast(R.string.stego_image_not_selected);
    }
  }

  private ProgressDialog progressDialog;
  private DecryptPresenter mPresenter;
  private boolean isSISelected = false;
  private ActivityResultLauncher<PickVisualMediaRequest> stegoPicker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decrypt);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle("Decrypt Image");

    ButterKnife.bind(this);

    progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Please wait...");

    mPresenter = new DecryptPresenterImpl(this);
    initPicker();
    //initToolbar();
  }

  private void initPicker() {
    stegoPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
      if (uri != null) {
        try {
          Bitmap bitmap = ImageUtils.decodeUriToBitmap(this, uri, 1500);
          File file = writeTempFile(bitmap);
          mPresenter.selectImage(file.getAbsolutePath());
        } catch (IOException e) {
          showToast(R.string.compress_error);
        }
      }
    });
  }

  private File writeTempFile(Bitmap bitmap) throws IOException {
    File dir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
    if (dir == null) dir = getCacheDir();
    File file = File.createTempFile("stego_", ".png", dir);
    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
    }
    return file;
  }

/*  @Override
  public void initToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setTitle("Decryption");
    }
  }*/

  @Override
  public void chooseImage() {
    stegoPicker.launch(new PickVisualMediaRequest.Builder()
      .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
      .build());
  }

  @Override
  public void startDecryptResultActivity(String secretMessage, String secretImagePath) {
    Intent intent = new Intent(DecryptActivity.this, DecryptResultActivity.class);

    if (secretMessage != null) {
      intent.putExtra(Constants.EXTRA_SECRET_TEXT_RESULT, secretMessage);
    }

    if (secretImagePath != null) {
      intent.putExtra(Constants.EXTRA_SECRET_IMAGE_RESULT, secretImagePath);
    }

    startActivity(intent);
  }

  public String getPath(Uri uri, AppCompatActivity activity) {
    String[] projection = {MediaStore.MediaColumns.DATA};
    Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
    cursor.moveToFirst();
    return cursor.getString(column_index);
  }

  @Override
  public Bitmap getStegoImage() {
    return ((BitmapDrawable) ivStegoImage.getDrawable()).getBitmap();
  }

  @Override
  public void setStegoImage(File file) {
    showProgressDialog();
    Picasso.with(this)
      .load(file)
      .fit()
      .placeholder(R.drawable.ic_upload)
      .into(ivStegoImage);
    stopProgressDialog();
    isSISelected = true;
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
}
