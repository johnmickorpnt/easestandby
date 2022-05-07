package com.test.easestandby;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {



    private List<ModelClass> userList;
    public Adapter(List<ModelClass>userList) {
        this.userList= userList;
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_design,parent,false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {

        String rank = userList.get(position).getTextView1();
        String score = userList.get(position).getTextView2();
        String name = userList.get(position).getTextView3();
        String line = userList.get(position).getDivider();

        holder.setData(rank,score,name);




    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{



        private TextView textView1;
        private TextView textView2;
        private TextView textView3;




        public ViewHolder(@NonNull View itemView) {
            super(itemView);




            textView1 =itemView.findViewById(R.id.textview);
            textView2=itemView.findViewById(R.id.textview2);
            textView3 =itemView.findViewById(R.id.textview3);



        }

        public void setData( String rank, String score, String name) {



            textView1.setText(rank);
            textView2.setText(score);
            textView3.setText(name);


        }
    }
}