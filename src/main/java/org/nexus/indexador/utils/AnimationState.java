package org.nexus.indexador.utils;

import javafx.animation.Timeline;

public class AnimationState {
  private Timeline timeline;
  private int currentFrameIndex;

  public AnimationState() {
    this.timeline = new Timeline();
    this.currentFrameIndex = 0;
  }

  public Timeline getTimeline() {
    return timeline;
  }

  public int getCurrentFrameIndex() {
    return currentFrameIndex;
  }

  public void setCurrentFrameIndex(int currentFrameIndex) {
    this.currentFrameIndex = currentFrameIndex;
  }
}
