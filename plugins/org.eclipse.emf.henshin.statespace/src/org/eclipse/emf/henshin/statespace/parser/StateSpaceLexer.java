// $ANTLR 3.0.1 StateSpace.g 2010-01-19 12:06:51

package org.eclipse.emf.henshin.statespace.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class StateSpaceLexer extends Lexer {
    public static final int RULE_CMD=13;
    public static final int INT=16;
    public static final int SEMICOLON=12;
    public static final int ID=14;
    public static final int EOF=-1;
    public static final int Tokens=19;
    public static final int LINE=4;
    public static final int LPAREN=7;
    public static final int LBRACKET=9;
    public static final int RPAREN=8;
    public static final int WS=17;
    public static final int COMMA=11;
    public static final int EQUAL=6;
    public static final int ARROW=5;
    public static final int RBRACKET=10;
    public static final int EscapeSequence=18;
    public static final int STRING=15;
    public StateSpaceLexer() {;} 
    public StateSpaceLexer(CharStream input) {
        super(input);
    }
    public String getGrammarFileName() { return "StateSpace.g"; }

    // $ANTLR start LINE
    public final void mLINE() throws RecognitionException {
        try {
            int _type = LINE;
            // StateSpace.g:6:6: ( '--' )
            // StateSpace.g:6:8: '--'
            {
            match("--"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LINE

    // $ANTLR start ARROW
    public final void mARROW() throws RecognitionException {
        try {
            int _type = ARROW;
            // StateSpace.g:7:7: ( '->' )
            // StateSpace.g:7:9: '->'
            {
            match("->"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ARROW

    // $ANTLR start EQUAL
    public final void mEQUAL() throws RecognitionException {
        try {
            int _type = EQUAL;
            // StateSpace.g:8:7: ( '=' )
            // StateSpace.g:8:9: '='
            {
            match('='); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end EQUAL

    // $ANTLR start LPAREN
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            // StateSpace.g:9:8: ( '(' )
            // StateSpace.g:9:10: '('
            {
            match('('); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LPAREN

    // $ANTLR start RPAREN
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            // StateSpace.g:10:8: ( ')' )
            // StateSpace.g:10:10: ')'
            {
            match(')'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RPAREN

    // $ANTLR start LBRACKET
    public final void mLBRACKET() throws RecognitionException {
        try {
            int _type = LBRACKET;
            // StateSpace.g:11:10: ( '[' )
            // StateSpace.g:11:12: '['
            {
            match('['); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end LBRACKET

    // $ANTLR start RBRACKET
    public final void mRBRACKET() throws RecognitionException {
        try {
            int _type = RBRACKET;
            // StateSpace.g:12:10: ( ']' )
            // StateSpace.g:12:12: ']'
            {
            match(']'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RBRACKET

    // $ANTLR start COMMA
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            // StateSpace.g:13:7: ( ',' )
            // StateSpace.g:13:9: ','
            {
            match(','); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end COMMA

    // $ANTLR start SEMICOLON
    public final void mSEMICOLON() throws RecognitionException {
        try {
            int _type = SEMICOLON;
            // StateSpace.g:14:11: ( ';' )
            // StateSpace.g:14:13: ';'
            {
            match(';'); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SEMICOLON

    // $ANTLR start RULE_CMD
    public final void mRULE_CMD() throws RecognitionException {
        try {
            int _type = RULE_CMD;
            // StateSpace.g:15:10: ( '#rule' )
            // StateSpace.g:15:12: '#rule'
            {
            match("#rule"); 


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end RULE_CMD

    // $ANTLR start ID
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            // StateSpace.g:157:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // StateSpace.g:157:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            // StateSpace.g:157:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // StateSpace.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end ID

    // $ANTLR start INT
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            // StateSpace.g:158:5: ( ( '0' .. '9' )+ )
            // StateSpace.g:158:7: ( '0' .. '9' )+
            {
            // StateSpace.g:158:7: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // StateSpace.g:158:7: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INT

    // $ANTLR start WS
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            // StateSpace.g:159:5: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // StateSpace.g:159:9: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // StateSpace.g:159:9: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='\t' && LA3_0<='\n')||LA3_0=='\r'||LA3_0==' ') ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // StateSpace.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);

             skip(); 

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WS

    // $ANTLR start STRING
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            // StateSpace.g:160:8: ( '\"' ( EscapeSequence | ~ ( '\\\\' | '\"' | '\\r' | '\\n' ) )* '\"' )
            // StateSpace.g:161:3: '\"' ( EscapeSequence | ~ ( '\\\\' | '\"' | '\\r' | '\\n' ) )* '\"'
            {
            match('\"'); 
            // StateSpace.g:162:3: ( EscapeSequence | ~ ( '\\\\' | '\"' | '\\r' | '\\n' ) )*
            loop4:
            do {
                int alt4=3;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='\\') ) {
                    alt4=1;
                }
                else if ( ((LA4_0>='\u0000' && LA4_0<='\t')||(LA4_0>='\u000B' && LA4_0<='\f')||(LA4_0>='\u000E' && LA4_0<='!')||(LA4_0>='#' && LA4_0<='[')||(LA4_0>=']' && LA4_0<='\uFFFE')) ) {
                    alt4=2;
                }


                switch (alt4) {
            	case 1 :
            	    // StateSpace.g:162:7: EscapeSequence
            	    {
            	    mEscapeSequence(); 

            	    }
            	    break;
            	case 2 :
            	    // StateSpace.g:163:13: ~ ( '\\\\' | '\"' | '\\r' | '\\n' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match('\"'); 

                    	// Automatically remove the surrounding quotes: 
                    	setText( getText().substring(1,getText().length()-1));
                    	

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end STRING

    // $ANTLR start EscapeSequence
    public final void mEscapeSequence() throws RecognitionException {
        try {
            // StateSpace.g:171:15: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' | ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | ( '0' .. '7' ) ( '0' .. '7' ) | ( '0' .. '7' ) ) )
            // StateSpace.g:172:3: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' | ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | ( '0' .. '7' ) ( '0' .. '7' ) | ( '0' .. '7' ) )
            {
            match('\\'); 
            // StateSpace.g:172:8: ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' | ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | ( '0' .. '7' ) ( '0' .. '7' ) | ( '0' .. '7' ) )
            int alt5=11;
            switch ( input.LA(1) ) {
            case 'b':
                {
                alt5=1;
                }
                break;
            case 't':
                {
                alt5=2;
                }
                break;
            case 'n':
                {
                alt5=3;
                }
                break;
            case 'f':
                {
                alt5=4;
                }
                break;
            case 'r':
                {
                alt5=5;
                }
                break;
            case '\"':
                {
                alt5=6;
                }
                break;
            case '\'':
                {
                alt5=7;
                }
                break;
            case '\\':
                {
                alt5=8;
                }
                break;
            case '0':
            case '1':
            case '2':
            case '3':
                {
                int LA5_9 = input.LA(2);

                if ( ((LA5_9>='0' && LA5_9<='7')) ) {
                    int LA5_11 = input.LA(3);

                    if ( ((LA5_11>='0' && LA5_11<='7')) ) {
                        alt5=9;
                    }
                    else {
                        alt5=10;}
                }
                else {
                    alt5=11;}
                }
                break;
            case '4':
            case '5':
            case '6':
            case '7':
                {
                int LA5_10 = input.LA(2);

                if ( ((LA5_10>='0' && LA5_10<='7')) ) {
                    alt5=10;
                }
                else {
                    alt5=11;}
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("172:8: ( 'b' | 't' | 'n' | 'f' | 'r' | '\\\"' | '\\'' | '\\\\' | ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' ) | ( '0' .. '7' ) ( '0' .. '7' ) | ( '0' .. '7' ) )", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // StateSpace.g:173:18: 'b'
                    {
                    match('b'); 

                    }
                    break;
                case 2 :
                    // StateSpace.g:174:18: 't'
                    {
                    match('t'); 

                    }
                    break;
                case 3 :
                    // StateSpace.g:175:18: 'n'
                    {
                    match('n'); 

                    }
                    break;
                case 4 :
                    // StateSpace.g:176:18: 'f'
                    {
                    match('f'); 

                    }
                    break;
                case 5 :
                    // StateSpace.g:177:18: 'r'
                    {
                    match('r'); 

                    }
                    break;
                case 6 :
                    // StateSpace.g:178:18: '\\\"'
                    {
                    match('\"'); 

                    }
                    break;
                case 7 :
                    // StateSpace.g:179:18: '\\''
                    {
                    match('\''); 

                    }
                    break;
                case 8 :
                    // StateSpace.g:180:18: '\\\\'
                    {
                    match('\\'); 

                    }
                    break;
                case 9 :
                    // StateSpace.g:181:18: ( '0' .. '3' ) ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    // StateSpace.g:181:18: ( '0' .. '3' )
                    // StateSpace.g:181:19: '0' .. '3'
                    {
                    matchRange('0','3'); 

                    }

                    // StateSpace.g:181:29: ( '0' .. '7' )
                    // StateSpace.g:181:30: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // StateSpace.g:181:40: ( '0' .. '7' )
                    // StateSpace.g:181:41: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 10 :
                    // StateSpace.g:182:18: ( '0' .. '7' ) ( '0' .. '7' )
                    {
                    // StateSpace.g:182:18: ( '0' .. '7' )
                    // StateSpace.g:182:19: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }

                    // StateSpace.g:182:29: ( '0' .. '7' )
                    // StateSpace.g:182:30: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;
                case 11 :
                    // StateSpace.g:183:18: ( '0' .. '7' )
                    {
                    // StateSpace.g:183:18: ( '0' .. '7' )
                    // StateSpace.g:183:19: '0' .. '7'
                    {
                    matchRange('0','7'); 

                    }


                    }
                    break;

            }


            }

        }
        finally {
        }
    }
    // $ANTLR end EscapeSequence

    public void mTokens() throws RecognitionException {
        // StateSpace.g:1:8: ( LINE | ARROW | EQUAL | LPAREN | RPAREN | LBRACKET | RBRACKET | COMMA | SEMICOLON | RULE_CMD | ID | INT | WS | STRING )
        int alt6=14;
        switch ( input.LA(1) ) {
        case '-':
            {
            int LA6_1 = input.LA(2);

            if ( (LA6_1=='-') ) {
                alt6=1;
            }
            else if ( (LA6_1=='>') ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("1:1: Tokens : ( LINE | ARROW | EQUAL | LPAREN | RPAREN | LBRACKET | RBRACKET | COMMA | SEMICOLON | RULE_CMD | ID | INT | WS | STRING );", 6, 1, input);

                throw nvae;
            }
            }
            break;
        case '=':
            {
            alt6=3;
            }
            break;
        case '(':
            {
            alt6=4;
            }
            break;
        case ')':
            {
            alt6=5;
            }
            break;
        case '[':
            {
            alt6=6;
            }
            break;
        case ']':
            {
            alt6=7;
            }
            break;
        case ',':
            {
            alt6=8;
            }
            break;
        case ';':
            {
            alt6=9;
            }
            break;
        case '#':
            {
            alt6=10;
            }
            break;
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case '_':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            alt6=11;
            }
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            {
            alt6=12;
            }
            break;
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            {
            alt6=13;
            }
            break;
        case '\"':
            {
            alt6=14;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens : ( LINE | ARROW | EQUAL | LPAREN | RPAREN | LBRACKET | RBRACKET | COMMA | SEMICOLON | RULE_CMD | ID | INT | WS | STRING );", 6, 0, input);

            throw nvae;
        }

        switch (alt6) {
            case 1 :
                // StateSpace.g:1:10: LINE
                {
                mLINE(); 

                }
                break;
            case 2 :
                // StateSpace.g:1:15: ARROW
                {
                mARROW(); 

                }
                break;
            case 3 :
                // StateSpace.g:1:21: EQUAL
                {
                mEQUAL(); 

                }
                break;
            case 4 :
                // StateSpace.g:1:27: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 5 :
                // StateSpace.g:1:34: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 6 :
                // StateSpace.g:1:41: LBRACKET
                {
                mLBRACKET(); 

                }
                break;
            case 7 :
                // StateSpace.g:1:50: RBRACKET
                {
                mRBRACKET(); 

                }
                break;
            case 8 :
                // StateSpace.g:1:59: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 9 :
                // StateSpace.g:1:65: SEMICOLON
                {
                mSEMICOLON(); 

                }
                break;
            case 10 :
                // StateSpace.g:1:75: RULE_CMD
                {
                mRULE_CMD(); 

                }
                break;
            case 11 :
                // StateSpace.g:1:84: ID
                {
                mID(); 

                }
                break;
            case 12 :
                // StateSpace.g:1:87: INT
                {
                mINT(); 

                }
                break;
            case 13 :
                // StateSpace.g:1:91: WS
                {
                mWS(); 

                }
                break;
            case 14 :
                // StateSpace.g:1:94: STRING
                {
                mSTRING(); 

                }
                break;

        }

    }


 

}