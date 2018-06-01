package uk.ac.ucl.excites.tapmap.storage;

import java.util.Date;
import lombok.AllArgsConstructor;

/**
 * Created by Michalis Vitos on 01/06/2018.
 */
@AllArgsConstructor
public class SessionController {

  private SessionDao sessionDao;

  public Session openSession() {
    return openSession("");
  }

  public Session openSession(String desc) {
    Session session = new Session();
    session.setStartTime(new Date());
    session.setDescription(desc);
    long id = sessionDao.insert(session);

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
      return openSession();
  }
}
