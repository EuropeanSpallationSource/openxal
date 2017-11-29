/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.jelog;

/**
 *
 * @author juanfestebanmuller
 */
public class LogbookAttribute {

        private boolean required;
        private boolean locked;
        private boolean multioption;
        private String[] options;

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public boolean isRequired() {
            return required;
        }

        public void setMultioption(boolean multioption) {
            this.multioption = multioption;
        }

        public boolean isMultioption() {
            return multioption;
        }

        public void setOptions(String[] options) {
            this.options = options;
        }

        public String[] getOptions() {
            return options;
        }

        LogbookAttribute(boolean required, boolean locked, boolean multioption,
                String[] options) {
            this.locked = locked;
            this.required = required;
            this.multioption = multioption;
            this.options = options;
        }

        public static LogbookAttribute newInstance(boolean required, boolean locked, boolean multioption,
                String[] options) {
            return new LogbookAttribute(required, locked, multioption, options);
        }

        public static LogbookAttribute newEmpty() {
            return new LogbookAttribute(false, false, false, null);
        }
    }