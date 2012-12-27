package fr.esipe.oc3.km.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import fr.esipe.agenda.parser.Event;
import fr.esipe.oc3.km.R;

public class OneWeekView extends Fragment{

	private ArrayList<EventParcelable> eventParce =null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		/* Getting args from bundle */
		Bundle data = getArguments();
		eventParce = data.getParcelableArrayList("events");
		Log.d("KM", (eventParce == null) ? "true": "false");
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

		public MyExpandableListAdapter(List<EventParcelable> eventParce) {
			Calendar cal = Calendar.getInstance();
			groups = new String[5];
			children = new String[5][8];
			int i = 0;
			Event event = new Event();
			event.setStartTime(new Date());
			event.setEndTime(new Date());
			List<String> labels = new Vector<String>();
			labels.add("Smartphone");
			labels.add("Yoann");
			labels.add("OC3");
			labels.add("1B15");
			event.setLabels(labels);
			List<Event> events = new Vector<Event>();
			events.add(event);
			events.add(event);
			cal.setTime(event.getStartTime());
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			for(int k = 0; k< 5;k++)
			{
				groups[k] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
				cal.add(Calendar.DAY_OF_YEAR, 1);
			}
			
			for(EventParcelable ev : eventParce) {
				cal.setTime(ev.getStartTime());
//				groups[i] = (String) android.text.format.DateFormat.format("EEEE", ev.getStartTime());
//				groups[i] = cal.getDisplayName(cal.get(Calendar.DAY_OF_WEEK), Calendar.SHORT, Locale.FRENCH);
				
				List<String> labels1 = ev.getLabels();
				children[i][0] = cal.get(Calendar.HOUR_OF_DAY) + "h" + cal.get(Calendar.MINUTE);
				for(int j = 0; j < labels1.size() ; j++)
				{
					if(!"OC3".equals(labels1.get(j)))
						children[i][j + 1] = labels1.get(j);
				}
				cal.setTime(ev.getEndTime());
				children[i][labels1.size() + 1] = cal.get(Calendar.HOUR_OF_DAY) + "h" + cal.get(Calendar.MINUTE);
				
				i++;
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
			 TextView textView3 =(TextView)inflatedView.findViewById(R.id.start_time);
	         textView3.setText(children[positionGroup][0]);
	         TextView textView4 =(TextView)inflatedView.findViewById(R.id.end_time);
	         textView4.setText(children[positionGroup][5]);
	        
			TextView textView =(TextView)inflatedView.findViewById(R.id.classroom);
            textView.setText(children[positionGroup][4]);
            TextView textView1 =(TextView)inflatedView.findViewById(R.id.subject);
            textView1.setText(children[positionGroup][1]);
            TextView textView2 =(TextView)inflatedView.findViewById(R.id.teacher);
            textView2.setText(children[positionGroup][2]);
//
//			TextView tv = new TextView(CopyOfMyFragment.this.getActivity());
//			tv.setText(getChild(positionGroup, positionChild).toString());
//			tv.setGravity(Gravity.CENTER);
			return inflatedView;
		}

		@Override
		public int getChildrenCount(int positionChild) {

			return 4;
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
			tv.setText(groups[positionGroup]);
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
		elv.setAdapter(new MyExpandableListAdapter(eventParce));		
		return elv;

	}
	
	
	
	
	

}
