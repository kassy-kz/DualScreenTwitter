
package orz.kassy.dts.twitter;

public class Photo {

    private String mName;

    private int mResId;

    public Photo(String name, int resourceId) {
        mName = name;
        mResId = resourceId;
    }

    public String getName() {
        return mName;
    }

    public int getResId() {
        return mResId;
    }

    public String toString() {
        return mName;
    }

}
