package me.timothy.bots;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.jreddit.Replyable;
import com.github.jreddit.comment.Comment;
import com.github.jreddit.comment.Comments;
import com.github.jreddit.message.Message;
import com.github.jreddit.message.MessageType;
import com.github.jreddit.message.Messages;
import com.github.jreddit.submissions.Submission;
import com.github.jreddit.submissions.Submissions;
import com.github.jreddit.submissions.Submissions.Popularity;
import com.github.jreddit.user.User;
import com.github.jreddit.utils.restclient.RestClient;

/**
 * Handles the functions of the bot (such as replying to comments,
 * checking if a comment summons the LoansBot, etc) but does not contain
 * the connecting logic (looping through each comment that has to be checked
 * and checking it, for example). 
 * <br><br>
 * This class does NOT ever sleep, so you may run into API restrictions. In an 
 * effort to standardize the various error protocols that jReddit uses, this uses
 * a C-like error system.
 * 
 * @author Timothy
 */
public class Bot {
	private Throwable lastError;
	@SuppressWarnings("unused")
	private Logger logger;

	private RestClient restClient;
	private User user;

	private String subreddit;

	private Comments comments;
	private Submissions submissions;
	private Messages messages;

	/**
	 * Creates the loan bot for the specified
	 * subreddit.
	 * @param subreddit the subreddit to scan
	 * @throws 
	 */
	public Bot(String subreddit) {
		this.subreddit = subreddit;

		logger = LogManager.getLogger();
	}

	/**
	 * Logs in to reddit. Upon failure use getLastException to
	 * see the issue.
	 * 
	 * @param username Username of the reddit account
	 * @param password Password of the reddit account
	 * @return true on success, false on failure
	 * @throws IllegalStateException if the rest client is null
	 */
	public boolean loginReddit(String username, String password) throws IllegalStateException {
		if(restClient == null)
			throw new IllegalStateException("Rest client is null");

		try {
			user = new User(restClient, username, password);
			user.connect();
			return true;
		}catch(Exception e) {
			lastError = e;
			user = null;
			return false;
		}
	}

	/**
	 * Returns a list of recent comments that might need to be scanned
	 * @return a list of recent comments
	 * @throws IllegalStateException if the user or rest client is null
	 * @throws InterruptedException if reddit failed to respond correctly and 10 retries have failed (over 10 + 20 + 40 + 80 + 160 + 320 + 640 + 1280 + 2560 seconds)
	 */
	public List<Comment> getRecentComments() throws IllegalStateException {
		if(user == null || restClient == null) {
			throw new IllegalStateException((user == null ? "User is null. " : "") + (restClient == null ? "Rest client is null. " : "")); 
		}


		if(comments == null)
			comments = new Comments(restClient);
		return comments.newComments(subreddit, user.getCookie());
	}

	/**
	 * Returns any new submissions
	 * @return new submissions, or null if an error occurs
	 * @throws IllegalStateException if the rest client or user is null
	 */
	public List<Submission> getRecentSubmissions() {
		if(user == null || restClient == null) {
			throw new IllegalStateException((user == null ? "User is null. " : "") + (restClient == null ? "Rest client is null. " : "")); 
		}

		if(submissions == null)
			submissions = new Submissions(restClient);
		try {
			return submissions.getSubmissions(subreddit, Popularity.NEW, null, user);
		}catch(Exception ex) {
			return null;
		}
	}

	/**
	 * Returns any new messages
	 * @return new messages, or null if an error occurs
	 * @throws IllegalStateException if the rest client or user is null
	 */
	public List<Message> getUnreadMessages() {
		if(user == null || restClient == null) {
			throw new IllegalStateException((user == null ? "User is null. " : "") + (restClient == null ? "Rest client is null. " : "")); 
		}
		if(messages == null)
			messages = new Messages(restClient);
		
		return messages.getMessages(user, Messages.ALL_MESSAGES, MessageType.UNREAD); 
				
	}
	
	
	/**
	 * Responds to the specified replyable with the specified message
	 * @param comment the comment
	 * @param message the message
	 * @return success
	 */
	public boolean respondTo(Replyable replyable, String message) throws IllegalStateException {
		if(restClient == null || user == null) {
			throw new IllegalStateException("null user");
		}
		try {
			replyable.respondTo(restClient, user, message);
			return true;
		} catch (IOException e) {
			lastError = e;
			return false;
		}
	}

	/**
	 * Marks the specified message as read
	 * @param ids the message
	 * @return if the message was probably marked as read
	 * @throws IllegalStateException if rest client or user is null
	 * @throws IOException 
	 */
	public boolean setReadMessage(String ids) throws IllegalStateException, IOException {
		if(restClient == null || user == null) {
			throw new IllegalStateException("null user");
		}
		return messages.readMessage(ids, user);
	}


	/**
	 * Sets the rest client of the LoansBot
	 * @param client the rest client to use
	 */
	public void setRestClient(RestClient client) {
		restClient = client;
	}

	/**
	 * Returns the last error, if there is one.
	 * @return most recent error
	 */
	public Throwable getLastError() {
		return lastError;
	}

	/**
	 * Clears the last error
	 */
	public void clearLastError() {
		lastError = null;
	}

	public Comment getCommentByFullname(final String fullname) throws IllegalStateException {
		if(restClient == null || user == null) {
			throw new IllegalStateException("null user");
		}

		if(comments == null)
			comments = new Comments(restClient);

		try {
			return comments.getCommentByFullname(subreddit, fullname);
		} catch (IllegalStateException e) {
			return null;
		}

	}

	public Submission getSubmissionByFullname(final String fullname) throws IllegalStateException {
		if(restClient == null || user == null) {
			throw new IllegalStateException("null user");
		}

		if(submissions == null)
			submissions = new Submissions(restClient);

		try {
			return submissions.getSubmissionByFullname(subreddit, fullname);
		} catch (IllegalStateException e) {
			return null;

		}

	}

	public List<Comment> getCommentsByUser(String username) {
		if(restClient == null || user == null) {
			throw new IllegalStateException("null user");
		}

		if(comments == null)
			comments = new Comments(restClient);

		try {
			return comments.comments(username);
		} catch (Exception e) {
			return null;
		} 
	}

	public User getUser() {
		return user;
	}
}

