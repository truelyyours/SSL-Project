<?php
// for logout
// if (isset($_GET['logout']) && $_GET['logout'] === 'true') {
// 	setcookie('username','', time() - 3600);
// 	setcookie('passwd','', time() - 3600);
// 	session_start();
// 	session_unset();
// 	session_destroy();
// 	header('location: login.php');
// 	exit();
// }

// session_start();
// if (!isset($_SESSION['username']) || !isset($_SESSION['userID'])) {
// 	header('Location: login.php');
// 	exit();
// }

if(isset($_POST['update_sidebar'])) {
	$conn = sqlsrv_connect('localhost', array( "Database"=>"voodle", "UID"=>"SA", "PWD"=>"Manaswi0411" ));
	if ($conn === false) {
		echo json_encode(array('message' => "Server not Reachable"));
		exit();
	}
	$sql = "SELECT session_name, start_time, end_time FROM sessions_ INNER JOIN instructors ON sessions_.userID = instructors.userID WHERE sessions_.userID = ?";
	$stmt = sqlsrv_query($conn, $sql, array($_SESSION['userID']));
	if ($stmt === false) {
		echo "Server Error";
		exit();
	}
	$answer = array();
	while( $row = sqlsrv_fetch_array( $stmt) ) {
    	array_push($answer, array('name' => $row['session_name'], 'start' => $row['start_time'], 'end' => $row['end_time']));
	}
	echo json_encode(array('sessions' => $answer, 'now' => time()));
	exit();
}

if (isset($_POST['update_charts'])) {
	$conn = sqlsrv_connect('localhost', array( "Database"=>"voodle", "UID"=>"SA", "PWD"=>"Manaswi0411" ));
	if ($conn === false) {
		echo json_encode(array('message' => "Server not Reachable"));
		exit();
	}
	$sql = "SELECT comments, no_of_students FROM questions WHERE session_id = ?";
	$stmt = sqlsrv_query($conn, $sql, array($_SESSION['userID']));
	if ($stmt === false) {
		echo "Server Error";
		exit();
	}
	$answer = array();
	while( $row = sqlsrv_fetch_array( $stmt) ) {
    	array_push($answer, array('no_of_students' => $row['no_of_students'], 'comments' => $row['comments']));
	}
	echo json_encode(array('jsonfile' => $answer));
	exit();
}
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
	 <link data-require="bootstrap-css" data-semver="3.3.6" rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.css" />
    <script data-require="jquery" data-semver="2.1.4" src="https://code.jquery.com/jquery-2.1.4.js"></script>
    <script data-require="bootstrap" data-semver="3.3.6" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="script.js"></script>
       
       <style>
    
    div.templateContainer {
      display: none;
    }

    </style>
		
</head>

<body>
	<input id='sidebar-toggle-checkbox' type='checkbox' style='display: none'>
	<div id="topnav" class='clearfix'>
		<label for='sidebar-toggle-checkbox'><h3 id='sidebar-toggle' style='color: white; float: left; margin: 0 25px; cursor: pointer; line-height: 48px'><i class="fas fa-bars"></i></h3></label>
		<div id='logo' style="background-color: rgba(0,0,0,0); color: white; float: left; padding: 0 16px; font-family: 'Cinzel Decorative'; user-select: none;">
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
							$('#sessions_list').append('<li><i class="fas fa-plus" style="margin: 0 8px 0 0"></i>Create a session</li>');
						}
						else for(var i = 0; i < sessions.length; i++) {
							if ((new Date(sessions[i]['start']['date']) <= new Date(response['now']*1000)) && (new Date(response['now']*1000) <= new Date(new Date(sessions[i]['end']['date'])))) {
								$('#sessions_list').append('<li><span style="display: inline-block; overflow: hidden; max-width: calc(100% - 2em); white-space: nowrap; text-overflow: ellipsis">'+sessions[i]['name']+'</span><i class="fas fa-feather-alt clearfix" style="float: right;"></i></li>');
								$('#sessions_list li:nth-child('+(i+2)+')').on('click', function() {
									window.location.assign('/results.php?name='+escape($(this).children(0).html()));
								});
							}
							else {
								$('#sessions_list').append('<li style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding-right: 0.5em">'+sessions[i]['name']+'</li>');
								$('#sessions_list li:nth-child('+(i+2)+')').on('click', function() {
									window.location.assign('/results.php?name='+escape($(this).html()));
								});
							}
						}
					}
				}
			};
			xhttp.open("POST", <?php echo "'".$_SERVER['PHP_SELF']."'"?> , true);
			xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			xhttp.send("update_sidebar");
		}
		update_sidebar();
		setInterval(update_sidebar, 2000);

		</script>
