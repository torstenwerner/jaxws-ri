/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package schema_validation.client;

import javax.xml.ws.soap.SOAPFaultException;

/**
 * Schema Validation sample
 *
 * @author Jitendra Kotamraju
 */

public class AddNumbersClient {
    public static void main (String[] args) {
        AddNumbersPortType port = new AddNumbersService().getAddNumbersPort ();

        int number1 = 1000;
        int number2 = 20;
        System.out.printf ("Invoking addNumbers(%d, %d)\n", number1, number2);
        int result = port.addNumbers (number1, number2);
        System.out.printf ("The result of adding %d and %d is %d.\n\n", number1, number2, result);

        number1 = 10001;     // more than 4 digits, so doesn't pass validation
        try {
            System.out.printf ("Invoking addNumbers(%d, %d) and this should generate schema validation exception. \n", number1, number2);
            result = port.addNumbers (number1, number2);
            throw new RuntimeException("Schema Validation didn't work");
        } catch(SOAPFaultException se) {
            se.printStackTrace();
            System.out.printf ("Caught expected exception since 10001 has 5 digits");
        }
            
    }
}