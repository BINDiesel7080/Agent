package com.dk.agent;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ClientOldOrders extends Activity {
	private dbAdapter dbi;
	Context ctx;
	TextView orderDateLabel;
	// TextView mixedOrderLabel;
	ListView ll1;
	ListView ll2;
	Cursor c1;
	Cursor c2;

	// CursorAdapter adapter1;
	// ListAdapter adapter2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_old_orders);
		ctx = this;
		dbi = new dbAdapter(this);
		orderDateLabel = (TextView) findViewById(R.id.order_date_label);
		// mixedOrderLabel = (TextView) findViewById(R.id.mixed_order_label);
		// adapter1 = new SimpleCursorAdapter(ctx, R.id.ll3, dbi.GetOldOrders(
		// ClientActivity.ClientID, ClientActivity.fromOrgID, 0),
		// new String[] { "GoodsName", "Price", "Quantity" }, new int[] {
		// R.id.tvGoods3, R.id.tvPrice3, R.id.tvQuantity3,
		// R.id.tvWeight });
		c1 = dbi.GetOldOrders(ClientActivity.ClientID,
				ClientActivity.fromOrgID, 0);
		c2 = dbi.GetOldOrders(ClientActivity.ClientID,
				ClientActivity.fromOrgID, 1);
		startManagingCursor(c1);
		startManagingCursor(c2);
		orderDateLabel.setText("Последняя заявка от "
				+ dbi.GetDocDate(ClientActivity.ClientID,
						ClientActivity.fromOrgID) + ":");
		ll1 = (ListView) findViewById(R.id.ll3);
		ll1.setAdapter(new SimpleCursorAdapter(ctx,
				R.layout.old_orders_list_item, c1, new String[] { "GoodsName",
						"Price", "Quantity" }, new int[] { R.id.tvGoods3,
						R.id.tvPrice3, R.id.tvQuantity3, R.id.tvWeight }));
		ll2 = (ListView) findViewById(R.id.ll5);
		ll2.setAdapter(new SimpleCursorAdapter(ctx,
				R.layout.old_orders_list_item, c2, new String[] { "GoodsName",
						"Price", "Quantity" }, new int[] { R.id.tvGoods3,
						R.id.tvPrice3, R.id.tvQuantity3, R.id.tvWeight }));
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

}
