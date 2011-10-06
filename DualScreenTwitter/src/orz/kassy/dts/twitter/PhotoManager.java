
package orz.kassy.dts.twitter;

public class PhotoManager {

    private static Photo[] mPhotos;

    static {
        mPhotos = new Photo[] {
                new Photo("茶色いネコ", R.drawable.icon),
                new Photo("ミケ猫", R.drawable.icon),
                new Photo("警戒するネコ", R.drawable.icon),
                new Photo("あっちを見るネコ", R.drawable.icon),
                new Photo("ネコたち", R.drawable.icon),
                new Photo("ネコのハナ", R.drawable.icon),
                new Photo("ネコ背", R.drawable.icon),
                new Photo("舌をだすネコ", R.drawable.icon),
        };
    }

    private PhotoManager() {
    }

    public static Photo[] getPhotos() {
        return mPhotos;
    }
}
