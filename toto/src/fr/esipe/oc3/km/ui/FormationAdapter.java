package fr.esipe.oc3.km.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.esipe.agenda.parser.Formation;

public class FormationAdapter extends BaseAdapter {
	
	List<Formation> formations;
	Context context;
	
	public FormationAdapter(Context context, List<Formation> formations) {
		this.formations = formations;
		this.context = context;
	}

	@Override
	public int getCount() {
		return formations.size();
	}

	@Override
	public Object getItem(int position) {
		return formations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int postion, View view, ViewGroup parent) {
		
		Formation formation = formations.get(postion);
		
		if(view == null)
			view = (View)LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
		
		TextView textView = (TextView) view.findViewById(android.R.id.text1);
		textView.setText(formation.getName());
		
		return view;
	}

}
