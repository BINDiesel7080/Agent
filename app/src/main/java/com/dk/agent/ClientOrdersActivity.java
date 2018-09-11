package com.dk.agent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class ClientOrdersActivity extends ListActivity {
	private dbAdapter dbi;
	private RadioGroup orderTypeRadiogroup;
	private Cursor OrdersCursor;
	private int pos;
	private long id;
	long DocRowID;
	float Weight;
	float ArticleWeight;
	int PiB; // штук в упаковке
	int cantDivide; // не продается поштучно
	float quantum;
	TextView dialogGoodsName;
	EditText dialogUnits;
	EditText dialogArticles;
	EditText dialogPlaces;
	EditText note;
	Button dialogOk;
	Button unitsInc;
	Button unitsDec;
	Button articlesInc;
	Button articlesDec;
	Button placesInc;
	Button placesDec;
	Spinner docNumberSpinner;
	private Cursor CurrentGoods;
	Context ctx;
	int orderTypeForNote;
	int docNumber;
	EditText weightTotal;
	EditText totalTotal;
	static final int ORDER_DELETE_DIALOG = 2;
	static final int RETURN_DELETE_DIALOG = 3;
	static final int CONTEXT_ORDER_DELETE_DIALOG = 4;

	// Button dialogCancel;

	OnItemSelectedListener docNumberSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View v, int pos,
				long id) {
			dbi.setNote(ClientActivity.fromOrgID, ClientActivity.ClientID,
					orderTypeForNote, note.getText().toString(), docNumber);
			docNumber = (int) docNumberSpinner.getSelectedItemId() - 1;
			GoodsRefresh(orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.OrderType ? 0
					: 1);
			getNote();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		setContentView(R.layout.client_orders);
		dbi = new dbAdapter(this);
		orderTypeRadiogroup = (RadioGroup) findViewById(R.id.radioGroup1);
		orderTypeRadiogroup.setOnCheckedChangeListener(orderTypeChangeListener);
		orderTypeRadiogroup.check(R.id.OrderType);
		orderTypeForNote = 0;
		note = (EditText) findViewById(R.id.noteEditText);
		note.addTextChangedListener(NoteListener);
		weightTotal = (EditText) findViewById(R.id.clientWeightTotal);
		totalTotal = (EditText) findViewById(R.id.cTotalTotal);
		weightTotal.setEnabled(false);
		totalTotal.setEnabled(false);
		docNumberSpinner = (Spinner) findViewById(R.id.docNumberSpinner);
		docNumberSpinner.setAdapter(new ArrayAdapter<String>(this,
				R.layout.doc_number_spinner, R.id.docNumberSpinnerName,
				ClientNewOrderActivity.docNumbers));
		docNumberSpinner.setOnItemSelectedListener(docNumberSelectedListener);
		registerForContextMenu(getListView());
	}

	TextWatcher NoteListener = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
			orderTypeForNote = orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.OrderType ? 0
					: 1;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		dbi.setNote(
				ClientActivity.fromOrgID,
				ClientActivity.ClientID,
				orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.OrderType ? 0
						: 1, note.getText().toString(), (int) docNumberSpinner
						.getSelectedItemId() - 1);
		dbi.close();
		super.onPause();
	}

	OnCheckedChangeListener orderTypeChangeListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(RadioGroup group, int checkedId) {
			dbi.setNote(ClientActivity.fromOrgID, ClientActivity.ClientID,
					orderTypeForNote, note.getText().toString(),
					(int) docNumberSpinner.getSelectedItemId() - 1);
			docNumber = 0;
			docNumberSpinner.setSelection(1);
			getNote();
			GoodsRefresh(checkedId == R.id.OrderType ? 0 : 1);
		}
	};

	void GoodsRefresh(int isReturn) {
		OrdersCursor = dbi.GetOrders(ClientActivity.fromOrgID,
				ClientActivity.ClientID, isReturn,
				(int) docNumberSpinner.getSelectedItemId() - 1);
		startManagingCursor(OrdersCursor);
		setListAdapter(new SimpleCursorAdapter(this, R.layout.orders_list_item,
				OrdersCursor, new String[] { "_id", "GoodsName", "Quantity",
						"Price" }, new int[] { R.id.tvID2, R.id.tvGoods2,
						R.id.tvQuantity2, R.id.tvPrice2 }));
		orderTypeForNote = isReturn;
		weightTotal.setText(String.valueOf(dbi.getWeightTotal(
				ClientActivity.ClientID, isReturn)) + "кг");
		totalTotal.setText(String.valueOf(dbi.getTotalTotal(
				ClientActivity.ClientID, isReturn)) + "р");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		dbi.open();
		int isReturn = orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.OrderType ? 0
				: 1;
		docNumber = 0;
		docNumberSpinner.setSelection(1);
		GoodsRefresh(isReturn);
		getNote();
		super.onResume();
	}

	void getNote() {
		if (docNumberSpinner.getSelectedItemId() > 0) {
			int isReturn = orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.OrderType ? 0
					: 1;
			Cursor c;
			c = dbi.GetDocs(ClientActivity.fromOrgID, ClientActivity.ClientID,
					isReturn, (int) docNumberSpinner.getSelectedItemId() - 1);
			if (c.getCount() > 0) {
				note.setText(dbi.GetString(c, "Note"));
			} else
				note.setText("");
			super.onResume();
		} else
			note.setText("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// запомним позицию списка для диалога
		updateOrder(id, position);
		super.onListItemClick(l, v, position, id);
	}

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
		inflater.inflate(R.menu.orders_contextmenu, menu);
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
		switch (item.getItemId()) {
		case R.id.editOrderMenuitem:
			updateOrder(info.id, info.position);
			return true;
		case R.id.deleteOrderMenuitem:
			DocRowID = info.id;
			showDialog(CONTEXT_ORDER_DELETE_DIALOG);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void updateOrder(long id, int position) {
		pos = position;
		this.id = id;
		showDialog(ClientNewOrderActivity.ORDER_DIALOG);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// создадим диалог
		Dialog dialog;
		AlertDialog.Builder builder;
		AlertDialog alert;
		switch (id) {
		case CONTEXT_ORDER_DELETE_DIALOG:
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Удалить строку заказа?").setPositiveButton(
					"Да", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dbi.deleteDocRow(DocRowID, true);
							GoodsRefresh(orderTypeRadiogroup
									.getCheckedRadioButtonId() == R.id.OrderType ? 0
									: 1);
						}
					});
			alert = builder.create();
			return alert;

		case ORDER_DELETE_DIALOG:
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Удалить все заказы клиента?")
					.setPositiveButton("Да",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dbi.deleteDocs(ClientActivity.ClientID, 0);
									GoodsRefresh(orderTypeRadiogroup
											.getCheckedRadioButtonId() == R.id.OrderType ? 0
											: 1);
								}
							});
			alert = builder.create();
			return alert;

		case RETURN_DELETE_DIALOG:
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Удалить все возвраты клиента?")
					.setPositiveButton("Да",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dbi.deleteDocs(ClientActivity.ClientID, 1);
									GoodsRefresh(orderTypeRadiogroup
											.getCheckedRadioButtonId() == R.id.OrderType ? 0
											: 1);
								}
							});
			alert = builder.create();
			return alert;

		case ClientNewOrderActivity.ORDER_DIALOG:
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
								.toString());
						q += 1;
						dialogArticles.setText(String.valueOf(q));
					} catch (Exception e) {
					}
				}
			});
			articlesDec.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						float q = Float.valueOf(dialogArticles.getText()
								.toString());
						q -= 1;
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
					ClientNewOrderActivity.CorrespondArticles(s,
							dialogArticles, Weight, ArticleWeight);
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub
				}

				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
				}
			});
			dialogArticles.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					ClientNewOrderActivity.CorrespondArticles(s, dialogUnits,
							ArticleWeight, Weight);
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub
				}

				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
				}
			});
			return dialog;
		default:
			return null;
		}
	}

	private OnClickListener okButtonListener = new OnClickListener() {
		public void onClick(View v) {
			int p;
			try {
				if (Float.valueOf(dialogUnits.getText().toString()) > 0) {
					try {
						p = Math.round(Float.valueOf(dialogPlaces.getText()
								.toString()));
					} catch (Exception e) {
						p = 0;
					} finally {
					}
					dbi.updateOrderRow(id, java.lang.Float.valueOf(dialogUnits
							.getText().toString()), p);
					dismissDialog(ClientNewOrderActivity.ORDER_DIALOG);
					GoodsRefresh(orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.OrderType ? 0
							: 1);
				} else
					ShowToast
							.show(ctx, "Количество должно быть положительным!");
			} catch (Exception e) {
				ShowToast.show(ctx, e.toString());
			}
		}
	};

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case ClientNewOrderActivity.ORDER_DIALOG:
			float Quantity;
			int places;
			CurrentGoods = dbi.GetGoods(dbi.GetString((Cursor) getListView()
					.getItemAtPosition(pos), "GoodsID"));
			PiB = dbi.GetInt(CurrentGoods, "PiecesInBox");
			cantDivide = dbi.GetInt(CurrentGoods, "cantDivide");
			quantum = cantDivide == 0 ? 1 : PiB;// минимальное приращение
			Weight = dbi.GetFloat(CurrentGoods, "Weight");
			ArticleWeight = dbi.GetFloat(CurrentGoods, "ArticleWeight");
			DocRowID = getListView().getItemIdAtPosition(pos);// убедиться что
																// дает норм
																// айди
			dialogGoodsName.setText(dbi.GetString(CurrentGoods, "Name"));
			Quantity = dbi.GetFloat(
					(Cursor) getListView().getItemAtPosition(pos), "Quantity");
			places = dbi.GetInt((Cursor) getListView().getItemAtPosition(pos),
					"boxes");
			dialogUnits.setText(java.lang.String.valueOf(Quantity));
			dialogPlaces.setText(java.lang.String.valueOf(places));
			if (ArticleWeight > 0) {
				dialogArticles.setEnabled(true);
			} else {
				dialogArticles.setEnabled(false);
			}
			if (ClientActivity.fromOrgID != dbAdapter.IrbisOrgID) {
				dialogPlaces.setEnabled(false);
				placesInc.setEnabled(false);
				placesDec.setEnabled(false);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.orders_optionsmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.deleteOrdersMenuitem:
			showDialog(ORDER_DELETE_DIALOG);
			break;
		case R.id.deleteReturnsMenuitem:
			showDialog(RETURN_DELETE_DIALOG);
			break;
		}
		GoodsRefresh(orderTypeRadiogroup.getCheckedRadioButtonId() == R.id.OrderType ? 0
				: 1);
		return super.onOptionsItemSelected(item);
	}
}
