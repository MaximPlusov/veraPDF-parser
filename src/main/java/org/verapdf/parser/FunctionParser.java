/**
 * This file is part of veraPDF Parser, a module of the veraPDF project.
 * Copyright (c) 2015, veraPDF Consortium <info@verapdf.org>
 * All rights reserved.
 *
 * veraPDF Parser is free software: you can redistribute it and/or modify
 * it under the terms of either:
 *
 * The GNU General public license GPLv3+.
 * You should have received a copy of the GNU General Public License
 * along with veraPDF Parser as the LICENSE.GPL file in the root of the source
 * tree.  If not, see http://www.gnu.org/licenses/ or
 * https://www.gnu.org/licenses/gpl-3.0.en.html.
 *
 * The Mozilla Public License MPLv2+.
 * You should have received a copy of the Mozilla Public License along with
 * veraPDF Parser as the LICENSE.MPL file in the root of the source tree.
 * If a copy of the MPL was not distributed with this file, you can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package org.verapdf.parser;

import org.verapdf.cos.COSInteger;
import org.verapdf.cos.COSObject;
import org.verapdf.cos.COSReal;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FunctionParser extends BaseParser {

    private static final Logger LOGGER = Logger.getLogger(FunctionParser.class.getCanonicalName());

    private static final Set<String> FUNCTION_KEYWORDS;
    static {
        Set<String> tempSet = new HashSet<>();
        tempSet.add("abs");
        tempSet.add("cvi");
        tempSet.add("floor");
        tempSet.add("mod");
        tempSet.add("sin");
        tempSet.add("add");
        tempSet.add("cvr");
        tempSet.add("idiv");
        tempSet.add("mul");
        tempSet.add("sqrt");
        tempSet.add("atan");
        tempSet.add("div");
        tempSet.add("ln");
        tempSet.add("neg");
        tempSet.add("sub");
        tempSet.add("ceiling");
        tempSet.add("exp");
        tempSet.add("log");
        tempSet.add("round");
        tempSet.add("truncate");
        tempSet.add("cos");
        tempSet.add("and");
        tempSet.add("false");
        tempSet.add("le");
        tempSet.add("not");
        tempSet.add("true");
        tempSet.add("bitshift");
        tempSet.add("ge");
        tempSet.add("lt");
        tempSet.add("or");
        tempSet.add("xor");
        tempSet.add("eq");
        tempSet.add("gt");
        tempSet.add("ne");
        tempSet.add("if");
        tempSet.add("ifelse");
        tempSet.add("copy");
        tempSet.add("exch");
        tempSet.add("pop");
        tempSet.add("dup");
        tempSet.add("index");
        tempSet.add("roll");
        tempSet.add("{");
        tempSet.add("}");

        FUNCTION_KEYWORDS = Collections.unmodifiableSet(tempSet);
    }

    private List<COSObject> operators = new ArrayList<>();

    public FunctionParser(InputStream functionStream) throws IOException {
        super(functionStream);
    }

    public void parse() throws IOException {

        initializeToken();

        skipSpaces(true);

        while (getToken().type != Token.Type.TT_EOF) {
            nextToken();
            processToken();
        }
    }

    private void processToken() {
        switch (this.getToken().type) {
            case TT_NONE:
            case TT_EOF:
                break;
            case TT_KEYWORD:
                if (!FUNCTION_KEYWORDS.contains(this.getToken().getValue())) {
                    LOGGER.log(Level.WARNING, "Invalid keyword in Function");
                }
                break;
            case TT_INTEGER:
                operators.add(COSInteger.construct(this.getToken().integer));
                break;
            case TT_REAL:
                operators.add(COSReal.construct(this.getToken().real));
                break;
            default:
                LOGGER.log(Level.WARNING,"Invalid object type in Function");
                break;
        }
    }

    public List<COSObject> getOperators() {
        return Collections.unmodifiableList(operators);
    }
}
