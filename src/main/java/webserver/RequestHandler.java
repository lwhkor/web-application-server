package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
        	
        	//1. index.html 응답하기
        	BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        	String reqMethod = "";
        	String requestLine = "";
        	String url = "";
        	String requestPath = "";
        	String params = "";
        	Map<String, String> paramMap;
        	User user;
        	
        	while(true) {
        		requestLine = br.readLine();
        		
        		if(null == requestLine) {
        			return;
        		}
        		
        		if("".equals(requestLine)) {
        			break;
        		}
        		
        		log.info(requestLine);
        		
        		if(url.equals("")) {
        			url = HttpRequestUtils.parseUrl(requestLine);
        		}
        	}
        	
        	//2.GET 방식으로 회원가입하기
        	if(url.startsWith("/user/create")) {
        		int index = url.indexOf("?");
        		requestPath = url.substring(0, index);
        		params = url.substring(index+1);
        		paramMap = HttpRequestUtils.parseQueryString(params);
        		user = new User(paramMap.get("userId"),paramMap.get("password"),paramMap.get("name"),paramMap.get("email"));
        	} else {
	        	byte[] body;
	        	if(!url.equals("")) {
	        		body = Files.readAllBytes(new File("./webapp" + url).toPath());
	    		} else {
	    			body = "Hello World".getBytes();
	    		}
	        	
	            DataOutputStream dos = new DataOutputStream(out);
	            
	            response200Header(dos, body.length);
	            responseBody(dos, body);
        	}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
}
