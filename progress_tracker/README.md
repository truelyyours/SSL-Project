# Progress Tracker

# `Goals`

> Start Android development  
> Add Graphs for during session  
> The session also has some checkpoints.  
> The session ends automatically according to the time given by the prof.  
> Display output.  
> Have a clear idea of what to do ahead in the project and how to do it.  

***
***

## Things completed:
### Web
> &#10003; login page finished  
> 
> &#10003; `WYSIWYG` text editor added (basic)  
>  
> &#10003; Can add any number of questions of 3 different types with optional correct choices for each question  
>  
> &#10003; Session starts and ends automatically at the desired time  
>  
> &#10003; Each Session is assigned a UNIQUE session key to enter a session and no student can enter any session without having its private key
>  
> &#10003; Students can be added manually / (using a csv) and are automatically given private keys corresponding to each session using the database  
>  
> &#10003; No two students have same key for same session to prevent passing of key from let's say a student in the examination hall to a student sitting in his hostel, while the instructor has facility to generate keys for every student for his session. *Approach Used* &mdash; `student_key = md5(session_key + LDAP)`  

***
### Android
> &#10003; User login and *remember me* added

***
## Things to do:

### Web


* Add facility for previous assignment results

***
### Android

* `Register User`

***

#### Possible extra things:
* Parse text editor data to prevent `script injection`
* Background for login page in `android`

***

Guest Account credentials - `Username - admin, Password - admin`