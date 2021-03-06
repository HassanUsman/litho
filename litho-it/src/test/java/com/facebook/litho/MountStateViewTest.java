/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import static com.facebook.litho.Column.create;
import static com.facebook.litho.testing.ComponentTestHelper.mountComponent;
import static com.facebook.yoga.YogaEdge.BOTTOM;
import static com.facebook.yoga.YogaEdge.LEFT;
import static com.facebook.yoga.YogaEdge.RIGHT;
import static com.facebook.yoga.YogaEdge.TOP;
import static org.assertj.core.api.Java6Assertions.assertThat;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.litho.testing.ComponentTestHelper;
import com.facebook.litho.testing.TestComponent;
import com.facebook.litho.testing.TestComponentContextWithView;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.TestViewComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(ComponentsTestRunner.class)
public class MountStateViewTest {

  private ComponentContext mContext;

  @Before
  public void setup() {
    mContext = new ComponentContext(RuntimeEnvironment.application);
  }

  @Test
  public void testViewPaddingAndBackground() {
    final int color = 0xFFFF0000;
    final LithoView lithoView = mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return create(c)
                .child(
                    TestViewComponent.create(c)
                        .paddingPx(LEFT, 5)
                        .paddingPx(TOP, 6)
                        .paddingPx(RIGHT, 7)
                        .paddingPx(BOTTOM, 8)
                        .backgroundColor(color))
                .build();
          }
        });

    final View child = lithoView.getChildAt(0);
    final Drawable background = child.getBackground();

    assertThat(child.getPaddingLeft()).isEqualTo(5);
    assertThat(child.getPaddingTop()).isEqualTo(6);
    assertThat(child.getPaddingRight()).isEqualTo(7);
    assertThat(child.getPaddingBottom()).isEqualTo(8);
    assertThat(background).isInstanceOf(ColorDrawable.class);
    assertThat(((ColorDrawable) background).getColor()).isEqualTo(color);
  }

  @Test
  public void testComponentDeepUnmount() {
    final TestComponent testComponent1 = TestDrawableComponent.create(mContext).build();
    final TestComponent testComponent2 = TestDrawableComponent.create(mContext).build();

    final Component mountedTestComponent1 =
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(Layout.create(c, testComponent1).widthPx(10).heightPx(10))
                .build();
          }
        };
    final Component mountedTestComponent2 =
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c)
                .child(Layout.create(c, testComponent2).widthPx(10).heightPx(10))
                .build();
          }
        };
    final LithoView child1 = mountComponent(mContext, mountedTestComponent1, true);
    final LithoView child2 = mountComponent(mContext, mountedTestComponent2, true);

    assertThat(testComponent1.isMounted()).isTrue();
    assertThat(testComponent2.isMounted()).isTrue();

    final ViewGroupWithLithoViewChildren viewGroup = new ViewGroupWithLithoViewChildren(mContext);
    removeParent(child1);
    removeParent(child2);
    viewGroup.addView(child1);
    viewGroup.addView(child2);

    final TestComponentContextWithView testComponentContextWithView =
        new TestComponentContextWithView(mContext, viewGroup);

    final LithoView parentView =
        mountComponent(
            testComponentContextWithView, TestViewComponent.create(mContext).build(), true);

    ComponentTestHelper.unmountComponent(parentView);

    assertThat(testComponent1.isMounted()).isFalse();
    assertThat(testComponent2.isMounted()).isFalse();
  }

  private void removeParent(View child) {
    final ViewGroup parent = (ViewGroup) child.getParent();
    parent.removeView(child);
  }

  private class ViewGroupWithLithoViewChildren extends ViewGroup implements HasLithoViewChildren {

    ViewGroupWithLithoViewChildren(Context context) {
      super(context);
    }

    @Override
    public void obtainLithoViewChildren(List<LithoView> lithoViews) {
      for (int i = 0, size = getChildCount(); i < size; i++) {
        lithoViews.add((LithoView) getChildAt(i));
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {}
  }
}
