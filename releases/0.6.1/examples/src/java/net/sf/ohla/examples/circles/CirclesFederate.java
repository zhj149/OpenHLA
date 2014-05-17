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

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class CirclesFederate<OIH, HLAC extends HLACircle<OIH>>
{
  public static final String FEDERATION_NAME = "Circles";

  protected final Stage stage = new Stage(StageStyle.TRANSPARENT);

  protected final Parent circles;
  protected final CirclesController circlesController;

  protected final ConcurrentMap<OIH, HLAC> hlaCircles = new ConcurrentHashMap<OIH, HLAC>();
  protected final ConcurrentMap<String, HLAC> hlaCirclesByName = new ConcurrentHashMap<String, HLAC>();

  protected CirclesFederate(String title)
    throws IOException
  {
    stage.setTitle(title);

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Circles.fxml"));

    circles = (Parent) fxmlLoader.load();
    circlesController = fxmlLoader.getController();

    circlesController.setCirclesFederate(this);

    Bounds bounds = circles.getLayoutBounds();
    stage.setWidth(bounds.getWidth());
    stage.setHeight(bounds.getHeight());

    Scene scene = new Scene(circles, null);
    stage.setScene(scene);

    stage.show();
  }

  public Stage getStage()
  {
    return stage;
  }

  public abstract Node registerCircle(double x, double y)
    throws Exception;

  protected void circleRegistered(final HLAC hlaCircle)
  {
    hlaCircles.put(hlaCircle.getObjectInstanceHandle(), hlaCircle);
    hlaCirclesByName.put(hlaCircle.getObjectInstanceName(), hlaCircle);

    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circlesController.circleDiscovered(hlaCircle.getCircle());
      }
    });
  }

  protected void circleDiscovered(final HLAC hlaCircle)
  {
    System.out.printf("discovered: %s\n", hlaCircle.getObjectInstanceName());

    hlaCircles.put(hlaCircle.getObjectInstanceHandle(), hlaCircle);
    hlaCirclesByName.put(hlaCircle.getObjectInstanceName(), hlaCircle);

    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circlesController.circleDiscovered(hlaCircle.getCircle());
      }
    });
  }

  protected void circleDeleted(final HLAC hlaCircle)
  {
    hlaCircles.remove(hlaCircle.getObjectInstanceHandle());
    hlaCirclesByName.remove(hlaCircle.getObjectInstanceName());

    Platform.runLater(new Runnable()
    {
      @Override
      public void run()
      {
        circlesController.circleRemoved(hlaCircle.getCircle());
      }
    });
  }

  protected void circleRemoved(OIH objectInstanceHandle)
  {
    final HLAC hlaCircle = hlaCircles.remove(objectInstanceHandle);
    if (hlaCircle != null)
    {
      hlaCirclesByName.remove(hlaCircle.getObjectInstanceName());

      Platform.runLater(new Runnable()
      {
        @Override
        public void run()
        {
          circlesController.circleRemoved(hlaCircle.getCircle());
        }
      });
    }
  }

  protected void buzzed(byte[] buffer)
  {
    if (buffer != null)
    {
      buzzed(decodeName(buffer));
    }
  }

  protected void buzzed(String objectInstanceName)
  {
    HLAC hlaCircle = hlaCirclesByName.get(objectInstanceName);
    if (hlaCircle != null)
    {
      hlaCircle.buzzed();
    }
  }

  protected String decodeName(byte[] buffer)
  {
    return Charset.forName("UTF-8").decode(ByteBuffer.wrap(buffer)).toString();
  }
}
