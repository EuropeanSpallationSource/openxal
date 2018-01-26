package xal.plugin.pvaccess;

/**
 * Translation between
 *
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
enum FieldNameConverter {
    HIHIField("HIHI") {
        @Override
        String getPvaFieldName() {
            return "valueAlarm.highAlarmLimit";
        }
    },
    LOLOField("LOLO") {
        @Override
        String getPvaFieldName() {
            return "valueAlarm.lowAlarmLimit";
        }
    },
    HIGHField("HIGH") {
        @Override
        String getPvaFieldName() {
            return "valueAlarm.highWarningLimit";
        }
    },
    LOWField("LOW") {
        @Override
        String getPvaFieldName() {
            return "valueAlarm.lowWarningLimit";
        }
    },
    DRVHField("DRVH") {
        @Override
        String getPvaFieldName() {
            return "control.limitHigh";
        }
    },
    DRVLField("DRVL") {
        @Override
        String getPvaFieldName() {
            return "control.limitLow";
        }
    },
    HOPRField("HOPR") {
        @Override
        String getPvaFieldName() {
            return "display.limitHigh";
        }
    },
    LOPRField("LOPR") {
        @Override
        String getPvaFieldName() {
            return "display.limitLow";
        }
    };

    private final String epicsFieldName;

    private FieldNameConverter(String epicsFieldName) {
        this.epicsFieldName = epicsFieldName;
    }

    static String getFieldName(String name) {
        for (FieldNameConverter t : FieldNameConverter.values()) {
            if (t.epicsFieldName.equals(name)) {
                return t.getPvaFieldName();
            }
        }
        return name;
    }

    abstract String getPvaFieldName();
}
