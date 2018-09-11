package com.dk.agent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ListActivity {
	public dbAdapter dbi;
	Activity ctx;
	EditText totalClients;
	EditText totalWeight;
	EditText totalTotal;
	public static final int ORDERS_DELETE_DIALOG = 1;
	public static final int FROMORG_CHOOSE_DIALOG = 2;
	public static final int PROGRESS_DIALOG = 3;
	public static final int ORDERS_STATISTICS_DIALOG = 4;
	private RadioGroup fromOrgChooser;
	private Button fromOrgSelected;
	private Button ordersStatisticsOkButton;
	private ListView ordersStatisticsListview;
	private Cursor ordersStatisticsCursor;
	long listId;
	int listPosition;
	static long fromOrgID;
	static String fromOrgName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ctx = this;
		dbi = new dbAdapter(this);
		// fromOrgID = dbAdapter.MadyaroffOrgID;
		// fromOrgName = dbi.GetString(dbi.getOrgs(fromOrgID), "Name");
		dbi.close();
		registerForContextMenu(getListView());
		totalClients = (EditText) findViewById(R.id.totalClients);
		totalWeight = (EditText) findViewById(R.id.totalWeight);
		totalTotal = (EditText) findViewById(R.id.totalTotal);
		totalClients.setEnabled(false);
		totalWeight.setEnabled(false);
		totalTotal.setEnabled(false);
		showDialog(FROMORG_CHOOSE_DIALOG);
	}

	@Override
	protected void onResume() {
		dbi.open();
		refreshContent();
		dbi.close();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	void refreshContent() {
		this.setTitle(fromOrgName);
		setListAdapter(new SimpleCursorAdapter(this, R.layout.client_list_item,
				dbi.GetClients(fromOrgID), new String[] { "_id", "Name",
						"Weight" }, new int[] { R.id.tvID, R.id.tvClient,
						R.id.tvWeight }));
		getTotals();
	}

	void getTotals() {
		totalClients.setText(String.valueOf(dbi.getClientCount()) + " клиент");
		totalWeight.setText(String.valueOf(dbi.getWeightTotal()) + " кг");
		totalTotal.setText(String.valueOf(dbi.getTotalTotal()) + " р");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		listId = id;
		listPosition = position;
		startClientActivity(getListView(), listPosition, listId);
	}

	void startClientActivity(ListView l, int position, long id) {
		dbi.open();
		Intent i = new Intent(this, ClientActivity.class);
		// i.putExtra("com.dk.agent.client_id", id);
		i.putExtra("com.dk.agent.client_id",
				dbi.GetString((Cursor) l.getItemAtPosition(position), "_id"));
		i.putExtra("com.dk.agent.client_name",
				((Cursor) l.getItemAtPosition(position)).getString(1));
		i.putExtra("com.dk.agent.from_org_id", fromOrgID);
		i.putExtra("com.dk.agent.from_org_name", fromOrgName);
		dbi.close();
		startActivityForResult(i, 1);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.clients_optionsmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		dbi.open();
		String fullDBtoSendName = dbAdapter.dbToSendFolder
				+ dbAdapter.dbName
				+ "_"
				+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar
						.getInstance().getTime()) + "_" + dbi.getTAName();
		dbi.close();

		switch (item.getItemId()) {
		case R.id.changeFromOrg:
			dbi.open();
			showDialog(FROMORG_CHOOSE_DIALOG);
			dbi.close();
			break;
		case R.id.deleteAllMenuitem:
			dbi.open();
			showDialog(ORDERS_DELETE_DIALOG);// диалог подтверждения удаления
												// заказов
			dbi.close();
			break;
		case R.id.prepareOrders:
			if (dbi.makeDBToSend(fullDBtoSendName))
				ShowToast.show(ctx,
						"Подготовка заявок к приему через USB закончена. Файл БД заявок - "
								+ dbAdapter.dbToSendName);
			break;
		case R.id.sendOrders:
			try {
				dbAdapter.copyTo(new File(dbAdapter.FullDbPathWithName),
						new File(fullDBtoSendName));
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (dbi.makeDBToSend(fullDBtoSendName)) {
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent
						.putExtra(
								Intent.EXTRA_EMAIL,
								new String[] { ctx
										.getString(fromOrgID == dbAdapter.RadugaOrgID ? R.string.officeEmailRaduga
												: R.string.officeEmail) });
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Заявка");
				emailIntent.putExtra(Intent.EXTRA_STREAM,
						Uri.parse("file:" + fullDBtoSendName));
				emailIntent.putExtra(Intent.EXTRA_TEXT,
						Html.fromHtml("<b>заявка</b>"));
				emailIntent.setType("application/octet-stream");
				ctx.startActivity(Intent.createChooser(emailIntent,
						"Отправить заявки в офис"));
			}
			break;
		case R.id.stat:
			showDialog(ORDERS_STATISTICS_DIALOG);
			break;

		case R.id.total_debts_menuitem:
			Intent i = new Intent(this, TotalDebtsList.class);
			startActivityForResult(i, 1);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case ORDERS_STATISTICS_DIALOG:
			ListAdapter tempAdapter;
			dbi.open();
			ordersStatisticsCursor = dbi.GetOrdersStatistics();
			startManagingCursor(ordersStatisticsCursor);
			tempAdapter = new SimpleCursorAdapter(this,
					R.layout.reports_statistics_row, ordersStatisticsCursor,
					new String[] { "Name", "TotalWeight", "TotalTotal" },
					new int[] { R.id.tvRepStatGroupName, R.id.tvRepStatWeight,
							R.id.tvRepStatTotal });
			ordersStatisticsListview.setAdapter(tempAdapter);
			dbi.close();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		// ProgressDialog progressDialog;
		// AlertDialog alert;
		Dialog dialog;
		switch (id) {
		// case PROGRESS_DIALOG:
		// progressDialog = new ProgressDialog(this);
		// progressDialog.closeOptionsMenu();
		// return progressDialog;
		case ORDERS_DELETE_DIALOG:// диалог подтверждения удаления заказов
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Удалить все заказы и возвраты?")
					.setPositiveButton("Да",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dbi.open();
									dbi.deleteDocs();
									refreshContent();
									dbi.close();
								}
							});
			return builder.create();
		case FROMORG_CHOOSE_DIALOG:
			dbi.open();
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.from_org_choose);
			fromOrgChooser = (RadioGroup) dialog
					.findViewById(R.id.from_org_group);

			try {
				((RadioButton) dialog.findViewById(R.id.fromMadyaroff))
						.setText(dbi.GetString(
								dbi.getOrgs(dbAdapter.MadyaroffOrgID), "Name"));
			} catch (Exception e) {
				((RadioButton) dialog.findViewById(R.id.fromMadyaroff))
						.setText("не используется");
			} finally {
			}

			try {
				((RadioButton) dialog.findViewById(R.id.fromST)).setText(dbi
						.GetString(dbi.getOrgs(dbAdapter.STOrgID), "Name"));
			} catch (Exception e) {
				((RadioButton) dialog.findViewById(R.id.fromST))
						.setText("не используется");
			} finally {
			}

			try {
				((RadioButton) dialog.findViewById(R.id.fromRain)).setText(dbi
						.GetString(dbi.getOrgs(dbAdapter.RadugaOrgID), "Name"));
			} catch (Exception e) {
				((RadioButton) dialog.findViewById(R.id.fromRain))
						.setText("не используется");
			} finally {

			}

			try {
				((RadioButton) dialog.findViewById(R.id.fromIrbis)).setText(dbi
						.GetString(dbi.getOrgs(dbAdapter.IrbisOrgID), "Name"));
			} catch (Exception e) {
				((RadioButton) dialog.findViewById(R.id.fromIrbis))
						.setText("не используется");
			} finally {

			}
			fromOrgChooser.check(R.id.fromMadyaroff);
			fromOrgSelected = (Button) dialog
					.findViewById(R.id.from_org_selected);
			dialog.setTitle("Торговать от:");
			fromOrgSelected.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						if (fromOrgChooser.getCheckedRadioButtonId() == R.id.fromMadyaroff) {
							fromOrgID = dbAdapter.MadyaroffOrgID;
						} else if (fromOrgChooser.getCheckedRadioButtonId() == R.id.fromST) {
							fromOrgID = dbAdapter.STOrgID;
						} else if (fromOrgChooser.getCheckedRadioButtonId() == R.id.fromIrbis) {
							fromOrgID = dbAdapter.IrbisOrgID;
						} else
							fromOrgID = dbAdapter.RadugaOrgID;
						dbi.open();
						fromOrgName = dbi.GetString(dbi.getOrgs(fromOrgID),
								"Name");
					} catch (Exception e) {
						fromOrgID = dbAdapter.MadyaroffOrgID;
						ShowToast
								.show(ctx,
										"Выбранная организация не существует. Вместо нее выбрана первая в списке!");
					} finally {

					}
					refreshContent();
					dbi.close();
					dismissDialog(FROMORG_CHOOSE_DIALOG);
				}
			});
			dbi.close();
			return dialog;
		case ORDERS_STATISTICS_DIALOG:
			// ListAdapter tempAdapter;
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.reports_statistics_dialog);
			ordersStatisticsOkButton = (Button) dialog
					.findViewById(R.id.orders_statistics_okButton);
			ordersStatisticsListview = (ListView) dialog
					.findViewById(R.id.orders_statistics_listview);
			dialog.setTitle("Статистика заказа");
			ordersStatisticsOkButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismissDialog(ORDERS_STATISTICS_DIALOG);
				}
			});
			// dbi.open();
			// ordersStatisticsCursor = dbi.GetOrdersStatistics();
			// startManagingCursor(ordersStatisticsCursor);
			// tempAdapter = new SimpleCursorAdapter(this,
			// R.layout.reports_statistics_row, ordersStatisticsCursor,
			// new String[] { "Name", "TotalWeight" }, new int[] {
			// R.id.tvRepStatGroupName, R.id.tvRepStatWeight });
			// ordersStatisticsListview.setAdapter(tempAdapter);
			// dbi.close();
			return dialog;
		default:
			return null;
		}
	}
}