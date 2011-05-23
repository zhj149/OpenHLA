/* Generated By:JavaCC: Do not edit this line. FEDParser.java */
/**
 * Copyright 2005-2011 Michael Newcomb
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

import java.io.IOException;
import java.net.URL;

import net.sf.ohla.rti.fdd.FDD;
import net.sf.ohla.rti.fdd.InteractionClass;
import net.sf.ohla.rti.fdd.ObjectClass;
import net.sf.ohla.rti.fed.FED;
import net.sf.ohla.rti.fed.RoutingSpace;
import net.sf.ohla.rti.i18n.ExceptionMessages;
import net.sf.ohla.rti.i18n.I18n;

import hla.rti.CouldNotOpenFED;
import hla.rti.ErrorReadingFED;

public class FEDParser implements FEDParserConstants {
  private FED fed;

  public FEDParser(URL source)
    throws IOException, ParseException, ErrorReadingFED
  {
    this(source.openStream(), "UTF-8");

    fed = new FED(source);

    parse();
  }

  public static FED parseFED(URL source)
    throws CouldNotOpenFED, ErrorReadingFED
  {
    try
    {
      return new FEDParser(source).fed;
    }
    catch (IOException ioe)
    {
      throw new CouldNotOpenFED(I18n.getMessage(ExceptionMessages.COULD_NOT_OPEN_FED, source.toString()), ioe);
    }
    catch (ParseException pe)
    {
      throw new ErrorReadingFED(I18n.getMessage(ExceptionMessages.ERROR_READING_FED, source.toString()), pe);
    }
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
    fed.setFEDName(fedName);
  }

  final public void FEDversion() throws ParseException {
    jj_consume_token(FEDVERSION);
    FEDDIFversionNumber();
    jj_consume_token(RPAREN);
  }

  final public String FEDDIFversionNumber() throws ParseException {
  Token t;
    t = jj_consume_token(VERSION);
    {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

  final public void Spaces() throws ParseException, ErrorReadingFED {
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
      Space();
    }
    jj_consume_token(RPAREN);
  }

  final public void Space() throws ParseException, ErrorReadingFED {
  RoutingSpace routingSpace;
  String routingSpaceName;
  String dimensionName;
    jj_consume_token(SPACE);
    routingSpaceName = SpaceName();
    if (routingSpaceName.startsWith("HLA"))
    {
      {if (true) throw new ErrorReadingFED(I18n.getMessage(
        ExceptionMessages.ERROR_READING_FED_ROUTING_SPACE_STARTS_WITH_HLA, routingSpaceName));}
    }

    routingSpace = fed.addRoutingSpace(routingSpaceName);
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
      Dimension(routingSpace);
    }
    jj_consume_token(RPAREN);
  }

  final public String SpaceName() throws ParseException {
  String spaceName;
    spaceName = NameString();
                           {if (true) return spaceName;}
    throw new Error("Missing return statement in function");
  }

  final public void Dimension(RoutingSpace routingSpace) throws ParseException {
  String dimensionName;
    jj_consume_token(DIMENSION);
    dimensionName = DimensionName();
    jj_consume_token(RPAREN);
    fed.addRoutingSpaceDimension(routingSpace, dimensionName);
  }

  final public String DimensionName() throws ParseException {
  String dimensionName;
    dimensionName = NameString();
                               {if (true) return dimensionName;}
    throw new Error("Missing return statement in function");
  }

  final public void ObjectClasses() throws ParseException, ErrorReadingFED {
    jj_consume_token(OBJECTS);
    ObjectRoot();
    jj_consume_token(RPAREN);
  }

  final public void ObjectRoot() throws ParseException, ErrorReadingFED {
  String objectClassName;
  ObjectClass objectRoot;
    jj_consume_token(CLASS);
    objectClassName = ObjectClassName();
    if (net.sf.ohla.rti.fed.FED.OBJECT_ROOT.equals(objectClassName))
    {
      objectRoot = fed.addObjectClass(FDD.HLA_OBJECT_ROOT, null);
    }
    else
    {
      {if (true) throw new ErrorReadingFED(I18n.getMessage(
        ExceptionMessages.ERROR_READING_FED_INVALID_OBJECT_ROOT, net.sf.ohla.rti.fed.FED.OBJECT_ROOT, objectClassName));}
    }
    PriviledgeToDeleteAttribute(objectRoot);
    RTIprivateObject(objectRoot);
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
      ObjectClass(objectRoot);
    }
    jj_consume_token(RPAREN);
  }

  final public void PriviledgeToDeleteAttribute(ObjectClass objectRoot) throws ParseException, ErrorReadingFED {
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
    if (net.sf.ohla.rti.fed.FED.PRIVILEGE_TO_DELETE.equals(attributeName))
    {
      fed.addAttribute(objectRoot, FDD.HLA_PRIVILEGE_TO_DELETE_OBJECT, transport, order, spaceName);
    }
    else
    {
      {if (true) throw new ErrorReadingFED(I18n.getMessage(
        ExceptionMessages.ERROR_READING_FED_INVALID_PRIVILEGE_TO_DELETE, objectRoot,
        net.sf.ohla.rti.fed.FED.PRIVILEGE_TO_DELETE, attributeName));}
    }
  }

  final public void RTIprivateObject(ObjectClass objectRoot) throws ParseException, ErrorReadingFED {
  String objectClassName;
    jj_consume_token(CLASS);
    objectClassName = ObjectClassName();
    jj_consume_token(RPAREN);
    if (net.sf.ohla.rti.fed.FED.RTI_PRIVATE.equals(objectClassName))
    {
      fed.addObjectClass(objectClassName, objectRoot);
    }
    else
    {
      {if (true) throw new ErrorReadingFED(I18n.getMessage(
        ExceptionMessages.ERROR_READING_FED_INVALID_RTI_PRIVATE, net.sf.ohla.rti.fed.FED.RTI_PRIVATE, objectClassName));}
    }
  }

  final public void ObjectClass(ObjectClass parent) throws ParseException, ErrorReadingFED {
  String objectClassName;

  ObjectClass objectClass;
    jj_consume_token(CLASS);
    objectClassName = ObjectClassName();
    objectClass = fed.addObjectClass(objectClassName, parent);
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
      Attribute(objectClass);
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
      ObjectClass(objectClass);
    }
    jj_consume_token(RPAREN);
  }

  final public void Attribute(ObjectClass objectClass) throws ParseException, ErrorReadingFED {
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
    fed.addAttribute(objectClass, attributeName, transport, order, spaceName);
  }

  final public void InteractionClasses() throws ParseException, ErrorReadingFED {
    jj_consume_token(INTERACTIONS);
    InteractionRoot();
    jj_consume_token(RPAREN);
  }

  final public void InteractionRoot() throws ParseException, ErrorReadingFED {
  String interactionClassName;
  String transport;
  String order;
  String spaceName = null;
  InteractionClass interactionRoot;
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
    if (net.sf.ohla.rti.fed.FED.INTERACTION_ROOT.equals(interactionClassName))
    {
      interactionRoot = fed.addInteractionClass(null, FDD.HLA_INTERACTION_ROOT, transport, order, spaceName);
    }
    else
    {
      {if (true) throw new ErrorReadingFED(I18n.getMessage(
        ExceptionMessages.ERROR_READING_FED_INVALID_INTERACTION_ROOT, net.sf.ohla.rti.fed.FED.INTERACTION_ROOT,
        interactionClassName));}
    }
    RTIprivateInteraction(interactionRoot);
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
      InteractionClass(interactionRoot);
    }
    jj_consume_token(RPAREN);
  }

  final public void RTIprivateInteraction(InteractionClass interactionRoot) throws ParseException, ErrorReadingFED {
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
    jj_consume_token(RPAREN);
    if (net.sf.ohla.rti.fed.FED.RTI_PRIVATE.equals(interactionClassName))
    {
      fed.addInteractionClass(interactionRoot, interactionClassName, transport, order, spaceName);
    }
    else
    {
      {if (true) throw new ErrorReadingFED(I18n.getMessage(
        ExceptionMessages.ERROR_READING_FED_INVALID_RTI_PRIVATE, net.sf.ohla.rti.fed.FED.RTI_PRIVATE,
        interactionClassName));}
    }
  }

  final public void InteractionClass(InteractionClass parent) throws ParseException, ErrorReadingFED {
  String interactionClassName;
  String transport;
  String order;
  String spaceName = null;

  InteractionClass interactionClass;
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
    interactionClass = fed.addInteractionClass(parent, interactionClassName, transport, order, spaceName);
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
      Parameter(interactionClass);
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
      InteractionClass(interactionClass);
    }
    jj_consume_token(RPAREN);
  }

  final public void Parameter(InteractionClass interactionClass) throws ParseException, ErrorReadingFED {
  String parameterName;
    jj_consume_token(PARAMETER);
    parameterName = ParameterName();
    jj_consume_token(RPAREN);
    fed.addParameter(interactionClass, parameterName);
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