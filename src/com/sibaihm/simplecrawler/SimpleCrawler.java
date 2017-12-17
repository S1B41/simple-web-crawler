package com.sibaihm.simplecrawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;

/*
 * consists of four methods:
 *   (1) crawl which only invokes
 *   (2) traverse which invokes
 *   (3) extractURLsFromHTML and
 *   (4) store
 */

public class SimpleCrawler {
	
	/*
	 * stores the visited urls, so that traverse method can avoid traversing the same url again
	 */
	
	private List<String> visitedURLs = new ArrayList<>();
	
	/*
	 * stores the domain name in the form of: example.com
	 */
	
	private String domain;
	
	/*
	 * stores the target url which is the root url of the website 
	 */
	
	private String targetURL;
	
	/*
	 * stores the number of unique pages on the website
	 */
	
	private int numOfUniqueURLs;
	
	/*
	 * stores the number of all urls on the website
	 */
	
	private int numOfAllURLs;
	
	/*
	 * the constructor
	 */
	
	public SimpleCrawler(String targetURL) {
		this.targetURL 	= targetURL;
		domain 		= targetURL.replace("https://", "").replace("http://", "").replace("www.", "");
	}
	
	/*
	 * the main method of the class which the user can invoke to start the traversing
	 * it only invokes the traversing method then prints the results on the console
	 */
	
	public void crawl() {
		
		traverse(targetURL);
		
		/*
		 * printing the found urls on the website
		 */
		
		System.out.println("Number of all URLs: " + numOfAllURLs);
		
		System.out.println("Number of unique URLs: " + numOfUniqueURLs);
	}
	
	/*
	 * the most important method of the class
	 * traverse a website tree starting with the root url
	 * it invokes the store method, which stores the HTML pages onto the file system
	 */
	
