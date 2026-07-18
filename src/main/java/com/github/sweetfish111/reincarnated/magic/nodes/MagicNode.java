package com.github.sweetfish111.reincarnated.magic.nodes;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

public interface MagicNode {
    void execute(MagicContext context);
    void connectTo(int sourcePortIndex, MagicNode targetNode, int targetPortIndex, boolean isDataFlow);
    Object getOutputData(int portIndex, MagicContext context);
}