<!-- 		Bargraph of checkpoints
 -->
		<div class="chart-container" style="position: relative; height:20vh; width:30vw">
		    <canvas id="canvas"></canvas>
		</div>	

		<script>

			$.ajax({
			url: <?php echo "'".$_SERVER['PHP_SELF']."'"?>,
			data: 'update_charts',
			type: 'POST',
			success: function (msg) {
				var response = JSON.parse(msg);
				console.log(msg);
			}
		});
		
			var jsonfile = {
		   "jsonarray": [{
		      "checkpoints": "1",
		      "students": 56
		   }, {
		      "checkpoints": "3",
		      "students": 48
		   }, {
		   	  "checkpoints":"4",
		   	  "students":100
		   }]
		};

		var labels = jsonfile.jsonarray.map(function(e) {
		   return 'Ques '+e.checkpoints;
		});
		var data = jsonfile.jsonarray.map(function(e) {
		   return e.students;
		});;

			var chartdata = {
				labels: labels,
				datasets : [
					{
						label: 'Number of students',
						backgroundColor: 'rgb(255, 51, 51,0.4)',
						borderColor: 'rgb(102, 0, 0,1)',
						hoverBackgroundColor: 'rgb(128, 128, 255,0.4)',
						hoverBorderColor: 'rgba(200, 200, 200, 1)',
						data: data
					}
				]
			};

			var ctx = $("canvas");

			var barGraph = new Chart(ctx, {
				borderWidth:2,
				type: 'bar',
				data: chartdata,
				 options : {
			      scales: {
			      	xAxes: [{
			      		gridlines: {
			      			display:false
			      		}
			      	}], 
			        yAxes: [{
			          scaleLabel: {
			            display: true,
			            labelString: 'Number of students'
			          }
			        }]
      }
    }
			});

		</script>


<!-- Comments as accordion
 -->	<div class="templateContainer">
		  <div class="panel panel-default template">
		    <div class="panel-heading" role="tab" id="headingOne">
		      
		        <a class="accordion-toggle" role="button" data-toggle="collapse" data-parent="#accordion" href="#collapseOne" aria-expanded="true" aria-controls="collapseOne">
		        </a>
		     
		    </div>
		    <div id="collapseOne" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="headingOne">
		      <div class="panel-body"></div>
		    </div>
		  </div>
		</div>
		<br><br>
		<div class="container">
		  <div class="panel-group" role="tablist" id="accordion">

		  </div>
		</div>
		 <script>
		 var actual_json = [{
		 	"checkpoint":1,
		 	"comments":[{
		 		"text":'blah',
		 		"id":170050048
		 	}, {
		 		"text":'blah-blah',
		 		"id":170050084
		 	}]
		 }, {
		 	"checkpoint":2,
		 	"comments":[{
		 		"text":'vlah',
		 		"id":170050045
		 	}, {
		 		"text":'vlah-vlah',
		 		"id":170050074
		 	}]
		 }, {
		 	"checkpoint":3,
		 	"comments":[{
		 		"text":'shit',
		 		"id":170050089
		 	}, {
		 		"text":'some more shit',
		 		"id":170050094
		 	}]
		 }];

		       var json = {
		        residus: [
		            {
		              name:'comments on checkpoint 1st', childs: [
		                {name:'170050048~ blah', childs: null},
		                {name:'170050059~ blah-blah', childs: null}
		              ]
		            },
		            {
		              name:'comments on checkpoint 2nd ', childs: [
		                {name:'170050048~ vlah', childs: null},
		                {name:'170050075~ vlah-vlah', childs: null}
		              ]
		            },
		            {
		              name:'comments on checkpoint 3rd', childs: [
		                {name:'170050079~ some shit', childs:null  }  , 
		                {name:'170050086~ some more shit', childs:null}           
		              ]
		            }]
		          };

		       
		    var collapseTemplate = Object.create(CollapseTemplate);
		    var params = {
		        templateSelector: 'div.templateContainer > div.template',
		        parentId: 'accordion'
		     };
		    
		    jQuery(document).ready( function() {

		      collapseTemplate.init(params);
		      collapseTemplate.load(json);
		      
		  });
		        

		    </script>


	</div>
</body>
</html>


