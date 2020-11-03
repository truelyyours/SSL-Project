<!doctype <!DOCTYPE html>

<?php
function sql_query ($user, $passwd) {
	if ($user != '' && $passwd != '') {
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if( $conn === false ) {
			return false;
		}
		$sql = "SELECT userID, username, passwd FROM instructors WHERE username = ? AND passwd = ?";
		$stmt = sqlsrv_query($conn, $sql, array($user, $passwd));
		if( $stmt === false ) {
			return false;
		}
		if( sqlsrv_fetch( $stmt ) === false) {
			return false;
		}
		$userID = sqlsrv_get_field($stmt, 0);
		$sql_username = sqlsrv_get_field($stmt, 1);
		$sql_passwd = sqlsrv_get_field($stmt, 2);
		if ($user == $sql_username && $passwd == $sql_passwd) {
			session_start();
			$_SESSION['username'] = $sql_username;
			$_SESSION['userID'] = $userID;
			return true;
		}
		return false;
	}
}

session_start();
if (isset($_SESSION['username']) && isset($_SESSION['userID'])) {
	header('location: dashboard.php');
	exit();
}

else if (isset($_COOKIE['username']) && isset($_COOKIE['passwd'])) {// for authentication
	$user = $_COOKIE['username']; $passwd = $_COOKIE['passwd'];
	if (sql_query($user, $passwd)) {
		header('Location: dashboard.php');
		exit();
	}
}

$invalid_credentials = false;
if ($_SERVER["REQUEST_METHOD"] == "POST") {
	if (isset($_POST['username']) && isset($_POST['passwd'])) {
		$user = $_POST['username']; $passwd = md5($_POST['passwd']);
		if (sql_query($user, $passwd)) {    
			
			if (isset($_POST['remember_me'])) {
				/* Set cookie to last 1 year */
				setcookie('username', $user, time()+86400*365);
				setcookie('passwd', $passwd, time()+ 86400*365);
			}
			header('Location: dashboard.php');
			exit();
			
		} else {
			$invalid_credentials = true;
		}
	}
}
?>
<html>
<head>
	<meta charset="utf-8" />
	<title>Voodle</title>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
	<link rel="stylesheet" type="text/css" media="screen" href="login.css" />
	<script src="login.js"></script>
	<script src="assets/particles.min.js"></script>
	<link rel='stylesheet' href='./assets/fonts.css'>
</head>
<body>
	<!-- Login Page -->
	<div id="init_info">
		<!-- Info Content like what the site is about -->
	</div>

	<div id='login'>
		<span style="position: absolute; top: 5%; right: 50%; color: #ffc107; font-family: 'cinzel decorative'; font-size: larger; transform: translateX(50%)">Voodle</span>
		<form action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]);?>" method="post">
			<?php if($invalid_credentials) echo '<p style="color: #F48FB1; text-align: center; margin: 0 0 16px 0">Invalid Username or Password</p>'; ?>
			<span>Username</span>
			<input type="text" placeholder="CSE LDAP" name="username" id="username" size="15" autocomplete='username' oninput='check_username(this)' required><br>
			<span>Password</span>
			<input type="password" placeholder="Password" name="passwd" id="passwd" autocomplete='current-password' size="15" oninput='check_password(this)' required><br>
			<label class='container'><input type="checkbox" checked="checked" name="remember_me"><span class='checkmark'></span><span style='display: inline-block;'>Remember Me</span></label>
			<input type="submit" value="Login" style="margin: 0; padding: 10px; position: relative; background-color: #ffa500; border: 1px solid red; width: 100%; cursor: pointer;">
		</form>
	</div>
</body>
</html>
