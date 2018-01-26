package com.shopback.notepad;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;
import static android.support.v7.widget.helper.ItemTouchHelper.RIGHT;

enum ButtonState{
    GONE, LEFT_VISIBLE, RIGHT_VISIBLE
}


public class SwipeController extends ItemTouchHelper.Callback {

    private boolean swipeBack = false;
    private ButtonState buttonShowedState = ButtonState.GONE;
    private static final float buttonWidth = 200;
    private RectF buttonInstance = null;
    private RecyclerView.ViewHolder currentItemViewHolder = null;
    private SwipeControllerActions buttonActions;

    SwipeController(SwipeControllerActions buttonActions) {
        this.buttonActions = buttonActions;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT|RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ACTION_STATE_SWIPE) {
            if(buttonShowedState != ButtonState.GONE) {
                if (buttonShowedState == ButtonState.LEFT_VISIBLE) {
                    dX = Math.max(dX, buttonWidth);
                }
                if (buttonShowedState == ButtonState.RIGHT_VISIBLE) {
                    dX = Math.min(dX, -buttonWidth);
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            } else {
                setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

        if(buttonShowedState == ButtonState.GONE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
        currentItemViewHolder = viewHolder;

    }

    private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
        View itemView = viewHolder.itemView;
        Paint p = new Paint();
        float buttonWidthWithPadding = buttonWidth + 0;

        RectF leftButton = new RectF(itemView.getLeft(), itemView.getTop(),
                itemView.getLeft() + buttonWidthWithPadding, itemView.getBottom());
        p.setColor(Color.BLACK);
        c.drawRect(leftButton, p);
        drawText(c, leftButton, p, "Edit");

        RectF rightButton = new RectF(itemView.getRight()-buttonWidthWithPadding,
                itemView.getTop(), itemView.getRight(), itemView.getBottom());
        p.setColor(Color.RED);
        c.drawRect(rightButton, p);
        drawText(c, rightButton, p, "Delete");

        buttonInstance = null;
        if(buttonShowedState == ButtonState.LEFT_VISIBLE) {
            buttonInstance = leftButton;
        } else if (buttonShowedState == ButtonState.RIGHT_VISIBLE) {
            buttonInstance = rightButton;
        }
    }

    private void drawText(Canvas c, RectF button, Paint p, String buttonText) {
        float textSize = 40;
        p.setColor(Color.WHITE);
        p.setTextSize(textSize);

        float textWidth = p.measureText(buttonText);
        c.drawText(buttonText, button.centerX()-(textWidth/2), button.centerY(), p);

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    private void setTouchListener(final Canvas c, final RecyclerView recyclerView,
                                  final RecyclerView.ViewHolder viewHolder, final float dX,
                                  final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                swipeBack = (motionEvent.getAction() == MotionEvent.ACTION_CANCEL ||
                        motionEvent.getAction() == MotionEvent.ACTION_UP);
                if(swipeBack) {
                   if (dX < -buttonWidth) {
                       Log.d("right button", "visible");
                       buttonShowedState = ButtonState.RIGHT_VISIBLE;
                   } else if (dX > buttonWidth) {
                       Log.d("left button", "visible");
                       buttonShowedState = ButtonState.LEFT_VISIBLE;
                   }

                    if (buttonShowedState != ButtonState.GONE) {
                        setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                        setItemsClickable(recyclerView, false);
                    }
                }
                return false;
            }

        });
    }

    private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView,
                                      final RecyclerView.ViewHolder viewHolder, final float dX,
                                      final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("touch down", "registered");
                    setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                return false;
            }
        });
    }

    private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("touch up", "registered");
                    SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
                    recyclerView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            return false;
                        }
                    });
                    setItemsClickable(recyclerView, true);
                    swipeBack = false;

                    if (buttonActions != null && buttonInstance != null && buttonInstance.contains(
                            event.getX(), event.getY())){
                        if (buttonShowedState == ButtonState.RIGHT_VISIBLE) {
                            buttonActions.onRightButtonClicked(((NotesRecyclerAdapter.NoteViewHolder)viewHolder).getId(), viewHolder.getAdapterPosition());
                        } else if (buttonShowedState == ButtonState.LEFT_VISIBLE) {
                            buttonActions.onLeftButtonClicked(((NotesRecyclerAdapter.NoteViewHolder)viewHolder).getId(), viewHolder.getAdapterPosition());
                        }
                    }
                    buttonShowedState = ButtonState.GONE;
                    currentItemViewHolder = null;
                }
                return false;
            }
        });
    }

    private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
        for (int i = 0; i < recyclerView.getChildCount(); ++i) {
            recyclerView.getChildAt(i).setClickable(isClickable);
        }
    }

    void onDraw(Canvas c) {
        if (currentItemViewHolder != null) {
            drawButtons(c, currentItemViewHolder);
        }
    }
}

abstract class SwipeControllerActions {
    public void onLeftButtonClicked(long rowId, int position){}
    public void onRightButtonClicked(long rowId, int position){}
}
