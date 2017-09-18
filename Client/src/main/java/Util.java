/**
 * Utility class
 */
class Util {

    private Util() {
    }

    /**
     * Convenience method for extracting the first char of a string and converting it to a digit.
     *
     * @param str The string to extract the first char from
     *
     * @return Returns the integer representation of the first char of the given {@code str}
     */
    static int convertToDigit(final String str) {

        if (str.length() > 1) {

            return -1;
        }

        return Character.digit(str.charAt(0), 10);
    }
}