	private void traverse(String targetURL) {
		
		/*
		 * if the targetURL is already visited
		 * return and don't continue 
		 */
		
		if(visitedURLs.contains(targetURL)) {
			return;
		}
		
		/*
		 * otherwise continue traversing
		 */
		
		try {
			
			/*
			 * add the url to the visisted urls list
			 */
			
			visitedURLs.add(targetURL);
			
			/*
			 * increase the number of visited urls
			 * to print it at the end as a number of unique pages on the website 
			 */
			
			numOfUniqueURLs++;
			
			/*
			 * initialize the url and the http or https requests
			 */

			URL url = new URL(targetURL);
			
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

			if(url.getProtocol().equals("https")) {
				httpConn = (HttpsURLConnection) url.openConnection();
			}
			
			/*
			 * set request header elements
			 */

			httpConn.setRequestMethod("POST");
			
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			httpConn.setDoOutput(true);
			
			/*
			 * send the request
			 */
			
			OutputStream outputStream = httpConn.getOutputStream();
			
			outputStream.flush();
			
			outputStream.close();
			
			/*
			 * get the response code from the request
			 */
			
			int responseCode = httpConn.getResponseCode();
			
			/*
			 * if response code is 200
			 * store the HTML page
			 * extract all urls from it
			 * continue traversing the website 
			 */
			
			if (responseCode == HttpURLConnection.HTTP_OK) {
				
				/*
				 * read the response 
				 */
				
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
				
				StringBuffer response = new StringBuffer();
				
				String inputLine;

				while ((inputLine = bufferedReader.readLine()) != null) {
					
					response.append(inputLine);
				}
				
				/*
				 * close the reader 
				 */
				
				bufferedReader.close();
				
				/*
				 * store the HTML page onto the file system
				 */
				
				store(response.toString(), targetURL);
				
				/*
				 * extract all urls in the HTML page
				 */
				
				List<String> urls = extractURLsFromHTML(response.toString());
				
				/*
				 * store all found urls in variable,
				 * it is not necessary of course, it is only printed in the results
				 */
				
				numOfAllURLs += urls.size();
				
				/*
				 * iterate through all found urls 
				 */
				
				for(String link : urls) {
					traverse(link);
				}
				
			/*
			 * else if the response code other than 200	
			 */
				
			} else {
				
				System.out.println("POST request not worked for: " + targetURL + " response code: " + responseCode);
			}
			
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		} catch (ProtocolException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	/*
	 * store the HTML page into a directory on the desktop
	 */
	
	private void store(String HTMLPage, String fileName) {
				
		try {
			
			/*
			 * the path of the directory in which the files will be stored
			 */
			
			String pathToDir = System.getProperty("user.home") + "\\Desktop\\" + domain + "/";
			
			/*
			 * create the directory 
			 */
			
			new File(pathToDir).mkdir();
			
			/*
			 * the file path
			 * remove the protocol and host name from the file name
			 */
			
			fileName = fileName.replace("https://", "").replace("http://",  "").replace(domain, "");
			
			/*
			 * remove the slash - if exists - from the beginning of the file name
			 */
			
			if(fileName.startsWith("/")) fileName = fileName.substring(1, fileName.length());
			
			/*
			 * remove the slash - if exists - from the end of the file name
			 */
			
			if(fileName.endsWith("/")) fileName = fileName.substring(0, fileName.length()-1);
			
			/*
			 * create sub-directory if necessary
			 * after removing any slashes in the previous statements, if the next condition is true
			 * that means there are sub-directories 
			 */
			
			if(fileName.contains("/")) {
				
				/*
				 * split the url by the slash 
				 */
				
				String[] subDir = fileName.split("/");
				
				/*
				 * take a copy of the main directory
				 * to create sub-directories based on it
				 * the new copy will be the main and the other sub-directories will be based on it 
				 */
				
				String pathToSubDir = pathToDir;
				
				/*
				 * loop the array to create sub-directories for all elements except the last one,
				 * which is the HTML file 
				 */
				
				for(int i=0;i<subDir.length-1;i++) {
					
					/*
					 * edit the depth of the path for every element in the array 
					 */
					
					pathToSubDir += subDir[i] + "/";
					
					/*
					 * create the new sub-directory
					 */

					new File(pathToSubDir).mkdir();
				}
			}
			
			/*
			 * write the HTML content into a file on the file system
			 */
			
			FileWriter fileWriter = new FileWriter(new File(pathToDir + fileName + ".html"));
			
			fileWriter.write(HTMLPage);
			
			/*
			 * close the file 
			 */
			
			fileWriter.close();
		
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	/*
	 * extract the urls from the HTMLPage,
	 * return a list with all the urls in the page
	 */
	
	private List<String> extractURLsFromHTML(String HTMLPage) {
		
		/*
		 * used to store the extracted <a> tags and href attributes 
		 */
		
		List<String> urls = new ArrayList<String>();
		
		/*
		 * pattern used to extract the attributes of <a> tag
		 */
		
		Pattern patternATag = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
		
		/*
		 * pattern used to extract only the content of href attribute
		 */
		
		Pattern patternHref = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
		
		/*
		 * match the htmlPage against the <a> tags
		 */
		
		Matcher matcherATag = patternATag.matcher(HTMLPage);

	    /*
	     * extract all <a> tags first, then extracts the href from these <a> tags
	     * the reason is to avoid adding CSS, JS and other links located in the header or footer into the list
	     * the first while is for extracting <a> tags
	     */
		
	    while (matcherATag.find()) {
	    	
	    	/*
	    	 * these conditions are to avoid inserting external websites urls
	    	 * NOT 100%. There are certainly other cases which are not included here
	    	 * but here are the most cases 
	    	 */
	    	
	    	/*
	    	 * if the url contains the whole domain name including the protocol
	    	 * its more likely that it is a link to some page in the domain and not to external website  
	    	 */
	    	
	    	if(matcherATag.group(1).contains(targetURL) && !urls.contains(matcherATag.group(1)))
	    		
	    		urls.add(matcherATag.group(1));
	    	
	    	/*
	    	 * if there is no popular domain name in the url
	    	 * its more likely that the url is a relative url, so it is not external url
	    	 * you can add more domain names to the if statement 
	    	 */
	    	
	    	if( (!matcherATag.group(1).contains("www") && !matcherATag.group(1).contains("com")
	    			&& !matcherATag.group(1).contains("net") && !matcherATag.group(1).contains("de")
	    			&& !matcherATag.group(1).contains("org") && !matcherATag.group(1).contains("edu") )
	    			&& !urls.contains(matcherATag.group(1)))
	    		
	    		urls.add(matcherATag.group(1));
	    }
	    
	        /*
		 * match the urls list against the href attribute
		 */
	    
	    Matcher matcherHref = patternHref.matcher(urls.toString());
	    
	        /*
		 * clear the list to use it again for storing the urls inside href attributes
		 */
	    
	    urls.clear();
	    
	    /*
	     * extracting the content of href attribute
	     * and editing the content to be in the form of: https://domain.name/blabla
	     * then adding it into urls list
	     */
	    
	    while (matcherHref.find()) {
	    	
	    	/*
	    	 * store the url in a variable preparing to edit it 
	    	 */
	    	
	    	String match = matcherHref.group(1);
	    	
	    	/*
	    	 * remove any single or double quotes
	    	 */
	    	
	    	match = match.replace("'", "").replace("\"", "");
	    	
	    	/*
	    	 * never add pages starting with # or ? or containing the full url 
	    	 * because that duplicates the pages
	    	 */
	    	
	    	if(match.contains(targetURL) || match.contains("#") || match.contains("?")) continue;
	    	
	    	/*
	    	 * add a slash to the beginning if it is missed 
	    	 */

	    	if(!match.startsWith("/")) match = "/" + match;
	    	
	    	/*
	    	 * add the page into the list if it is not already added 
	    	 */

    		if(!urls.contains(targetURL + match)) urls.add(targetURL + match);
		}
	    
	    /*
	     * return the list of urls 
	     */
	    
	    return urls;
	}
}

