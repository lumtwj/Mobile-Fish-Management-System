package com.mocentury.fis.util;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.zxing.WriterException;
import com.mocentury.fis.R;
import com.mocentury.fis.object.Data;

/**
 * Created by lumtwj on 23/4/16.
 */
public class FishInfoDialog {
    MaterialDialog md;
    ImageView ivQrCode;
    TextView tvFishInfo;

    public FishInfoDialog(Context context) {
        md = new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .title("Fish info")
                .customView(R.layout.dialog_fish_info, true)
                .positiveText("Okay")
//                .negativeText("Cancel")
                .build();

        init();
    }

    private void init() {
        View v = md.getCustomView();

        ivQrCode = (ImageView) v.findViewById(R.id.ivQrCode);
        tvFishInfo = (TextView) v.findViewById(R.id.tvFishInfo);
    }

    public void loadData(Data d) throws WriterException {
        ivQrCode.setImageBitmap(ImageUtil.generateQRCode(d.toString()));

        tvFishInfo.setText(Html.fromHtml(String.format("<b>id:</b> %s<br><b>Species:</b> %s<br><b>Length:</b> %s<br><b>Co-ordinates:</b> %f (lat), %f (lng)<br><b>Time:</b> %s",
                d.getId(),
                d.getSpecies(),
                formatLength(d.getSpecies(), d.getLength()),
                d.getLat(),
                d.getLng(),
                d.getTime()
        )));

        md.show();
    }

    public String formatLength(String species, double length) {
        String colour = "red";

        switch (species) {
            case "SWORDFISH":
                if (length >= 47)
                    colour = "green";
                break;
            case "YELLOWFIN TUNA":
                if (length >= 27)
                    colour = "green";
                break;
            case "BLUEFIN TUNA":
                if (length >= 73)
                    colour = "green";
                break;
        }

        return String.format("<font color=\"%s\">%.3f inch</font>", colour, length);
    }
}
