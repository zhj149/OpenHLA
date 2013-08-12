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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public abstract class HLACircle<OIH>
{
  protected final OIH objectInstanceHandle;
  protected final String objectInstanceName;

  protected final Node circle;
  protected final CircleController circleController;

  protected HLACircle(OIH objectInstanceHandle, String objectInstanceName, boolean owned)
    throws Exception
  {
    this.objectInstanceHandle = objectInstanceHandle;
    this.objectInstanceName = objectInstanceName;

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Circle.fxml"));

    circle = (Node) fxmlLoader.load();
    circleController = fxmlLoader.getController();

    if (owned)
    {
      circleController.circleRegistered(this);
    }
    else
    {
      circleController.circleDiscovered(this);
    }
  }

  public OIH getObjectInstanceHandle()
  {
    return objectInstanceHandle;
  }

  public String getObjectInstanceName()
  {
    return objectInstanceName;
  }

  public Node getCircle()
  {
    return circle;
  }

  public CircleController getCircleController()
  {
    return circleController;
  }

  public void updateCenter(double x, double y)
    throws Exception
  {
    circleController.centerUpdated(new Point2D(x, y));
  }

  public void updateRadius(double radius)
    throws Exception
  {
    circleController.radiusUpdated(radius);
  }

  public void updateColor(Color color)
    throws Exception
  {
    circleController.colorUpdated(color);
  }

  public abstract void requestOwnership()
    throws Exception;

  public abstract void requestCenterOwnership()
    throws Exception;

  public abstract void requestRadiusOwnership()
    throws Exception;

  public abstract void requestColorOwnership()
    throws Exception;

  public abstract void delete()
    throws Exception;

  public abstract void buzz()
    throws Exception;

  public void buzzed()
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        // TODO: shake circle
      }
    });
  }

  protected void centerUpdated(byte[] buffer)
  {
    if (buffer != null)
    {
      final Point2D center = decodeCenter(buffer);

      System.out.printf("updated: %s\n", center);

      Platform.runLater(new Runnable()
      {
        @Override
        public void run()
        {
          circleController.centerUpdated(center);
        }
      });
    }
  }

  protected void radiusUpdated(byte[] buffer)
  {
    if (buffer != null)
    {
      final double radius = decodeRadius(buffer);

      Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circleController.radiusUpdated(radius);
      }
    });
    }
  }

  protected void colorUpdated(byte[] buffer)
  {
    if (buffer != null)
    {
      final Color color = decodeColor(buffer);

      Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circleController.colorUpdated(color);
      }
    });
    }
  }

  protected void ownershipRequestResponse(final boolean owned)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circleController.ownershipRequestResponse(owned);
      }
    });
  }

  protected void centerOwnershipRequestResponse(final boolean owned)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circleController.centerOwnershipRequestResponse(owned);
      }
    });
  }

  protected void radiusOwnershipRequestResponse(final boolean owned)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circleController.radiusOwnershipRequestResponse(owned);
      }
    });
  }

  protected void colorOwnershipRequestResponse(final boolean owned)
  {
    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circleController.colorOwnershipRequestResponse(owned);
      }
    });
  }

  protected byte[] encodeCenter(double x, double y)
  {
    return ByteBuffer.allocate(16).putDouble(x).putDouble(y).array();
  }

  protected Point2D decodeCenter(byte[] buffer)
  {
    ByteBuffer wrapper = ByteBuffer.wrap(buffer);
    double x = wrapper.getDouble();
    double y = wrapper.getDouble();

    return new Point2D(x, y);
  }

  protected byte[] encodeRadius(double radius)
  {
    return ByteBuffer.allocate(8).putDouble(radius).array();
  }

  protected byte[] encodeName(String objectInstanceName)
  {
    return Charset.forName("UTF-8").encode(objectInstanceName).array();
  }

  protected double decodeRadius(byte[] buffer)
  {
    return ByteBuffer.wrap(buffer).getDouble();
  }

  protected byte[] encodeColor(Color color)
  {
    return ByteBuffer.allocate(24).putDouble(color.getRed()).putDouble(color.getGreen()).putDouble(
      color.getBlue()).array();
  }

  protected Color decodeColor(byte[] buffer)
  {
    ByteBuffer wrapper = ByteBuffer.wrap(buffer);

    double red = wrapper.getDouble();
    double green = wrapper.getDouble();
    double blue = wrapper.getDouble();

    return Color.color(red, green, blue);
  }
}
