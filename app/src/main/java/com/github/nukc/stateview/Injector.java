package com.github.nukc.stateview;

import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import top.niunaijun.blackboxa.R;

public class Injector {

    public static void changeChildrenConstraints(ViewGroup viewParent, FrameLayout root, int injectViewId) {
        if (viewParent instanceof ConstraintLayout) {
            int rootId = R.id.root_id;
            root.setId(rootId);
            ConstraintLayout rootGroup = ((ConstraintLayout) viewParent);
            for (int i = 0, count = rootGroup.getChildCount(); i < count; i++) {
                View child = rootGroup.getChildAt(i);
                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) child.getLayoutParams();
                if (layoutParams.circleConstraint == injectViewId) {
                    layoutParams.circleConstraint = rootId;
                } else {
                    if (layoutParams.leftToLeft == injectViewId) {
                        layoutParams.leftToLeft = rootId;
                    } else if (layoutParams.leftToRight == injectViewId) {
                        layoutParams.leftToRight = rootId;
                    }

                    if (layoutParams.rightToLeft == injectViewId) {
                        layoutParams.rightToLeft = rootId;
                    } else if (layoutParams.rightToRight == injectViewId) {
                        layoutParams.rightToRight = rootId;
                    }

                    if (layoutParams.topToTop == injectViewId) {
                        layoutParams.topToTop = rootId;
                    } else if (layoutParams.topToBottom == injectViewId) {
                        layoutParams.topToBottom = rootId;
                    }

                    if (layoutParams.bottomToTop == injectViewId) {
                        layoutParams.bottomToTop = rootId;
                    } else if (layoutParams.bottomToBottom == injectViewId) {
                        layoutParams.bottomToBottom = rootId;
                    }

                    if (layoutParams.baselineToBaseline == injectViewId) {
                        layoutParams.baselineToBaseline = rootId;
                    }
                }
            }
        }
    }
}
