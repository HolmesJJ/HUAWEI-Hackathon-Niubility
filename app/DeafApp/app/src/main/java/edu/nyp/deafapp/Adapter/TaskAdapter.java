package edu.nyp.deafapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.nyp.deafapp.Model.Task;
import edu.nyp.deafapp.R;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class TaskAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Task> taskList;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        TextView taskContent;
        ImageView taskIsFinish;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_view_task, null);
            holder.taskContent = convertView.findViewById(R.id.task_content);
            holder.taskIsFinish = convertView.findViewById(R.id.task_is_finish);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.taskContent.setText(taskList.get(position).getContent());

        if(taskList.get(position).isFinish()) {
            holder.taskIsFinish.setImageResource(R.drawable.ic_tick);
        }
        else {
            holder.taskIsFinish.setImageResource(R.drawable.ic_exclamation);
        }

        return convertView;
    }
}
