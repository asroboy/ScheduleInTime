package fr.esipe.oc3.km.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import fr.esipe.oc3.km.R;

public class OneWeekView extends Fragment{

	private int currentView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		/* Getting args from bundle */
		Bundle data = getArguments();
		
		currentView = data.getInt("current_view", 0);
	}
	
	
	public class MyExpandableListAdapter extends BaseExpandableListAdapter {

		private String[] groups;
		private String[][] children;

		//		private String[] groups = { "People Names", "Dog Names", "Cat Names", "Fish Names" };
		//        private String[][] children = {
		//                { "Arnold", "Barry", "Chuck", "David" },
		//                { "Ace", "Bandit", "Cha-Cha", "Deuce" },
		//                { "Fluffy", "Snuggles" },
		//                { "Goldy", "Bubbles" }
		//        };

		public MyExpandableListAdapter() {
			int imax = 5;
			int jmax = 4;
			groups = new String[imax];
			children = new String[imax][jmax];
			for(int i = 0; i<imax; i++) {
				groups[i] = "group " + i;
				for(int j = 0; j< jmax; j++) {
					if(i == 2) {
						children[i][j] = "youhou";
					}
					children[i][j] = "child " + j;
				}
			}
		}

		@Override
		public Object getChild(int positionGroup, int positionChild) {
			return children[positionGroup][positionChild];
		}

		@Override
		public long getChildId(int positionGroup, int positionChild) {
			return positionChild;
		}

		@Override
		public View getChildView(int positionGroup, int positionChild, boolean arg2, View arg3,
				ViewGroup arg4) {
			
			View inflatedView = View.inflate(getActivity().getApplicationContext(),
                    R.layout.child_layout, null);
//            inflatedView.setPadding(50, 0, 0, 0);
            TextView textView =(TextView)inflatedView.findViewById(R.id.teacher);
            textView.setText(children[positionGroup][positionChild]);
//
//			TextView tv = new TextView(CopyOfMyFragment.this.getActivity());
//			tv.setText(getChild(positionGroup, positionChild).toString());
//			tv.setGravity(Gravity.CENTER);
			return inflatedView;
		}

		@Override
		public int getChildrenCount(int positionChild) {

			return children[positionChild].length;
		}

		@Override
		public Object getGroup(int positionGroup) {

			return groups[positionGroup];
		}

		@Override
		public int getGroupCount() {

			return groups.length;
		}

		@Override
		public long getGroupId(int arg0) {
			return arg0;
		}

		@Override
		public View getGroupView(int positionGroup, boolean arg1, View arg2,
				ViewGroup parent) {
			View inflatedView = View.inflate(getActivity().getApplicationContext(),
                    R.layout.groups_layout, null);

			inflatedView.setPadding(100, 20, 0, 20);
			TextView tv = (TextView) inflatedView.findViewById(R.id.group);
			tv.setText("coucou");
//			TextView tv = new TextView(CopyOfMyFragment.this.getActivity());
//			tv.setText(getGroup(positionGroup).toString());
			
			return inflatedView;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		View v = inflater.inflate(R.layout.one_week_layout, container,false);
		ExpandableListView elv = (ExpandableListView) v.findViewById(R.id.list);
		elv.setAdapter(new MyExpandableListAdapter());		
		return elv;

	}
	
	
	
	
	

}
