/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.wrangler.parser;

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.CompileStatus;
import io.cdap.wrangler.api.Compiler;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.RecipeParser;
import io.cdap.wrangler.api.RecipeSymbol;
import io.cdap.wrangler.api.TokenGroup;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.Token;
import io.cdap.wrangler.api.parser.TokenType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/**
 * Tests {@link GrammarBasedParser}
 */
public class GrammarBasedParserTest {

  @Test
  public void testBasic() throws Exception {
    String[] recipe = new String[] {
      "#pragma version 2.0;",
      "rename :col1 :col2",
      "parse-as-csv :body ',' true;",
      "#pragma load-directives text-reverse, text-exchange;",
      "${macro} ${macro_2}",
      "${macro_${test}}"
    };

    RecipeParser parser = TestingRig.parse(recipe);
    List<Directive> directives = parser.parse();
    Assert.assertEquals(2, directives.size());
  }

  @Test
  public void testLoadableDirectives() throws Exception {
    String[] recipe = new String[] {
      "#pragma version 2.0;",
      "#pragma load-directives text-reverse, text-exchange;",
      "rename col1 col2",
      "parse-as-csv body , true",
      "text-reverse :body;",
      "test prop: { a='b', b=1.0, c=true};",
      "#pragma load-directives test-change,text-exchange, test1,test2,test3,test4;"
    };

    Compiler compiler = new RecipeCompiler();
    CompileStatus status = compiler.compile(new MigrateToV2(recipe).migrate());
    Assert.assertEquals(7, status.getSymbols().getLoadableDirectives().size());
  }

  @Test
  public void testCommentOnlyRecipe() throws Exception {
    String[] recipe = new String[] {
      "// test"
    };

    RecipeParser parser = TestingRig.parse(recipe);
    List<Directive> directives = parser.parse();
    Assert.assertEquals(0, directives.size());
  }
  
  /**
   * Test parsing of ByteSize and TimeDuration token types in directives
   */
  @Test
  public void testByteSizeAndTimeDurationParsing() throws Exception {
    String[] recipe = new String[] {
      // Using a test directive that accepts byte size and time duration tokens
      "aggregate-stats :data_size :response_time total_size total_time 10MB 500ms",
      "aggregate-stats :data_size :response_time total_size total_time 1GB 2s 'TOTAL'",
      "aggregate-stats :data_size :response_time total_size total_time '5KB' '30s'"
    };

    Compiler compiler = new RecipeCompiler();
    CompileStatus status = compiler.compile(new MigrateToV2(recipe).migrate());
    
    // Get the token groups from the compilation
    RecipeSymbol symbols = status.getSymbols();
    Assert.assertNotNull("Symbols should not be null", symbols);
    Assert.assertEquals(3, symbols.size());
    
    // Get an iterator for the token groups
    Iterator<TokenGroup> groupIterator = symbols.iterator();
    Assert.assertTrue(groupIterator.hasNext());
    
    // Check the first directive's tokens
    TokenGroup firstGroup = groupIterator.next();
    
    // Find ByteSize token - we need to iterate through tokens to find it
    ByteSize byteSize = null;
    TimeDuration timeDuration = null;
    
    Iterator<Token> tokenIterator = firstGroup.iterator();
    while (tokenIterator.hasNext()) {
      Token token = tokenIterator.next();
      if (token.type() == TokenType.BYTE_SIZE) {
        byteSize = (ByteSize) token;
      } else if (token.type() == TokenType.TIME_DURATION) {
        timeDuration = (TimeDuration) token;
      }
    }
    
    // Verify ByteSize token
    Assert.assertNotNull("ByteSize token not found", byteSize);
    Assert.assertEquals("10MB", byteSize.value());
    Assert.assertEquals(10 * 1000 * 1000, byteSize.getBytes());
    
    // Verify TimeDuration token
    Assert.assertNotNull("TimeDuration token not found", timeDuration);
    Assert.assertEquals("500ms", timeDuration.value());
    Assert.assertEquals(500, timeDuration.getMilliseconds());
  }
}
