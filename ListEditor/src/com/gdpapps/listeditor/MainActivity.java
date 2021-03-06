package com.gdpapps.listeditor;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.gdpapps.listeditor.Objects.Info;
import com.gdpapps.listeditor.Objects.List;
import com.gdpapps.listeditor.Objects.ListAdapter;
import com.gdpapps.listeditor.Utils.Constants;
import com.gdpapps.listeditor.Utils.FileIO;
import com.gdpapps.listeditor.Utils.Utilities;
import com.gdpapps.listeditor.Objects.Date;
import com.gdpapps.listeditor.Objects.Date.DateType;
import com.gdpapps.listeditor.Objects.Date.DayOfWeek;
import android.widget.*;

public class MainActivity extends Activity implements Constants {

	ArrayList<List> listFiles;
	ListView lstList;
	FileIO ioManager;

	long idSelected = ID_Null;
	boolean longClick = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		InflatePopUpMenus();	
		initComponents();
	}

	private void initComponents(){ 
		ioManager = new FileIO(this, ListsFolderPath, ListsDataFileName);
		
		listFiles = ioManager.readFile();
		
		lstList = (ListView) findViewById(R.id.main_lstList);
		
		resetListAdapter();
		
		lstList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
					MainActivity.this.listClick(childView, position, id);
				}
		});
		lstList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parentView, View childView, int position, long id) {
					MainActivity.this.listLongClick(childView, position, id);
					return false;
				}
		});
		lstList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View childView, int position, long id) {
					MainActivity.this.listSelected(childView, position, id);
				}
				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
					MainActivity.this.listNothingSelected();
				}
	     	});
	}

	private void InflatePopUpMenus() {
		final Button 
			file_cmd = (Button) findViewById(R.id.main_mnuFile), 
			edit_cmd = (Button) findViewById(R.id.main_mnuEdit),
			options_cmd = (Button) findViewById(R.id.main_mnuOptions);

		final int 
			file_menuXml = R.menu.mainmenu_file, 
			edit_menuXml = R.menu.mainmenu_edit,  
			options_menuXml = R.menu.mainmenu_options;

		final PopupMenu
			file_popup = new PopupMenu(this, file_cmd), 
			edit_popup = new PopupMenu(this, edit_cmd),  
			options_popup = new PopupMenu(this, options_cmd);

		file_popup.getMenuInflater().inflate(file_menuXml, file_popup.getMenu());
		edit_popup.getMenuInflater().inflate(edit_menuXml, edit_popup.getMenu());
		options_popup.getMenuInflater().inflate(options_menuXml, options_popup.getMenu());

		file_cmd.setOnClickListener(new View.OnClickListener() 
			{@Override public void onClick(View v) {file_popup.show();}});
		edit_cmd.setOnClickListener(new View.OnClickListener() 
			{@Override public void onClick(View v) {edit_popup.show();}});
		options_cmd.setOnClickListener(new View.OnClickListener()
			{@Override public void onClick(View v) {options_popup.show();}});
		
		file_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.mnuFileNew:
					MainActivity.this.mnuFileNew(); break;
				case R.id.mnuFileSaveAll:
				    Utilities.showToast(MainActivity.this, "save method");
					MainActivity.this.mnuFileSave(); break;
				case R.id.mnuFileEdit:
					MainActivity.this.mnuFileEdit(); break;
				case R.id.mnuFileDelete:
					MainActivity.this.mnuFileDelete(); break;
				case R.id.mnuFileExit:
					MainActivity.this.mnuFileExit(); break;
				}
				return true;
			}
		});
		edit_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.mnuEditTitle:
					MainActivity.this.mnuEditTitle(); break;
				case R.id.mnuEditInfo:
					MainActivity.this.mnuEditInfo(); break;
				case R.id.mnuEditDuplicate:
					MainActivity.this.mnuEditDuplicate(); break;
				case R.id.mnuEditEraseContent:
					MainActivity.this.mnuEditEraseContent(); break;
				case R.id.mnuEditForceFormat:
					MainActivity.this.mnuEditForceFormat(); break;
				}
				return true;
			}
		});
		options_popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.mnuOptionsFormatOptions:
					MainActivity.this.mnuOptionsFormatOptions(); break;
				case R.id.mnuOptionsConfig:
					MainActivity.this.mnuOptionsConfig(); break;
				}
				return true;
			}
		});
	}

	public void listArrowButtonClick(View v){
		runEditionWindow((List)v.getTag());
	}
	
	private void listClick(View child, int pos, long id){
		if(MainActivity.this.isSelectionValid() && MainActivity.this.getIdSelected() == id){
			MainActivity.this.setIdSelected(ID_Null);
			child.setSelected(false);
			Utilities.logPrint("Click: Cleared selection");
		} else {
			MainActivity.this.setIdSelected(id);
			child.setSelected(true);
			Utilities.logPrint("Click: Selected new item: " + id);
		}
		if (longClick){child.setSelected(false); longClick = false;}
	}
	
    	private void listLongClick(View child, int pos, long id){
		//true action: show popup menu/contextual menu
        longClick = true;
		runEditionWindow(listFiles.get((int) id));
	}

	private void listSelected(View child, int pos, long id){

	}
	
	private void listNothingSelected(){
		
	}
	
	private void resetListAdapter(){
		lstList.setAdapter(new ListAdapter(this, listFiles));
	}
	
	private void runEditionWindow(List item) {
		longClick = false;
		idSelected = ID_Null;
		Intent intent = new Intent(MainActivity.this, EditionActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(BundleKey, item);
		intent.putExtras(bundle);
		startActivity(intent);
	}

//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		switch (requestCode) {
//			case 0x100: {
//				if (resultCode == RESULT_OK) {
//					List item = data.getExtras().getParcelable(BundleKey);
//					
//				}
//			}
//		}
//	}
	

	private void setIdSelected(long idSelected){
		this.idSelected = idSelected; 
		Utilities.logPrint("id selected: " + idSelected);
	}
	
	private long getIdSelected()
     	{return idSelected;} 
	
	private boolean isSelectionValid()
	    {if(idSelected == ID_Null){return false;} else {return true;}}
   
   
	private void mnuFileNew(){
	    listFiles = ioManager.readFile();	
	}
	private void mnuFileSave(){
		ioManager.writeFile(listFiles);
	}
	private void mnuFileEdit(){
		
	}
	private void mnuFileDelete(){
		listFiles.add(new List(1, new Info("Name Three", "Location Four", 
		new Date(03, DayOfWeek.SATURDAY, DateType.EVENT_DATE), 
		new Date(4,  DayOfWeek.SUNDAY, DateType.LIST_CLOSURE)), 
		new ArrayList<String>()));
		resetListAdapter();
	}
	private void mnuFileExit(){
		mnuFileSave();
	}
	
	private void mnuEditTitle(){
		
	}
	private void mnuEditInfo(){
		
	}
	private void mnuEditDuplicate(){
		
	}
	private void mnuEditEraseContent(){
		
	}
	private void mnuEditForceFormat(){
		
	}
	
	private void mnuOptionsFormatOptions(){
		
	}
	private void mnuOptionsConfig(){
		
	}
	private void mnuOptionsItem2(){
		
	}
	
}

