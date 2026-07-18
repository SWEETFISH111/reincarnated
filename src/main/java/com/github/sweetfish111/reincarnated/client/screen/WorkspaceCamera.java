package com.github.sweetfish111.reincarnated.client.screen;

public class WorkspaceCamera {
    //視点のずれ
    public double panX = 0;
    public double panY = 0;
    //ズーム倍率
    public float zoom = 1.0f;

    private boolean isPanning = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    //画面上のマウスの座標を、キャンバス内の「本当の座標」に変換する。
    public double getCanvasX(double mouseX){
        return (mouseX - panX) / zoom;
    }
    public double getCanvasY(double mouseY){
        return (mouseY - panY) / zoom;
    }

    //ズーム
    public void zoomAt(double mouseX, double mouseY, double scrollDelta){
        if(scrollDelta > 0){
            this.zoom *= 1.1f;
        }else if(scrollDelta < 0){
            this.zoom /= 1.1f;
        }

        this.zoom = Math.max(0.2f, Math.min(this.zoom, 5.0f));
    }

    //何もない空間をドラッグして視点を動かす処理
    public boolean mouseClicked(double mouseX, double mousey, int button){
        if(button == 0){
            this.isPanning = true;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double dragX, double dragY){
        if(this.isPanning){
            //マウスを動かした分だけ視点をずらす。
            this.panX += dragX;
            this.panY += dragY;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(){
        if(this.isPanning){
            this.isPanning = false;
            return true;
        }
        return false;
    }



}
