package gfx.tick1;

public class Camera {

    // Width and height of image plane in px (screen units)
    private int width_px, height_px;

    // Width and height of image plane in m (world units)
    private double width_m, height_m;

    // Horizontal field of view in degrees
    double fov = 45;

    // Ratio between width and height of image
    double aspectRatio;

    // The distance in world units between each screen-space pixel
    private double x_step_m, y_step_m;

    public Camera(int width, int height) {
        this.width_px = width;
        this.height_px = height;

        this.aspectRatio = ((double) width) / ((double) height);

        this.width_m = 2 * Math.tan(Math.toRadians(fov) / 2);
        this.height_m = width_m / aspectRatio;

        x_step_m = this.width_m / this.width_px;
        y_step_m = this.height_m / this.height_px;
    }

    public Ray castRay(int x, int y) {
		Vector3 d = new Vector3(
			(x_step_m - width_m) / 2 + x * x_step_m,
			(y_step_m + height_m) / 2 - y * y_step_m,
			1
		).normalised();
		
        return new Ray(new Vector3(0), d);
		
		// Orthogonal
		//return new Ray(new Vector3(x_pos, y_pos, 0), new Vector3(0, 0, 1));
    }
}
