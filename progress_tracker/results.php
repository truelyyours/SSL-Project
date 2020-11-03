<?php
// for logout
if (isset($_GET['logout']) && $_GET['logout'] === 'true') {
	setcookie('username','', time() - 3600);
	setcookie('passwd','', time() - 3600);
	session_start();
	session_unset();
	session_destroy();
	header('location: login.php');
	exit();
}

session_start();
if (!isset($_SESSION['username']) || !isset($_SESSION['userID'])) {
	header('Location: login.php');
	exit();
}
if ($_SERVER['REQUEST_METHOD'] == 'POST') {
	if(isset($_POST['update_sidebar'])) {
		$client_time = $_POST['update_sidebar'];
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = "SELECT session_name, session_id, start_time, end_time FROM sessions_ INNER JOIN instructors ON sessions_.userID = instructors.userID WHERE sessions_.userID = ?";
		$stmt = sqlsrv_query($conn, $sql, array($_SESSION['userID']));
		if ($stmt === false) {
			echo json_encode(array('message' => "Server Error"));
			exit();
		}
		$answer = array();
		while( $row = sqlsrv_fetch_array( $stmt) ) {
			array_push($answer, array('name' => $row['session_name'], 'unique_identifier' => $row['session_id'], 'start' => $row['start_time']->format('Y-m-d H:i:s'), 'end' => $row['end_time']->format('Y-m-d H:i:s')));
		}
		echo json_encode(array('sessions' => $answer, 'now' => time()));
		exit();
	}

	if (isset($_POST['update_charts'])) {
		$conn = sqlsrv_connect('LAPTOP-DJ46JC9S', array( "Database"=>"voodle", "UID"=>"voodle", "PWD"=>"KanekiK" ));
		if ($conn === false) {
			echo json_encode(array('message' => "Server not Reachable"));
			exit();
		}
		$sql = "SELECT comments, students_crossed FROM questions WHERE session_id = ?";
		$stmt = sqlsrv_query($conn, $sql, array($_SESSION['id']));
		if ($stmt === false) {
			echo '{"message": "Server Error"}';
			exit();
		}
		$chckpt = array();
		while( $row = sqlsrv_fetch_array( $stmt) ) {
			array_push($chckpt, array('students_checkpoint' => $row['students_crossed'], 'comments' => json_decode($row['comments'])));
		}
		$sql = "SELECT pings.LDAP, [name], [message], question_no, CONVERT(nvarchar, [time], 8) AS [time] FROM pings INNER JOIN questions ON pings.ques_id = questions.ques_id INNER JOIN students ON students.LDAP = pings.LDAP WHERE questions.session_id = ? AND pings.LDAP != 1";
		$stmt = sqlsrv_query($conn, $sql, array($_SESSION['id']));
		if ($stmt === false) {
			echo '{"message": "Server Error"}';
			exit();
		}
		$pings = array();
		while( $row = sqlsrv_fetch_array( $stmt) ) {
			array_push($pings, $row);
		}
		$sql = "SELECT COUNT(*) FROM students WHERE [session] = ?";
		$stmt = sqlsrv_query($conn, $sql, array($_SESSION['id']));
		if ($stmt === false) {
			echo '{"message": "Server Error"}';
			exit();
		}
		$students = 0;
		if ( $row = sqlsrv_fetch_array( $stmt) ) {
			$students = $row[0];
		}
		$sql = "SELECT session_name FROM sessions_ WHERE session_id = ?";
		$stmt = sqlsrv_query($conn, $sql, array($_SESSION['id']));
		if ($stmt === false) {
			echo '{"message": "Server Error"}';
			exit();
		}
		$name;
		if ( $row = sqlsrv_fetch_array( $stmt) ) {
			$name = $row[0];
		}
		echo json_encode(array('name' => $name, 'online' => $students, 'checkpoint_comments' => $chckpt, 'pings' => $pings));
		exit();
	}
}
$_SESSION['id'] = $_GET['identifier'];
?>

<!doctype html>
<html>
<head>
	<meta charset="utf-8" />
	<title>Voodle</title>
	<meta name="viewport" content="width=device-width, initial-scale=1">
	<!-- using jquery for easy scripting -->
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
	<!-- using font awesome for icons -->
	<link rel="stylesheet" href="./assets/fontawesome/css/all.min.css">
	<link href="https://fonts.googleapis.com/css?family=Cinzel+Decorative|Josefin+Slab:400,700|Quicksand:400,700" rel="stylesheet">
	<link rel="stylesheet" type="text/css" media="screen" href="results.css" />
	<script src='./assets/chart_module/chart.js/dist/Chart.js'></script>
	<script src="results.js"></script>
</head>

