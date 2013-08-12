/*
 * Copyright (c) 2005-2011, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.examples.circles;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class CirclesController
{
  @FXML
  private Group group;

  @FXML
  private Rectangle background;

  private volatile SimpleBooleanProperty mouseOverCircle = new SimpleBooleanProperty(this, "mouseOverCircle", false);

  private CirclesFederate<?, ?> circlesFederate;

  private double initialX;
  private double initialY;

  public void setCirclesFederate(CirclesFederate<?, ?> circlesFederate)
  {
    this.circlesFederate = circlesFederate;
  }

  public void circleDiscovered(Node circle)
  {
    group.getChildren().add(circle);
  }

  public void circleRemoved(Node circle)
  {
    group.getChildren().remove(circle);
  }

  @FXML
  private void handleMousePressed(MouseEvent mouseEvent)
  {
    initialX = mouseEvent.getScreenX() - circlesFederate.getStage().getX();
    initialY = mouseEvent.getScreenY() - circlesFederate.getStage().getY();

    mouseEvent.consume();
  }

  @FXML
  private void handleMouseClicked(MouseEvent mouseEvent)
  {
    if (mouseEvent.getClickCount() == 2 && mouseEvent.isStillSincePress())
    {
      try
      {
        circlesFederate.registerCircle(mouseEvent.getX(), mouseEvent.getY());
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      mouseEvent.consume();
    }
  }

  @FXML
  private void handleMouseDragged(MouseEvent mouseEvent)
  {
    circlesFederate.getStage().setX(mouseEvent.getScreenX() - initialX);
    circlesFederate.getStage().setY(mouseEvent.getScreenY() - initialY);

    mouseEvent.consume();
  }
}
