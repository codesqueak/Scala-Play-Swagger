package controllers

import io.swagger.annotations.{ApiParam, _}
import models.{Key, ShoppingListItem}
import play.api.libs.json._
import play.api.mvc._

import java.util.UUID
import javax.inject._
import scala.collection.mutable

@Singleton
@Api(value = "Shopping List Controller", tags = Array("Shopping", "Weird Stuff"))
@SwaggerDefinition(
  schemes = Array(SwaggerDefinition.Scheme.HTTP) // Add HTTPS for production

)
class ShoppingListController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  // Make sure items appear in the order added
  private val shoppingList = new mutable.LinkedHashMap[UUID, ShoppingListItem]

  // Set up some initial data
  shoppingList += UUID.randomUUID() -> ShoppingListItem("Teabag", 100, 2.50)
  shoppingList += UUID.randomUUID() -> ShoppingListItem("Milk", 1, 0.75)
  shoppingList += UUID.randomUUID() -> ShoppingListItem("Sugar", 1, 2.00)

  // Implicits to allow model data to be converted to json for writing
  implicit def shoppingListJson: OFormat[ShoppingListItem] = Json.format[ShoppingListItem]

  implicit def keyJson: OFormat[Key] = Json.format[Key]

  @ApiOperation(
    value = "Get items", notes = "Get all shopping list items", responseContainer = "List", tags = Array("Shopping"))
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "Shopping list has at least one item", response = classOf[List[ShoppingListItem]]),
    new ApiResponse(code = 204, message = "Shopping list is empty")
  ))
  // GET query parameters and return of model data as json
  // curl -i -X GET "http://localhost:9000/shopping?page=0&size=10" -H  "accept: application/json"
  def getAll(@ApiParam("Page number") page: Int, @ApiParam("Page size") size: Int): Action[AnyContent] = Action {
    val list = shoppingList.slice(page * size, (page + 1) * size).toList
    if (list.isEmpty)
      NoContent
    else
      Ok(Json.toJson(list))
  }

  @ApiOperation(
    value = "Get an item", notes = "Get a single item by its id value", tags = Array("Shopping"))
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "Item found", response = classOf[ShoppingListItem]),
    new ApiResponse(code = 404, message = "No such item")
  ))
  // GET url parameter and return of model data as json
  // curl -i -X GET "http://localhost:9000/shopping/2d31dd2c-8d91-4069-bf75-70651403c5b0" -H  "accept: application/json"
  def getById(@ApiParam("Item ID to search by") itemId: UUID): Action[AnyContent] = Action { _ =>
    val listItem = shoppingList.get(itemId)
    listItem match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  @ApiOperation(
    value = "Delete an item", notes = "Delete a single item by its id value", tags = Array("Shopping"))
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "Item deleted", response = classOf[ShoppingListItem]),
    new ApiResponse(code = 404, message = "No such item")
  ))
  // DELETE url parameter
  // curl -i -X DELETE "http://localhost:9000/shopping/<uuid>" -H  "accept: application/json"
  def deleteById(@ApiParam("Item ID to delete by") itemId: UUID): Action[AnyContent] = Action {
    shoppingList.remove(itemId) match {
      case Some(_) => Ok
      case None => NotFound
    }
  }

  @ApiOperation(
    value = "Delete all items", notes = "Delete all shopping list items", tags = Array("Shopping"))
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "All items deleted", response = classOf[String])
  ))
  // DELETE return non json value
  // curl -i -X DELETE "http://localhost:9000/shopping" -H  "accept: application/json"
  def deleteAll(): Action[AnyContent] = Action {
    val size = shoppingList.size
    shoppingList.keySet.foreach(key => shoppingList.remove(key))
    Ok(size.toString)
  }

  @ApiOperation(
    value = "Add an items", notes = "Add an item to the bottom of the list", tags = Array("Shopping"))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "Item to be added",
      required = true,
      dataType = "models.ShoppingListItem",
      paramType = "body")
  ))
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "Item added", response = classOf[UUID]),
    new ApiResponse(code = 400, message = "Item invalid")
  ))
  // curl -i -X POST "http://localhost:9000/shopping" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{  \"description\": \"aaa\",  \"quantity\": 10,  \"price\": 12.34}"
  def addNewItem(): Action[AnyContent] = Action {
    implicit request =>
      request.body.asJson match {
        case Some(json) =>
          val shoppingListItem = json.as[ShoppingListItem]
          val key = UUID.randomUUID()
          shoppingList.put(UUID.randomUUID(), shoppingListItem)
          Created(Json.toJson(Key(key)))
        case None =>
          BadRequest
      }
  }

  @ApiOperation(
    value = "Update an item", notes = "Update an existing item", tags = Array("Shopping"))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "Item to be updated",
      required = true,
      dataType = "models.ShoppingListItem",
      paramType = "body")
  ))
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "Item added", response = classOf[UUID]),
    new ApiResponse(code = 400, message = "Item invalid")
  ))
  // curl -i -X PATCH "http://localhost:9000/shopping/6b2e9c9d-039e-4d07-abd9-582858c3c403" -H  "accept: application/json" -H  "Content-Type: application/json" -d "{  \"description\": \"coffee\",  \"quantity\": 5,  \"price\": 2.75}"
  def patchItem(itemId: UUID): Action[AnyContent] = Action {
    implicit request =>
      val originalItem = shoppingList.get(itemId)
      originalItem match {
        case Some(_) =>
          val body = request.body.asJson
          body match {
            case Some(json) =>
              val replacementItem = json.as[ShoppingListItem]
              shoppingList.put(itemId, replacementItem)
              Created(Json.toJson(Key(itemId)))
            case None =>
              BadRequest
          }
        case None => NotFound
      }
  }

  // Less usual requests

  @ApiOperation(
    value = "Get options", notes = "Get all request option types", tags = Array("Weird Stuff"))
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "List of available HTTP request methods")
  ))
  // OPTION
  // curl -i -X OPTIONS "http://localhost:9000/shopping"
  def options(): Action[AnyContent] = Action {
    Ok.withHeaders(ALLOW -> "GET,HEAD,POST,DELETE,OPTIONS,PATCH")
  }

  @ApiOperation(
    value = "Head items", notes = "Head all shopping list items", tags = Array("Weird Stuff"))
  @ApiResponses(value = Array(
    new ApiResponse(code = 200, message = "Shopping list has at least one item"),
    new ApiResponse(code = 204, message = "Shopping list is empty")
  ))
  // HEAD query parameters and return of model data as json
  // curl -i -X HEAD "http://localhost:9000/shopping?page=0&size=10" -H  "accept: application/json"
  def headAll(@ApiParam("Page number") page: Int, @ApiParam("Page size") size: Int): Action[AnyContent] = Action {
    val list = shoppingList.slice(page * size, (page + 1) * size).toList
    if (list.isEmpty)
      NoContent
    else
      Ok
  }


  // post example

  // https://sahebmotiani.medium.com/swagger-with-play-all-you-need-to-know-d9147089d990


}
