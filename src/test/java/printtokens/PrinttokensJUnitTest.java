package printtokens;

//Java imports
import java.beans.Transient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

//JUnit imports
//import static org.junit.*; Did not work for JUnit 5

// Imports for JUnit 5
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;


public class PrinttokensJUnitTest {

	private Printtokens pt;

	static int error = 0;
	static int keyword = 1;
	static int spec_symbol = 2;
	static int identifier = 3;
	static int num_constant = 41;
	static int str_constant = 42;
	static int char_constant = 43;
	static int comment = 5;
	
	@BeforeEach
	public void setUp()
	{
		pt = new Printtokens();
	}

	/***********************************************/
	/* NMAE:	open_character_stream          */
	/* INPUT:       a filename                     */
	/* OUTPUT:      a BufferedReader */
	/* DESCRIPTION: when not given a filename,     */
	/*              open stdin,otherwise open      */
	/*              the existed file               */
	/***********************************************/
	BufferedReader open_character_stream(String fname) {
		BufferedReader br = null;
		if (fname == null) {
			br = new BufferedReader(new InputStreamReader(System.in));
		} else {
			try {
				FileReader fr = new FileReader(fname);
				br = new BufferedReader(fr);
			} catch (FileNotFoundException e) {
				System.out.print("The file " + fname +" doesn't exists\n");
				e.printStackTrace();
			}
		}
		
		return null; 
	}

	@Test
	void test_open_character_stream_for_null()
	{
		BufferedReader br = pt.open_character_stream(null);

		//Assertion will fail on the orginal code because it returns null
		//If this assertion fails, the fault has been detected
		assertNotNull(br, "Buffered Reader should not be null");
	}

	@Test
	void test_open_character_stream_for_valid_file() throws IOException
	{
		File tempFile = File.createTempFile("test1", ".txt");
		//getAbsolutePath() will allow a tempFile called test1.txt to be created on your 
		//hard drive so that the JUnit test can be passed
		BufferedReader br = pt.open_character_stream(tempFile.getAbsolutePath());
		//If this assertion fails, the fault has been detected
		assertNotNull(br, "Buffered Reader should not be null when opening a valid file");
		tempFile.delete(); //tempFile deleted
	}

	@Test
	void test_open_character_stream_file_not_found()
	{
		BufferedReader br = pt.open_character_stream("this_file_does_not_exist.txt");
		assertNull(br, "Buffered Reader should be null when file is missing");
	}
	
