/*
 Copyright © 2012 Paul Houghton and Futurice on behalf of the Tantalum Project.
 All rights reserved.

 Tantalum software shall be used to make the world a better place for everyone.

 This software is licensed for use under the Apache 2 open source software license,
 http://www.apache.org/licenses/LICENSE-2.0.html

 You are kindly requested to return your improvements to this library to the
 open source community at http://projects.developer.nokia.com/Tantalum

 The above copyright and license notice notice shall be included in all copies
 or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package org.tantalum.storage;

/**
 * Each StaticCache has a handler which handles conversions from network and RMS
 * binary form into the form used in memory and the cache. For example, image
 * format decompression or XML parsing.
 * 
 * @author phou
 */
public interface DataTypeHandler {
    
    /**
     * Shift from binary to in-memory usable form. This may increase or decrease
     * the total heap memory load, but in both cases it allows the RAM version
     * to be rapidly returned without re-parsing the binary data.
     * 
     * Every StaticCache must be assigned a DataTypeHandler
     * 
     * @param bytes
     * @return 
     */
    public Object convertToUseForm(byte[] bytes);
}
