package com.dk.agent;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

public class ClientActivity extends TabActivity {

	static String ClientID;
	static String ClientName;
	static long fromOrgID;
	static String fromOrgName;
	private dbAdapter dbi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		Bundle extras = getIntent().getExtras();
		ClientID = extras.getString("com.dk.agent.client_id");
		ClientName = extras.getString("com.dk.agent.client_name");
		fromOrgID = extras.getLong("com.dk.agent.from_org_id");
		fromOrgName = extras.getString("com.dk.agent.from_org_name");
		this.setTitle(fromOrgName);
		dbi = new dbAdapter(this);
		((TextView) findViewById(R.id.ClientName)).setText(ClientName + "   "
				+ dbi.GetDocsCount(fromOrgID, ClientID) + "док");
		// ((TextView) findViewById(R.id.fromOrgName)).setText(fromOrgName);

		TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost); // The
																		// activity
																		// TabHost
		tabHost.setup();
		TabHost.TabSpec spec; // Reusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		tabHost.getTabWidget().setDividerDrawable(R.drawable.divider);
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, ClientNewOrderActivity.class);
		View v = createTabView(tabHost.getContext(), "новый заказ");
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("new_order");/*
												 * .setIndicator("Новый заказ")
												 * .setContent(intent);
												 */
		// View v = getLayoutInflater().inflate(R.layout.tab_new_order, null);
		spec.setIndicator(v);
		spec.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, ClientOrdersActivity.class);
		spec = tabHost.newTabSpec("orders");
		v = createTabView(tabHost.getContext(), "уже заказано");
		spec.setIndicator(v);
		spec.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, ClientOldOrders.class);
		spec = tabHost.newTabSpec("old_orders");
		v = createTabView(tabHost.getContext(), "а раньше?");
		spec.setIndicator(v);
		spec.setContent(intent);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, DebtsList.class);
		spec = tabHost.newTabSpec("debts");
		v = createTabView(tabHost.getContext(), "ДТ");
		spec.setIndicator(v);
		spec.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(3);
	}

	private static View createTabView(final Context context, final String text) {

		View view = LayoutInflater.from(context).inflate(R.layout.tab_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

}