	/**********************************************/
	/* NAME:	get_char                      */
	/* INPUT:       a BufferedReader      */
	/* OUTPUT:      a character (f2,remove"when EOF, return -1" in the comment) */
	/**********************************************/
	int get_char(BufferedReader br){
            int ch = 0;
	    try {
	    	br.mark(3); 
		ch= br.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return ch;
	}

	//No fault should be detected. The original code should produce the correct output
	// based on the given input.
	@Test
	void test_get_char_normal_read() throws IOException
	{
		StringReader sr = new StringReader("a");
		BufferedReader br = new BufferedReader(sr);

		int result = pt.get_char(br);

		assertEquals(97, result, "Should return ASCII 97 for character 'a'");
	}

	@Test
	void test_get_char_exception_returns_zero() throws IOException
	{
		StringReader sr = new StringReader("abcde");
		BufferedReader br = new BufferedReader(sr);

		//Close immediately to forece an IOException
		br.close();

		int result = pt.get_char(br);

		//If this assertion fails, the fault has been detected
		assertEquals(-1, result, "Method should return -1 on IOException, but it returned 0");
	}
	
	/***************************************************/
	/* NAME:      unget_char                           */
	/* INPUT:     a BufferedReader,a character */
	/* OUTPUT:    a character                          */
	/* DESCRIPTION: move backward  */
	/***************************************************/
	char unget_char (int ch,BufferedReader br) { 
	  try {
		br.reset();
	} catch (IOException e) {
		e.printStackTrace();
	}
		 return 0;
	}

	@Test
	void test_unget_char_returns_correct_character() throws IOException
	{
		StringReader sr = new StringReader("testing");
		BufferedReader br = new BufferedReader(sr);
		br.mark(10);
		br.read();

		char inputChar = 'a';
		char result = pt.unget_char(inputChar, br);

		//If this assertion fails, the fault has been detected
		assertEquals(inputChar, result, "unget_char should return the character that was passed in ('a')");
	}

	@Test
	void test_unget_char_exception()
	{
		StringReader sr = new StringReader("testing");
		BufferedReader br = new BufferedReader(sr);

		char inputChar = 'a';
		char result = pt.unget_char(inputChar, br);

		//If this assertion fails, the fault has been detected
		assertEquals(inputChar, result, "Should return \'a\' even if reset fails");
	}

	/********************************************************/
	/* NAME:	open_token_stream                       */
	/* INPUT:       a filename                              */
	/* OUTPUT:      a BufferedReader             */
	/* DESCRIPTION: when filename is EMPTY,choice standard  */
	/*              input device as input source            */
	/********************************************************/
	BufferedReader open_token_stream(String fname)
	{
		BufferedReader br;
	 if(fname.equals(null)) 
	    br=open_character_stream(null);
	 else
	    br=open_character_stream(fname);
	 return br;
	}
	
	/********************************************************/
	/* NAME :	get_token                               */
	/* INPUT: 	a BufferedReader          */
	/* OUTPUT:      a token string                                */
	/* DESCRIPTION: according the syntax of tokens,dealing  */
	/*              with different case  and get one token  */
	/********************************************************/
	String get_token(BufferedReader br)
	{ 
	  int i=0,j;
	  int id=0;
	  int res = 0;
	  char ch = '\0';
	 
	  StringBuilder sb = new StringBuilder();

	   try {
		   res = get_char(br);
		   if (res == -1) {
			   return null;
		   }
		   ch = (char)res;
		while(ch=='\t'||ch=='\n' || ch == '\r')     /* strip all blanks until meet characters */  
	      {
			res = get_char(br);
			ch = (char)res;
	      } 
	   
	   if(res == -1)return null;
	   sb.append(ch);
	   if(is_spec_symbol(ch)==true)return sb.toString(); 
	   if(ch =='"')id=2;    /* prepare for string */  
	   if(ch ==59)id=1;    /* prepare for comment */    
	   
	   res = get_char(br);
	   if (res == -1) {
		   unget_char(ch,br);
		   return sb.toString();
	   }
	   ch = (char)res;

	   while (is_token_end(id,res) == false)/* until meet the end character */
	   {
	       sb.append(ch);
	       br.mark(4);
	       res = get_char(br);
		   if (res == -1) {
			   break;
		   }
		   ch = (char)res;
	   }
	 
	   if(res == -1)       /* if end character is eof token    */
	      { unget_char(ch,br);        /* then put back eof on token_stream */
	        return sb.toString();
	      }
	 
	   if(is_spec_symbol(ch)==true)     /* if end character is special_symbol */
	      { unget_char(ch,br);        /* then put back this character       */
	        return sb.toString();
	      }
	   if(id==1)                  /* if end character is " and is string */
	     {                     
	       sb.append(ch);
	       return sb.toString(); 
	     }
	   if(id==0 && ch==59)
	                                   /* when not in string or comment,meet ";" */
	     { unget_char(ch,br);       /* then put back this character         */
	       return sb.toString(); 
	     }
	} catch (IOException e) {
		e.printStackTrace();
	}
	   
	   return sb.toString();                   /* return nomal case token             */
	}
	
	/*******************************************************/
	/* NAME:	is_token_end                           */
	/* INPUT:       a character,a token status             */
	/* OUTPUT:	a BOOLEAN value                        */
	/*******************************************************/
	static boolean is_token_end(int str_com_id, int res)
	{
	 if(res==-1)return(true); /* is eof token? */
	 char ch = (char)res;
	 if(str_com_id==1)          /* is string token */
	    { if(ch=='"' | ch=='\n' || ch == '\r')   /* for string until meet another " */
	         return true;
	      else
	         return false;
	    }

	 if(str_com_id==2)    /* is comment token */
	   { if(ch=='\n' || ch == '\r' || ch=='\t')     /* for comment until meet end of line */ 
	        return true;
	      else
	        return false;
	   }

	 if(is_spec_symbol(ch)==true) return true; /* is special_symbol? */
	 if(ch ==' ' || ch=='\n'|| ch=='\r' || ch==59) return true; 
	               
	 return false;               /* other case,return FALSE */
	}

	@Test
	void test_is_token_end_paths()
	{
		//Test EOF
		assertTrue(Printtokens.is_token_end(0, -1), "EOF should end token");

		//Test string ends with quote
		assertTrue(Printtokens.is_token_end(1, 34), "Quote should end string token");

		//Test string that continues with 'a'
		assertFalse(Printtokens.is_token_end(1, 97), "char should not end string token");

		//Test comment that ends with newline
		assertTrue(Printtokens.is_token_end(2, 10), "Newline should end comment token");

		//Test comment that continues with 'x'
		assertFalse(Printtokens.is_token_end(2, 120), "char should not end comment token");

		//Test special symbol '('
		assertTrue(Printtokens.is_token_end(0, 40), "Special symbol should end token");

		//Test whitespace ' '
		assertTrue(Printtokens.is_token_end(0, 32), "Space should end token");

		//Test a normal character like 'k'
		assertFalse(Printtokens.is_token_end(0, 107), "Normal character should not end token");
	}
	
	/****************************************************/
	/* NAME :	token_type                          */
	/* INPUT:       a token              */
	/* OUTPUT:      an integer value                    */
	/* DESCRIPTION: the integer value is corresponding  */
	/*              to the different token type         */
	/****************************************************/
	static int token_type(String tok)
	{ 
	 if(is_keyword(tok))return(keyword);
	 if(is_spec_symbol(tok.charAt(0)))return(spec_symbol);
	 if(is_identifier(tok))return(identifier);
	 if(is_num_constant(tok))return(num_constant);
	 if(is_str_constant(tok))return(str_constant);
	 if(is_char_constant(tok))return(char_constant);
	 if(is_comment(tok))return(comment);
	 return(error);                    /* else look as error token */
	}

	@Test
	void test_token_type_paths()
	{
		//Test keyword
		assertEquals(1, Printtokens.token_type("if"), "Path {1,2} failed");	

		//Test sepcial symbol
		assertEquals(2, Printtokens.token_type("("), "Path {1,3,4} failed");

		//Test idetifier
		assertEquals(3, Printtokens.token_type("alpha"), "Path {1,3,5,6} failed");

		//Test numeric constant
		assertEquals(4, Printtokens.token_type("123"), "Path {1,3,5,7,8} failed");
		
		//Test string constant
		assertEquals(5, Printtokens.token_type("\"hi\""), "Path {1,3,5,7,9,10} failed");

		//Test character constant
		assertEquals(6, Printtokens.token_type("'a'"), "Path {1,3,5,7,9,11,12} failed");

		//Test comment
		assertEquals(7, Printtokens.token_type(";msg"), "Path {1,3,5,7,9,11,13,14} failed");

		//Test empty string
		assertEquals(0, Printtokens.token_type(""), "Path {1,3,5,7,9,11,13,15} should return error 0");
	}
	
	/****************************************************/
	/* NAME:	print_token                             */
	/* INPUT:	a token                                 */
	/****************************************************/
	void print_token(String tok)
	{ int type;
	  type=token_type(tok);
	 if(type==error)
	   { 
	   	System.out.print("error,\"" + tok + "\".\n");
	   }
	   
	 if(type==keyword)
	   {
	   System.out.print("keyword,\"" + tok + "\".\n");
	   }
	  
	 if(type==spec_symbol)print_spec_symbol(tok);
	 if(type==identifier)
	   {
	   System.out.print("identifier,\"" + tok + "\".\n");
	   }
	 if(type==num_constant)
	   {
	   System.out.print("numeric," + tok + ".\n");
	   }

	 
	 if(type==char_constant)
	   {
	    System.out.print("character,\"" + tok.charAt(1) + "\".\n");
	   }

	   }

	/* the code for tokens judgment function */

	public class PrintTokenTest 
	{
		private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
		private final PrintStream originalOut = System.out;

		@BeforeEach
		public void setUpStreams() 
		{
			System.setOut(new PrintStream(outContent));
		}

		@AfterEach
		public void restoreStreams() 
		{
			System.setOut(originalOut);
		}

		@Test
		void test_print_token_fault_path() 
		{
			//Testing using a string constant
			pt.print_token("\"hello\"");
			
			//If this assertion fails, the fault has been detected
			String expected = "string,\"\"hello\"\".\n";
			assertEquals(expected, outContent.toString(), "Should print string constant info");
		}

		@Test
		void test_print_token_keyword_path() 
		{
			//Testing keyword
			pt.print_token("if");
			
			String expected = "keyword,\"if\".\n";
			assertEquals(expected, outContent.toString());
		}  
	}

	/*************************************/
	/* NAME:	is_comment           */
	/* INPUT: 	a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_comment(String ident)
	{
	  if( ident.charAt(0) ==59 )   /* the char is 59   */
	     return true;
	  else
	     return false;
	}


