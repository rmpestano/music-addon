package com.github.forge.addon.music.util;

/**
 * Created by rafael-pestano on 20/10/2015.
 */
public class StopWatch {

  private long begin;
  private long pauseTime;

  public void start(){
    begin = System.currentTimeMillis();
  }

  public void pause(){
    pauseTime = System.currentTimeMillis() - begin;
  }

  public void resume(){
    begin = System.currentTimeMillis() - pauseTime;
  }

  public long getMilliseconds() {
    return System.currentTimeMillis()-begin;
  }

  public double getSeconds() {
    return (System.currentTimeMillis() - begin) / 1000.0;
  }

  public double getMinutes() {
    return (System.currentTimeMillis() - begin) / 60000.0;
  }

  public double getHours() {
    return (System.currentTimeMillis() - begin) / 3600000.0;
  }
}