<body>
	<input id='sidebar-toggle-checkbox' type='checkbox' style='display: none'>
	<div id="topnav" class='clearfix'>
		<label for='sidebar-toggle-checkbox'><h3 id='sidebar-toggle' style='color: white; float: left; margin: 0 25px; cursor: pointer; line-height: 48px'><i class="fas fa-bars"></i></h3></label>
		<div id='logo' onclick='window.location.assign("./dashboard.php")' style="cursor: pointer; background-color: rgba(0,0,0,0); color: white; float: left; padding: 0 16px; font-family: 'Cinzel Decorative'; user-select: none;">
			Voodle
		</div>
		<div id='_' style='float: right; color: white; position: relative;'>
			<a id='logout' href="<?php echo $_SERVER['PHP_SELF'] .'?logout=true'?>" style='float: left; color: white; text-decoration: none; text-align: center; padding: 0 4px;'><i style='padding: 0 16px' class="fas fa-sign-out-alt"></i>Logout</a>
		</div>
	</div>
	<div style='display: flex; position: absolute; width: 100%; flex-direction: row; height: calc(100% - 48px); overflow: hidden'>
		<div id='right-side-bar'>
			<span style='color: white; text-align: center; display: block; cursor: default; padding: 12px 4px;'><i style='padding: 0 16px' class='fas fa-user'></i><?php echo $_SESSION['username']?></span>
			<ul id='sessions_list' class='expand-dropdown'><span><i class="fas fa-angle-down"></i>Your Sessions</span>
			</ul>
		</div>
		<!-- For updating sidebar every 2 seconds -->
		<script>
		async function update_sidebar () {
			if ($('#sessions_list > span > i').hasClass('fa-angle-right')) return;
			if ($('#sidebar-toggle-checkbox').prop('checked')) return;
			var xhttp = new XMLHttpRequest();
			xhttp.onreadystatechange = function() {
				if (this.readyState == 4 && this.status == 200) {
					var response = JSON.parse(this.responseText);
					$('#sessions_list li').remove();
					if ('message' in response)
						$('#sessions_list').append('<li style="text-align: center; padding-left: 0; color: #ef9a9a">' + response['message'] + '</li>');
					else {
						var sessions = response['sessions'];
						if (sessions.length === 0) {
							$('#sessions_list').append('<li onclick="window.location.assign(\'./dashboard.php\')"><i class="fas fa-plus" style="margin: 0 8px 0 0"></i>Create a session</li>');
						}
						else for(var i = 0; i < sessions.length; i++) {
							if ((new Date(sessions[i]['start']) <= new Date(response['now']*1000)) && (new Date(response['now']*1000) <= new Date(new Date(sessions[i]['end'])))) {
								$('#sessions_list').append('<li identifier="'+sessions[i]['unique_identifier']+'"><span style="display: inline-block; overflow: hidden; max-width: calc(100% - 2em); white-space: nowrap; text-overflow: ellipsis">'+sessions[i]['name']+'</span><i class="fas fa-feather-alt clearfix" style="float: right;"></i></li>');
								$('#sessions_list li:nth-child('+(i+2)+')').on('click', function() {
									window.location.assign('/results.php?identifier='+escape($(this).attr('identifier')));
								});
							}
							else {
								$('#sessions_list').append('<li identifier="'+sessions[i]['unique_identifier']+'" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 0.5em">'+sessions[i]['name']+'</li>');
								$('#sessions_list li:nth-child('+(i+2)+')').on('click', function() {
									window.location.assign('/results.php?identifier='+escape($(this).attr('identifier')));
								});
							}
						}
					}
				}
			};
			xhttp.open("POST", "<?php echo $_SERVER['PHP_SELF']?>" , true);
			xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			xhttp.send("update_sidebar");
		}
		update_sidebar();
		setInterval(update_sidebar, 2000);
		</script>
		<div id='site'>
			<div id="modal">
				<div id="modal-content">
					<span id="close">&times;</span>
					<ul></ul>
					<div style='display: flex; flex-direction: row'>
						<textarea placeholder="Reply here"></textarea><i class='fa fa-paper-plane' onclick='post_ping($(this))'></i>
					</div>
				</div>
			</div>
			<div style='font-size: x-large; display: flex; flex-direction: row; justify-content: space-between'>
				<span id='s_name'></span>
				<div style='color: #8bc34a; background-color: #000; border-radius: 8px; padding: 8px 16px'>
					<span>Number of Students online:</span>
					<span id='students_online'></span>
					<i class='fas fa-feather-alt'></i>
				</div>
			</div>
			<div class="chart-container" style="position: relative">
			    <canvas id="hist"></canvas>
			</div>
			<div id='comments' style='display: flex'></div>
			<div class="chart-container" style="position: relative">
			    <canvas id="pinghist"></canvas>
			</div>
			<div id='ping-messages' style='display: flex'></div>
			<script>
				// When the user clicks on <span> (x), close the modal
				$('#close').on('click', function() {
					$('#modal').css('display', 'none');
					clearInterval(refresh);
					refresh = setInterval(data_update, 1000);
					data_update();
				});

				// When the user clicks anywhere outside of the modal, close it
				$('#modal').on('click', function(event) {
					if (event.target == this) {
						$(this).css('display', 'none');
						clearInterval(refresh);
						refresh = setInterval(data_update, 1000);
						data_update();
					}
				});
			</script>
			<script>
				var myChart = new Chart($('#hist'), {
					type: 'bar',
					data: {
						labels: [],
						datasets: [{
							label: 'Number of Students Crossed',
							data: [],
							backgroundColor: [
								'rgba(255, 99, 132, 0.2)',
								'rgba(54, 162, 235, 0.2)',
								'rgba(255, 206, 86, 0.2)',
								'rgba(75, 192, 192, 0.2)',
								'rgba(153, 102, 255, 0.2)',
								'rgba(255, 159, 64, 0.2)'
							],
							borderColor: [
								'rgba(255,99,132,1)',
								'rgba(54, 162, 235, 1)',
								'rgba(255, 206, 86, 1)',
								'rgba(75, 192, 192, 1)',
								'rgba(153, 102, 255, 1)',
								'rgba(255, 159, 64, 1)'
							],
							borderWidth: 1
						}]
					},
					options: {
						scales: {
							yAxes: [{
								ticks: {
									beginAtZero:true,
									callback: function(value) {if (value % 1 === 0) {return value;}}
								}
							}],
							xAxes: [{
								categoryPercentage: 1.0,
								barPercentage: 1.0
							}]
						},
						tooltips: {
							intersect: false,
							mode: 'index'
						}
					}
				});

				var mypingChart = new Chart($('#pinghist'), {
					type: 'bar',
					data: {
						labels: [],
						datasets: [{
							label: 'Number of Students Pinged',
							data: [],
							backgroundColor: [
								'rgba(255, 99, 132, 0.2)',
								'rgba(54, 162, 235, 0.2)',
								'rgba(255, 206, 86, 0.2)',
								'rgba(75, 192, 192, 0.2)',
								'rgba(153, 102, 255, 0.2)',
								'rgba(255, 159, 64, 0.2)'
							],
							borderColor: [
								'rgba(255,99,132,1)',
								'rgba(54, 162, 235, 1)',
								'rgba(255, 206, 86, 1)',
								'rgba(75, 192, 192, 1)',
								'rgba(153, 102, 255, 1)',
								'rgba(255, 159, 64, 1)'
							],
							borderWidth: 1
						}]
					},
					options: {
						scales: {
							yAxes: [{
								ticks: {
									beginAtZero:true,
									callback: function(value) {if (value % 1 === 0) {return value;}}
								}
							}],
							xAxes: [{
								categoryPercentage: 1.0,
								barPercentage: 1.0
							}]
						},
						tooltips: {
							intersect: false,
							mode: 'index'
						}
					}
				});
				$.ajax({
					type: 'POST',
					url: <?php echo "'".$_SERVER['PHP_SELF']."'";?>,
					data: 'update_charts',
					success: function(msg) {
						var response = JSON.parse(msg);
						if ('message' in response)
							console.log(response['message']);
						else {
							var len = response.checkpoint_comments.length;
							$('#students_online').html(response['online']);
							$('#s_name').html(response['name']);
							var labels = [];
							for(var i = 0; i < len; i++) {
								labels.push("Ques "+(i+1));
							}
							myChart.data.labels = labels;
							mypingChart.data.labels = labels;
							for (var i = 0; i < len; i++) {
								$('#comments').append('<div><button onclick="$(this).next().toggle()">Comments on Question '+(i+1)+'</button><div><ul ques="'+i+'" style="display: flex"></ul><div style="display: flex; flex-direction: row"><textarea style="flex-grow: 1" placeholder="Comment here"></textarea><i class="fa fa-paper-plane" onclick="post_comment($(this))" aria-hidden="true"></i></div></div></div>');
							}
							for( var i = 0;i<len ; i++) {
								$('#ping-messages').append('<div><button onclick="$(this).next().toggle()">Pings on Question '+(i+1)+'</button><div><ul ques="'+i+'" style="display: flex"></ul><div style="display: flex; flex-direction: row"></div></div></div>');
							}
						}
					}
				});
				function data_update() {
					$.ajax({
						type: 'POST',
						url: <?php echo "'".$_SERVER['PHP_SELF']."'";?>,
						data: 'update_charts',
						success: function(msg) {
							var response = JSON.parse(msg);
							if ('message' in response)
								console.log(response['message']);
							else {
								var len = response.checkpoint_comments.length;
								$('#students_online').html(response['online']);
								var data = [];
								var pingdata = [];

								for(var i = 0; i < len; i++) {
									data.push(response.checkpoint_comments[i].students_checkpoint);
									pingdata.push(0);
								}
								for(var i = 0;i < response.pings.length; i++) {
									pingdata[response.pings[i].question_no - 1] += 1;
								}
								myChart.data.datasets[0].data = data;
								mypingChart.data.datasets[0].data = pingdata;
								mypingChart.update();
								myChart.update();
								for(var i = 0; i < len; i++) {
								var comments = response.checkpoint_comments[i].comments;
								$('#comments ul[ques='+i+']').empty();
									for(var j = comments.length - 1; j >= 0; j--) {
										if (comments[j]['ldap'] == 1)
											$('#comments ul[ques='+i+']').append('<li class="instructor"><div class="c_header"><span class="c_ldap">'+"You"+'</span><span class="c_time">'+comments[j]['time']+'</span></div><span class="c_comment">'+comments[j]['comment']+'</span></li>');
										else
											$('#comments ul[ques='+i+']').append('<li><div class="c_header"><span class="c_ldap">'+comments[j]['ldap']+'</span><span class="c_time">'+comments[j]['time']+'</span></div><span class="c_comment">'+comments[j]['comment']+'</span></li>');
									}
								}
								for(var i=0; i<len; i++){
									$('#ping-messages ul[ques='+i+']').empty();
								}
								for(var i = response.pings.length - 1; i >= 0; i--){
									var ping = response.pings[i];
									var question_no = response.pings[i].question_no-1;
									$('#ping-messages ul[ques='+question_no+']').append('<li title="Reply to '+ping['LDAP']+'"><div class="c_header"><span class="c_ldap">'+ping['LDAP']+'</span><span class="c_time">'+ping['time']+'</span></div><span class="c_comment">'+ping['message']+'</span></li>');
									// Get the modal
									var modal = $('#modal');

									// Get the <span> element that closes the modal
									var span = $('#close');

									var btn = $('#ping-messages ul > li');

									btn.on('click', function() {
										var who = $(this).children().children().html();
										var ques = $(this).parent().attr('ques');
										if (who == 'You') {
											return;
										}
										console.log($(this).html());
										modal.attr('ques', ques);
										modal.attr('who', who);
										modal.css('display', 'block');
										clearInterval(refresh);
										refresh = setInterval(function () {fetch_pings(ques, who);}, 1000);
										fetch_pings(ques, who);
									})
								} 
 

							}
						}
					});
				};
				var refresh = setInterval(data_update, 1000);
				data_update();

				function post_comment(post_icon) {
					var textarea = post_icon.prev();
					if (textarea.val())
					$.ajax({
						type: 'POST',
						url: './student_interface.php',
						data: {'who': JSON.stringify([<?php echo $_SESSION['id']; ?>, parseInt(textarea.parent().prev().attr('ques')) + 1]), 'comment': JSON.stringify({'ldap': 1, 'comment': textarea.val()})},
						success: function(msg) {
							textarea.val('');
						}
					})
				}

				function post_ping(post_icon) {
					var textarea = post_icon.prev();
					if (textarea.val())
					$.ajax({
						type: 'POST',
						url: './student_interface.php',
						data: {'who': JSON.stringify([<?php echo $_SESSION['id']; ?>, parseInt($('#modal').attr('ques')) + 1]), 'ping': JSON.stringify({'ldap': 1, 'comment': textarea.val()}), 'to': $('#modal').attr('who')},
						success: function(msg) {
							console.log(msg);
							textarea.val('');
						}
					})
				}

				function fetch_pings(ques_no, ldap) {
					$.ajax({
						type: 'POST',
						url: './student_interface.php',
						data: {'who': JSON.stringify([<?php echo $_SESSION['id']; ?>, parseInt($('#modal').attr('ques')) + 1]), 'ldap': ldap},
						success: function(msg) {
							var response = JSON.parse(msg);
							$('#modal ul').empty();
							for(var i = response.length - 1; i >= 0; i--) {
								if (response[i]['LDAP'] == 1)
									$('#modal ul').append('<li class="instructor"><div class="c_header"><span class="c_time">'+response[i]['time']+'</span></div><span class="c_comment">'+response[i]['message']+'</span></li>');
								else 
									$('#modal ul').append('<li><div class="c_header"><span class="c_time">'+response[i]['time']+'</span></div><span class="c_comment">'+response[i]['message']+'</span></li>');
							}
						}
					})
				}
			</script>
		</div>
	</div>
</body>
</html>