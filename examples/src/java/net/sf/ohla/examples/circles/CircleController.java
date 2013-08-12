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

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class CircleController
{
  private final SimpleObjectProperty<Object> objectInstanceHandle =
    new SimpleObjectProperty<Object>(this, "objectInstanceHandle");
  private final SimpleStringProperty objectInstanceName = new SimpleStringProperty(this, "objectInstanceName");

  private final SimpleBooleanProperty owned = new SimpleBooleanProperty(this, "owned", false);
  private final SimpleBooleanProperty ownershipRequested =
    new SimpleBooleanProperty(this, "ownershipRequested", false);

  private final SimpleObjectProperty<Point2D> center =
    new SimpleObjectProperty<Point2D>(this, "center", new Point2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
  private final SimpleBooleanProperty centerOwned = new SimpleBooleanProperty(this, "centerOwned", false);
  private final SimpleBooleanProperty centerOwnershipRequested =
    new SimpleBooleanProperty(this, "centerOwnershipRequested", false);

  private final SimpleDoubleProperty radius = new SimpleDoubleProperty(this, "radius", 40);
  private final SimpleBooleanProperty radiusOwned = new SimpleBooleanProperty(this, "radiusOwned", false);
  private final SimpleBooleanProperty radiusOwnershipRequested =
    new SimpleBooleanProperty(this, "radiusOwnershipRequested", false);

  private final SimpleObjectProperty<Color> color = new SimpleObjectProperty<Color>(this, "color", Color.RED);
  private final SimpleBooleanProperty colorOwned = new SimpleBooleanProperty(this, "colorOwned", false);
  private final SimpleBooleanProperty colorOwnershipRequested =
    new SimpleBooleanProperty(this, "colorOwnershipRequested", false);

  private final BackgroundMousePressedHandler backgroundMousePressedHandler = new BackgroundMousePressedHandler();
  private final BackgroundMouseDraggedHandler backgroundMouseDraggedHandler = new BackgroundMouseDraggedHandler();

  private final RadiusRingMousePressedHandler radiusRingMousePressedHandler = new RadiusRingMousePressedHandler();
  private final RadiusRingMouseDraggedHandler radiusRingMouseDraggedHandler = new RadiusRingMouseDraggedHandler();

  @FXML
  private Group group;

  @FXML
  private Circle background;

  @FXML
  private Circle radiusRing;

  private HLACircle hlaCircle;

  private double initialCenterX;
  private double initialCenterY;

  private double initialRadius;
  private double initialMouseRadius;

  public Object getObjectInstanceHandle()
  {
    return objectInstanceHandle.get();
  }

  public ReadOnlyObjectProperty<Object> objectInstanceHandleProperty()
  {
    return objectInstanceHandle;
  }

  public void setObjectInstanceHandle(Object objectInstanceHandle)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public String getObjectInstanceName()
  {
    return objectInstanceName.get();
  }

  public ReadOnlyStringProperty objectInstanceNameProperty()
  {
    return objectInstanceName;
  }

  public void setObjectInstanceName(String objectInstanceName)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public boolean getOwned()
  {
    return owned.get();
  }

  public ReadOnlyBooleanProperty ownedProperty()
  {
    return owned;
  }

  public void setOwned(boolean owned)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public boolean getOwnershipRequested()
  {
    return ownershipRequested.get();
  }

  public ReadOnlyBooleanProperty ownershipRequestedProperty()
  {
    return ownershipRequested;
  }

  public void setOwnershipRequested(boolean ownershipRequested)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public Point2D getCenter()
  {
    return center.get();
  }

  public ReadOnlyObjectProperty<Point2D> centerProperty()
  {
    return center;
  }

  public void setCenter(Point2D center)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public boolean getCenterOwned()
  {
    return centerOwned.get();
  }

  public ReadOnlyBooleanProperty centerOwnedProperty()
  {
    return centerOwned;
  }

  public void setCenterOwned(boolean centerOwned)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public boolean getCenterOwnershipRequested()
  {
    return centerOwnershipRequested.get();
  }

  public ReadOnlyBooleanProperty centerOwnershipRequestedProperty()
  {
    return centerOwnershipRequested;
  }

  public void setCenterOwnershipRequested(boolean centerOwnershipRequested)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public double getRadius()
  {
    return radius.get();
  }

  public ReadOnlyDoubleProperty radiusProperty()
  {
    return radius;
  }

  public void setRadius(double radius)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public boolean getRadiusOwned()
  {
    return radiusOwned.get();
  }

  public ReadOnlyBooleanProperty radiusOwnedProperty()
  {
    return radiusOwned;
  }

  public void setRadiusOwned(boolean radiusOwned)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public boolean getRadiusOwnershipRequested()
  {
    return radiusOwnershipRequested.get();
  }

  public ReadOnlyBooleanProperty radiusOwnershipRequestedProperty()
  {
    return radiusOwnershipRequested;
  }

  public void setRadiusOwnershipRequested(boolean radiusOwnershipRequested)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public Color getColor()
  {
    return color.get();
  }

  public ReadOnlyObjectProperty<Color> colorProperty()
  {
    return color;
  }

  public void setColor(Object color)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public boolean getColorOwned()
  {
    return colorOwned.get();
  }

  public ReadOnlyBooleanProperty colorOwnedProperty()
  {
    return colorOwned;
  }

  public void setColorOwned(boolean colorOwned)
  {
    // bug in JavaFX that requires a 'setter' for the property to be visible in the fxml template

    assert false;
  }

  public boolean getColorOwnershipRequested()
  {
    return colorOwnershipRequested.get();
  }

  public ReadOnlyBooleanProperty colorOwnershipRequestedProperty()
  {
    return colorOwnershipRequested;
  }

  public void setColorOwnershipRequested(boolean colorOwnershipRequested)
  {
    this.colorOwnershipRequested.set(colorOwnershipRequested);
  }

  @SuppressWarnings("unchecked")
  public <OIH> HLACircle<OIH> getHLACircle()
  {
    return hlaCircle;
  }

  @SuppressWarnings("unchecked")
  public void circleRegistered(HLACircle<?> hlaCircle)
  {
    this.hlaCircle = hlaCircle;

    objectInstanceHandle.set(hlaCircle.getObjectInstanceHandle());
    objectInstanceName.set(hlaCircle.getObjectInstanceName());

    ownershipRequestResponse(true);
    centerOwnershipRequestResponse(true);
    radiusOwnershipRequestResponse(true);
    colorOwnershipRequestResponse(true);
  }

  @SuppressWarnings("unchecked")
  public void circleDiscovered(HLACircle<?> hlaCircle)
  {
    this.hlaCircle = hlaCircle;

    objectInstanceHandle.set(hlaCircle.getObjectInstanceHandle());
    objectInstanceName.set(hlaCircle.getObjectInstanceName());
  }

  public void centerUpdated(Point2D center)
  {
    this.center.set(center);

    group.setTranslateX(center.getX());
    group.setTranslateY(center.getY());
  }

  public void radiusUpdated(double radius)
  {
    this.radius.set(radius);
  }

  public void colorUpdated(Color color)
  {
    this.color.set(color);
  }

  public void ownershipRequestResponse(boolean owned)
  {
    this.owned.set(owned);
    ownershipRequested.set(false);

    group.setOpacity(owned ? 1 : .3);
  }

  public void centerOwnershipRequestResponse(boolean owned)
  {
    centerOwned.set(owned);
    centerOwnershipRequested.set(false);

    if (owned)
    {
      background.addEventHandler(MouseEvent.MOUSE_PRESSED, backgroundMousePressedHandler);
      background.addEventHandler(MouseEvent.MOUSE_DRAGGED, backgroundMouseDraggedHandler);
    }
    else
    {
      background.removeEventHandler(MouseEvent.MOUSE_PRESSED, backgroundMousePressedHandler);
      background.removeEventHandler(MouseEvent.MOUSE_DRAGGED, backgroundMouseDraggedHandler);
    }
  }

  public void radiusOwnershipRequestResponse(boolean owned)
  {
    radiusOwned.set(owned);
    radiusOwnershipRequested.set(false);

    if (owned)
    {
      radiusRing.addEventHandler(MouseEvent.MOUSE_PRESSED, radiusRingMousePressedHandler);
      radiusRing.addEventHandler(MouseEvent.MOUSE_DRAGGED, radiusRingMouseDraggedHandler);
    }
    else
    {
      radiusRing.removeEventHandler(MouseEvent.MOUSE_PRESSED, radiusRingMousePressedHandler);
      radiusRing.removeEventHandler(MouseEvent.MOUSE_DRAGGED, radiusRingMouseDraggedHandler);
    }
  }

  public void colorOwnershipRequestResponse(boolean owned)
  {
    colorOwned.set(owned);
    colorOwnershipRequested.set(false);
  }

  @FXML
  private void initialize()
  {
    group.setTranslateX(center.get().getX());
    group.setTranslateX(center.get().getY());

    background.radiusProperty().bind(radius.subtract(radiusRing.strokeWidthProperty().multiply(.5)));
    background.fillProperty().bind(color);

    radiusRing.radiusProperty().bind(radius);

//    group.hoverProperty().addListener(new ChangeListener<Boolean>()
//    {
//      @Override
//      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue)
//      {
//        System.out.printf("hover? %s\n", newValue);
//      }
//    });
  }

  @FXML
  private void requestOwnership(ActionEvent actionEvent)
  {
    try
    {
      hlaCircle.requestOwnership();

      ownershipRequested.set(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @FXML
  private void requestCenterOwnership(ActionEvent actionEvent)
  {
    try
    {
      hlaCircle.requestCenterOwnership();

      centerOwnershipRequested.set(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @FXML
  private void requestRadiusOwnership(ActionEvent actionEvent)
  {
    try
    {
      hlaCircle.requestRadiusOwnership();

      radiusOwnershipRequested.set(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @FXML
  private void requestColorOwnership(ActionEvent actionEvent)
  {
    try
    {
      hlaCircle.requestColorOwnership();

      colorOwnershipRequested.set(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @FXML
  private void delete(ActionEvent actionEvent)
  {
    try
    {
      hlaCircle.delete();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @FXML
  private void buzz(ActionEvent actionEvent)
  {
    try
    {
      hlaCircle.buzz();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private class BackgroundMousePressedHandler
    implements EventHandler<MouseEvent>
  {
    @Override
    public void handle(MouseEvent mouseEvent)
    {
      group.toFront();

      initialCenterX = mouseEvent.getScreenX() - group.getTranslateX();
      initialCenterY = mouseEvent.getScreenY() - group.getTranslateY();

      mouseEvent.consume();
    }
  }

  private class BackgroundMouseDraggedHandler
    implements EventHandler<MouseEvent>
  {
    @Override
    public void handle(MouseEvent mouseEvent)
    {
      double centerX = mouseEvent.getScreenX() - initialCenterX;
      double centerY = mouseEvent.getScreenY() - initialCenterY;

      try
      {
        hlaCircle.updateCenter(centerX, centerY);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      mouseEvent.consume();
    }
  }

  private class RadiusRingMousePressedHandler
    implements EventHandler<MouseEvent>
  {
    @Override
    public void handle(MouseEvent mouseEvent)
    {
      group.toFront();

      initialRadius = radius.get();
      initialMouseRadius = Math.pow((mouseEvent.getX() * mouseEvent.getX()) + (mouseEvent.getY() * mouseEvent.getY()), .5);

      mouseEvent.consume();
    }
  }

  private class RadiusRingMouseDraggedHandler
    implements EventHandler<MouseEvent>
  {
    @Override
    public void handle(MouseEvent mouseEvent)
    {
      double dRadius = initialMouseRadius -
                       Math.pow((mouseEvent.getX() * mouseEvent.getX()) + (mouseEvent.getY() * mouseEvent.getY()), .5);

      double radius = initialRadius - dRadius;

      try
      {
        hlaCircle.updateRadius(radius);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }

      mouseEvent.consume();
    }
  }
}
