package com.jeeves.vpl;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord.CreateRequest;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class LoginRegisterPane extends Pane{
	private Stage stage;
	@FXML private TextField txtUsername;
	@FXML private PasswordField txtPassword;
	@FXML private TextField txtFirstName;
	@FXML private TextField txtLastName;
	@FXML private TextField txtEmail;
	@FXML private PasswordField txtRegPassword;
	@FXML private PasswordField txtRegPasswordConfirm;
	@FXML private Label lblError;
	Subject currentUser;

	public LoginRegisterPane(Main gui, Stage stage) {
		this.stage = stage;
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setController(this);

		URL location = this.getClass().getResource("/LoginRegister.fxml");

		fxmlLoader.setLocation(location);
		try {
			Node root = (Node) fxmlLoader.load();
			getChildren().add(root);

			//default values for now
			txtUsername.setText("dman@dan.com");
			txtPassword.setText("password");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML 
	public void validate(Event e){
		String email = txtEmail.getText();
		String password = txtRegPassword.getText();
		String firstName = txtFirstName.getText();
		String lastName = txtLastName.getText();
		CreateRequest request = null;
		try{
			request = new CreateRequest()
					.setEmail(email)
					.setEmailVerified(false)
					.setPassword(password)
					.setDisplayName(firstName + " " + lastName)
					.setDisabled(false);
		}
		catch(Exception err){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Registration failed");
			alert.setHeaderText(null);
			alert.setContentText("Sorry, that didn't work. " + err.getMessage());
			alert.showAndWait();
			return;
		}
		FirebaseAuth.getInstance().createUser(request)
		.addOnSuccessListener(userRecord -> {
			Platform.runLater(new Runnable(){
				public void run(){
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Registration successful");
					alert.setHeaderText(null);
					alert.setContentText("Successfully registered new user. You can now log in to Jeeves!");
					alert.showAndWait();
				}
			});

			addUserToConfig(email,password,userRecord.getUid());
		})
		.addOnFailureListener(err -> {
			Platform.runLater(new Runnable(){
				public void run(){
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Registration failed");
					alert.setHeaderText(null);
					alert.setContentText("Sorry, that didn't work. " + err.getMessage());
					alert.showAndWait();
				}
			});

			System.err.println("Error creating new user: " + err.getMessage());
		});

	}


	public void addUserToConfig(String email, String password,String uid){
		Sha256Hash sha256Hash = new Sha256Hash(password);
		try {

			Files.write(Paths.get("shiro.ini"), System.getProperty("line.separator").getBytes(), StandardOpenOption.APPEND);
			Files.write(Paths.get("shiro.ini"), (email + " = " + sha256Hash.toHex() + "," + uid).getBytes(), StandardOpenOption.APPEND);

		}catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}
	@FXML
	public void login(Event e){
		Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory("shiro.ini");
		org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();

		SecurityUtils.setSecurityManager(securityManager);
		String username = txtUsername.getText();
		String password = txtPassword.getText();
		currentUser = SecurityUtils.getSubject();
		if ( !currentUser.isAuthenticated() ) {

			Sha256Hash sha256Hash = new Sha256Hash(password);
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			token.setRememberMe(true);
			try {
				currentUser.login( token );
				stage.close();
			}catch (UnknownAccountException uae) {
				lblError.setText("There is no user with username of " + token.getPrincipal());
			} catch (IncorrectCredentialsException ice) {
				lblError.setText("Password for account " + token.getPrincipal() + " was incorrect!");
			} catch (LockedAccountException lae) {
				lblError.setText("The account for username " + token.getPrincipal() + " is locked.  " +
						"Please contact your administrator to unlock it.");
			}	
		}
	}

	@FXML
	public void close(Event e){
		stage.close();
		System.exit(0);
	}

	@FXML
	public void showGlow(Event e){
		Node image = (Node)e.getSource();
		image.getStyleClass().add("drop_shadow");
	}

	@FXML
	public void hideGlow(Event e){
		Node image = (Node)e.getSource();
		image.getStyleClass().remove("drop_shadow");
	}
}