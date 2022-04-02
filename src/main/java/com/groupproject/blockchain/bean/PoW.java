package com.groupproject.blockchain.bean;//please do not delete it ,trying to update a function to automatically increase difficulty
//Difficulty must be updated every 10 block



public class PoW {
    public static int getDifficulty(Long firstTimeStamp,Long secondTimeStamp,Integer lastDiff,Integer lastIndex,Integer blockGenerationInterval,Integer diffAdjustInterval) {
        int timeExpect = blockGenerationInterval * diffAdjustInterval;
        if (lastIndex % diffAdjustInterval ==0 &  lastIndex!=0){
            return getAdjustedDifficulty(firstTimeStamp,secondTimeStamp,timeExpect,lastDiff);
        } else{
            return lastDiff;
        }
    };

    public static int getAdjustedDifficulty(Long firstTimeStamp,Long secondTimeStamp,int timeExpect,Integer lastDiff){
        int timeTaken = secondTimeStamp.intValue() - firstTimeStamp.intValue();
        if (timeTaken < timeExpect / 2) {
            return lastDiff + 1;
        } else if (timeTaken > timeExpect * 2) {
            return lastDiff - 1;
        } else {
            return lastDiff;
        }

    };
};

