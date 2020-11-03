<?php
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
	if (isset($_POST['ldap']) && isset($_POST['password'])) {
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = 'SELECT [name], keys FROM students WHERE LDAP = ? AND passwd = ?';
		$stmt = sqlsrv_query($conn, $sql, array($_POST['ldap'], $_POST['password']));
		if ($stmt == false) {
			echo json_encode(array('message' => "LDAP must be integer"));
			exit();
		}
		if ($row = sqlsrv_fetch_array($stmt)) {
			$name = $row['name'];
			$keys_b4 = json_decode($row['keys']);
			$keys_after = array();
			for($i = 0; $i < sizeOf($keys_b4); $i++) {
				if (new DateTime($keys_b4[$i]->time) >= new DateTime()) {
					array_push($keys_after, $keys_b4[$i]);
				}
			}
			$sql = 'UPDATE students SET keys = ? WHERE LDAP = ?';
			$stmt = sqlsrv_query($conn, $sql, array(json_encode($keys_after), $_POST['ldap']));
			if ($stmt == false) {
				echo json_encode(array('message' => "Server Error"));
				exit();
			}
			$answer = array();
			$sql = 'SELECT instructors.username, sessions_.session_name, start_time, duration, session_id FROM sessions_ INNER JOIN instructors ON sessions_.userID = instructors.userID WHERE start_time <= GETDATE() AND GETDATE() <= end_time';
			$stmt = sqlsrv_query($conn, $sql);
			if ($stmt == false) {
				echo json_encode(array('message' => "Server Error"));
				exit();
			}
			while ($row = sqlsrv_fetch_array($stmt)) {
				array_push($answer, array('instructor' => $row[0], 'session' => $row[1], 'time' => $row[3], 'identifier' => $row[4]));
			}
			echo json_encode(array('name' => $name, 'sessions' => $answer, 'keys' => $keys_after));
			exit();
		}
		else {
			echo json_encode(array('message' => 'Invalid Credentials'));
			exit();
		}
	}
	else if (isset($_POST['who']) && isset($_POST['comment'])) {
		$id = json_decode($_POST['who'])[0];
		$ques_no = json_decode($_POST['who'])[1];
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = "UPDATE questions SET comments = JSON_MODIFY(comments, 'append $', JSON_QUERY(?)) WHERE session_id = ? AND question_no = ?";
		$comment = json_decode($_POST['comment']);
		$comment->time = date('H:i');
		$stmt = sqlsrv_query($conn, $sql, array(json_encode($comment), $id, $ques_no));
		if ($stmt == false) {
			echo json_encode(array('message' => "Server Error"));
			exit();
		}
		echo '{"done": 1}';
	}
	else if (isset($_POST['ldap']) && isset($_POST['identifier'])) {
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = 'SELECT username, duration, session_name, details, (SELECT COUNT(DISTINCT question_no) FROM questions WHERE session_id = ?) AS count FROM sessions_ INNER JOIN instructors ON instructors.userID = sessions_.userID WHERE sessions_.session_id = ? AND start_time <= GETDATE() AND GETDATE() <= end_time';
		$stmt = sqlsrv_query($conn, $sql, array($_POST['identifier'], $_POST['identifier']));
		if ($stmt == false) {
			echo json_encode(array('message' => "Server Error"));
			exit();
		}
		if ($row = sqlsrv_fetch_array($stmt)) {
			echo json_encode(array('instructor' => $row['username'], 'instruction' => $row['details'], 'time' => $row['duration'], 'session' => $row['session_name'], 'total' => $row['count']));
			exit();
		}
		else {
			echo json_encode(array('message' => 'The session has expired, please refresh'));
			exit();
		}
	}
	else if (isset($_POST['ldap']) && isset($_POST['question_data'])) {
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = 'SELECT problem, options, [type], comments FROM questions INNER JOIN sessions_ ON sessions_.session_id = questions.session_id WHERE question_no = ? AND questions.session_id = ? AND start_time <= GETDATE() AND GETDATE() <= end_time';
		$session = json_decode($_POST['question_data'])[0];
		$question = json_decode($_POST['question_data'])[1];
		$stmt = sqlsrv_query($conn, $sql, array($question, $session));
		if ($stmt == false) {
			echo json_encode(array('message' => "Server Error"));
			exit();
		}
		if ($row = sqlsrv_fetch_array($stmt)) {
			echo json_encode(array('problem' => $row['problem'], 'options' => json_decode($row['options']), 'comments' => json_decode($row['comments']), 'type' => $row['type']));
			exit();
		}
		else {
			echo json_encode(array('message' => 'You are not allowed to enter this session or the session has expired'));
			exit();
		}
	}
	else if ($_POST['question_data'] && $_POST['active']) {
		$session = json_decode($_POST['question_data'])[0];
		$ldap = json_decode($_POST['question_data'])[1];
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($_POST['active'] == -1) {
			$sql = 'UPDATE students SET [session] = NULL WHERE ldap = ?';
			$stmt = sqlsrv_query($conn, $sql, array($ldap));
			if ($stmt == false) {
				echo json_encode(array('message' => "Server Error"));
				exit();
			}
		}
		else if ($_POST['active'] == 1) {
			$sql = 'UPDATE students SET [session] = ? WHERE LDAP = ?';
			$stmt = sqlsrv_query($conn, $sql, array($session, $ldap));
			if ($stmt == false) {
				echo json_encode(array('message' => "Server Error"));
				exit();
			}
		}
	}
	else if (isset($_POST['question_data']) && isset($_POST['attempted'])) {
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = 'UPDATE questions SET students_crossed += ? WHERE question_no = ? AND session_id = ?';
		$session = json_decode($_POST['question_data'])[0];
		$question = json_decode($_POST['question_data'])[1];
		$stmt = sqlsrv_query($conn, $sql, array($_POST['attempted'], $question, $session));
		if ($stmt == false) {
			echo json_encode(array('message' => "Server Error"));
			exit();
		}
		echo '{"done": 1}';
	}
	else if (isset($_POST['who']) && isset($_POST['ldap'])) {
		$id = json_decode($_POST['who'])[0];
		$ques_no = json_decode($_POST['who'])[1];
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = "SELECT LDAP, [message], CONVERT(nvarchar, [time], 8) AS [time] FROM pings INNER JOIN questions ON pings.ques_id = questions.ques_id WHERE (LDAP = ? OR LDAP = 1) AND questions.question_no = ? AND session_id = ? AND reply_to = ? ORDER BY [time]";
		$stmt = sqlsrv_query($conn, $sql, array($_POST['ldap'], $ques_no, $id, $_POST['ldap']));
		if ($stmt == false) {
			echo json_encode(array('message' => "Server Error"));
			exit();
		}
		$answer = array();
		while ($row = sqlsrv_fetch_array($stmt)) {
			array_push($answer, $row);
		}
		echo json_encode($answer);
	}
	else if (isset($_POST['who']) && isset($_POST['ping'])) {
		$id = json_decode($_POST['who'])[0];
		$ques_no = json_decode($_POST['who'])[1];
		$ldap = json_decode($_POST['ping'])->ldap;
		$msg = json_decode($_POST['ping'])->comment;
		$to;
		if (!isset($_POST['to'])) {
			$to = $ldap;
		}
		else {
			$to = $_POST['to'];
		}
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = "INSERT INTO pings (ques_id, LDAP, [message], reply_to) VALUES ((SELECT ques_id FROM questions WHERE question_no = ? AND session_id = ?), ?, ?, ?)";
		$stmt = sqlsrv_query($conn, $sql, array($ques_no, $id, $ldap, $msg, $to));
		if ($stmt == false) {
			echo json_encode(array('message' => "Server Error"));
			exit();
		}
	}
}
?>