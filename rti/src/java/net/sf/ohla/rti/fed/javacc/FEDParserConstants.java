/*
 * Copyright (c) 2006-2007, Michael Newcomb
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.ohla.rti.fed.javacc;

public interface FEDParserConstants {

  int EOF = 0;
  int RPAREN = 6;
  int INTEGER_LITERAL = 7;
  int VERSION = 8;
  int FED = 9;
  int FEDERATION = 10;
  int FEDVERSION = 11;
  int SPACES = 12;
  int SPACE = 13;
  int DIMENSION = 14;
  int OBJECTS = 15;
  int CLASS = 16;
  int ATTRIBUTE = 17;
  int INTERACTIONS = 18;
  int PARAMETER = 19;
  int NameString = 20;
  int NameCharacter = 21;
  int Letter = 22;
  int DecimalDigit = 23;

  int DEFAULT = 0;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\r\"",
    "\"\\f\"",
    "\"\\n\"",
    "\")\"",
    "<INTEGER_LITERAL>",
    "<VERSION>",
    "\"(FED\"",
    "\"(Federation\"",
    "\"(FEDversion\"",
    "\"(spaces\"",
    "\"(space\"",
    "\"(dimension\"",
    "\"(objects\"",
    "\"(class\"",
    "\"(attribute\"",
    "\"(interactions\"",
    "\"(parameter\"",
    "<NameString>",
    "<NameCharacter>",
    "<Letter>",
    "<DecimalDigit>",
  };

}
