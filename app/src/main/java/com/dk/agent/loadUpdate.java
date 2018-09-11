package com.dk.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class loadUpdate extends Activity {
	TextView t1;
	public SQLiteDatabase db;

	private static String FullDbPath;
	private static String FullDbPathWithName;
	Context ctx;
	public dbAdapter dbi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ctx = this;
		Intent intent;
		InputStream is;
		Button delOrdersButton;
		Button dnDelOrdersButton;
		intent = this.getIntent();
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_update);
		t1 = (TextView) findViewById(R.id.updateExplain);
		t1.setText(this.getString(R.string.afterUpdate));

		File f = Environment.getExternalStorageDirectory();
		FullDbPath = f.getPath() + getString(R.string.dbPath);
		FullDbPathWithName = FullDbPath + getString(R.string.updateDBname);

		File dbDir = new File(FullDbPath);
		dbDir.mkdirs();

		ContentResolver resolver = getContentResolver();
		try {
			is = resolver.openInputStream(intent.getData());
			File f2 = new File(FullDbPathWithName);// сюда скачаем присланный
													// апдейт
			FileOutputStream fout = new FileOutputStream(f2);
			byte[] b = new byte[512];
			int n = 0;
			while ((n = is.read(b)) != -1)
				fout.write(b, 0, n);
			fout.close();
			is.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		delOrdersButton = (Button) findViewById(R.id.del_orders);
		dnDelOrdersButton = (Button) findViewById(R.id.dndel_orders);

		delOrdersButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					dbi = new dbAdapter(ctx);
					dbAdapter.copyTo(new File(FullDbPathWithName), new File(
							dbi.FullDbPathWithName));
					Intent i = new Intent(ctx, MainActivity.class);
					startActivity(i);
				} catch (Exception e) {
					//ShowToast.show(ctx, "ошибка!");
					e.printStackTrace();
				}
			}
		});

		dnDelOrdersButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					dbi = new dbAdapter(ctx);
					dbi.loadUpdateDB();
					dbAdapter.copyTo(new File(FullDbPathWithName), new File(
							dbi.FullDbPathWithName));
					Intent i = new Intent(ctx, MainActivity.class);
					startActivity(i);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
