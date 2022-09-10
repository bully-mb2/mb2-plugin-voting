package com.templars_server.voting;

import com.templars_server.model.Context;

public interface VoteCallback {

    void onVoteComplete(Context context, String result);

}
