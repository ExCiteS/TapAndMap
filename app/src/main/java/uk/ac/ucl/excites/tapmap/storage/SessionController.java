package uk.ac.ucl.excites.tapmap.storage;

import java.util.Date;
import lombok.AllArgsConstructor;

/**
 * Created by Michalis Vitos on 01/06/2018.
 */
@AllArgsConstructor
public class SessionController {

  private SessionDao sessionDao;

  public Session openNewSession() {
    return openNewSession("");
  }

  public Session openNewSession(String desc) {

    // 1. Close previous session if needed
    final Session lastSession = sessionDao.findLast();
    if (lastSession != null && !isSessionClosed(lastSession))
      closeSession(lastSession);

    // 2. Create new Session
    final Session newSession = new Session();
    newSession.setStartTime(new Date());
    newSession.setDescription(desc);
    long id = sessionDao.insert(newSession);

    return sessionDao.findById(id);
  }

  public void closeSession(Session session) {
    session.setEndTime(new Date());
    sessionDao.insert(session);
  }

  public boolean isSessionClosed(Session session) {
    return session.getEndTime() != null;
  }

  public Session getActiveSession() {
    Session session = sessionDao.findLast();

    if (session != null && !isSessionClosed(session))
      return session;
    else
      return openNewSession();
  }

  /**
   * Get the id of the ActiveSession
   */
  public long getActiveSessionId() {
    return getActiveSession().getId();
  }
}
