/* Generated By:JavaCC: Do not edit this line. FEDParser.java */
/**
 * Copyright 2005 Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.fed.javacc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ohla.rti.fed.FEDAttribute;
import net.sf.ohla.rti.fed.FEDFDD;
import net.sf.ohla.rti.fed.FEDInteractionClass;
import net.sf.ohla.rti.fed.FEDObjectClass;
import net.sf.ohla.rti.fed.RoutingSpace;

import net.sf.ohla.rti1516.fdd.Attribute;
import net.sf.ohla.rti1516.fdd.Dimension;
import net.sf.ohla.rti1516.fdd.FDD;
import net.sf.ohla.rti1516.fdd.InteractionClass;
import net.sf.ohla.rti1516.fdd.ObjectClass;
import net.sf.ohla.rti1516.fdd.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hla.rti.CouldNotOpenFED;
import hla.rti.ErrorReadingFED;
import hla.rti.NameNotFound;

import hla.rti1516.ErrorReadingFDD;

public class FEDParser implements FEDParserConstants {
  private static Logger log = LoggerFactory.getLogger(FEDParser.class);

  protected FEDFDD fedFDD = new FEDFDD();

  public FEDParser(URL url)
    throws CouldNotOpenFED, ErrorReadingFED
  {
    this(new ByteArrayInputStream(new byte[0]));

    if (url == null)
    {
      throw new CouldNotOpenFED("null");
    }

    try
    {
      ReInit(url.openStream(), "UTF-8");
      parse();
    }
    catch (IOException ioe)
    {
      throw new CouldNotOpenFED(url.toString(), ioe);
    }
    catch (ParseException pe)
    {
      throw new ErrorReadingFED(url.toString(), pe);
    }
  }

  public FDD getFDD()
  {
    return fedFDD;
  }

  final public void parse() throws ParseException, ErrorReadingFED {
    jj_consume_token(FED);
    Federation();
    FEDversion();
    Spaces();
    ObjectClasses();
    InteractionClasses();
    jj_consume_token(RPAREN);
    jj_consume_token(0);
  }

  final public void Federation() throws ParseException {
    jj_consume_token(FEDERATION);
    FEDname();
    jj_consume_token(RPAREN);
  }

  final public void FEDname() throws ParseException {
  String fedName;
    fedName = NameString();
    fedFDD.setFEDName(fedName);
  }

  final public void FEDversion() throws ParseException {
    jj_consume_token(FEDVERSION);
    FEDDIFversionNumber();
    jj_consume_token(RPAREN);
  }

  final public String FEDDIFversionNumber() throws ParseException {
  Token t;
    t = jj_consume_token(VERSION);
    fedFDD.setFEDDIFVersionNumber(t.image);
    {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

  final public void Spaces() throws ParseException, ErrorReadingFED {
  AtomicInteger routingSpaceCount = new AtomicInteger();
  RoutingSpace routingSpace;
    jj_consume_token(SPACES);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SPACE:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      routingSpace = Space(routingSpaceCount);
      fedFDD.add(routingSpace);
    }
    jj_consume_token(RPAREN);
  }

  final public RoutingSpace Space(AtomicInteger routingSpaceCount) throws ParseException, ErrorReadingFED {
  RoutingSpace routingSpace;
  String routingSpaceName;
  String dimensionName;
    jj_consume_token(SPACE);
    routingSpaceName = SpaceName();
    if (routingSpaceName.startsWith("HLA"))
    {
      {if (true) throw new ErrorReadingFED(String.format(
        "routing space begins with 'HLA': ", routingSpaceName));}
    }

    routingSpace = new RoutingSpace(routingSpaceName, routingSpaceCount);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case DIMENSION:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      dimensionName = Dimension();
      Dimension dimension =
        new Dimension(String.format("%s.%s", routingSpaceName, dimensionName));
      fedFDD.add(dimension);
      routingSpace.add(dimensionName, dimension);
    }
    jj_consume_token(RPAREN);
    {if (true) return routingSpace;}
    throw new Error("Missing return statement in function");
  }

  final public String SpaceName() throws ParseException {
  String spaceName;
    spaceName = NameString();
                           {if (true) return spaceName;}
    throw new Error("Missing return statement in function");
  }

  final public String Dimension() throws ParseException {
  String dimensionName;
    jj_consume_token(DIMENSION);
    dimensionName = DimensionName();
    jj_consume_token(RPAREN);
    {if (true) return dimensionName;}
    throw new Error("Missing return statement in function");
  }

  final public String DimensionName() throws ParseException {
  String dimensionName;
    dimensionName = NameString();
                               {if (true) return dimensionName;}
    throw new Error("Missing return statement in function");
  }

  final public void ObjectClasses() throws ParseException, ErrorReadingFED {
  FEDObjectClass objectRoot;
    jj_consume_token(OBJECTS);
    objectRoot = ObjectRoot();
      fedFDD.add(objectRoot);
    jj_consume_token(RPAREN);
  }

  final public FEDObjectClass ObjectRoot() throws ParseException, ErrorReadingFED {
  AtomicInteger objectClassCount = new AtomicInteger();
  AtomicInteger attributeCount = new AtomicInteger();

  FEDObjectClass objectRoot;
  String objectClassName;

  FEDAttribute attribute;
  FEDObjectClass subObject;
    jj_consume_token(CLASS);
    objectClassName = ObjectClassName();
    if (!FEDObjectClass.OBJECT_ROOT.equals(objectClassName))
    {
      {if (true) throw new ErrorReadingFED(String.format(
        "invalid root object: %s (must be %s)", objectClassName,
        FEDObjectClass.OBJECT_ROOT));}
    }

    objectRoot = new FEDObjectClass(
      ObjectClass.HLA_OBJECT_ROOT, objectClassCount);
    attribute = PriviledgeToDeleteAttribute(attributeCount);
    objectRoot.add(attribute);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CLASS:
        ;
        break;
      default:
        jj_la1[2] = jj_gen;
        break label_3;
      }
      subObject = ObjectClass(objectRoot, objectClassCount, attributeCount);
      objectRoot.add(subObject);
    }
    jj_consume_token(RPAREN);
    {if (true) return objectRoot;}
    throw new Error("Missing return statement in function");
  }

  final public FEDAttribute PriviledgeToDeleteAttribute(AtomicInteger attributeCount) throws ParseException, ErrorReadingFED {
  String attributeName;
  String transport;
  String order;
  String spaceName = null;
    jj_consume_token(ATTRIBUTE);
    attributeName = AttributeName();
    transport = Transport();
    order = Order();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NameString:
      spaceName = SpaceName();
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    jj_consume_token(RPAREN);
    if (!FEDAttribute.PRIVILEGE_TO_DELETE.equals(attributeName))
    {
      {if (true) throw new ErrorReadingFED(String.format(
        "invalid attribute: %s (must be %s)", attributeName,
        FEDAttribute.PRIVILEGE_TO_DELETE));}
    }

    FEDAttribute attribute = new FEDAttribute(
      Attribute.HLA_PRIVILEGE_TO_DELETE_OBJECT, attributeCount);
    try
    {
      attribute.setOrder(order);
      attribute.setTransportation(transport);

      if (spaceName != null)
      {
        attribute.setRoutingSpace(fedFDD.getRoutingSpace(spaceName));
      }
    }
    catch (ErrorReadingFDD erfdd)
    {
      {if (true) throw new ErrorReadingFED(erfdd);}
    }
    catch (NameNotFound nnf)
    {
      {if (true) throw new ErrorReadingFED(nnf);}
    }

    {if (true) return attribute;}
    throw new Error("Missing return statement in function");
  }

  final public FEDObjectClass ObjectClass(FEDObjectClass parent,
                           AtomicInteger objectClassCount,
                           AtomicInteger attributeCount) throws ParseException, ErrorReadingFED {
  FEDObjectClass objectClass;
  String objectClassName;

  FEDAttribute attribute;
  FEDObjectClass subObject;
    jj_consume_token(CLASS);
    objectClassName = ObjectClassName();
    objectClass = new FEDObjectClass(objectClassName, parent, objectClassCount);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case ATTRIBUTE:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_4;
      }
      attribute = Attribute(attributeCount);
      objectClass.add(attribute);
    }
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CLASS:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_5;
      }
      subObject = ObjectClass(objectClass, objectClassCount, attributeCount);
      objectClass.add(subObject);
    }
    jj_consume_token(RPAREN);
    {if (true) return objectClass;}
    throw new Error("Missing return statement in function");
  }

  final public FEDAttribute Attribute(AtomicInteger attributeCount) throws ParseException, ErrorReadingFED {
  String attributeName;
  String transport;
  String order;
  String spaceName = null;
    jj_consume_token(ATTRIBUTE);
    attributeName = AttributeName();
    transport = Transport();
    order = Order();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NameString:
      spaceName = SpaceName();
      break;
    default:
      jj_la1[6] = jj_gen;
      ;
    }
    jj_consume_token(RPAREN);
    FEDAttribute attribute = new FEDAttribute(attributeName, attributeCount);
    try
    {
      attribute.setOrder(order);
      attribute.setTransportation(transport);

      if (spaceName != null)
      {
        attribute.setRoutingSpace(fedFDD.getRoutingSpace(spaceName));
      }
    }
    catch (ErrorReadingFDD erfdd)
    {
      {if (true) throw new ErrorReadingFED(erfdd);}
    }
    catch (NameNotFound nnf)
    {
      {if (true) throw new ErrorReadingFED(nnf);}
    }

    {if (true) return attribute;}
    throw new Error("Missing return statement in function");
  }

  final public void InteractionClasses() throws ParseException, ErrorReadingFED {
  FEDInteractionClass interactionClass;
    jj_consume_token(INTERACTIONS);
    interactionClass = InteractionRoot();
      fedFDD.add(interactionClass);
    jj_consume_token(RPAREN);
  }

  final public FEDInteractionClass InteractionRoot() throws ParseException, ErrorReadingFED {
  AtomicInteger interactionClassCount = new AtomicInteger();
  AtomicInteger parameterCount = new AtomicInteger();

  FEDInteractionClass interactionRoot;
  String interactionClassName;
  String transport;
  String order;
  String spaceName = null;

  FEDInteractionClass rtiPrivateInteraction;
  FEDInteractionClass subInteraction;
    jj_consume_token(CLASS);
    interactionClassName = InteractionClassName();
    transport = Transport();
    order = Order();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NameString:
      spaceName = SpaceName();
      break;
    default:
      jj_la1[7] = jj_gen;
      ;
    }
    if (!FEDInteractionClass.INTERACTION_ROOT.equals(interactionClassName))
    {
      {if (true) throw new ErrorReadingFED(String.format(
        "invalid root interaction: %s (must be %s)", interactionClassName,
        FEDInteractionClass.INTERACTION_ROOT));}
    }

    interactionRoot = new FEDInteractionClass(
      InteractionClass.HLA_INTERACTION_ROOT, interactionClassCount);

    try
    {
      interactionRoot.setOrder(order);
      interactionRoot.setTransportation(transport);

      if (spaceName != null)
      {
        interactionRoot.setRoutingSpace(fedFDD.getRoutingSpace(spaceName));
      }
    }
    catch (ErrorReadingFDD erfdd)
    {
      {if (true) throw new ErrorReadingFED(erfdd);}
    }
    catch (NameNotFound nnf)
    {
      {if (true) throw new ErrorReadingFED(nnf);}
    }
    rtiPrivateInteraction = RTIprivateInteraction(interactionRoot, interactionClassCount, parameterCount);
    interactionRoot.add(rtiPrivateInteraction);
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CLASS:
        ;
        break;
      default:
        jj_la1[8] = jj_gen;
        break label_6;
      }
      subInteraction = InteractionClass(interactionRoot, interactionClassCount,
                                          parameterCount);
      interactionRoot.add(subInteraction);
    }
    jj_consume_token(RPAREN);
    {if (true) return interactionRoot;}
    throw new Error("Missing return statement in function");
  }

  final public FEDInteractionClass RTIprivateInteraction(FEDInteractionClass parent,
                                          AtomicInteger interactionClassCount,
                                          AtomicInteger parameterCount) throws ParseException, ErrorReadingFED {
  FEDInteractionClass rtiPrivateInteraction;
  String interactionClassName;
  String transport;
  String order;
  String spaceName = null;
    jj_consume_token(CLASS);
    interactionClassName = InteractionClassName();
    transport = Transport();
    order = Order();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NameString:
      spaceName = SpaceName();
      break;
    default:
      jj_la1[9] = jj_gen;
      ;
    }
    rtiPrivateInteraction = new FEDInteractionClass(
      interactionClassName, parent, interactionClassCount);

    try
    {
      rtiPrivateInteraction.setOrder(order);
      rtiPrivateInteraction.setTransportation(transport);

      if (spaceName != null)
      {
        rtiPrivateInteraction.setRoutingSpace(
          fedFDD.getRoutingSpace(spaceName));
      }
    }
    catch (ErrorReadingFDD erfdd)
    {
      {if (true) throw new ErrorReadingFED(erfdd);}
    }
    catch (NameNotFound nnf)
    {
      {if (true) throw new ErrorReadingFED(nnf);}
    }
    jj_consume_token(RPAREN);
    {if (true) return rtiPrivateInteraction;}
    throw new Error("Missing return statement in function");
  }

  final public FEDInteractionClass InteractionClass(FEDInteractionClass parent,
                                     AtomicInteger interactionClassCount,
                                     AtomicInteger parameterCount) throws ParseException, ErrorReadingFED {
  FEDInteractionClass interactionClass;
  String interactionClassName;
  String transport;
  String order;
  String spaceName = null;

  Parameter parameter;
  FEDInteractionClass subInteraction;
    jj_consume_token(CLASS);
    interactionClassName = InteractionClassName();
    transport = Transport();
    order = Order();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NameString:
      spaceName = SpaceName();
      break;
    default:
      jj_la1[10] = jj_gen;
      ;
    }
    interactionClass = new FEDInteractionClass(interactionClassName, parent,
                                               interactionClassCount);

    try
    {
      interactionClass.setOrder(order);
      interactionClass.setTransportation(transport);

      if (spaceName != null)
      {
        interactionClass.setRoutingSpace(fedFDD.getRoutingSpace(spaceName));
      }
    }
    catch (ErrorReadingFDD erfdd)
    {
      {if (true) throw new ErrorReadingFED(erfdd);}
    }
    catch (NameNotFound nnf)
    {
      {if (true) throw new ErrorReadingFED(nnf);}
    }
    label_7:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PARAMETER:
        ;
        break;
      default:
        jj_la1[11] = jj_gen;
        break label_7;
      }
      parameter = Parameter(parameterCount);
      interactionClass.add(parameter);
    }
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CLASS:
        ;
        break;
      default:
        jj_la1[12] = jj_gen;
        break label_8;
      }
      subInteraction = InteractionClass(interactionClass, interactionClassCount,
                                          parameterCount);
      interactionClass.add(subInteraction);
    }
    jj_consume_token(RPAREN);
    {if (true) return interactionClass;}
    throw new Error("Missing return statement in function");
  }

  final public Parameter Parameter(AtomicInteger parameterCount) throws ParseException, ErrorReadingFED {
  String parameterName;
    jj_consume_token(PARAMETER);
    parameterName = ParameterName();
    jj_consume_token(RPAREN);
    {if (true) return new Parameter(parameterName, parameterCount);}
    throw new Error("Missing return statement in function");
  }

  final public String ObjectClassName() throws ParseException {
  String objectClassName;
    objectClassName = NameString();
                                 {if (true) return objectClassName;}
    throw new Error("Missing return statement in function");
  }

  final public String AttributeName() throws ParseException {
  String attributeName;
    attributeName = NameString();
                               {if (true) return attributeName;}
    throw new Error("Missing return statement in function");
  }

  final public String InteractionClassName() throws ParseException {
  String interactionClassName;
    interactionClassName = NameString();
                                      {if (true) return interactionClassName;}
    throw new Error("Missing return statement in function");
  }

  final public String ParameterName() throws ParseException {
  String parameterName;
    parameterName = NameString();
                               {if (true) return parameterName;}
    throw new Error("Missing return statement in function");
  }

  final public String Transport() throws ParseException {
  String transport;
    transport = NameString();
                           {if (true) return transport;}
    throw new Error("Missing return statement in function");
  }

  final public String Order() throws ParseException {
  String order;
    order = NameString();
                       {if (true) return order;}
    throw new Error("Missing return statement in function");
  }

  final public String NameString() throws ParseException {
  Token t;
    t = jj_consume_token(NameString);
                   {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

  public FEDParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[13];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x2000,0x4000,0x10000,0x100000,0x20000,0x10000,0x100000,0x100000,0x10000,0x100000,0x100000,0x80000,0x10000,};
   }

  public FEDParser(java.io.InputStream stream) {
     this(stream, null);
  }
  public FEDParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new FEDParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
  }

  public FEDParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new FEDParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
  }

  public FEDParser(FEDParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
  }

  public void ReInit(FEDParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 13; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[24];
    for (int i = 0; i < 24; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 13; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 24; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}