	@Test
	void test_is_comment()
	{
        boolean result = pt.is_comment(";this is a comment");
        assertTrue(result, "Should return true for strings starting with ';'");
    }

	@Test
    void test_not_is_comment() 
	{
        boolean result = pt.is_comment("variableName");
        assertFalse(result, "Should return false for strings not starting with ';'");
    }

	@Test
    void test_is_comment_empty_string_fault()
	{
        // Description: An empty string should simply not be a comment.
        //If this assertion fails, the fault has been detected
        try 
		{
            boolean result = Printtokens2.is_comment("");
            assertFalse(result, "Empty string should return false, not crash.");
        } 
		catch (StringIndexOutOfBoundsException e) 
		{
            fail("The method crashed with an empty string! Fault detected.");
        }
    }

	/*************************************/
	/* NAME:	is_keyword           */
	/* INPUT: 	a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_keyword(String str)
	{ 
	 if (str.equals("and") || str.equals("or") || str.equals("if") ||
			 str.equals("xor")||str.equals("lambda")||str.equals("=>"))
	      return true;
	  else 
	      return false;
	}

	@Test
    void test_is_keyword_() 
	{
        boolean result = Printtokens2.is_keyword("if");
        assertTrue(result, "Path {1, 2} failed: 'if' should be a keyword.");
    }


    @Test
    void test_is_keyword_missing_not() 
	{
        boolean result = Printtokens2.is_keyword("not");
        
        //If this assertion fails, the fault has been detected
        assertTrue(result, "Fault Detected: 'not' is a keyword but returned false.");
    }

	@Test
    void test_is_keyword_case_insensitive() 
	{
        // Input "IF" is not in the code because it uses .equals()
        boolean result = Printtokens2.is_keyword("IF");
        
        //If this assertion fails, the fault has been detected
        assertTrue(result, "Fault Detected: Keywords should be case-insensitive.");
	}

	/*************************************/
	/* NAME:	is_char_constant     */
	/* INPUT: 	a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_char_constant(String str)
	{
	  if (str.length() > 2 && str.charAt(0)=='#' && Character.isLetter(str.charAt(1)))  
	     return true;
	  else  
	     return false;
	}
	
	/*************************************/
	/* NAME:	is_num_constant      */
	/* INPUT: 	a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_num_constant(String str)
	{
	  int i=1;
	  
	  if ( Character.isDigit(str.charAt(0))) 
	    {
	    while ( i <= str.length() && str.charAt(i) != '\0' )   /* until meet token end sign */ 
	      {
	       if(Character.isDigit(str.charAt(i+1)))	 
	         i++;
	       else
	         return false;
	      }                         /* end WHILE */
	    return true;
	    }
	  else
	   return false;               /* other return FALSE */
	}
	
	/*************************************/
	/* NAME:	is_str_constant      */
	/* INPUT: 	a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_str_constant(String str)
	{
	  int i=1;
	 
	  if ( str.charAt(0) =='"')
	     { while (i < str.length() && str.charAt(0)!='\0')  /* until meet the token end sign */
	         { if(str.charAt(i)=='"')
	             return true;        /* meet the second '"'           */
	           else
	           i++;
	         }               /* end WHILE */
	     return true;	
	    }
	  else
	    return false;       /* other return FALSE */
	}
	
	/*************************************/
	/* NAME:	is_identifier         */
	/* INPUT: 	a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_identifier(String str)
	{
	  int i=0; 

	  if ( Character.isLetter(str.charAt(0)) ) 
	     {
	        while(i < str.length() && str.charAt(i) !='\0' )   /* unti meet the end token sign */
	           { 
	            if(Character.isLetter(str.charAt(i)) || Character.isDigit(str.charAt(i)))   
	               i++;
	            else
	               return false;
	           }      /* end WHILE */
	     return false; 
	     }
	  else
	     return true; 
	}

	@Test
    void test_is_identifer_starts_with_digit() 
	{
        boolean result = Printtokens2.is_identifier("123");
        
        //If this assertion fails, the fault has been detected
        assertFalse(result, "Fault Detected: '123' should NOT be an identifier.");
    }

	@Test
    void test_is_identifier_incorrect_return()
	{
        boolean result = Printtokens2.is_identifier("abc");
        
        //If this assertion fails, the fault has been detected
        assertTrue(result, "Fault Detected: 'abc' SHOULD be a valid identifier.");
    }
	
	@Test
    void test_is_identifier_special_character() 
	{
        boolean result = Printtokens2.is_identifier("a#b");
        assertFalse(result, "An identifier should not contain special characters.");
    }

	/******************************************/
	/* NAME:	unget_error               */
	/* INPUT:      a BufferedReader */
	/* OUTPUT: 	print error message       */
	/******************************************/
	static void unget_error(BufferedReader br)
	{
		System.out.print("It can not get charcter\n");
	}
	
	/*************************************************/
	/* NAME:        print_spec_symbol                */
	/* INPUT:       a spec_symbol token */
	/* OUTPUT :     print out the spec_symbol token  */
	/*              according to the form required   */
	/*************************************************/
	static void print_spec_symbol(String str)
	{
	    if      (str.equals("{")) 
	    {
	         
	             System.out.print("lparen.\n");
	             return;
	    } 
	    if (str.equals(")"))
	    {
	      
	             System.out.print("rparen.\n");
	             return;
	    }
	    if (str.equals("["))
	    {
	             System.out.print("lsquare.\n");
	             return;
	    }
	    if (str.equals("]"))
	    {
	       
	             System.out.print("rsquare.\n");
	             return;
	    }
	    if (str.equals("'"))
	    {
	             System.out.print("quote.\n");
	             return;
	    }
	    if (str.equals("`"))
	    {
	 
	             System.out.print("bquote.\n");
	             return;
	    }
	    
	    
	}
	
	/*************************************/
	/* NAME:        is_spec_symbol       */
	/* INPUT:       a token */
	/* OUTPUT:      a BOOLEAN value      */
	/*************************************/
	static boolean is_spec_symbol(char c)
	{
	    if (c == '(')
	    {  
	        return true;
	    }
	    if (c == ')')
	    {
	        return true;
	    }
	    if (c == '[')
	    {
	        return true;
	    }
	    if (c == ']')
	    {
	        return true;
	    }
	    if (c == '/') 
	    {
	        return true;
	    }
	    if (c == '`')
	    {
	        return true;
	    }
	    if (c == ',')
	    {
	        return true;
	    }
	    return false;     /* others return FALSE */
	}

	@Test
    void test_is_spec_symbol_valid_symbols() 
	{
        assertTrue(Printtokens2.is_spec_symbol('('), "Path {1, 2} failed");

        assertTrue(Printtokens2.is_spec_symbol(')'), "Path {1, 3, 4} failed");

        assertTrue(Printtokens2.is_spec_symbol('['), "Path {1, 3, 5, 6} failed");

        assertTrue(Printtokens2.is_spec_symbol(']'), "Path {1, 3, 5, 7, 8} failed");

        assertTrue(Printtokens2.is_spec_symbol('/'), "Path {1, 3, 5, 7, 9, 10} failed");

        assertTrue(Printtokens2.is_spec_symbol('`'), "Path {1, 3, 5, 7, 9, 11, 12} failed");

        assertTrue(Printtokens2.is_spec_symbol(','), "Path {1, 3, 5, 7, 9, 11, 13, 14} failed");

        // Use ';' because it is a special symbol that should return true.
        //If this assertion fails, the fault has been detected
        assertTrue(Printtokens2.is_spec_symbol(';'), "Fault Detected: Semicolon should be a special symbol.");
    }
    
    @Test
    void test_is_spec_symbol_valid_symbol_not_included() 
	{
        // Additional check for the false path using a letter
        assertFalse(Printtokens2.is_spec_symbol('a'), "Letters cannot not be special symbols.");
    }
	
	public static void main(String[] args) throws IOException {
		String fname = null;
		if (args.length == 0) {	/* if not given filename,take as '""' */
			fname = new String();
		} else if (args.length == 1) {
			fname = args[1]; 
		} else {
			System.out.print("Error!,please give the token stream\n");
			System.exit(0);
		}
		Printtokens t = new Printtokens();
		BufferedReader br = t.open_token_stream(fname);	/* open token stream */
		String tok = t.get_token(br);
		while (tok != null) {	/* take one token each time until eof */
			t.print_token(tok);
			tok = t.get_token(br);
		}
		
		System.exit(0);
	}
}
