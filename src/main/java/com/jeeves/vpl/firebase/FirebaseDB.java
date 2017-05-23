package com.jeeves.vpl.firebase;

import static com.jeeves.vpl.Constants.DBNAME;
import static com.jeeves.vpl.Constants.DB_URL;
import static com.jeeves.vpl.Constants.PATIENTS_COLL;
import static com.jeeves.vpl.Constants.PROJECTS_COLL;
import static com.jeeves.vpl.Constants.SERVICE_JSON;

import java.util.Map;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jeeves.vpl.Main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FirebaseDB {

	private DatabaseReference dbRef;// = myFirebaseRef.child(DBNAME);
	private DatabaseReference firebaseRef;
	private ObservableList<FirebasePatient> newpatients = FXCollections.observableArrayList();
	private ObservableList<FirebaseProject> newprojects = FXCollections.observableArrayList();
	private Main gui;
	public FirebaseDB(Main gui) {
		this.gui = gui;
		FirebaseOptions options = new FirebaseOptions.Builder().setDatabaseUrl(DB_URL)
				.setServiceAccount(FirebaseDB.class.getResourceAsStream(SERVICE_JSON))// new
																						// FileInputStream("/Users/Daniel/Documents/workspace/NewJeeves/Jeeves-9b9326e90601.json"))
				.build();
		FirebaseApp.initializeApp(options);
	}

	public void addListeners() {
		firebaseRef = FirebaseDatabase.getInstance().getReference();
		dbRef = firebaseRef.child(DBNAME);
		dbRef.addValueEventListener(new ValueEventListener() {
			@Override
			public void onCancelled(DatabaseError arg0) {
			}

			@Override
			public void onDataChange(DataSnapshot arg0) {
				FirebaseMain appdata = arg0.getValue(FirebaseMain.class);
				newprojects.clear();
				newpatients.clear();
				if (appdata != null) {
					Map<String, FirebaseProject> projects = appdata.getprojects();
					Map<String, FirebasePatient> patients = appdata.getpatients();
					if (projects != null)
						for (String key : projects.keySet())
							newprojects.add(projects.get(key));
					if (patients != null)
						newpatients.addAll(patients.values());
				}
			}
		});
	}

	public boolean addPatient(FirebasePatient object) {
		DatabaseReference globalRef = firebaseRef.child(DBNAME).child(PATIENTS_COLL).child(object.getUid());
		globalRef.setValue(object);
		return true;
	}

	public void loadProject(String name){
		DatabaseReference globalRef = firebaseRef.child(DBNAME).child(PROJECTS_COLL).child(name);
		globalRef.addListenerForSingleValueEvent(new ValueEventListener() {
		   @Override
		   public void onDataChange(DataSnapshot dataSnapshot) {
			   @SuppressWarnings("unchecked")
			   FirebaseProject proj = dataSnapshot.getValue(FirebaseProject.class);
			   Platform.runLater(new Runnable(){
				   public void run(){
					   gui.setCurrentProject(proj);

				   }
			   });
		   }

		@Override
		public void onCancelled(DatabaseError arg0) {
			// TODO Auto-generated method stub
			
		}

	
		});
	}

	public boolean addProject(String oldname, FirebaseProject object) {

		DatabaseReference globalRef = null;
		if (oldname == null || oldname.equals(""))
			globalRef = firebaseRef.child(DBNAME).child(PROJECTS_COLL).child(object.getname());
		else {
			globalRef = firebaseRef.child(DBNAME).child(PROJECTS_COLL).child(oldname); // Update
																						// an
																						// old
																						// one
			globalRef.removeValue();
			globalRef = firebaseRef.child(DBNAME).child(PROJECTS_COLL).child(object.getname());
		}
		globalRef.setValue(object);
		return true;
	}

	public ObservableList<FirebasePatient> getpatients() {
		return newpatients;
	}

	// public void setUsername(String username){
	// this.username = username;
	// }
	
	public ObservableList<FirebaseProject> getprojects() {
		return newprojects;
	}

}
