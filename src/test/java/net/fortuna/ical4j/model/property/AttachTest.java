/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.model.property;

import junit.framework.TestCase;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Encoding;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * $Id$
 * <p/>
 * Created on 7/04/2005
 *
 * @author Ben Fortuna
 *         <p/>
 *         Test case for Attach property.
 */
public class AttachTest extends TestCase {

    private Logger log = LoggerFactory.getLogger(AttachTest.class);

    private Attach attach;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        FileInputStream fin = new FileInputStream("etc/artwork/logo.png");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i = fin.read(); i >= 0; ) {
            bout.write(i);
            i = fin.read();
        }

        List<Parameter> params = new ArrayList<>();
        params.add(Encoding.BASE64);
        params.add(Value.BINARY);

//        Attach attach = new Attach(params, Base64.encodeBytes(bout.toByteArray(), Base64.DONT_BREAK_LINES));
        attach = new Attach(params, bout.toByteArray());
    }

    /*
     * Class under test for void Attach(ParameterList, String)
     */
    public void testAttachParameterListString() throws IOException, URISyntaxException, ValidationException, ParserException {

        //log.info(attach);

        DtStart start = new DtStart<>(LocalDate.now().withMonth(12).withDayOfMonth(25));
        start.getParameters().add(Value.DATE);

        Summary summary = new Summary("Christmas Day; \n this is a, test\\");

        VEvent christmas = new VEvent();
        christmas.getProperties().add(start);
        christmas.getProperties().add(summary);
        christmas.getProperties().add(attach);
        christmas.getProperties().add(new Uid("000001@modularity.net.au"));

        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getComponents().add(christmas);

        StringWriter sw = new StringWriter();
        CalendarOutputter out = new CalendarOutputter();
        out.output(calendar, sw);

        CalendarBuilder builder = new CalendarBuilder();
        Calendar cout = builder.build(new StringReader(sw.toString()));

        Optional<VEvent> eout = cout.getComponent(Component.VEVENT);

        Optional<Attach> aout = eout.get().getProperty(Property.ATTACH);
        assertNotNull(aout);
        assertEquals(attach, aout.get());

        log.info(sw.toString());
    }

    /**
     * Unit testing of serialization.
     */
    public void testSerialization() throws IOException, ClassNotFoundException,
            URISyntaxException {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(attach);

        ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bout.toByteArray()));

        Attach clone = (Attach) in.readObject();

        assertNotNull(clone);
        assertEquals(attach, clone);

        // set a bogus value to trigger logging..
        clone.getParameters().removeIf(p -> p.getName().equals(Parameter.ENCODING));
        clone.getParameters().add(new Encoding("BOGUS"));
        clone.setValue("");
    }
}
