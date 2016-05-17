package com.may.amy.piqz.view.adapter;

import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.may.amy.piqz.databinding.NativeAdBinding;
import com.may.amy.piqz.databinding.PostGalleryBinding;
import com.may.amy.piqz.databinding.PostImageBinding;
import com.may.amy.piqz.databinding.PostTextBinding;
import com.may.amy.piqz.model.NewsItem;
import com.may.amy.piqz.model.holder.AdVH;
import com.may.amy.piqz.model.holder.BaseVH;
import com.may.amy.piqz.model.holder.GalleryVH;
import com.may.amy.piqz.model.holder.ImagePostVH;
import com.may.amy.piqz.model.holder.TextPostVH;
import com.may.amy.piqz.util.KaC;

/**
 * Created by kuhnertj on 15.04.2016.
 */
public class PostAdapter extends RecyclerView.Adapter<BaseVH> {
    private ObservableArrayList<NewsItem> mPosts;


    public PostAdapter(ObservableArrayList<NewsItem> posts) {
        mPosts = posts;
    }


    @Override
    public BaseVH onCreateViewHolder(final ViewGroup parent, final int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case KaC.TYPE_IMAGE:
                return new ImagePostVH(PostImageBinding.inflate(inflater, parent, false));
            case KaC.TYPE_SELF:
                return new TextPostVH(PostTextBinding.inflate(inflater, parent, false));
            case KaC.TYPE_GALLERY:
                return new GalleryVH(PostGalleryBinding.inflate(inflater, parent, false));
            case KaC.TYPE_AD:
                return new AdVH(NativeAdBinding.inflate(inflater, parent, false));
            case KaC.TYPE_GENERIC:
            default:
                return new TextPostVH(PostTextBinding.inflate(inflater, parent, false));

        }
    }

    @Override
    public void onBindViewHolder(final BaseVH holder, final int position) {
        if (holder instanceof AdVH){
            holder.displayPost(mPosts.get(position));
            ((AdVH) holder).displayAd();
        }else{
            holder.displayPost(mPosts.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mPosts.get(position).getPostType();

    }
}
