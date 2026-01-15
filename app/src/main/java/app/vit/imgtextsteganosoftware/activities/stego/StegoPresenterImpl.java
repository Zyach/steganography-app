package app.vit.imgtextsteganosoftware.activities.stego;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;

import app.vit.imgtextsteganosoftware.R;
import app.vit.imgtextsteganosoftware.utils.ImageUtils;


class StegoPresenterImpl implements StegoPresenter {

  private StegoView mView;

  StegoPresenterImpl(StegoView mView) {
    this.mView = mView;
  }

  @Override
  public boolean saveStegoImage(String stegoPath) {
    mView.showProgressDialog();
    File stegoFile = new File(stegoPath);
    if (!stegoFile.exists()) {
      mView.showToast(R.string.save_image_error);
      mView.stopProgressDialog();
      return false;
    }
    try {
      Bitmap bitmap = BitmapFactory.decodeFile(stegoPath);
      if (bitmap == null) throw new IOException("decode failed");
      ImageUtils.saveBitmapToPictures((Context) mView, bitmap, "Stego_" + System.currentTimeMillis() + ".png");
      mView.showToast(R.string.save_image_success);
      mView.stopProgressDialog();
      return true;
    } catch (IOException e) {
      mView.showToast(R.string.save_image_error);
      mView.stopProgressDialog();
      return false;
    }
  }

}
