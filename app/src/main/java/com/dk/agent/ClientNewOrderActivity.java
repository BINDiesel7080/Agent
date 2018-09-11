package com.dk.agent;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.FloatMath;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ClientNewOrderActivity extends ListActivity {
	private Spinner s1;
	private Spinner s2;
	private Spinner s3;
	private RadioGroup orderTypeRadiogroup;
	private Cursor c;
	private Cursor c2;
	private Cursor c3;
	private Cursor GoodsCursor;
	private dbAdapter dbi;
	Context ctx;
	SimpleCursorAdapter SubGroupsAdapter;
	SimpleCursorAdapter SubGroups2Adapter;
	TextView dialogGoodsName;
	EditText dialogUnits;
	EditText dialogArticles;
	Button dialogOk;
	Button unitsInc;
	Button unitsDec;
	Button articlesInc;
	Button articlesDec;
	EditText dialogPlaces;
	Button placesInc;
	Button placesDec;
	static final int ORDER_DIALOG = 1;
	private int pos;
	private long id;
	private int docNumber;
	String GoodsName;
	String GoodsID;
	float Weight;
	float ArticleWeight;
	private int isCurrentActionReturn = 0;
	int PiB; // штук в упаковке
	int cantDivide; // не продается поштучно
	float quantum;
	// идентификаторы строк меню для заказов в несколько доков
	public static int addOrderToDoc[] = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
			12, 13, 14, 15, 16, 17, 18, 19, 20 };
	public static int addReturnToDoc[] = { 21, 22, 23, 24, 25, 26, 27, 28, 29,
			30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 };
	public static String docNumbers[] = { "все документы", "документ 1",
			"документ 2", "документ 3", "документ 4", "документ 5",
			"документ 6", "документ 7", "документ 8", "документ 9",
			"документ 10", "документ 11", "документ 12", "документ 13",
			"документ 14", "документ 15", "документ 16", "документ 17",
			"документ 18", "документ 19", "документ 20" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_new_order);
		ctx = this;
		dbi = new dbAdapter(this);
		orderTypeRadiogroup = (RadioGroup) findViewById(R.id.radioGroupNew);
		orderTypeRadiogroup.setOnCheckedChangeListener(orderTypeChangeListener);
		orderTypeRadiogroup.check(R.id.radioOrder);
		s1 = (Spinner) findViewById(R.id.GroupSpinner);
		s2 = (Spinner) findViewById(R.id.SubgroupSpinner);
		s3 = (Spinner) findViewById(R.id.Subgroup2Spinner);

		// наполним спиннеры фильтров групп и подгрупп
		c = dbi.GetGroups(ClientActivity.fromOrgID);
		startManagingCursor(c);
		s1.setAdapter(new SimpleCursorAdapter(this, R.layout.group_spinner, c,
				new String[] { "Name" }, new int[] { R.id.GroupSpinnerName }));
		s1.setPrompt("Группа");
		s1.setOnItemSelectedListener(GroupSelectedListener);

		c2 = dbi.GetSubgroups();
		startManagingCursor(c2);
		SubGroupsAdapter = new SimpleCursorAdapter(this,
				R.layout.subgroup_spinner, c2, new String[] { "Name" },
				new int[] { R.id.SubgroupSpinnerName });
		s2.setAdapter(SubGroupsAdapter);
		s2.setPrompt("Подгруппа");
		s2.setOnItemSelectedListener(SubgroupSelectedListener);

		c3 = dbi.GetSubgroups2();
		startManagingCursor(c3);
		SubGroups2Adapter = new SimpleCursorAdapter(this,
				R.layout.subgroup_spinner, c3, new String[] { "Name" },
				new int[] { R.id.SubgroupSpinnerName });
		s3.setAdapter(SubGroups2Adapter);
		s3.setPrompt("Вид товара");
		s3.setOnItemSelectedListener(SubgroupSelectedListener);

		registerForContextMenu(getListView());
	}

	@Override
	protected void onPause() {
		dbi.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		dbi.open();
		super.onResume();
	}

	OnCheckedChangeListener orderTypeChangeListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(RadioGroup group, int checkedId) {
			GoodsRefresh(checkedId == R.id.radioOrder ? 0 : 1);
		}
	};

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		docNumber = 0;
		doAction(id, position, 0);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case ORDER_DIALOG:
			GoodsID = dbi.GetString((Cursor) getListView().getItemAtPosition(pos),"_id");
			PiB = dbi.GetInt((Cursor) getListView().getItemAtPosition(pos),
					"PiecesInBox");
			cantDivide = dbi
					.GetInt((Cursor) getListView().getItemAtPosition(pos),
							"cantDivide");
			quantum = cantDivide == 0 ? 1 : PiB;// минимальное приращение
			Weight = dbi.GetFloat(
					(Cursor) getListView().getItemAtPosition(pos), "Weight");
			ArticleWeight = ClientActivity.fromOrgID == dbAdapter.STOrgID ? dbi
					.GetFloat((Cursor) getListView().getItemAtPosition(pos),
							"ArticleWeight") : 0;
			dialogGoodsName.setText(dbi.GetString((Cursor) getListView()
					.getItemAtPosition(pos), "Name"));

			if (ArticleWeight > 0) {
				// если задан вес изделия, пишем единичку в изделия, если нет -
				// квант в штуки
				dialogArticles.setText("1");
				dialogArticles.setEnabled(true);
			} else {
				dialogUnits.setText(String.valueOf(quantum));
				dialogArticles.setEnabled(false);
			}
			if (ClientActivity.fromOrgID != dbAdapter.IrbisOrgID) {
				dialogPlaces.setEnabled(false);
				placesInc.setEnabled(false);
				placesDec.setEnabled(false);
			}
		}
	}

	// заполняет поля единиц и изделий соответствующими друг другу значениями
	// et - EditText, значение которого нужно выставить
	// weight1, weight2 - веса единицы и изделия или наоборот в зависимости
	// какое поле нужно выставить
	// сравнения перед присваиванием нужны для предотвращения бесконечного цикла
	// событий изменения текста
	public static void CorrespondArticles(CharSequence s, EditText et,
			float weight1, float weight2) {
		try {
			float s2 = Float.valueOf(s.toString());
			float w3 = weight1 / weight2;
			if (s.length() > 0 & !Float.isInfinite(s2) & !Float.isNaN(s2)
					& !Float.isInfinite(w3) & !Float.isNaN(w3)) {
				if (et.getText().length() > 0) {
					if (s2 * w3 != Float.valueOf(et.getText().toString())) {
						et.setText(String.valueOf(s2 * w3));
					}
				} else
					et.setText(String.valueOf(s2 * w3));
			} else if (et.getText().length() > 0)
				et.setText("");
		} catch (Exception e) {
			if (et.getText().length() > 0)
				et.setText("");
			// ShowToast.show(ctx, e.toString());
		}
	}

	OnItemSelectedListener GroupSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			c2 = dbi.GetSubgroups(id);
			startManagingCursor(c2);
			SubGroupsAdapter.changeCursor(c2);
			s2.setSelection(0);
			c3 = dbi.GetSubgroups2(id);
			startManagingCursor(c3);
			SubGroups2Adapter.changeCursor(c3);
			s3.setSelection(0);
			GoodsRefresh(orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.radioOrder ? 0
					: 1);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	OnItemSelectedListener SubgroupSelectedListener = new OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			GoodsRefresh(orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.radioOrder ? 0
					: 1);
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	// наполним список товаров
	void GoodsRefresh(int isReturn) {
		String s;
		if (ClientActivity.fromOrgID == dbAdapter.MadyaroffOrgID)
			s = "QuantityM";
		else if (ClientActivity.fromOrgID == dbAdapter.STOrgID)
			s = "QuantityST";
		else
			s = "QuantityRain";
		GoodsCursor = dbi.GetGoods(s1.getSelectedItemId(),
				s2.getSelectedItemId(), s3.getSelectedItemId(),
				ClientActivity.fromOrgID, isReturn);
		startManagingCursor(GoodsCursor);
		setListAdapter(new SimpleCursorAdapter(ctx, R.layout.goods_list_item,
				GoodsCursor, new String[] { "Name", "Price", s }, new int[] {
						R.id.tvGoods, R.id.tvPrice, R.id.tvQ }));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		// создадим диалог
		Dialog dialog;
		switch (id) {
		case ORDER_DIALOG:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.order_dialog);
			dialog.setTitle("Количество");
			dialogGoodsName = (TextView) dialog.findViewById(R.id.GoodsName);
			dialogUnits = (EditText) dialog.findViewById(R.id.units);
			dialogArticles = (EditText) dialog.findViewById(R.id.articles);
			dialogPlaces = (EditText) dialog.findViewById(R.id.places);
			dialogOk = (Button) dialog.findViewById(R.id.okButton);
			// dialogCancel = (Button) dialog.findViewById(R.id.cancelButton);
			dialogOk.setOnClickListener(okButtonListener);
			unitsInc = (Button) dialog.findViewById(R.id.unitsInc);
			unitsDec = (Button) dialog.findViewById(R.id.unitsDec);
			articlesInc = (Button) dialog.findViewById(R.id.articlesInc);
			articlesDec = (Button) dialog.findViewById(R.id.articlesDec);
			placesInc = (Button) dialog.findViewById(R.id.placesInc);
			placesDec = (Button) dialog.findViewById(R.id.placesDec);
			dialogUnits.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					((EditText) v).selectAll();
					return false;
				}
			});
			unitsInc.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						float q = Float.valueOf(dialogUnits.getText()
								.toString());
						q += q % PiB == 0 ? quantum : 1;
						dialogUnits.setText(String.valueOf(q));
					} catch (Exception e) {
					}
				}
			});
			unitsDec.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						float q = Float.valueOf(dialogUnits.getText()
								.toString());
						q -= q % PiB == 0 ? quantum : 1;
						dialogUnits.setText(String.valueOf(q));
					} catch (Exception e) {
					}
				}
			});
			articlesInc.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						float q = Float.valueOf(dialogArticles.getText()
								.toString()) + 1;
						dialogArticles.setText(String.valueOf(q));
					} catch (Exception e) {
					}
				}
			});
			articlesDec.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						float q = Float.valueOf(dialogArticles.getText()
								.toString()) - 1;
						dialogArticles.setText(String.valueOf(q));
					} catch (Exception e) {
					}
				}
			});
			placesInc.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						float q = Float.valueOf(dialogPlaces.getText()
								.toString()) + 1;
						dialogPlaces.setText(String.valueOf(q));
					} catch (Exception e) {
						dialogPlaces.setText(String.valueOf(0));
					}
				}
			});
			placesDec.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						float q = Float.valueOf(dialogPlaces.getText()
								.toString()) - 1;
						dialogPlaces.setText(String.valueOf(q));
					} catch (Exception e) {
						dialogPlaces.setText(String.valueOf(0));
					}
				}
			});
			dialogUnits.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				public void afterTextChanged(Editable s) {
					CorrespondArticles(s, dialogArticles, Weight, ArticleWeight);
				}
			});
			dialogArticles.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				public void afterTextChanged(Editable s) {
					CorrespondArticles(s, dialogUnits, ArticleWeight, Weight);
				}
			});
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	private OnClickListener okButtonListener = new OnClickListener() {
		public void onClick(View v) {
			int p;
			try {
				float u = Float.valueOf(dialogUnits.getText().toString());
				if (u > 0) {
					try {
						p = Math.round(Float.valueOf(dialogPlaces.getText()
								.toString()));
					} catch (Exception e) {
						p = 0;
					} finally {
					}
					dbi.insertOrder(ClientActivity.fromOrgID,
							ClientActivity.ClientID, GoodsID, u, "",
							isCurrentActionReturn, docNumber, p);

					dismissDialog(ORDER_DIALOG);
				} else
					ShowToast
							.show(ctx, "Количество должно быть положительным!");
			} catch (Exception e) {
				ShowToast.show(ctx, e.toString());
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.goods_contextmenu, menu);
		// начиная со второго дока по последний плюс 1
		for (int i = 1; i < dbi.GetDocsCount(ClientActivity.fromOrgID,
				ClientActivity.ClientID, 0) + 1; i++) {
			menu.getItem(0)
					.getSubMenu()
					.add(Menu.NONE, addOrderToDoc[i], i,
							"в документ " + (i + 1));
		}
		for (int i = 1; i < dbi.GetDocsCount(ClientActivity.fromOrgID,
				ClientActivity.ClientID, 1) + 1; i++) {
			menu.getItem(1)
					.getSubMenu()
					.add(Menu.NONE, addReturnToDoc[i], i,
							"в документ " + (i + 1));
		}

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (info != null) {
			// запомним позицию списка
			pos = info.position;
			this.id = info.id;
		}
		docNumber = 0;
		switch (item.getItemId()) {
		case R.id.addOrderMenuitem:
			doAction(id, pos, 0);
			return true;
		case R.id.addReturnMenuitem:
			doAction(id, pos, 1);
			return true;
			// case R.id.addOrderMenuLink:
			// doAction(id, pos, 0);
			// return true;
			// case R.id.addReturnMenuLink:
			// doAction(id, pos, 1);
			// return true;
		default:
			for (int i = 1; i < 20; i++) {
				if (item.getItemId() == addOrderToDoc[i]) {
					docNumber = i;
					doAction(id, pos, 0);
					return true;
				}
				if (item.getItemId() == addReturnToDoc[i]) {
					docNumber = i;
					doAction(id, pos, 1);
					return true;
				}
			}
			return super.onContextItemSelected(item);
		}
	}

	void doAction(long id, int position, int isReturn) {
		// если возврат или выбран заказ
		if (!(isReturn == 0 && orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.radioReturn)) {

			isCurrentActionReturn = isReturn;
			// запомним позицию списка для диалога
			pos = position;
			this.id = id;
			if ((ClientActivity.fromOrgID == dbAdapter.STOrgID)
					&& (isReturn == 0)) {
				ShowToast.show(ctx, "Заказы запрещены!");
				return;
			}

			showDialog(ORDER_DIALOG);
		} else {
			ShowToast.show(ctx, "Для заказа переключите список внизу!");
		}
	}
}
