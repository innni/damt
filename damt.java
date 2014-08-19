/*
 ** Delete All Tweets
 ** Hina @ codecyb.org
 ** Application: http://www.codecyb.org/delete-all-my-tweets/
 ** Feel free to use the code any way you like.
 */

package com.codecyborg.damt;

import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.Status;
import twitter4j.Paging;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

public class DeleteAllTweets {

    public static void main(String[] args) {

        /*
         * Should be in the root directory of the project
         * with properties for
         * Access Token, Access Token Secret (we'll fetch these)
         * Consumer key and Consumer Secret (provided by dev.twitter)
         */
        File pFile = new File("twitter4j.properties");
        InputStream inStream = null;
        OutputStream outStream = null;
        Properties properties = new Properties();

        try {
                //Proceed if the properties file exists
                if (pFile.exists()) {
                    inStream = new FileInputStream(pFile);
                    properties.load(inStream);
                    System.out.println("Properties file loaded.");
                } else {
                    System.out.println("Error: twitter4j.properties file not found.");
                    System.exit(-1);
                }

                try{
                    if(inStream != null) inStream.close();
                } catch (IOException ioerror) {
                    System.out.println("Error: Could not close twitter4j.properties.");
                    ioerror.printStackTrace();
                }


                //Proceed if the file has consumer key and consumer secret
                if (properties.getProperty("oauth.consumerKey") == null ||
                    properties.getProperty("oauth.consumerSecret") == null) {
                    System.out.println("twitter4j.properties requires Consumer Key and Access Token Secret.");
                    System.exit(-1);
                }

                System.out.println("Consumer Key: " + properties.getProperty("oauth.consumerKey"));
                System.out.println("Consumer Secret: " + properties.getProperty("oauth.consumerSecret"));

                //Now that we have the required information to work with the API
                Twitter twitter = new TwitterFactory().getInstance();


                //If the file doesn't have access token/secret, request for a token
                //to obtain authorization
                if (properties.getProperty("oauth.accessToken") == null ||
                    properties.getProperty("oauth.accessTokenSecret") == null) {

                    //Request for a token to make an authorization request
                    RequestToken requestToken = twitter.getOAuthRequestToken();
                    System.out.println("Got request token.");
                    System.out.println("Request token: " + requestToken.getToken());
                    System.out.println("Request token secret: " + requestToken.getTokenSecret());

                    //Make an authorization request with the token and obtain the
                    //access token and access token secret.
                    AccessToken accessToken = null;
                    //
                    BufferedReader buffReader = new BufferedReader(new InputStreamReader(System.in));
                    //Persist in asking for an accessToken until we have one
                    while (null == accessToken) {
                        //Ask the user to open this URL
                        System.out.println("Open the following URL to get a PIN to allow this application to access your account:");
                        System.out.println(requestToken.getAuthorizationURL());

                        /*We'll also try opening the browser automatically
                        /*We don't want the program to quit here if the browser fails to open.
                        /*It's not the end of the world.
                        /*So, let's just handle the errors related to it right here
                        */

                        try {
                                Desktop.getDesktop().browse(new URI(requestToken.getAuthorizationURL()));
                            } catch (UnsupportedOperationException uoerror) {
                                System.out.println("Encountered an unsupported operation exception.");
                                uoerror.printStackTrace();
                            } catch (IOException ioerror) {
                                System.out.println("Encountered an input/output exception.");
                                ioerror.printStackTrace();
                            } catch (URISyntaxException urierror) {
                                System.out.println("Encountered an URI syntax exception.");
                                urierror.printStackTrace();
                            }
                        //Get pin from user
                        System.out.print("Enter the PIN obtained from the above URL: ");
                        String pin = buffReader.readLine();
                        //obtain the access token
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);

                     }

                     //Write these values to the properties file.
                     properties.setProperty("oauth.accessToken", accessToken.getToken());
                     properties.setProperty("oauth.accessTokenSecret", accessToken.getTokenSecret());
                     //Save them to file
                     try {
                            outStream = new FileOutputStream(pFile);
                            properties.store(outStream, "twitter4j.properties");
                            outStream.close();
                            System.out.println("Stored new properties to file.");
                      } catch (IOException ioerror) {
                            System.out.println("Error: Could not save new properties.");
                            ioerror.printStackTrace();
                            System.exit(-1);
                      }

                } //properties are no longer null

                //To help debug...
                 System.out.println("Access token: " + properties.getProperty("oauth.accessToken"));
                 System.out.println("Access token secret: " + properties.getProperty("oauth.accessTokenSecret"));

                //Let's do something now

                //get username
                ArrayList<Long> statusIDs = new ArrayList<Long>();
                String userName = twitter.verifyCredentials().getScreenName();
                User user = twitter.showUser(userName);
                int totalTweets = user.getStatusesCount();
                if (totalTweets > 3200) {
                    System.out.println("Twitter allows access to only 3200 tweets but let's try to delete as many as we can." );
                }

                //keep a count of the tweets
                int count = 0;

                //Twitter lets you fetch 200 tweets at a time
                //So we access 200 each time through paging
                //And save all the ids
                if(totalTweets > 200) {
                    int totalPages = totalTweets/200;
                    int page = 1;
                    while(totalPages != 0) {
                        List<Status> tweets = twitter.getUserTimeline(new Paging(page, 200));
                        page++;
                        totalPages--;
                        for (Status tweet : tweets) {
                            statusIDs.add(tweet.getId());
                        }
                    }
                } else { //if we have less than 200, then oh well. Do this:
                    List<Status> tweets = twitter.getUserTimeline(new Paging(1, 200));
                    for (Status tweet : tweets) {
                            statusIDs.add(tweet.getId());
                        }
                }

                //Delete every tweet
                for(Long id:statusIDs) {
                        twitter.destroyStatus(id);
                        count++;
                 }
                System.out.println("Deleted "+count+ " tweets");
        } catch(IOException ioerror) {
            System.out.println("IOException caught.");
            ioerror.printStackTrace();
        } catch(TwitterException terror) {
            System.out.println("Twitter Exception caught.");
            terror.printStackTrace();
        }


 }

}
