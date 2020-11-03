import pyodbc, csv, sys, json
with open(sys.argv[1], 'r') as fh:
	server = 'LAPTOP-DJ46JC9S'
	database = 'voodle'
	username = 'voodle'
	password = 'KanekiK'
	try:
		conn = pyodbc.connect('DRIVER={ODBC Driver 17 for SQL Server};SERVER=' + server + ';DATABASE=' + database + ';UID=' + username + ';PWD=' + password)
	except:
		print("Error: Server Unreachable")
		exit()
	cursor = conn.cursor()
	file = csv.reader(fh)
	students = []
	try:
		for name in file:
			try:
				temp = int(name[0])
				cursor.execute('SELECT [name], LDAP FROM students WHERE ([name] = ? OR LDAP = ?) AND LDAP != 1', [name[0], name[0]])
			except:
				cursor.execute('SELECT [name], LDAP FROM students WHERE [name] = ? AND LDAP != 1', [name[0]])
			ldaps = cursor.fetchall()
			if len(ldaps) == 0:
				raise Exception('Error: No student found with the name/ldap = \''+name[0]+'\'')
			elif len(ldaps) != 1:
				raise Exception('Server Error!')
			students.append({'name': ldaps[0][0], 'ldap': ldaps[0][1]})
		print(json.dumps(list(students)))
	except Exception as e:
		print(e)