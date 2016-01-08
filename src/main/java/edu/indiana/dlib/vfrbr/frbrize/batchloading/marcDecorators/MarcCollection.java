/**
 * Copyright 2009-2011, Trustees of Indiana University
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * Neither the name of Indiana University nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;

import java.io.FileInputStream;
import java.io.InputStream;

public class MarcCollection {

    private MarcReader marcReader;

    public MarcCollection(String file) throws Exception {
        InputStream in = new FileInputStream(file);
        // defaults to Latin-1 or UTF8 input, unless...
        this.marcReader = new MarcStreamReader(in);
    }

    public boolean hasNext() {
        return this.marcReader.hasNext();
    }

    public MarcRecord next() {
        if (this.hasNext()) {
            return new MarcRecord(this.marcReader.next());
        } else {
            return null;
        }
    }

//    public static void main(String[] args) throws Exception {
//        final MarcCollection collection = new MarcCollection(args[0]);
//        DAOFactory daoFac = new DAOFactory();
//        Counts count = new Counts();
//        count.setFileName(args[0]);
//        while (collection.hasNext()) {
//            final MarcRecord marcRecord = collection.next();
//            final MarcRecordHandler recHandler = new MarcRecordHandler(daoFac, count);
//            recHandler.frbrizeRecord(marcRecord);
//        }
//    }
}
