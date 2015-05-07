package org.knime.knip.scifio.writerbug;

import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
import io.scif.img.SCIFIOImgPlus;
import junit.framework.Assert;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.integer.ByteType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by gabriel on 5/7/15.
 */
public class WriterBug {

    List<Long> m_dimList = new ArrayList<Long>();
    List<AxisType> m_axisList = new ArrayList<AxisType>();


    @Test
    public void writerbugtest() throws Exception {

        // This img has X,Y,Z and Channel Dimensions
        final ImgPlus<ByteType> imgPlus = createByteTypeImgPlus();

        ImgSaver m_saver = new ImgSaver();
        String imgName = "test.ome.tif";
        m_saver.saveImg(imgName, imgPlus);

        final ImgOpener opener = new ImgOpener();
        List<SCIFIOImgPlus<?>> imgs = opener.openImgs(imgName);
        SCIFIOImgPlus<?> sfimg = imgs.get(0); // This Image suddenly has X, Y, Channel, Time Dimensions

        for (int i = 0; i < 4; i++) {
            String imgPlusAxis = imgPlus.axis(i).type().getLabel();
            String scifImgPlusAxis = sfimg.axis(i).type().getLabel();
            assertEquals("Axes are not equal: read axis is " + scifImgPlusAxis +
                    ", but the source img axis was: " + imgPlusAxis, imgPlusAxis, scifImgPlusAxis); // This fails !!
        }
    }

    // HELPER METHODS

    /**
     * @return Empty ByteType ImgPlus
     */
    private ImgPlus<ByteType> createByteTypeImgPlus() {

        // Dimension creation playground
        processDimension(100, "X");
        processDimension(100, "Y");
        processDimension(4, "Z");
        processDimension(3, "Channel");
        processDimension(0, "Time");


        final long[] dims = new long[m_dimList.size()];
        for (int d = 0; d < m_dimList.size(); d++) {
            dims[d] = m_dimList.get(d);
        }

        ArrayImgFactory<ByteType> fac = new ArrayImgFactory<ByteType>();
        ByteType type = new ByteType();
        Img<ByteType> img = fac.create(dims, type);
        final ImgPlus<ByteType> imgPlus = new ImgPlus<ByteType>(img);

        int d = 0;
        for (final AxisType a : m_axisList) {
            imgPlus.setAxis(new DefaultLinearAxis(a), d++);
        }
        return imgPlus;
    }

    /**
     * Add this dimensions to the list of axes and dims.
     *
     * @param val   the value, 0 means ignore
     * @param label the label to use for the axis
     */
    private void processDimension(final int val, final String label) {
        // ignore empty dimensions
        if (val != 0) {
            m_dimList.add((long) val);
            m_axisList.add(Axes.get(label));
        }
    }


}
