package com.example.webp_example;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.webp.libwebp;

public class MainActivity extends Activity {
  static {
    System.loadLibrary("webp");
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    //Read a webp
    byte[] webpEncodedData = loadFileAsByteArray("/sdcard/test.webp");
    Bitmap bitmap = webpToBitmap(webpEncodedData);
    ImageView imageView1 = (ImageView) this.findViewById(R.id.imageView1);
    imageView1.setImageBitmap(bitmap);
    
    //Write a webp
    byte[] webpData = bitmapToWebp("/sdcard/test.png");
    writeFileFromByteArray("/sdcard/test.webp", webpData);
  }
  
  private byte[] loadFileAsByteArray(String filePath) {
    File file = new File(filePath);
    byte[] data = new byte[(int)file.length()];
    try {
      FileInputStream inputStream;
      inputStream = new FileInputStream(file);
      inputStream.read(data);
      inputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return data;
  }

  private Bitmap webpToBitmap(byte[] encoded) {
    int[] width = new int[] { 0 };
    int[] height = new int[] { 0 };
    byte[] decoded = libwebp.WebPDecodeARGB(encoded, encoded.length, width, height);
    int[] pixels = new int[decoded.length / 4];
    ByteBuffer.wrap(decoded).asIntBuffer().get(pixels);
    return Bitmap.createBitmap(pixels, width[0], height[0], Bitmap.Config.ARGB_8888);
  }

  private byte[] bitmapToWebp(String filePath) {
    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
    int bytes = bitmap.getByteCount();
    ByteBuffer buffer = ByteBuffer.allocate(bytes);
    bitmap.copyPixelsToBuffer(buffer);
    byte[] pixels = buffer.array();

    int height = bitmap.getHeight();
    int width = bitmap.getWidth();
    int stride = width * 4;
    int quality = 100;
    byte[] rgb = new byte[3];
    
    for (int y = 0; y < height * 4; y++) {
      for (int x = 0; x < width; x+=4) {
        for (int i = 0; i < 3; i++) {
          rgb[i] = pixels[x + y * width + i];
        }
        for (int i = 0; i < 3; i++) {
          pixels[x + y * width + 2 - i] = rgb[i];
        }
      }
    }

    byte[] encoded = libwebp.WebPEncodeBGRA(pixels, width, height, stride, quality);
    return encoded;
  }
  
  private void writeFileFromByteArray(String filePath, byte[] data) {
    File webpFile = new File(filePath);
    BufferedOutputStream bos;
    try {
      bos = new BufferedOutputStream(new FileOutputStream(webpFile));
      bos.write(data);
      bos.flush();
      bos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
