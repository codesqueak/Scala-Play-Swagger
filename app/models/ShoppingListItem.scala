package models

import io.swagger.annotations.{ApiModel, ApiModelProperty}

import java.util.UUID


@ApiModel
case class ShoppingListItem(
                             @ApiModelProperty(value = "description")
                             description: String,
                             @ApiModelProperty(value = "quantity")
                             quantity: Int,
                             @ApiModelProperty(value = "price")
                             price: Double)


@ApiModel
case class Key(
                @ApiModelProperty(value = "key")
                key: UUID)

