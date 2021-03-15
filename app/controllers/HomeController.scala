package controllers

import play.mvc.Results.ok
import play.mvc._


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
class HomeController extends Controller {
  /**
   * An action that renders an HTML page with a welcome message.
   * The configuration in the <code>routes</code> file means that
   * this method will be called when the application receives a
   * <code>GET</code> request with a path of <code>/</code>.
   */
  def index: Result = ok(views.html.index.render())

}
